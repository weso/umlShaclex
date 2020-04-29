package es.weso.uml
import UMLDiagram._
import io.circe.Json

/**
  * Represents a UML component
  */
sealed abstract class UMLComponent {
  def toJson: Json
}

/**
  * UML class
  * @param id node Id
  * @param label Label
  * @param href Hyperref
  * @param entries list of UML entries
  * @param _extends List of nodes that it extends
  */
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

/**
  * UMLComponent that represents an operator
  * @param name name of operator
  * @param args list of arguments
  */
case class Operator(name: Name, args: List[UMLComponent]) extends UMLComponent {
  override def toJson: Json = {
    Json.fromFields(List(
      ("name", Json.fromString(name)),
      ("components", Json.fromValues(args.toList.map(_.toJson)))
    ))
  }
}

/**
  * UML constant
  * @param name
  */
case class UMLConstant(name: Name) extends UMLComponent {
  override def toJson: Json = {
    Json.fromFields(List(
      ("name", Json.fromString(name))
    ))
  }
}
