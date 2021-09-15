/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Authentication tokens.
 * @since 0.5
 */
public interface Tokens {

    /**
     * Get token item by token string.
     * @param token Token
     * @return Full token info if present
     */
    CompletionStage<Optional<TokenItem>> get(String token);

    /**
     * Find token item by username.
     * @param token Token
     * @return Full token info if found
     */
    CompletionStage<Optional<TokenItem>> find(String token);

    /**
     * Generates token for username.
     * @param name User name
     * @return Token string
     */
    CompletionStage<String> generate(String name);

    /**
     * Token item: username, token and expiration day.
     * @since 0.5
     */
    final class TokenItem {

        /**
         * Name of the user.
         */
        private final String name;

        /**
         * Token.
         */
        private final String token;

        /**
         * Expiration date.
         */
        private final Date expire;

        /**
         * Ctor.
         * @param name User name
         * @param token Token
         * @param expire Expiration date
         */
        public TokenItem(final String name, final String token, final Date expire) {
            this.name = name;
            this.token = token;
            this.expire = expire;
        }
    }
}
