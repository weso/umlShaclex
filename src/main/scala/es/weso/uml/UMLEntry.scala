package es.weso.uml
import UMLDiagram._
import io.circe.Json

/**
  * UML Entry
  */
sealed abstract class UMLEntry {
  def toJson: Json
}

/**
  * UML entry that represents a constant
  */
sealed abstract class ValueConstraint extends UMLEntry {
  override def toJson: Json
}

/**
  * UML entry that represents a basic constraint
  * @param name name of the constraint
  * @param href hyperref
  */
case class RefConstraint(name: Name,
                         href: String
                        ) extends ValueConstraint {
  override def toJson: Json = Json.fromFields(List(
    ("type", Json.fromString("Reference")),
    ("name", Json.fromString(name)),
    ("href", Json.fromString(href))
  ))
}

/**
  * UML entry that represents a Datatype constraint
  * @param name
  * @param href
  */
case class DatatypeConstraint(name: Name,
                              href: String
                             ) extends ValueConstraint {
  override def toJson: Json = Json.fromFields(List(
    ("type", Json.fromString("DatatypeConstraint")),
    ("name", Json.fromString(name)),
    ("href", Json.fromString(href))
  ))
}

/**
  * UML entry that represents no constraint
  * @param name
  * @param href
  */
case class NoConstraint() extends ValueConstraint {
  override def toJson: Json = Json.fromFields(List(
    ("type", Json.fromString("NoConstraint"))
  ))
}

/**
  * A constant value
  * @param name
  */
case class Constant(name: Name) extends ValueConstraint {
  override def toJson: Json = Json.fromFields(List(
    ("type", Json.fromString("Constant")),
    ("name", Json.fromString(name))
  ))
}

/**
  * Represents a set of values
  * @param values
  */
case class ValueSet(values: List[Value]) extends ValueConstraint {
  override def toJson: Json = Json.fromValues(
    values.map(_.toJson)
  )
}

/**
  * Represents a value expression
  * @param operator name of the expression
  * @param vs list of arguments
  */
case class ValueExpr(operator: Name, vs: List[ValueConstraint]) extends ValueConstraint {
  def toJson: Json = Json.fromFields(
    List(
      ("operator", Json.fromString(operator)),
      ("values", Json.fromValues(vs.map(_.toJson)))
    )
  )
}

case class Value(name: String, href: Option[String]) {
  def toJson: Json = Json.fromFields(
    List(
      ("name", Json.fromString(name)),
      ("href", href.fold(Json.Null)(Json.fromString(_)))
    )
  )
}

/**
  * UML entry that represents a field
  * @param name
  * @param href
  * @param valueConstraints
  * @param card
  */
case class UMLField(name: Name,
                    href: Option[HRef],
                    valueConstraints: List[ValueConstraint],
                    card: UMLCardinality
                   ) extends UMLEntry {
  def toJson: Json = {
    val valueCs = Json.fromValues(valueConstraints.map(_.toJson))
    Json.fromFields(
      List(
        ("name", Json.fromString(name)),
        ("card", card.toJson),
        ("href",href.fold(Json.Null)(Json.fromString(_))),
        ("valueConstraints", valueCs)
      )
    )
  }
}

case class FieldExpr(operator: Name,
                     es: List[UMLField]
                    ) extends UMLEntry {
  def toJson: Json = Json.fromFields(List(
    ("operator", Json.fromString(operator)),
    ("fields", Json.fromValues(es.map(_.toJson)))
   )
  )
}
