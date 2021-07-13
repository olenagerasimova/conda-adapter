/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.meta;

import com.artipie.asto.test.TestResource;
import com.fasterxml.jackson.core.JsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import org.cactoos.set.SetOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link JsonMaid.Jackson}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class JsonMaidTest {

    @Test
    void removedPackage() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(new SetOf<>("1fe3c3f4250e51886838e8e0287e39076d601b9f493ea05c37a2630a9fe5810f"));
        MatcherAssert.assertThat(
            stream.toString(),
            new IsEqual<>(
                String.join(
                    "\n", "{", "  \"packages\" : {",
                    String.format("%s,\n%s", this.funPackageZeroOne(), this.funPackageZeroTwo()),
                    "  }", "}"
                )
            )
        );
    }

    @Test
    void removesLastPackage() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(new SetOf<>("xyx123"));
        MatcherAssert.assertThat(
            stream.toString(),
            new IsEqual<>(
                String.join(
                    "\n", "{", "  \"packages\" : {",
                    String.format("%s,\n%s", this.funPackageZeroOne(), this.testPackage()),
                    "  }", "}"
                )
            )
        );
    }

    @Test
    void removesFirstPackage() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(new SetOf<>("1fe3c3f4250e51886838e8e0287e39029d601b9f493ea05c37a2630a9fe5810f"));
        MatcherAssert.assertThat(
            stream.toString(),
            new IsEqual<>(
                String.join(
                    "\n",
                    "{",
                    "  \"packages\" : {",
                    String.format("%s,\n%s", this.testPackage(), this.funPackageZeroTwo()),
                    "  }",
                    "}"
                )
            )
        );
    }

    @Test
    void doesNothingIfPackageDoesNotExists() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(new SetOf<String>("098"));
        MatcherAssert.assertThat(
            stream.toByteArray(),
            new IsEqual<>(resource.asBytes())
        );
    }

    @Test
    void doesNothingIfChecksumsAreEmpty() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(Collections.emptySet());
        MatcherAssert.assertThat(
            stream.toByteArray(),
            new IsEqual<>(resource.asBytes())
        );
    }

    private String funPackageZeroTwo() {
        return String.join(
            "\n",
            "    \"super-fun-package-0.2.0-py37_0.tar.bz2\" : {",
            "      \"build\" : \"py37_0\",",
            "      \"build_number\" : 0,",
            "      \"depends\" : [ \"some-depends\" ],",
            "      \"license\" : \"BSD\",",
            "      \"md5\" : \"abc123\",",
            "      \"name\" : \"super-fun-package\",",
            "      \"sha256\" : \"xyx123\",",
            "      \"size\" : 3832,",
            "      \"subdir\" : \"win-64\",",
            "      \"timestamp\" : 1530731681870,",
            "      \"version\" : \"0.2.0\"",
            "    }"
        );
    }

    private String funPackageZeroOne() {
        return String.join(
            "\n",
            "    \"super-fun-package-0.1.0-py37_0.tar.bz2\" : {",
            "      \"build\" : \"py37_0\",",
            "      \"build_number\" : 0,",
            "      \"depends\" : [ \"some-depends\" ],",
            "      \"license\" : \"BSD\",",
            "      \"md5\" : \"a75683f8d9f5b58c19a8ec5d0b7f796e\",",
            "      \"name\" : \"super-fun-package\",",
            // @checkstyle LineLengthCheck (1 line)
            "      \"sha256\" : \"1fe3c3f4250e51886838e8e0287e39029d601b9f493ea05c37a2630a9fe5810f\",",
            "      \"size\" : 3832,",
            "      \"subdir\" : \"win-64\",",
            "      \"timestamp\" : 1530731681870,",
            "      \"version\" : \"0.1.0\"",
            "    }"
        );
    }

    private String testPackage() {
        return String.join(
            "\n",
            "    \"test-package-0.5.0-py36_0.conda\" : {",
            "      \"build\" : \"py37_0\",",
            "      \"build_number\" : 0,",
            "      \"depends\" : [ \"some-depends\" ],",
            "      \"license\" : \"BSD\",",
            "      \"md5\" : \"a75683f8d9f5b58c19a8ec5d0b7f786e\",",
            "      \"name\" : \"test-package\",",
            // @checkstyle LineLengthCheck (1 line)
            "      \"sha256\" : \"1fe3c3f4250e51886838e8e0287e39076d601b9f493ea05c37a2630a9fe5810f\",",
            "      \"size\" : 123,",
            "      \"subdir\" : \"macOS\",",
            "      \"timestamp\" : 153073163434,",
            "      \"version\" : \"0.5.0\"",
            "    }"
        );
    }

}
