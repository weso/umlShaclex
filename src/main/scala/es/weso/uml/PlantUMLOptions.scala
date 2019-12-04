package es.weso.uml
// import net.sourceforge.plantuml.graph2.Plan

case class PlantUMLOptions(
    watermark: Option[String]
)
object PlantUMLOptions {
    def empty: PlantUMLOptions = PlantUMLOptions(watermark = None)
}