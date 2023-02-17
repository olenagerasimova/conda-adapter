/*
 * The MIT License (MIT) Copyright (c) 2020-2023 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.http.auth;

import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.TokenAuthentication;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Simple in memory implementation of {@link TokenAuthentication}.
 * @since 0.5
 */
public final class TokenAuth implements TokenAuthentication {

    /**
     * Anonymous token authentication.
     */
    public static final TokenAuthentication ANONYMOUS = token ->
        CompletableFuture.completedFuture(Optional.of(new Authentication.User("anonymous")));

    /**
     * Tokens.
     */
    private final TokenAuthentication tokens;

    /**
     * Ctor.
     * @param tokens Tokens and users
     */
    public TokenAuth(final TokenAuthentication tokens) {
        this.tokens = tokens;
    }

    @Override
    public CompletionStage<Optional<Authentication.User>> user(final String token) {
        return this.tokens.user(token);
    }
}
