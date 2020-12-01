package es.weso.uml

import es.weso.rdf.PREFIXES._
import es.weso.rdf.nodes.IRI
import es.weso.shex.{IRILabel, Schema}
import es.weso.uml.UMLDiagram._
import es.weso.utils.FileUtils
import org.scalatest._ 
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class UMLTest extends AnyFunSpec with Matchers {

  describe("Serialize PlantUML") {
    it("Should generate plantuml diagram") {
      val field1 = UMLField("xsd:name", Some("http://schema.org/name"), List(DatatypeConstraint("xsd:string","http;//xmlschema.org/string")), Range(1,Unbounded))
      val field2 = UMLField("xsd:age", Some("http://schema.org/age"), List(DatatypeConstraint("xsd:int","http;//xmlschema.org/int")), Range(1,IntMax(1)))
      val field3 = UMLField("xsd:homePage", Some("http://schema.org/homePage"), List(Constant("IRI")), Range(1,IntMax(1)))
      val cls1 = UMLClass(1, ":User", Some("http://schema.org/User"), List(List(field1, field2)),List())
      val cls2 = UMLClass(2, ":Company", Some("http://schema.org/User"), List(List(field3)), List())
      val link1 = Relationship(1,2,"schema:worksFor","http://schema.org", Star)
      val uml = UML(Map(IRILabel(IRI("L1")) -> 1, IRILabel(IRI("L2")) -> 2), Map(1 -> cls1, 2 -> cls2), List(link1))
      uml.components.size should be(2)
      uml.links.length should be(1)
      println(uml.toPlantUML(PlantUMLOptions(watermark=None)))
    }
  }

}
