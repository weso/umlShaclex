package es.weso.uml
import UMLDiagram._
import io.circe.Json

sealed abstract class UMLComponent {
  def toJson: Json
}
case class UMLClass(id: NodeId,
                    label: Name,
                    href: Option[HRef],
                    entries: List[List[UMLEntry]],
                    _extends: List[NodeId]
                   ) extends UMLComponent {
  override def toJson: Json = {
    def fn(es: List[UMLEntry]): Json = Json.fromValues(es.map(_.toJson))
    Json.fromFields(List(
      ("id", Json.fromInt(id)),
      ("label", Json.fromString(label)),
      ("entries",Json.fromValues(entries.map(fn))),
      ("href", href.fold(Json.Null)(Json.fromString(_)))
     ))
  }
}

case class Operator(name: Name, args: List[UMLComponent]) extends UMLComponent {
  override def toJson: Json = {
    Json.fromFields(List(
      ("name", Json.fromString(name)),
      ("components", Json.fromValues(args.toList.map(_.toJson)))
    ))
  }
}
case class UMLConstant(name: Name) extends UMLComponent {
  override def toJson: Json = {
    Json.fromFields(List(
      ("name", Json.fromString(name))
    ))
  }
}
