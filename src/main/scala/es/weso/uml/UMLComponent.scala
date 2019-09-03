package es.weso.uml
import UMLDiagram._

sealed abstract class UMLComponent
case class UMLClass(id: NodeId,
                    label: Name,
                    href: Option[HRef],
                    entries: List[List[UMLEntry]],
                    _extends: List[NodeId]
                   ) extends UMLComponent

case class Operator(name: Name, args: List[UMLComponent]) extends UMLComponent
case class UMLConstant(name: Name) extends UMLComponent
