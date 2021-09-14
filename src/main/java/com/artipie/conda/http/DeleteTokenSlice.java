/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.http;

import com.artipie.conda.http.auth.TokenAuthScheme;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.auth.Authentication;
import com.artipie.http.headers.Authorization;
import com.artipie.http.headers.WwwAuthenticate;
import com.artipie.http.rq.RqHeaders;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import org.reactivestreams.Publisher;

/**
 * Delete token slice.
 * <a href="https://api.anaconda.org/docs#/authentication/delete_authentications">Documentation</a>.
 * @since 0.5
 */
public final class DeleteTokenSlice implements Slice {

    /**
     * Auth tokens.
     */
    private final ConcurrentMap<String, Authentication.User> tokens;

    /**
     * Ctor.
     * @param tokens Auth tokens
     */
    public DeleteTokenSlice(final ConcurrentMap<String, Authentication.User> tokens) {
        this.tokens = tokens;
    }

    @Override
    public Response response(final String line,
        final Iterable<Map.Entry<String, String>> headers, final Publisher<ByteBuffer> body) {
        return new AsyncResponse(
            CompletableFuture.supplyAsync(
                () -> {
                    final Optional<String> token = new RqHeaders(headers, Authorization.NAME)
                        .stream().findFirst().map(Authorization::new)
                        .map(auth -> new Authorization.Token(auth.credentials()).token());
                    final Response res;
                    if (token.isPresent()) {
                        final Authentication.User removed = this.tokens.remove(token.get());
                        final RsStatus status;
                        if (removed == null) {
                            status = RsStatus.BAD_REQUEST;
                        } else {
                            status = RsStatus.CREATED;
                        }
                        res = new RsWithStatus(status);
                    } else {
                        res = new RsWithHeaders(
                            new RsWithStatus(RsStatus.UNAUTHORIZED),
                            new Headers.From(new WwwAuthenticate(TokenAuthScheme.NAME))
                        );
                    }
                    return res;
                }
            )
        );
    }
}
