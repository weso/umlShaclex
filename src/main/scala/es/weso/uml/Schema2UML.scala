package es.weso.uml
import com.typesafe.scalalogging.LazyLogging
import es.weso.schema.{Schema, ShExSchema, ShaclexSchema}

object Schema2UML extends LazyLogging {
  def schema2UML(schema: Schema): Either[String,UML] = {
    schema match {
      case shexSchema: ShExSchema => ShEx2UML.schema2Uml(shexSchema.schema)
      case shaclSchema: ShaclexSchema  => SHACL2UML.schema2Uml(shaclSchema.schema)
      case _ => Left(s"Unsupported conversion of ${schema.name} to UML")
    }
  }
}