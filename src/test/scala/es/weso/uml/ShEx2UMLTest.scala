package es.weso.uml

import es.weso.rdf.PREFIXES._
import es.weso.rdf.nodes.IRI
import es.weso.shex.{IRILabel, Schema}
import es.weso.uml.UMLDiagram._
import es.weso.utils.FileUtils
import org.scalatest.{FunSpec, Matchers}

class ShExUMLTest extends FunSpec with Matchers {

  describe("Dot") {
    it("Should generate plantuml diagram") {
      val field1 = UMLField("xsd:name", Some("http://schema.org/name"), List(DatatypeConstraint("xsd:string","http;//xmlschema.org/string")), Range(1,Unbounded))
      val field2 = UMLField("xsd:age", Some("http://schema.org/age"), List(DatatypeConstraint("xsd:int","http;//xmlschema.org/int")), Range(1,IntMax(1)))
      val field3 = UMLField("xsd:homePage", Some("http://schema.org/homePage"), List(Constant("IRI")), Range(1,IntMax(1)))
      val cls1 = UMLClass(1, ":User", Some("http://schema.org/User"), List(List(field1, field2)),List())
      val cls2 = UMLClass(2, ":Company", Some("http://schema.org/User"), List(List(field3)), List())
      val link1 = UMLLink(1,2,"schema:worksFor","http://schema.org", Star)
      val uml = UML(Map(IRILabel(IRI("L1")) -> 1, IRILabel(IRI("L2")) -> 2), Map(1 -> cls1, 2 -> cls2), List(link1))
      uml.classes.size should be(2)
      uml.links.length should be(1)
      // println(uml.toPlantUML)
    }
  }

  describe(s"ShEx2UML") {
    it(s"Should convert simple Shape with IRI") {
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
  }

}