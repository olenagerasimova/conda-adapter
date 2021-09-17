/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.json.JsonObject;

/**
 * Authentication tokens.
 * @since 0.5
 */
public interface AuthTokens {

    /**
     * Get valid token item by token string.
     * @param token Token
     * @return Full token info if present and is not expired
     */
    CompletionStage<Optional<TokenItem>> get(String token);

    /**
     * Find valid token item by username.
     * @param token Token
     * @return Full token info if found and is not expired
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
        private final String uname;

        /**
         * Token.
         */
        private final String token;

        /**
         * Expiration date.
         */
        private final Instant expire;

        /**
         * Ctor.
         * @param token Token
         * @param info Token info in json format
         */
        public TokenItem(final String token, final JsonObject info) {
            this.token = token;
            this.uname = info.getString("name");
            this.expire = Instant.ofEpochMilli(info.getJsonNumber("expire").longValue());
        }

        /**
         * Name of the user, token owner.
         * @return User name
         */
        public String userName() {
            return this.uname;
        }

        /**
         * Is this token expired?
         * @return True if yes
         */
        public boolean expired() {
            return this.expire.compareTo(Instant.now()) > 0;
        }

    }
}
