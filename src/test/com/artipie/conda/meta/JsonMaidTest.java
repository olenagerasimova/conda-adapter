/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.meta;

import com.artipie.asto.test.TestResource;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link JsonMaid.Jackson}.
 * @since 0.1
 */
class JsonMaidTest {

    @Test
    void doesNothingIfChecksumsAreEmpty() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream, JsonEncoding.UTF8).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(Collections.emptySet());
        MatcherAssert.assertThat(
            stream.toByteArray(),
            new IsEqual<>(resource.asBytes())
        );
    }

}
