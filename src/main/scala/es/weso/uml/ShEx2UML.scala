package es.weso.uml
import cats._
import cats.data._
import cats.implicits._
import es.weso.shex.implicits.eqShEx._
import cats.data._
import es.weso.rdf.PrefixMap
import es.weso.rdf.nodes.{BNode, IRI}
import es.weso.rdf.PREFIXES._
import es.weso.shex._
import es.weso.uml.UMLDiagram._
import es.weso.uml.{
 Optional => UMLOptional,
 Star => UMLStar,
 Plus => UMLPlus,
 IntMax => UMLIntMax,
 _
}
import es.weso.shex.implicits.showShEx._
import es.weso.rdf.operations.Comparisons._
import RDF2UML._

object ShEx2UML {

  def schema2Uml(schema: Schema): Either[String,(UML,List[String])] = {
    val (warnings, (state,maybe)) =
      cnvSchema(schema).value.run(StateValue(UML.empty,0)).run
    maybe.map(_ => ((state.uml, warnings)))
  }


  type Id = Int

  case class StateValue(uml: UML, currentId: Id)
  type Logged[A] = Writer[List[String],A]
  type S[A] = StateT[Logged,StateValue,A]
  type Converter[A] = EitherT[S,String,A]

  private def ok[A](x:A): Converter[A] =
    EitherT.pure[S, String](x)

  private def err[A](s: String): Converter[A] =
    EitherT.left[A](StateT.pure(s))

  private def log[A](msg: String, v: A): Converter[A] = {
    val w: Logged[A] = Writer(List(msg), v)
    EitherT.liftF(StateT.liftF(w))
  }

  private def modify(fn: StateValue => StateValue): Converter[Unit] =
    EitherT.liftF(StateT.modify(fn))

  private def updateUML(fn: UML => UML): Converter[Unit] =
    modify(s => s.copy(uml = fn(s.uml)))

  private def getUML: Converter[UML] = get.map(_.uml)

  private def setUML(uml: UML): Converter[Unit] = modify(s => s.copy(uml = uml))

  private def generateId: Converter[Id] = for {
    s <- get
    _ <- modify(_ => s.copy(currentId = s.currentId + 1))
  } yield s.currentId

  private def get: Converter[StateValue] = {
    val s: S[StateValue] = StateT.get
    EitherT.liftF(s)
  }

  private def cnvList[A,B](vs: List[A], cnv: A => Converter[B]): Converter[List[B]] =
    vs.map(cnv(_)).sequence[Converter,B]

  private def newLabel(maybeLbl: Option[ShapeLabel]): Converter[NodeId] =
    maybeLbl match {
      case Some(lbl) =>
        mkLabel(lbl)
      case None => for {
        uml <- getUML
        newId <- generateId
        (uml1,id) = uml.newLabel(BNodeLabel(BNode("L" + newId)))
        _ <- setUML(uml1)
      } yield id
    }

  private def mkLabel(lbl: ShapeLabel): Converter[NodeId] = for {
    uml <- getUML
    (uml1, id) = uml.newLabel(lbl)
    _ <- setUML(uml1)
  } yield id

  private def newLabels(lbls: List[ShapeLabel]): Converter[List[NodeId]] =
    lbls.map(mkLabel(_)).sequence[Converter,NodeId]

  private def cnvSchema(schema: Schema): Converter[Unit] = {
    schema.shapes match {
      case None => err(s"No shapes in schema")
      case Some(shapes) => {
        def cmb(x: Unit, s: ShapeExpr): Converter[Unit] = for {
          id <- newLabel(s.id)
          cls <- {
            cnvShapeExpr(id, s, schema.prefixMap)
          }
          _ <- updateUML(_.addClass(cls))
        } yield (())
        shapes.foldM(())(cmb)
      }
    }
  }

