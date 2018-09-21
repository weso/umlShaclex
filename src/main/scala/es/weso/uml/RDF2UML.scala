package es.weso.uml

import es.weso.rdf.PrefixMap
import es.weso.rdf.nodes.IRI
import es.weso.shex.{BNodeLabel, IRILabel, ShapeLabel}

object RDF2UML {

  private[uml] def iri2Label(iri: IRI, pm: PrefixMap): String = {
    // It changes <uri> by [uri] to avoid problems visualizing SVG in HTML
    val ltgt = "<(.*)>".r
    pm.qualify(iri) match {
      case ltgt(s) => s"$s"
      case s => s
    }
  }

  private[uml] def mkLabelHref(id: Option[ShapeLabel],
                               pm: PrefixMap): (String, Option[String]) =
   id match {
    case None => ("?", None)
    case Some(lbl) => lbl match {
      case i: IRILabel => (iri2Label(i.iri,pm), Some(i.iri.str))
      case b: BNodeLabel => (b.bnode.id, None)
    }
  }


}