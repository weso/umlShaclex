package es.weso.uml

import io.circe.Json

sealed abstract class UMLCardinality {
  def toJson: Json
}
case object Star extends UMLCardinality {
  override def toString = "*"
  override def toJson: Json = Json.fromString("*")
}
case object Plus extends UMLCardinality {
  override def toString = "+"
  override def toJson: Json = Json.fromString("+")
}
case object Optional extends UMLCardinality {
  override def toString = "?"
  override def toJson: Json = Json.fromString("?")
}
case class Range(min: Int, max: IntOrUnbounded) extends UMLCardinality {
  override def toString = s"{$min,$max}"
  override def toJson: Json =
    Json.fromFields(
      List(
        ("min", Json.fromInt(min)),
        ("max", max.toJson)
      )
    )
}
sealed abstract class IntOrUnbounded {
  def toJson: Json
}

case object Unbounded extends IntOrUnbounded {
  override def toString = "&#8734;"
  override def toJson: Json = Json.fromString("&#8734;")
}
case class IntMax(v: Int) extends IntOrUnbounded {
  override def toString: String = v.toString
  override def toJson: Json = Json.fromInt(v)
}
case object NoCard extends UMLCardinality {
  override def toString = " "
  override def toJson: Json = Json.fromString(" ")
}
