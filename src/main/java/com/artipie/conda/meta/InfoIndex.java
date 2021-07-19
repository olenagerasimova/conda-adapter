/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.meta;

import com.artipie.ArtipieException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;

/**
 * Conda package metadata file info/index.json.
 * @since 0.2
 */
public interface InfoIndex {

    /**
     * Conda package metadata info/index.json content as json object.
     * @return Metadata json
     * @throws IOException On error
     */
    JsonObject json() throws IOException;

    /**
     * Implementation of {@link InfoIndex} to read metadata from `tar.bz2` conda package.
     * @since 0.2
     */
    final class TarBz implements InfoIndex {

        /**
         * Conda `tar.bz2` package as input stream.
         */
        private final InputStream input;

        /**
         * Ctor.
         * @param input Conda `tar.bz2` package as input stream
         */
        public TarBz(final InputStream input) {
            this.input = input;
        }

        @Override
        @SuppressWarnings("PMD.AssignmentInOperand")
        public JsonObject json() throws IOException {
            try (
                ArchiveInputStream archive = new ArchiveStreamFactory().createArchiveInputStream(
                    new ByteArrayInputStream(
                        IOUtils.toByteArray(new BZip2CompressorInputStream(this.input))
                    )
                )
            ) {
                ArchiveEntry entry;
                while ((entry = archive.getNextEntry()) != null) {
                    if (!archive.canReadEntryData(entry) || entry.isDirectory()) {
                        continue;
                    }
                    if ("info/index.json".equals(entry.getName())) {
                        return Json.createReader(archive).readObject();
                    }
                }
            } catch (final ArchiveException ex) {
                throw new IOException(ex);
            }
            throw new ArtipieException("Illegal conda package: info/index.json file not found");
        }
    }
}
