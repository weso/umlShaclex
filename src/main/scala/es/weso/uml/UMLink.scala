package es.weso.uml
import UMLDiagram._

case class UMLLink(source: NodeId,
                   target: NodeId,
                   label: Name,
                   href: HRef,
                   card: UMLCardinality
                  )
