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
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
class JsonMaidTest {

    @Test
    void removedPackages() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(
            new SetOf<>(
                "b37f144a5c2349b1c58ef17a663cb79086a1f2f49e35503e4f411f6f698cee1a",
                "be2a62bd5a3a6abda7f2309f4f2ddce7bededb40adb91341f18438b246d7fc7e"
            )
        );
        final String actual = stream.toString();
        MatcherAssert.assertThat(
            actual,
            new IsEqual<>(
                String.join(
                    "\n", "{", "  \"packages\" : {",
                    String.format("%s,\n%s", this.cram(), this.decorator()),
                    "  },",
                    "  \"packages.conda\" : {",
                    this.tenacity(),
                    "  }",
                    "}"
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
        ).clean(new SetOf<>("47d6dd01a1cff52af31804bbfffb4341fd8676c75d00d120cc66d9709e78ea7f"));
        MatcherAssert.assertThat(
            stream.toString(),
            new IsEqual<>(
                String.join(
                    "\n", "{", "  \"packages\" : {",
                    String.format("%s,\n%s,\n%s", this.cram(), this.decorator(), this.pyqt()),
                    "  },",
                    "  \"packages.conda\" : {",
                    this.notebook(),
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
        ).clean(new SetOf<>("4b36cb59651f6218449bd71a7d37182f062f545240b502eebed319f77fa54b08"));
        MatcherAssert.assertThat(
            stream.toString(),
            new IsEqual<>(
                String.join(
                    "\n", "{", "  \"packages\" : {",
                    String.format("%s,\n%s", this.decorator(), this.pyqt()),
                    "  },",
                    "  \"packages.conda\" : {",
                    String.format("%s,\n%s", this.notebook(), this.tenacity()),
                    "  }", "}"
                )
            )
        );
    }

    @Test
    void removesAllTarPackages() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(
            new SetOf<>(
                "4b36cb59651f6218449bd71a7d37182f062f545240b502eebed319f77fa54b08",
                "b5f77880181b37fb2e180766869da6242648aaec5bdd6de89296d9dacd764c14",
                "b37f144a5c2349b1c58ef17a663cb79086a1f2f49e35503e4f411f6f698cee1a"
            )
        );
        MatcherAssert.assertThat(
            stream.toString(),
            new IsEqual<>(
                String.join(
                    "\n", "{", "  \"packages\" : { },",
                    "  \"packages.conda\" : {",
                    String.format("%s,\n%s", this.notebook(), this.tenacity()),
                    "  }", "}"
                )
            )
        );
    }

    @Test
    void removesAllCondaPackages() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        final TestResource resource = new TestResource("repodata.json");
        new JsonMaid.Jackson(
            factory.createGenerator(stream).useDefaultPrettyPrinter(),
            factory.createParser(resource.asInputStream())
        ).clean(
            new SetOf<>(
                "be2a62bd5a3a6abda7f2309f4f2ddce7bededb40adb91341f18438b246d7fc7e",
                "47d6dd01a1cff52af31804bbfffb4341fd8676c75d00d120cc66d9709e78ea7f"
            )
        );
        MatcherAssert.assertThat(
            stream.toString(),
            new IsEqual<>(
                String.join(
                    "\n", "{", "  \"packages\" : {",
                    String.format("%s,\n%s,\n%s", this.cram(), this.decorator(), this.pyqt()),
                    "  },",
                    "  \"packages.conda\" : { }",
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

    private String cram() {
        return String.join(
            "\n",
            "    \"cram-0.7-py36_1.tar.bz2\" : {",
            "      \"build\" : \"py36_1\",",
            "      \"build_number\" : 1,",
            "      \"depends\" : [ \"python >=3.6,<3.7.0a0\" ],",
            "      \"license\" : \"GPL-2.0\",",
            "      \"license_family\" : \"GPL2\",",
            "      \"md5\" : \"609e6545899d1d098f4160dd98fbc74d\",",
            "      \"name\" : \"cram\",",
            // @checkstyle LineLengthCheck (1 line)
            "      \"sha256\" : \"4b36cb59651f6218449bd71a7d37182f062f545240b502eebed319f77fa54b08\",",
            "      \"size\" : 40151,",
            "      \"subdir\" : \"linux-64\",",
            "      \"timestamp\" : 1539182927005,",
            "      \"version\" : \"0.7\"",
            "    }"
        );
    }

    private String decorator() {
        return String.join(
            "\n",
            "    \"decorator-4.2.1-py27_0.tar.bz2\" : {",
            "      \"build\" : \"py27_0\",",
            "      \"build_number\" : 0,",
            "      \"depends\" : [ \"python >=2.7,<2.8.0a0\" ],",
            "      \"license\" : \"BSD 3-Clause\",",
            "      \"md5\" : \"0ebe0cb0d62eae6cd237444ba8fded66\",",
            "      \"name\" : \"decorator\",",
            // @checkstyle LineLengthCheck (1 line)
            "      \"sha256\" : \"b5f77880181b37fb2e180766869da6242648aaec5bdd6de89296d9dacd764c14\",",
            "      \"size\" : 15638,",
            "      \"subdir\" : \"linux-64\",",
            "      \"timestamp\" : 1516376429792,",
            "      \"version\" : \"4.2.1\"",
            "    }"
        );
    }

    private String pyqt() {
        return String.join(
            "\n",
            "    \"pyqt-5.6.0-py36h0386399_5.tar.bz2\" : {",
            "      \"build\" : \"py36h0386399_5\",",
            "      \"build_number\" : 5,",
            // @checkstyle LineLengthCheck (1 line)
            "      \"depends\" : [ \"dbus >=1.10.22,<2.0a0\", \"libgcc-ng >=7.2.0\", \"libstdcxx-ng >=7.2.0\", \"python >=3.6,<3.7.0a0\", \"qt 5.6.*\", \"sip 4.18.*\" ],",
            "      \"license\" : \"Commercial, GPL-2.0, GPL-3.0\",",
            "      \"license_family\" : \"GPL\",",
            "      \"md5\" : \"b1624f76a6bba705869f849f7456bd39\",",
            "      \"name\" : \"pyqt\",",
            // @checkstyle LineLengthCheck (1 line)
            "      \"sha256\" : \"b37f144a5c2349b1c58ef17a663cb79086a1f2f49e35503e4f411f6f698cee1a\",",
            "      \"size\" : 5715107,",
            "      \"subdir\" : \"linux-64\",",
            "      \"timestamp\" : 1505739175210,",
            "      \"version\" : \"5.6.0\"",
            "    }"
        );
    }

    private String notebook() {
        return String.join(
            "\n",
            "    \"notebook-6.1.1-py38_0.conda\" : {",
            "      \"app_cli_opts\" : [ {",
            "        \"args\" : \"--port %s\",",
            "        \"default\" : \"8080\",",
            "        \"name\" : \"port\",",
            "        \"summary\" : \"Server port ...\"",
            "      } ],",
            "      \"app_entry\" : \"jupyter-notebook\",",
            "      \"app_type\" : \"web\",",
            "      \"build\" : \"py38_0\",",
            "      \"build_number\" : 0,",
            // @checkstyle LineLengthCheck (1 line)
            "      \"depends\" : [ \"argon2-cffi\", \"ipykernel\", \"ipython_genutils\", \"jinja2\", \"send2trash\", \"terminado >=0.8.1\", \"tornado >=5.0\", \"traitlets >=4.2.1\" ],",
            "      \"icon\" : \"df7feebede9861a203b480c119e38b49.png\",",
            "      \"license\" : \"BSD-3-Clause\",",
            "      \"license_family\" : \"BSD\",",
            "      \"md5\" : \"0a90b675d8e46db6dd92b48f085df274\",",
            "      \"name\" : \"notebook\",",
            // @checkstyle LineLengthCheck (1 line)
            "      \"sha256\" : \"be2a62bd5a3a6abda7f2309f4f2ddce7bededb40adb91341f18438b246d7fc7e\",",
            "      \"size\" : 4233953,",
            "      \"subdir\" : \"linux-64\",",
            "      \"summary\" : \"Jupyter Notebook\",",
            "      \"timestamp\" : 1596838674769,",
            "      \"type\" : \"app\",",
            "      \"version\" : \"6.1.1\"",
            "    }"
        );
    }

    private String tenacity() {
        return String.join(
            "\n",
            "    \"tenacity-6.2.0-py37_0.conda\" : {",
            "      \"build\" : \"py37_0\",",
            "      \"build_number\" : 0,",
            "      \"depends\" : [ \"python >=3.7,<3.8.0a0\", \"six >=1.9.0\" ],",
            "      \"license\" : \"Apache 2.0\",",
            "      \"md5\" : \"e5a8af970f391f432c96b1480f535c43\",",
            "      \"name\" : \"tenacity\",",
            // @checkstyle LineLengthCheck (1 line)
            "      \"sha256\" : \"47d6dd01a1cff52af31804bbfffb4341fd8676c75d00d120cc66d9709e78ea7f\",",
            "      \"size\" : 40072,",
            "      \"subdir\" : \"linux-64\",",
            "      \"timestamp\" : 1597706734992,",
            "      \"version\" : \"6.2.0\"",
            "    }"
        );
    }

}