  private def cnvShapeExpr(id: Id, se: ShapeExpr, pm: PrefixMap): Converter[UMLClass] = se match {
    case _: ShapeOr => {
      log(s"Not implemented ShapeOr $se", UMLClass(id,"OR not implemented yet", None, List(), List()))
    }
    case sa: ShapeAnd => for {
      entries <- cnvListShapeExprEntries(sa.shapeExprs, id, pm)
    } yield {
      val (label,href) = mkLabelHref(se.id,pm)
      UMLClass(id, label, href, entries, List())
    }
    case sn: ShapeNot => 
      log(s"Not implemented ShapeNot $sn", UMLClass(id,"NOT not implemented yet", None, List(), List()))
    case sd: ShapeDecl => 
      log(s"Not implemented declarations of abstract shapes yet", UMLClass(id,"Not implemented ShapeDecl",None, List(), List()))
    case s: Shape => {
      for {
        entries <- cnvShape(s,id,pm)
        _extends <- newLabels(s._extends.getOrElse(List()))
      } yield {
        val (label,href) = mkLabel(se.id, pm)
        UMLClass(id,label,href,entries, _extends)
      }
    }
    case s: NodeConstraint => for {
      entries <- cnvNodeConstraint(s,pm)
    } yield {
      val (label,href) = mkLabel(s.id, pm)
      UMLClass(id, label, href, entries, List())
    }
    case s: ShapeExternal => ok(UMLClass(id,"External",None,List(),List()))
    case sr: ShapeRef => for {
      rid <- newLabel(Some(sr.reference))
      _ <- updateUML(_.addLink(Inheritance(id,rid)))
  } yield {
    val (labelShape,hrefShape) = mkLabel(se.id, pm)
    UMLClass(id,labelShape,hrefShape,List(),List())
  }


  }

  private def mkLabel(maybeLabel: Option[ShapeLabel], pm: PrefixMap):(String,Option[String]) =
    maybeLabel match {
      case None => ("?", None)
      case Some(lbl) => lbl match {
        case Start => ("Start", None)
        case i: IRILabel => (iri2Label(i.iri,pm), Some(i.iri.str))
        case b: BNodeLabel => (b.bnode.id, None)
      }
    }

  private def cnvListShapeExprEntries(se: List[ShapeExpr],
                                      id: NodeId,
                                      pm: PrefixMap
                                     ): Converter[List[List[UMLEntry]]] = {
    def cmb(xss: List[List[UMLEntry]], se: ShapeExpr): Converter[List[List[UMLEntry]]] = for {
      fs <- cnvShapeExprEntries(se,id,pm)
    } yield fs ++ xss
    val zero: List[List[UMLEntry]] = List(List())
    se.reverse.foldM(zero)(cmb)
  }


  private def cnvShapeExprEntries(se: ShapeExpr, id: NodeId, pm: PrefixMap): Converter[List[List[UMLEntry]]] = se match {
    case sa: ShapeAnd => for {
      ess <- cnvListShapeExprEntries(sa.shapeExprs,id,pm)
    } yield ess
    case so: ShapeOr => log(s"Nested OR not supported yet $so", List(List()))
    case sn: ShapeNot => log(s"Nested NOT not supported yet $sn", List(List()))
    case s: Shape => cnvShape(s,id,pm)
    case nk: NodeConstraint => for {
      vcs <- cnvNodeConstraint(nk,pm)
    } yield vcs
    case s: ShapeExternal => ok(List(List(UML.external)))
    case sr: ShapeRef => cnvShapeRef(sr,id,pm)
    case sd: ShapeDecl => log(s"Nested ShapeDecl not supported yet $sd", List(List()))
  }

  private def cnvShape(s: Shape, id: NodeId, pm: PrefixMap): Converter[List[List[UMLEntry]]] = for {
    closedEntries <- if (s.isClosed)
      ok(List(List(UML.umlClosed)))
    else ok(List())
    extraEntries <- {
      val extras = s.extra.getOrElse(List())
      if (extras.isEmpty) ok(List())
      else ok(List(List(Constant(s"EXTRA ${extras.map(pm.qualify(_)).mkString(" ")}"))))
    }
    // TODO virtual
    inheritedEntries <- s._extends match {
      case None => ok(List())
      case Some(ls) => cnvExtends(ls,id)
    }
    exprEntries <- s.expression match {
      case None => ok(List())
      case Some(e) => cnvTripleExpr(e, id, pm)
    }
  } yield mkList(closedEntries, extraEntries, inheritedEntries, exprEntries)

  private def cnvShapeRef(sr: ShapeRef, id: NodeId, pm:PrefixMap): Converter[List[List[UMLEntry]]] = {
    for {
        rid <- newLabel(Some(sr.reference))
        _ <- updateUML(_.addLink(Inheritance(id,rid)))
    } yield {
        List()
    }
  }

  private def cnvExtends(ls: List[ShapeLabel],
                         id: NodeId
                        ): Converter[List[List[UMLEntry]]] = {
    log("cnvExtends (we don't do anything here as it is already handled at shape)", List())
  }

  private def cnvNodeConstraint(nc: NodeConstraint, pm: PrefixMap): Converter[List[List[ValueConstraint]]] = for {
    nks <- cnvNodeKind(nc.nodeKind)
    dt <- cnvDatatype(nc.datatype,pm)
    facets <- cnvFacets(nc.xsFacets,pm)
    values <- cnvValues(nc.values,pm)
  } yield mkLs(nks,dt,facets,values)

