/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.asto;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.conda.Tokens;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Asto implementation of {@link Tokens}: adds/updates tokens in storage. Tokens
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
 */
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
public final class AstoTokens implements Tokens {

    /**
     * Tokens storage item key.
     */
    private static final Key TKNS = new Key.From(".tokens.json");

    /**
     * Abstract storage.
     */
    private final Storage asto;

    /**
     * Time to live.
     */
    private final String ttl;

    /**
     * Ctor.
     * @param asto Abstract storage
     * @param ttl Time to live
     */
    public AstoTokens(final Storage asto, final String ttl) {
        this.asto = asto;
        this.ttl = ttl;
    }

    @Override
    public CompletionStage<Optional<TokenItem>> get(final String token) {
        return null;
    }

    @Override
    public CompletionStage<Optional<TokenItem>> find(final String token) {
        return null;
    }

    @Override
    public CompletionStage<String> generate(final String name) {
        return null;
    }
}
