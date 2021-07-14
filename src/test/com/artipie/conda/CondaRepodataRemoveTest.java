/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda;

import com.artipie.asto.test.TestResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.cactoos.set.SetOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link CondaRepodata.Remove}.
 * @since 0.1
 */
class CondaRepodataRemoveTest {

    @Test
    void removesPackagesInfo() throws IOException {
        try (InputStream input = new TestResource("repodata.json").asInputStream()) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            new CondaRepodata.Remove(input, out).perform(
                new SetOf<>(
                    "1fe3c3f4250e51886838e8e0287e39029d601b9f493ea05c37a2630a9fe5810f", "xyx123"
                )
            );
            MatcherAssert.assertThat(
                out.toString(),
                new IsEqual<>(
                    String.join(
                        "",
                        "{",
                        "\"packages\":{",
                        "\"test-package-0.5.0-py36_0.conda\":{",
                        "\"build\":\"py37_0\",",
                        "\"build_number\":0,",
                        "\"depends\":[\"some-depends\"],",
                        "\"license\":\"BSD\",",
                        "\"md5\":\"a75683f8d9f5b58c19a8ec5d0b7f786e\",",
                        "\"name\":\"test-package\",",
                        // @checkstyle LineLengthCheck (1 line)
                        "\"sha256\":\"1fe3c3f4250e51886838e8e0287e39076d601b9f493ea05c37a2630a9fe5810f\",",
                        "\"size\":123,",
                        "\"subdir\":\"macOS\",",
                        "\"timestamp\":153073163434,",
                        "\"version\":\"0.5.0\"",
                        "}",
                        "}",
                        "}"
                    )
                )
            );
        }
    }

    @Test
    void doesNothingIfGivenFileIsEmpty() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final String file = "{\"packages\":{}}";
        new CondaRepodata.Remove(
            new ByteArrayInputStream(file.getBytes()), out
        ).perform(new SetOf<>("abc123", "xyx098"));
        MatcherAssert.assertThat(
            out.toString(),
            new IsEqual<>(file)
        );
    }

}
