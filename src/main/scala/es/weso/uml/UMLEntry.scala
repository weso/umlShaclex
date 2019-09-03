package es.weso.uml
import UMLDiagram._

sealed abstract class UMLEntry

sealed abstract class ValueConstraint extends UMLEntry
case class DatatypeConstraint(name: Name,
                              href: String
                             ) extends ValueConstraint

case class Constant(name: Name) extends ValueConstraint

case class ValueSet(values: List[Value]) extends ValueConstraint

case class ValueExpr(operator: Name, vs: List[ValueConstraint]) extends ValueConstraint

case class Value(name: String, href: Option[String])

case class UMLField(name: Name,
                    href: Option[HRef],
                    valueConstraints: List[ValueConstraint],
                    card: UMLCardinality
                   ) extends UMLEntry

case class FieldExpr(operator: Name,
                     es: List[UMLField]
                    ) extends UMLEntry
