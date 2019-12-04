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

The project uses [sbt](http://www.scala-sbt.org/) for compilation as well as Java 1.8 or later.

* `sbt packageBin` compiles and generates executable Jar file

## Usage

The library can be used programmmatically or from the command line. Examples:

-Convert a ShEx schema file to SVG

```bash
run --schema examples/shex/simple.shex --schemaFormat ShExC -o simple.svg -f svg
```

-Convert a ShEx schema file to UML (using [PlantUML](http://plantuml.com/) format)

```bash
run --schema examples/shex/simple.shex --schemaFormat ShExC -o simple.uml -f uml
```

## Contribution

Contributions are greatly appreciated.
Please fork this repository and open a
pull request to add more features or [submit issues](https://github.com/labra/umlShaclex/issues)
