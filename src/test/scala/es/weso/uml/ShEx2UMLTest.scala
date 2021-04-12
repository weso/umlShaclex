package es.weso.uml
import es.weso.rdf.PREFIXES._
import es.weso.rdf.nodes.IRI
import es.weso.shex.{IRILabel, Schema}
import es.weso.utils.IOUtils._
import cats.effect._ 
import cats.data._
import munit._

class ShExUMLTest extends CatsEffectSuite {


    test(s"Should convert simple Shape with node constraint IRI") {
      val shexStr =
        """|prefix : <http://example.org/>
           |
           |:User IRI
        """.stripMargin
      val ex = IRI(s"http://example.org/")
      // val nameField = UMLField(":name", Some((ex + "name").str), List(Constant("IRI")), NoCard)
      // val aField = UMLField("a", Some((`rdf:type`).str), List(Constant("IRI")), NoCard)

      val umlClass = UMLClass(0,":User", Some((ex + "User").str), List(List(Constant("IRI"))), List())
      val uml = UML(Map(IRILabel(ex + "User") -> 0), Map(0 -> umlClass), List())
      val maybe: EitherT[IO,String,(UML,List[String],String)] = for {
        shex <- io2es(Schema.fromString(shexStr,"ShExC"))
        pair <- either2es(ShEx2UML.schema2Uml(shex))
        (uml,es) = pair
        svg <- io2es(uml.toSVG(PlantUMLOptions.empty))
      } yield (uml,es,svg)
      run_es(maybe).map(e => e match { 
        case Right(pair) => {
          val (umlConverted,_,svg) = pair
          assertEquals(uml, umlConverted)
          assertEquals(svg.contains("<svg"), true)
        }
        case Left(e) => fail(e)
      }
      )
    }

    test(s"Should convert simple Shape with IRI") {
      val shexStr =
        """|prefix : <http://example.org/>
           |
           |:User {
           | a     IRI ;
           |}
        """.stripMargin
      val ex = IRI(s"http://example.org/")
      val aField = UMLField("a", Some((`rdf:type`).str), List(Constant("IRI")), NoCard)

      val umlClass = UMLClass(0,":User", Some((ex + "User").str), List(List(aField)), List())
      val uml = UML(Map(IRILabel(ex + "User") -> 0), Map(0 -> umlClass), List())
      val maybe = for {
        shex <- io2es(Schema.fromString(shexStr,"ShExC"))
        pair <- either2es(ShEx2UML.schema2Uml(shex))
        (uml,es) = pair
        svg <- io2es(uml.toSVG(PlantUMLOptions.empty))
      } yield (uml,es,svg)
      run_es(maybe).map(_.fold(
        e => fail(s"Error converting to UML: $e"),
        pair => {
          val (umlConverted,warnings,svg) = pair
          // info(s"Expected: \n$uml\nObtained:\n$umlConverted")
          assertEquals(uml, umlConverted)
          assertEquals(svg.contains("<svg"), true)
        }
      ))
    }

    test(s"Should convert simple Shape with IRI and a name") {
      val shexStr =
        """|prefix : <http://example.org/>
           |
           |:User {
           | a     IRI ;
           | :name IRI ;
           |}
        """.stripMargin
      val ex = IRI(s"http://example.org/")
      val nameField = UMLField(":name", Some((ex + "name").str), List(Constant("IRI")), NoCard)
      val aField = UMLField("a", Some((`rdf:type`).str), List(Constant("IRI")), NoCard)

      val umlClass = UMLClass(0,":User", Some((ex + "User").str), List(List(aField),List(nameField)), List())
      val uml = UML(Map(IRILabel(ex + "User") -> 0), Map(0 -> umlClass), List())
      val maybe = for {
        shex <- io2es(Schema.fromString(shexStr,"ShExC"))
        pair <- either2es(ShEx2UML.schema2Uml(shex))
        (uml,es) = pair
        svg <- io2es(uml.toSVG(PlantUMLOptions.empty))
      } yield (uml,es,svg)
      run_es(maybe).map(_.fold(
        e => fail(s"Error converting to UML: $e"),
        pair => {
          val (umlConverted,_,svg) = pair
          // info(s"Expected: \n$uml\nObtained:\n$umlConverted")
          assertEquals(uml, umlConverted)
          assertEquals(svg.contains("<svg"), true)
        }
      ))
    }

    test(s"Should convert simple Shape with self-reference") {
      val shexStr =
        """|prefix : <http://example.org/>
           |
           |:User {
           | :knows @:User ;
           |}
        """.stripMargin
      val ex = IRI(s"http://example.org/")
      val umlClass = UMLClass(0,":User", Some((ex + "User").str), List(), List())
      val uml = UML(
        Map(IRILabel(ex + "User") -> 0),
        Map(0 -> umlClass),
        List(Relationship(0,0,":knows",(ex + "knows").str, NoCard))
      )
      val maybe = for {
        shex <- io2es(Schema.fromString(shexStr,"ShExC"))
        uml <- either2es(ShEx2UML.schema2Uml(shex))
      } yield uml
      run_es(maybe).map(_.fold(
        e => fail(s"Error converting to UML: $e"),
        pair => {
          val (umlConverted,_) = pair
          // info(s"Expected: \n$uml\nObtained:\n$umlConverted")
          assertEquals(uml, umlConverted)
          // uml.toSVG should include ("<svg")
        }
      ))
    }

    test(s"Should convert value set with rdf:type") {
      val shexStr =
        """|prefix : <http://example.org/>
           |
           |:User CLOSED Extra a {
           | a [ :Person <Friend> "Hi"~ @es] ;
           | :worksFor @:Company OR :Factory ;
           | :unknwon . ;
           | :parent { :name . } ;
           | :knows @:User ;
           |}
        """.stripMargin
      val maybe = for {
        shex <- io2es(Schema.fromString(shexStr,"ShExC"))
        uml <- either2es(ShEx2UML.schema2Uml(shex))
      } yield uml
      run_es(maybe).map(_.fold(
        e => fail(s"Error converting to UML: $e"),
        uml => {
        //  info(s"Converted")
          // uml.toSVG should include("<svg")
          assertEquals(true,true)
        }
      ))
    }
/*
    it(s"Shouldn't fail with FHIR schema") {
      val fhirFile = "examples/shex/fhir/observation.shex"
      val maybe = for {
        str <- FileUtils.getContents(fhirFile)
        shex <- Schema.fromString(str,"ShExC")
        uml <- ShEx2UML.schema2Uml(shex)
      } yield uml
      maybe.fold(
        e => fail(s"Error converting to UML: $e"),
        uml => {
          info(s"FHIR schema converted to UML")
        }
      )
    }
*/
/*    it(s"Should convert a Shape with OR and self-reference") {
      val shexStr =
        """|prefix : <http://example.org/>
           |
           |:User {
           | :knows (@:User OR IRI)? ;
           |}
        """.stripMargin
      val ex = IRI(s"http://example.org/")
      val umlClass = UMLClass(0,":User", Some((ex + "User").str), List(), List())
      val uml = UML(Map(IRILabel(ex + "User") -> 0), Map(0 -> umlClass), List())

      val maybe = for {
        shex <- Schema.fromString(shexStr,"ShExC")
        uml <- ShEx2UML.schema2Uml(shex)
      } yield uml
      maybe.fold(
        e => fail(s"Error converting to UML: $e"),
        umlConverted => {
          info(s"Expected: \n$uml\nObtained:\n$umlConverted")
          uml should be(umlConverted)
          uml.toSVG(PlantUMLOptions(watermark = None)) should include ("<svg")
        }
      )
    } */

}
