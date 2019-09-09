package es.weso.uml
import UMLDiagram._
import io.circe.Json

case class UMLLink(source: NodeId,
                   target: NodeId,
                   label: Name,
                   href: HRef,
                   card: UMLCardinality
                  ) {
  def toJson: Json = {
    Json.fromFields(
      List(
        ("source", Json.fromInt(source)),
        ("target", Json.fromInt(target)),
        ("label", Json.fromString(label)),
        ("href", Json.fromString(href)),
        ("card", card.toJson)
      ))
  }
}