  private def cnvShapeOrInline(es: List[ShapeExpr], pm: PrefixMap): Converter[List[List[ValueConstraint]]] = {
    for {
      values <- cnvList(es, cnvFlat(pm))
    } yield List(List(ValueExpr("OR", values)))
  }

  private def cnvShapeAndInline(es: List[ShapeExpr], pm: PrefixMap): Converter[List[List[ValueConstraint]]] =
    for {
      values <- cnvList(es, cnvFlat(pm))
    } yield List(List(ValueExpr("AND", values)))

  private def cnvFacets(fs: List[XsFacet], pm:PrefixMap): Converter[List[ValueConstraint]] = {
    val zero: List[ValueConstraint] = List()
    def cmb(next: List[ValueConstraint], c: XsFacet): Converter[List[ValueConstraint]] = for {
      v <- cnvFacet(c,pm)
    } yield v :: next
    fs.foldM(zero)(cmb)
  }

  private def cnvFacet(facet: XsFacet, pm:PrefixMap): Converter[ValueConstraint] = facet match {
    case MinLength(v) => ok(Constant(s"MinLength($v)"))
    case MaxLength(v) => ok(Constant(s"MaxLength($v)"))
    case Length(v) => ok(Constant(s"Length($v)"))
    case Pattern(r,flags) => ok(Constant(s"/$r/${flags.getOrElse("")}"))
    case MinInclusive(n) => ok(Constant(s">= ${cnvNumericLiteral(n)}"))
    case MaxInclusive(n) => ok(Constant(s"<= ${cnvNumericLiteral(n)}"))
    case MinExclusive(n) => ok(Constant(s"> ${cnvNumericLiteral(n)}"))
    case MaxExclusive(n) => ok(Constant(s"< ${cnvNumericLiteral(n)}"))
    case TotalDigits(n) => ok(Constant(s"TotalDigits($n)"))
    case FractionDigits(n) => ok(Constant(s"FractionDigits($n)"))
  }

  private def cnvNumericLiteral(l: NumericLiteral): String = l match {
    case NumericInt(_,repr) => repr
    case NumericDouble(_,repr) => repr
    case NumericDecimal(_,repr) => repr
  }

  private def cnvValues(vs: Option[List[ValueSetValue]], pm:PrefixMap): Converter[List[ValueConstraint]] =
    vs match {
      case None => ok(List())
      case Some(vs) => for {
        values <- cnvList(vs, cnvValue(_: ValueSetValue, pm))
      } yield List(ValueSet(values))
    }

  private def cnvValue(v: ValueSetValue, pm: PrefixMap): Converter[Value] = v match {
    case IRIValue(iri) => ok(Value(iri2Label(iri,pm),Some(iri.str)))
    case StringValue(str) => ok(Value(str,None))
    case DatatypeString(s,iri) => ok(Value("\"" + s + "\"^^" + iri2Label(iri,pm), None))
    case LangString(s,lang) => ok(Value("\"" + s + "\"@" + lang.lang,None))
    case IRIStem(stem) => ok(Value(s"${iri2Label(stem,pm)}~",None))
    case IRIStemRange(stem,excs) => log(s"Not implemented IRIStemRange($stem,$excs) yet", Value(s"IRIStemRange($stem,$excs)",None))
    case LanguageStem(lang) => ok(Value(s"@${lang.lang}~",None))
    case LanguageStemRange(lang,excs) => log(s"Not implemented LanguageStemRange($lang,$excs) yet", Value(s"LanguageStemRange($lang,$excs)", None))
    case LiteralStem(stem) => ok(Value("\"" + "\"" + stem + "\"~",None))
    case LiteralStemRange(stem,excs) => log(s"Not implemented LiteralStemRange($stem,$excs) yet", Value(s"LanguageStemRange($stem,$excs)",None))
    case Language(lang) => ok(Value(s"@${lang.lang}",None))
  }

  private def cnvDatatype(dt: Option[IRI], pm:PrefixMap): Converter[List[ValueConstraint]] = dt match {
    case None => ok(List())
    case Some(iri) => ok(List(UML.datatype(iri2Label(iri,pm), iri.str)))
  }

  private def cnvNodeKind(nk: Option[NodeKind]): Converter[List[ValueConstraint]] = nk match {
    case None => ok(List())
    case Some(IRIKind) => ok(List(UML.iriKind))
    case Some(BNodeKind) => ok(List(UML.bnodeKind))
    case Some(LiteralKind) => ok(List(UML.literalKind))
    case Some(NonLiteralKind) => ok(List(UML.nonLiteralKind))
  }

