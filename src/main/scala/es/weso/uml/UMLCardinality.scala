package es.weso.uml

sealed abstract class UMLCardinality
case object Star extends UMLCardinality {
  override def toString = "*"
}
case object Plus extends UMLCardinality {
  override def toString = "+"
}
case object Optional extends UMLCardinality {
  override def toString = "?"
}
case class Range(min: Int, max: IntOrUnbounded) extends UMLCardinality {
  override def toString = s"{$min,$max}"
}
sealed abstract class IntOrUnbounded
case object Unbounded extends IntOrUnbounded {
  override def toString = "&#8734;"
}
case class IntMax(v: Int) extends IntOrUnbounded {
  override def toString: String = v.toString
}
case object NoCard extends UMLCardinality {
  override def toString = " "
}
