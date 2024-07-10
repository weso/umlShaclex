package es.weso.uml
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

import es.weso.shex.{BNodeLabel, IRILabel, ShapeLabel, Start}
import UMLDiagram._
import io.circe.Json
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import cats.effect.IO
import net.sourceforge.plantuml.core.DiagramDescription

/** Represents a UML-like class diagram that can be serialized to PlantUML
  * syntax
  * @param labels
  *   associates ShapeLabels to NodeIds
  * @param components
  *   associates NodeIds to UMLComponents
  * @param links
  *   list of links
  */
case class UML(
    labels: Map[ShapeLabel, NodeId],
    components: Map[NodeId, UMLComponent],
    links: List[UMLLink]
) {

  /** Adds a label to a UML diagram If exists, return the existing nodeId
    * @param label
    *   Shape label
    * @return
    *   a pair with the updated UML diagram and the nodeId
    */
  def newLabel(label: ShapeLabel): (UML, NodeId) = {
    labels
      .get(label)
      .fold {
        val id = this.labels.size
        (this.copy(labels = this.labels.updated(label, id)), id)
      } { id =>
        (this, id)
      }
  }

  /** Get nodeId of a shape label
    * @param label
    *   shape label
    * @return
    */
  def getId(label: ShapeLabel): Option[NodeId] = labels.get(label)

  def addClass(cls: UMLClass): UML = {
    this.copy(components = components.updated(cls.id, cls))
  }

  def addLink(link: UMLLink): UML =
    this.copy(links = link :: links)

  private def strHref(href: Option[HRef], lbl: Name): String = href match {
    case None       => ""
    case Some(href) => s"[[${href} ${lbl}]]"
  }

  def cnvValueConstraint(vc: ValueConstraint): String = vc match {
    case Constant(c) => c
    case ValueSet(vs) => {
      "[ " + vs
        .map { v =>
          v.href match {
            case None       => v.name
            case Some(href) => s"[[${href} ${v.name}]]"
          }
        }
        .mkString(" ") + " ]"
    }
    case DatatypeConstraint(name, href) => {
      s"[[${href} ${name}]] "
    }
    case RefConstraint(name, href) => {
      s"[[${href} @${name}]] "
    }
    case ValueExpr(op, vs) => vs.map(cnvValueConstraint(_)).mkString(s" ${op} ")
    case NoConstraint()    => s"."
  }

  def cnvFieldExpr(fe: FieldExpr): String = {
    fe.es.map(cnvEntry(_)).mkString(fe.operator)
  }

  def cnvEntry(entry: UMLEntry): String = entry match {
    case field: UMLField     => cnvField(field)
    case vc: ValueConstraint => cnvValueConstraint(vc)
    case fe: FieldExpr       => cnvFieldExpr(fe)
  }

  def cnvField(field: UMLField): String =
    s"${strHref(field.href, field.name)} : ${field.valueConstraints
        .map(cnvValueConstraint(_))
        .mkString(" ")} ${field.card}"

  def cnvComponent(c: UMLComponent): String = c match {
    case cls: UMLClass => cnvClass(cls)
    case _ => s"Error not yet implemented cnvComponent for non classes"
  }

  def cnvClass(cls: UMLClass): String = {
    val sb = new StringBuilder
    sb.append(s"""class "${cls.label}" as ${cls.id} <<(S,#FF7700)>> ${strHref(
        cls.href,
        cls.label
      )} {\n""")
    cls.entries.foreach { entryLs =>
      entryLs.foreach { entry =>
        sb.append(cnvEntry(entry))
        sb.append("\n")
      }
      sb.append("--\n")
    }
    sb.append("}\n")
    sb.toString
  }

  def cnvExtends(c: UMLComponent): String = c match {
    case cls: UMLClass => {
      val sb = new StringBuilder
      cls._extends.foreach(s => {
        sb.append(s"""$s <|--${cls.id}\n""")
      })
      sb.toString
    }
    case _ => ""
  }

  def cnvLink(link: UMLLink): String = link match {
    case r: Relationship =>
      s"""${r.source} --> "${r.card}" ${r.target} : [[${r.href} ${r.label}]]\n"""
    case r: Inheritance => s"""${r.source} --|> ${r.target} \n"""
  }

  def toPlantUML(options: PlantUMLOptions): String = {
    val sb = new StringBuilder
    sb.append("@startuml\n")
    components.values.foreach { c =>
      sb.append(cnvComponent(c))
    }
    links.foreach { link => sb.append(cnvLink(link)) }
    components.values.foreach { cls =>
      sb.append(cnvExtends(cls))
    }
    options.watermark match {
      case None => ()
      case Some(watermarkFooter) =>
        sb.append(s"\nright footer $watermarkFooter\n")
    }
    sb.append("@enduml\n")
    sb.toString
  }

  /** Convert a UML diagram to some format
    * @param options
    *   plantUML options
    * @param format
    *   output format
    * @return
    */
  def toFormat(
      options: PlantUMLOptions,
      format: net.sourceforge.plantuml.FileFormat
  ): IO[String] = try {
    val reader: SourceStringReader = new SourceStringReader(
      this.toPlantUML(options)
    )
    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
    val d: DiagramDescription =
      reader.outputImage(os, new FileFormatOption(format))
    // System.out.println(s"Generated diagram description: ${d.getDescription}")
    os.close
    val outStr: String = new String(os.toByteArray(), Charset.forName("UTF-8"))
    IO.pure(outStr)
  } catch {
    case e: Exception =>
      IO.raiseError(
        new RuntimeException(
          s"Exception converting to format: ${format}: ${e.getMessage}"
        )
      )
  }

  /** Converts a diagram to SVG
    * @param options
    *   PlantUML options
    * @return
    *   an IO action that generates the SVG representation of the UML diagram
    */
  def toSVG(options: PlantUMLOptions): IO[String] =
    this.toFormat(options, FileFormat.SVG)

  private def label2Json(label: ShapeLabel): Json = label match {
    case IRILabel(iri)  => Json.fromString(iri.toString)
    case BNodeLabel(bn) => Json.fromString(bn.id)
    case Start          => Json.fromString("START")

  }
  private def node2Json(node: NodeId): Json = Json.fromInt(node)

  private def labelPair2Json(pair: (ShapeLabel, NodeId)): Json = {
    val (label, node) = pair
    Json.fromFields(
      List(
        ("shapeLabel", label2Json(label)),
        ("node", node2Json(node))
      )
    )
  }

  private def componentPair2Json(pair: (NodeId, UMLComponent)): Json = {
    val (node, component) = pair
    Json.fromFields(
      List(
        ("node", node2Json(node)),
        ("component", component.toJson)
      )
    )
  }

  /** Convert the UML diagram to JSON
    * @return
    *   JSON representation of the diagram
    */
  def toJson: Json = Json.fromFields(
    List(
      ("labels", Json.fromValues(labels.toList.map(labelPair2Json(_)))),
      (
        "components",
        Json.fromValues(components.toList.map(componentPair2Json(_)))
      ),
      ("links", Json.fromValues(links.map(_.toJson)))
    )
  )

}

object UML {
  def empty: UML = UML(Map(), Map(), List())
  lazy val external = Constant("External")
  lazy val iriKind = Constant("IRI")
  lazy val bnodeKind = Constant("BNode")
  lazy val nonLiteralKind = Constant("NonLiteral")
  lazy val literalKind = Constant("Literal")
  lazy val umlClosed = Constant("Closed")
  lazy val anyConstraint = Constant(".")
  def datatype(label: String, href: String) = DatatypeConstraint(label, href)

}
