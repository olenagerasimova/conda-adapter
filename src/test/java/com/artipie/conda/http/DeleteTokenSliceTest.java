/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.http;

import com.artipie.asto.Content;
import com.artipie.http.Headers;
import com.artipie.http.auth.Authentication;
import com.artipie.http.headers.Authorization;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.hm.SliceHasResponse;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link DeleteTokenSlice}.
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class DeleteTokenSliceTest {

    @Test
    void removesToken() {
        final ConcurrentMap<String, Authentication.User> tokens = new ConcurrentHashMap<>();
        final String token = "abc123";
        tokens.put(token, new Authentication.User("Alice"));
        MatcherAssert.assertThat(
            "Incorrect response status, 201 CREATED is expected",
            new DeleteTokenSlice(tokens),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.CREATED),
                new RequestLine(RqMethod.DELETE, "/authentications$"),
                new Headers.From(new Authorization.Token(token)),
                Content.EMPTY
            )
        );
        MatcherAssert.assertThat(
            tokens.isEmpty(),
            new IsEqual<>(true)
        );
    }

    @Test
    void returnsBadRequestIfTokenIsNotFound() {
        MatcherAssert.assertThat(
            "Incorrect response status, BAD_REQUEST is expected",
            new DeleteTokenSlice(new ConcurrentHashMap<>()),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.BAD_REQUEST),
                new RequestLine(RqMethod.DELETE, "/authentications$"),
                new Headers.From(new Authorization.Token("any")),
                Content.EMPTY
            )
        );
    }

    @Test
    void returnsUnauthorizedIfHeaderIsNotPresent() {
        MatcherAssert.assertThat(
            "Incorrect response status, BAD_REQUEST is expected",
            new DeleteTokenSlice(new ConcurrentHashMap<>()),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.UNAUTHORIZED),
                new RequestLine(RqMethod.DELETE, "/authentications$"),
                Headers.EMPTY,
                Content.EMPTY
            )
        );
    }

}
