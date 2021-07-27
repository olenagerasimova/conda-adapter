/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Multi conda repodata: merges repodata (possibly obtained from different remotes) into single
 * repodata index.
 * @since 0.3
 */
public interface MultiRepodata {

    /**
     * Merges repodata.jsons into single repodata.json.
     * @param input Collections of repodata.json to merge
     * @param result Where to write the result
     */
    void merge(Collection<InputStream> input, OutputStream result);

    /**
     * Implementation of {@link MultiRepodata} that merges Repodata.json indexes checking for
     * duplicates and writes unique `packages` and `packages.conda` to the output stream.
     * Duplicates are checked by filename, first met package is written into resulting repodata,
     * other packages with the same filename are skipped.
     * Implementation does not close input or output streams, these operations should be made from
     * the outside.
     * @since 0.3
     */
    class Unique implements MultiRepodata {

        @Override
        public void merge(final Collection<InputStream> input, final OutputStream result) {
            throw new NotImplementedException("Not implemented yet");
        }
    }
}
