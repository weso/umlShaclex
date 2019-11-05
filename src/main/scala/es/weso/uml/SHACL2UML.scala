package es.weso.uml

import cats.data.{EitherT, State}
import cats.implicits._
import es.weso.rdf.PREFIXES._
import es.weso.rdf.PrefixMap
import es.weso.rdf.nodes.{BNode, IRI, RDFNode}
import es.weso.shex.{BNodeLabel, IRILabel, ShapeLabel}
import es.weso.shacl._
import es.weso.uml.UMLDiagram._
import RDF2UML._

object SHACL2UML {

  def schema2Uml(schema: Schema, options: ConversionOptions): Either[String,UML] = {
    val (state, maybe) = cnvSchema(schema).value.run(StateValue(UML.empty,0)).value
    maybe.map(_ => state.uml)
  }

  type Id = Int
  case class StateValue(uml: UML, currentId: Id)
  type S[A] = State[StateValue,A]
  type Converter[A] = EitherT[S,String,A]

  private def ok[A](x:A): Converter[A] =
    EitherT.pure[S, String](x)

  private def err[A](s: String): Converter[A] =
    EitherT.left[A](State.pure(s))

  private def modify(fn: StateValue => StateValue): Converter[Unit] =
    EitherT.liftF(State.modify(fn))

  private def updateUML(fn: UML => UML): Converter[Unit] =
    modify(s => s.copy(uml = fn(s.uml)))

  private def getUML: Converter[UML] = get.map(_.uml)

  private def setUML(uml: UML): Converter[Unit] = modify(s => s.copy(uml = uml))

  private def generateId: Converter[Id] = for {
    s <- get
    _ <- modify(_ => s.copy(currentId = s.currentId + 1))
  } yield s.currentId

  private def get: Converter[StateValue] = {
    val s: State[StateValue,StateValue] = State.get
    EitherT.liftF(s)
  }

  private def cnvList[A,B](vs: List[A], cnv: A => Converter[B]): Converter[List[B]] =
    vs.map(cnv(_)).sequence[Converter,B]

  private def newLabel(maybeLbl: Option[ShapeLabel]): Converter[NodeId] =
    maybeLbl match {
      case Some(lbl) => for {
        uml <- getUML
        (uml1, id) = uml.newLabel(lbl)
        _ <- setUML(uml1)
      } yield id
      case None => for {
        uml <- getUML
        newId <- generateId
        (uml1,id) = uml.newLabel(BNodeLabel(BNode("L" + newId)))
        _ <- setUML(uml1)
      } yield id
    }

  private def shapeRef2label(s: RefNode): Option[ShapeLabel] =
    node2label(s.id)

  private def node2label(node: RDFNode): Option[ShapeLabel] = node match {
    case iri: IRI => Some(IRILabel(iri))
    case bn: BNode => Some(BNodeLabel(bn))
    case _ => None
  }

  private def cnvComponents(cs: Seq[Component], schema: Schema): Converter[List[UMLEntry]] =
    ok(List())

  private def cnvPropertyShapes(cs: Seq[RefNode],
                                schema: Schema
                               ): Converter[List[UMLEntry]] =
    ok(List())

  private def cnvShape(id: NodeId, s: Shape, schema: Schema): Converter[UMLClass] = for {
    components <- cnvComponents(s.components, schema)
    propertyShapes <- cnvPropertyShapes(s.propertyShapes,schema)
  } yield {
    val maybeLabel = node2label(s.id)
    val (lbl,href) = mkLabelHref(maybeLabel,schema.pm)
    UMLClass(id, lbl,href, List(components,propertyShapes), List())
  }

  private def cnvSchema(schema: Schema): Converter[Unit] = {
        def cmb(x: Unit, s: (RefNode,Shape)): Converter[Unit] = {
          val lbl = shapeRef2label(s._1)
          for {
          id <- newLabel(lbl)
          cls <- {
            cnvShape(id, s._2, schema)
          }
          _ <- updateUML(_.addClass(cls))
        } yield (())
        }
    schema.shapesMap.toList.foldM(())(cmb)
  }

  private def iri2Label(iri: IRI, pm: PrefixMap): String = {
    // It changes <uri> by [uri] to avoid problems visualizing SVG in HTML
    val ltgt = "<(.*)>".r
    pm.qualify(iri) match {
      case ltgt(s) => s"$s"
      case s => s
    }
  }

  private def predicate2lbl(iri: IRI, pm: PrefixMap): (Name, HRef) = iri match {
    case `rdf:type` => ("a",iri.str)
    case _ => (iri2Label(iri,pm), iri.str)
  }

  private def mkLs[A](ls: List[A]*): List[List[A]] = {
    val zero: List[List[A]] = List()
    def cmb(rest: List[List[A]], x: List[A]): List[List[A]] =
      if (x.isEmpty) rest
      else x :: rest
    ls.foldLeft(zero)(cmb)
  }

  private def mkList[A](lss: List[List[A]]*): List[List[A]] = {
    val zero: List[List[A]] = List()
    def cmb(rest: List[List[A]], current: List[List[A]]): List[List[A]] =
      if (current == List() || current == List(List())) rest
      else rest ++ current
    lss.foldLeft(zero)(cmb)
  }

}
