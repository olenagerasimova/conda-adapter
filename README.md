<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/artipie/conda-adapter)](http://www.rultor.com/p/artipie/conda-adapter)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Javadoc](http://www.javadoc.io/badge/com.artipie/conda-adapter.svg)](http://www.javadoc.io/doc/com.artipie/conda-adapter)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/com.artipie/conda-adapter/blob/master/LICENSE)
[![codecov](https://codecov.io/gh/artipie/conda-adapter/branch/master/graph/badge.svg)](https://codecov.io/gh/artipie/conda-adapter)
[![Hits-of-Code](https://hitsofcode.com/github/artipie/conda-adapter)](https://hitsofcode.com/view/github/artipie/conda-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/com.artipie/conda-adapter.svg)](https://maven-badges.herokuapp.com/maven-central/com.artipie/conda-adapter)
[![PDD status](http://www.0pdd.com/svg?name=artipie/conda-adapter)](http://www.0pdd.com/p?name=artipie/conda-adapter)

This Java library turns your binary storage (files, S3 objects, anything) into Conda repository.
You may add it to your binary storage and it will become a fully-functionable Conda repository, 
which [anaconda](https://anaconda.org/) will perfectly understand.

## Conda repository structure

Conda repository is [structured directory tree](https://docs.conda.io/projects/conda-build/en/latest/resources/package-spec.html#repository-structure-and-index) 
with platform subdirectories, each platform subdirectory contains index file and conda packages. 

```commandline
<root>/linux-64/repodata.json
                repodata.json.bz2
                misc-1.0-np17py27_0.tar.bz2
      /win-32/repodata.json
              repodata.json.bz2
              misc-1.0-np17py27_0.tar.bz2
```

### Repodata file

Repodata json [contains](https://docs.conda.io/projects/conda-build/en/latest/concepts/generating-index.html#repodata-json) 
list of the packages metadata in platform directory and subdir where package is located:

```json
{
  "packages": {
    "super-fun-package-0.1.0-py37_0.tar.bz2": {
      "build": "py37_0",
      "build_number": 0,
      "depends": [
        "some-depends"
      ],
      "license": "BSD",
      "md5": "a75683f8d9f5b58c19a8ec5d0b7f796e",
      "name": "super-fun-package",
      "sha256": "1fe3c3f4250e51886838e8e0287e39029d601b9f493ea05c37a2630a9fe5810f",
      "size": 3832,
      "subdir": "win-64",
      "timestamp": 1530731681870,
      "version": "0.1.0"
    }
  }
}
```
