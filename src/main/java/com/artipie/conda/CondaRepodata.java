/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda;

import com.artipie.asto.ArtipieIOException;
import com.artipie.conda.meta.JsonMaid;
import com.fasterxml.jackson.core.JsonFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * Conda repository repodata.
 * @since 0.1
 */
public interface CondaRepodata {

    /**
     * Removes records about conda packages from repodata file.
     * Output/Input streams are not closed by this implementation, these operation should
     * be done from outside.
     * @since 0.1
     */
    final class Remove {

        /**
         * Json repodata input stream.
         */
        private final InputStream input;

        /**
         * Json repodata output, where write the result.
         */
        private final OutputStream out;

        /**
         * Ctor.
         * @param input Json repodata input stream
         * @param out Json repodata output
         */
        public Remove(final InputStream input, final OutputStream out) {
            this.input = input;
            this.out = out;
        }

        /**
         * Removes items from repodata json.
         * @param checksums List of the checksums of the packages to remove.
         * @throws ArtipieIOException On IO errors
         */
        public void perform(final Set<String> checksums) {
            final JsonFactory factory = new JsonFactory();
            try {
                new JsonMaid.Jackson(
                    factory.createGenerator(this.out), factory.createParser(this.input)
                ).clean(checksums);
            } catch (final IOException err) {
                throw new ArtipieIOException(err);
            }
        }
    }

}
