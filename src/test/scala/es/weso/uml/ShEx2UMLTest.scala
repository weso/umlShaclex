package es.weso.uml
import es.weso.rdf.PREFIXES._
import es.weso.rdf.nodes.IRI
import es.weso.shex.{IRILabel, Schema}
import es.weso.utils.FileUtils
import org.scalatest.{FunSpec, Matchers}

class ShExUMLTest extends FunSpec with Matchers {

  describe(s"ShEx2UML") {
/*    it(s"Should convert simple Shape with IRI") {
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
        shex <- Schema.fromString(shexStr,"ShExC")
        uml <- ShEx2UML.schema2Uml(shex)
      } yield uml
      maybe.fold(
        e => fail(s"Error converting to UML: $e"),
        umlConverted => {
          info(s"Expected: \n$uml\nObtained:\n$umlConverted")
          uml should be(umlConverted)
          uml.toSVG should include ("<svg")
        }
      )
    }

    it(s"Should convert simple Shape with self-reference") {
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
        List(UMLLink(0,0,":knows",(ex + "knows").str, NoCard))
      )
      val maybe = for {
        shex <- Schema.fromString(shexStr,"ShExC")
        uml <- ShEx2UML.schema2Uml(shex)
      } yield uml
      maybe.fold(
        e => fail(s"Error converting to UML: $e"),
        umlConverted => {
          info(s"Expected: \n$uml\nObtained:\n$umlConverted")
          uml should be(umlConverted)
          // uml.toSVG should include ("<svg")
        }
      )
    }

    it(s"Should convert value set with rdf:type") {
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
        shex <- Schema.fromString(shexStr,"ShExC")
        uml <- ShEx2UML.schema2Uml(shex)
      } yield uml
      maybe.fold(
        e => fail(s"Error converting to UML: $e"),
        uml => {
          info(s"Converted")
          // uml.toSVG should include("<svg")
        }
      )
    }

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
    it(s"Should convert a Shape with OR and self-reference") {
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
          uml.toSVG should include ("<svg")
        }
      )
    }
  }

}
