# shaclex

Convert [SHACL](http://w3c.github.io/data-shapes/shacl/) and
[ShEx](http://www.shex.io) to UML-like class diagrams

[![Build Status](https://travis-ci.org/labra/umlShaclex.svg?branch=master)](https://travis-ci.org/labra/umlShaclex)

## Author & contributors

* Author: [Jose Emilio Labra Gayo](http://www.di.uniovi.es/~labra)

## Adopters

* [RDFShape](http://rdfshape.weso.es): An online demo powered by this library.

## Installation and compilation

The project uses [sbt](http://www.scala-sbt.org/) for compilation as well as Java 1.8 or later.


* `sbt packageBin` compiles and generates executable Jar file


## Usage

The library can be used programmmatically or from the command line. Examples:

- Convert a ShEx schema file to SVG

``` 
run --schema examples/shex/simple.shex --schemaFormat ShExC -o simple.svg -f svg
```

- Convert a ShEx schema file to UML (using [PlantUML](http://plantuml.com/) format)

``` 
run --schema examples/shex/simple.shex --schemaFormat ShExC -o simple.uml -f uml
```

## Contribution

Contributions are greatly appreciated.
Please fork this repository and open a
pull request to add more features or [submit issues](https://github.com/labra/umlShaclex/issues)