  private def cnvTripleExpr(e: TripleExpr, id: NodeId, pm: PrefixMap): Converter[List[List[UMLEntry]]] = e match {
    case eo: EachOf => cnvEachOf(eo,id,pm)
    case oo: OneOf => log(s"Id:Not supported oneOf yet: $oo", List(List(Constant("OneOf not implemented"))))
    case i: Inclusion => log(s"Not supported inclusion $i yet", List())
    case e: Expr => log(s"Not supported Expr $e yet", List())
    case tc: TripleConstraint => cnvTripleConstraint(tc,id,pm)
  }

  private def cnvEachOf(eo: EachOf, id: NodeId, pm: PrefixMap): Converter[List[List[UMLEntry]]] = {
    val zero: List[List[UMLEntry]] = List()
    def cmb(next: List[List[UMLEntry]], te: TripleExpr): Converter[List[List[UMLEntry]]] = for {
      es <- cnvTripleExpr(te,id,pm)
    } yield es ++ next
    eo.expressions.reverse.foldM(zero)(cmb)
  }

  private def cnvTripleConstraint(tc: TripleConstraint,
                                  id: NodeId,
                                  pm: PrefixMap): Converter[List[List[UMLEntry]]] = {
    val card = cnvCard(tc.min, tc.max)
    val (label,href) = predicate2lbl(tc.predicate,pm)
    
    tc.valueExpr match {
      case None => 
        // err(s"No value expr for triple constraint $tc")
        ok(List(List(UMLField(label, Some(href), List(NoConstraint()), card))))

      case Some(se) => se match {
        case r: ShapeRef => for {
          rid <- newLabel(Some(r.reference))
          _ <- if (!tc.inverse) updateUML(_.addLink(Relationship(id, rid, label, href, card)))
          else updateUML(_.addLink(Relationship(rid, id, label, href, card)))
        } yield {
          List()
        }
        case nc: NodeConstraint => for {
          constraints <- cnvNodeConstraint(nc, pm)
        } yield {
          List(List(UMLField(label, Some(href), constraints.flatten, card)))
        }
        case _ if se === Shape.empty =>
          ok(List(List(UMLField(label, Some(href), List(UML.anyConstraint), card))))

        case s: Shape => for {
          sid <- newLabel(s.id)
          entries <- cnvShape(s, sid, pm)
          (labelShape,hrefShape) = mkLabel(s.id, pm)
          cls = UMLClass(sid,labelShape,hrefShape,entries, List())
          _ <- updateUML(_.addClass(cls))
          _ <- updateUML(_.addLink(Relationship(id,sid,label,href,card)))
        } yield
          List()
        case so: ShapeOr => for {
          vso <- cnvShapeOrInline(so.shapeExprs,pm)
        } yield List(List(UMLField(label, Some(href), vso.flatten, card)))
        case so: ShapeAnd => for {
          vso <- cnvShapeAndInline(so.shapeExprs,pm)
        } yield List(List(UMLField(label, Some(href), vso.flatten, card)))

        case _ => log(s"Complex shape $se in triple constraint with predicate ${label} not implemented yet", List())
      }
    }
  }

  private def cnvFlat(pm: PrefixMap)(se: ShapeExpr): Converter[ValueConstraint] = se match {
    case sr:ShapeRef => sr.reference.toRDFNode.toIRI match {
      case Left(e) => err(e)
      case Right(iri) => ok(DatatypeConstraint(iri2Label(iri,pm),iri.str))
    }
    case nc:NodeConstraint => for {
      vs <- cnvNodeConstraint(nc,pm)
      single <- vs match {
        case List(List(vc)) => ok(vc)
        case _ => log(s"cnvFlat: result of cnvNodeConstraint($nc) = $vs (too complex)",Constant(s"Complex nodeConstraint"))
      }
    } yield single
    case s: Shape => log(s"Complex shape: ${s.show}", Constant(s"Complex shape"))
    case _ => err(s"cnvFlat. Unexpected shape expr $se")
  }

  /*  private def allFlat(es: List[ShapeExpr]): Boolean =
      es.forall(isFlat(_)) */

  /*  private def isFlat(e: ShapeExpr): Boolean = e match {
      case ShapeRef(r) => true
      case nc: NodeConstraint => true
      case _ => false
    } */

  private def cnvCard(min: Int, max: Max): UMLCardinality = (min,max) match {
    case (0,es.weso.shex.Star) => UMLStar
    case (1,es.weso.shex.Star) => UMLPlus
    case (0, es.weso.shex.IntMax(1)) => UMLOptional
    case (1, es.weso.shex.IntMax(1)) => NoCard
    case (m, es.weso.shex.Star) => Range(m,Unbounded)
    case (m,es.weso.shex.IntMax(n)) => Range(m,UMLIntMax(n))
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