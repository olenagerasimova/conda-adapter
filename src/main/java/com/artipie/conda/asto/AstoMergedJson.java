/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.asto;

import com.artipie.asto.ArtipieIOException;
import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.misc.UncheckedIOConsumer;
import com.artipie.asto.misc.UncheckedIOFunc;
import com.artipie.conda.meta.MergedJson;
import com.fasterxml.jackson.core.JsonFactory;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.json.JsonObject;
import org.cqfn.rio.Buffers;
import org.cqfn.rio.WriteGreed;
import org.cqfn.rio.stream.ReactiveInputStream;
import org.cqfn.rio.stream.ReactiveOutputStream;

/**
 * Asto merged json adds packages metadata to repodata index, reading and writing to/from
 * abstract storage.
 * @since 0.4
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class AstoMergedJson {

    /**
     * Abstract storage.
     */
    private final Storage asto;

    /**
     * Repodata file key.
     */
    private final Key key;

    /**
     * Ctor.
     * @param asto Abstract storage
     * @param key Repodata file key
     */
    public AstoMergedJson(final Storage asto, final Key key) {
        this.asto = asto;
        this.key = key;
    }

    /**
     * Merges or adds provided new packages items into repodata.json.
     * @param items Items to merge
     * @return Completable operation
     */
    public CompletionStage<Void> merge(final Map<String, JsonObject> items) {
        return this.asto.exists(this.key).thenCompose(
            exists -> {
                final CompletionStage<Void> future;
                Optional<PipedInputStream> pis = Optional.empty();
                Optional<PipedOutputStream> pos = Optional.empty();
                try (PipedOutputStream outout = new PipedOutputStream()) {
                    if (exists) {
                        pis = Optional.of(new PipedInputStream());
                        final PipedOutputStream out = new PipedOutputStream(pis.get());
                        pos = Optional.of(out);
                        future = this.asto.value(this.key).thenCompose(
                            input -> new ReactiveOutputStream(out).write(input, WriteGreed.SYSTEM)
                        );
                    } else {
                        future = CompletableFuture.allOf();
                        pis = Optional.empty();
                    }
                    final PipedInputStream src = new PipedInputStream(outout);
                    future.thenCompose(
                        nothing -> this.asto.save(
                            this.key,
                            new Content.From(
                                new ReactiveInputStream(src).read(Buffers.Standard.K16)
                            )
                        )
                    );
                    final JsonFactory factory = new JsonFactory();
                    new MergedJson.Jackson(
                        factory.createGenerator(outout),
                        pis.map(new UncheckedIOFunc<>(factory::createParser))
                    ).merge(items);
                } catch (final IOException err) {
                    throw new ArtipieIOException(err);
                } finally {
                    pis.ifPresent(new UncheckedIOConsumer<>(PipedInputStream::close));
                    pos.ifPresent(new UncheckedIOConsumer<>(PipedOutputStream::close));
                }
                return future;
            }
        );
    }
}
