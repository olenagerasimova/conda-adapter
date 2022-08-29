/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.asto;

import com.artipie.asto.ArtipieIOException;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.misc.UncheckedIOConsumer;
import com.artipie.asto.misc.UncheckedIOFunc;
import com.artipie.asto.streams.ContentAsStream;
import com.artipie.asto.streams.StorageValuePipeline;
import com.artipie.conda.AuthTokens;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Asto implementation of {@link AuthTokens}: adds/updates tokens in storage. Tokens
 * are stored in storage by key `.tokens.json` in json format:
 * {
 *   "tokens": {
 *     "abc123": {
 *       "name": "alice",
 *       "expire": 1505739175210
 *     },
 *     "xyz098": {
 *       "name": "John",
 *       "expire": 1516376429792
 *     }
 *   }
 * }
 * @since 0.5
 * @checkstyle ConstantUsageCheck (20 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
public final class AstoAuthTokens implements AuthTokens {

    /**
     * Tokens storage item key.
     */
    static final Key TKNS = new Key.From(".tokens.json");

    /**
     * Tokens json field name.
     */
    static final String TOKENS = "tokens";

    /**
     * Token length.
     */
    private static final int LEN = 30;

    /**
     * Abstract storage.
     */
    private final Storage asto;

    /**
     * Ctor.
     * @param asto Abstract storage
     */
    public AstoAuthTokens(final Storage asto) {
        this.asto = asto;
    }

    @Override
    public CompletionStage<Optional<TokenItem>> get(final String token) {
        return this.checkAndFind(AstoAuthTokens.findByToken(token));
    }

    @Override
    public CompletionStage<Optional<TokenItem>> find(final String username) {
        return this.checkAndFind(AstoAuthTokens.findByUsername(username));
    }

    @Override
    public CompletionStage<TokenItem> generate(final String name, final Duration ttl) {
        return CompletableFuture.supplyAsync(
            () -> RandomStringUtils.random(AstoAuthTokens.LEN, true, true)
        ).thenApply(
            str -> new TokenItem(str, name, Instant.now().plus(ttl))
        ).thenCompose(
            token -> new StorageValuePipeline<>(this.asto, AstoAuthTokens.TKNS).process(
                (opt, out) -> {
                    final JsonFactory fact = new JsonFactory();
                    final Optional<JsonParser> parser =
                        opt.map(new UncheckedIOFunc<>(fact::createParser));
                    try (JsonGenerator gen = fact.createGenerator(out)) {
                        gen.writeStartObject();
                        gen.writeFieldName(AstoAuthTokens.TOKENS);
                        gen.writeStartObject();
                        parser.ifPresent(item -> AstoAuthTokens.copy(gen, item));
                        gen.writeFieldName(token.token());
                        gen.writeStartObject();
                        gen.writeStringField("name", name);
                        gen.writeNumberField("expire", token.validUntil().toEpochMilli());
                        gen.writeEndObject();
                        gen.writeEndObject();
                    } catch (final IOException err) {
                        throw new ArtipieIOException(err);
                    } finally {
                        parser.ifPresent(new UncheckedIOConsumer<>(JsonParser::close));
                    }
                }
            ).thenApply(nothing -> token)
        );
    }

    @Override
    public CompletionStage<Boolean> remove(final String token) {
        return new StorageValuePipeline<Boolean>(this.asto, AstoAuthTokens.TKNS).processWithResult(
            (opt, out) -> {
                boolean removed = false;
                if (opt.isPresent()) {
                    removed = AstoAuthTokens.copyExcept(opt.get(), out, token);
                }
                return removed;
            }
        );
    }

    /**
     * Checks whether .token.json exists and applies provided functions to find token.
     * @param action Function to search for token
     * @return Token if found as completion result
     */
    private CompletionStage<Optional<TokenItem>> checkAndFind(
        final Function<InputStream, Optional<TokenItem>> action
    ) {
        return this.asto.exists(AstoAuthTokens.TKNS).thenCompose(
            exists -> {
                CompletionStage<Optional<TokenItem>> res =
                    CompletableFuture.completedFuture(Optional.empty());
                if (exists) {
                    res = this.asto.value(AstoAuthTokens.TKNS).thenCompose(
                        pub -> new ContentAsStream<Optional<TokenItem>>(pub).process(action)
                    );
                }
                return res;
            }
        );
    }

    /**
     * Find token item to by token string.
     * @param token Token to search for
     * @return Token item if found
     */
    @SuppressWarnings("PMD.AssignmentInOperand")
    private static Function<InputStream, Optional<TokenItem>> findByToken(final String token) {
        return input -> {
            try {
                final JsonParser parser = new JsonFactory().createParser(input);
                JsonToken jtoken;
                Optional<TokenItem> result = Optional.empty();
                while ((jtoken = parser.nextToken()) != null) {
                    if (jtoken == JsonToken.FIELD_NAME && parser.getCurrentName().equals(token)) {
                        parser.nextToken();
                        parser.setCodec(new ObjectMapper());
                        final TokenItem item = new TokenItem(
                            token, parser.<ObjectNode>readValueAsTree()
                        );
                        if (!item.expired()) {
                            result = Optional.of(item);
                        }
                    }
                }
                return result;
            } catch (final IOException err) {
                throw new ArtipieIOException(err);
            }
        };
    }

    /**
     * Searches not expired token item to by user name.
     * @param name Name of the user
     * @return Token item if found
     */
    @SuppressWarnings("PMD.AssignmentInOperand")
    private static Function<InputStream, Optional<TokenItem>> findByUsername(final String name) {
        return input -> {
            try {
                final JsonParser parser = new JsonFactory().createParser(input);
                JsonToken jtoken;
                Optional<TokenItem> result = Optional.empty();
                while ((jtoken = parser.nextToken()) != null) {
                    if (jtoken == JsonToken.FIELD_NAME
                        && !parser.getCurrentName().equals(AstoAuthTokens.TOKENS)) {
                        final String token = parser.getCurrentName();
                        parser.nextToken();
                        parser.setCodec(new ObjectMapper());
                        final TokenItem item = new TokenItem(
                            token, parser.<ObjectNode>readValueAsTree()
                        );
                        if (name.equals(item.userName()) && !item.expired()) {
                            result = Optional.of(item);
                        }
                    }
                }
                return result;
            } catch (final IOException err) {
                throw new ArtipieIOException(err);
            }
        };
    }

    /**
     * Copies existing tokens.
     * @param gen Where to write
     * @param parser From what to read
     */
    @SuppressWarnings("PMD.AssignmentInOperand")
    private static void copy(final JsonGenerator gen, final JsonParser parser) {
        try {
            JsonToken jtoken;
            while ((jtoken = parser.nextToken()) != null) {
                if (jtoken == JsonToken.FIELD_NAME
                    && !parser.getCurrentName().equals(AstoAuthTokens.TOKENS)) {
                    final String token = parser.getCurrentName();
                    parser.nextToken();
                    parser.setCodec(new ObjectMapper());
                    gen.writeFieldName(token);
                    gen.setCodec(new ObjectMapper());
                    gen.writeTree(parser.<ObjectNode>readValueAsTree());
                }
            }
        } catch (final IOException err) {
            throw new ArtipieIOException(err);
        }
    }

    /**
     * Copies tokens from input to output streams except for the token to remove.
     * @param input Input steam to read tokens from
     * @param out Output to write the result
     * @param token Token to delete
     * @return True if token was found and removed
     * @checkstyle ExecutableStatementCountCheck (25 lines)
     */
    @SuppressWarnings("PMD.AssignmentInOperand")
    private static boolean copyExcept(final InputStream input, final OutputStream out,
        final String token) {
        final JsonFactory fact = new JsonFactory();
        boolean found = false;
        try (
            JsonGenerator gen = fact.createGenerator(out);
            JsonParser parser = fact.createParser(input)
        ) {
            gen.writeStartObject();
            gen.writeFieldName(AstoAuthTokens.TOKENS);
            gen.writeStartObject();
            JsonToken jtoken;
            while ((jtoken = parser.nextToken()) != null) {
                if (jtoken == JsonToken.FIELD_NAME
                    && !parser.getCurrentName().equals(AstoAuthTokens.TOKENS)) {
                    final String current = parser.getCurrentName();
                    parser.nextToken();
                    parser.setCodec(new ObjectMapper());
                    final ObjectNode node = parser.<ObjectNode>readValueAsTree();
                    if (current.equals(token)) {
                        found = true;
                    } else {
                        gen.writeFieldName(current);
                        gen.setCodec(new ObjectMapper());
                        gen.writeTree(node);
                    }
                }
            }
            gen.writeEndObject();
            gen.writeEndObject();
        } catch (final IOException err) {
            throw new ArtipieIOException(err);
        }
        return found;
    }
}
