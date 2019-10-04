package es.weso.uml
import UMLDiagram._
import io.circe.Json

sealed abstract class UMLLink {
  def toJson: Json
}

case class Relationship(source: NodeId,
                    target: NodeId,
                    label: Name,
                    href: HRef,
                    card: UMLCardinality
                   ) extends UMLLink {
  override def toJson: Json = {
    Json.fromFields(
      List(
        ("type", Json.fromString("Relationship")),
        ("source", Json.fromInt(source)),
        ("target", Json.fromInt(target)),
        ("label", Json.fromString(label)),
        ("href", Json.fromString(href)),
        ("card", card.toJson)
      ))
  }
}

case class Inheritance(source: NodeId,
                       target: NodeId) extends UMLLink {
  override def toJson: Json = {
    Json.fromFields(
      List(
        ("type", Json.fromString("Inheritance")),
        ("source", Json.fromInt(source)),
        ("target", Json.fromInt(target))
      ))
  }
}
