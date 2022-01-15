# umlShaclex

Convert [SHACL](http://w3c.github.io/data-shapes/shacl/) and
[ShEx](http://www.shex.io) to UML-like class diagrams

[![Build Status](https://travis-ci.org/weso/umlShaclex.svg?branch=master)](https://travis-ci.org/weso/umlShaclex)
[![codecov](https://codecov.io/gh/weso/umlShaclex/branch/master/graph/badge.svg)](https://codecov.io/gh/weso/umlShaclex)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/74d68f09d6cf488c8ac9da54bfbdc416)](https://www.codacy.com/gh/weso/umlShaclex?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=weso/umlShaclex&amp;utm_campaign=Badge_Grade)

## Author & contributors

* Author: [Jose Emilio Labra Gayo](http://www.di.uniovi.es/~labra)

## Adopters

* [RDFShape](http://rdfshape.weso.es): An online demo powered by this library.

## Installation and compilation

The project uses [sbt](http://www.scala-sbt.org/) for compilation.

Once you have installed sbt, you can generate an executable, running:

```sh
sbt universal:packageBin
```

It will generate an executable zip file in folder:

```
target/universal/umlShaclex-x.y.z.zip
```

Once you have obtained the binary, you can uncompress in a folder and run it.

## Usage

If you created a binary as described in previous section, you can invoke it as `umlShaclex`

It is also possible to run it using sbt as: `sbt run <args>`.


The library can be used programmmatically or from the command line. Examples:

-Convert a ShEx schema file to SVG

```bash
umlShaclex --schema examples/shex/simple.shex --schemaFormat ShExC -o simple.svg -f svg
```

-Convert a ShEx schema file to UML (using [PlantUML](http://plantuml.com/) format)

```bash
umlShaclex --schema examples/shex/simple.shex --schemaFormat ShExC -o simple.uml -f uml
```


## Publishing to OSS-Sonatype

This project uses [the sbt ci release](https://github.com/olafurpg/sbt-ci-release) plugin for publishing to [OSS Sonatype](https://oss.sonatype.org/).

##### SNAPSHOT Releases
Open a PR and merge it to watch the CI release a -SNAPSHOT version

##### Full Library Releases
1. Push a tag and watch the CI do a regular release
2. `git tag -a v0.1.0 -m "v0.1.0"`
3. `git push origin v0.1.0`
_Note that the tag version MUST start with v._

## Contribution

Contributions are greatly appreciated.
Please fork this repository and open a
pull request to add more features or [submit issues](https://github.com/labra/umlShaclex/issues)
