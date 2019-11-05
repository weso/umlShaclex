package es.weso.uml.cmdline

import org.rogach.scallop._
import org.rogach.scallop.exceptions._
import com.typesafe.scalalogging._
import es.weso.uml._
import scala.io.Source
import es.weso.schema._
import es.weso.rdf.jena.RDFAsJenaModel
import es.weso.utils.FileUtils
import scala.util._
import java.nio.file._
import es.weso.rdf.RDFReader

object Main extends App with LazyLogging {

  val defaultOutFormat = "uml"

  try {
    run(args)
  } catch {
    case (e: Exception) => {
      println(s"Error: ${e.getMessage}")
    }
  }

  def run(args: Array[String]): Unit = {
    val opts = new MainOpts(args, errorDriver)
    opts.verify()

    val baseFolder: Path = if (opts.baseFolder.isDefined) {
      Paths.get(opts.baseFolder())
    } else {
      Paths.get(".")
    }

    val eitherResult = for {
      schema <- getSchema(opts, baseFolder, RDFAsJenaModel.empty)
      uml <- Schema2UML.schema2UML(schema)
    } yield uml

    val outFormat = opts.outFormat.getOrElse(defaultOutFormat)

    eitherResult match {
      case Left(e) => {
        println(s"Error: $e")
      }
      case Right(uml) => {
        val outputStr = outFormat match {
          case "uml" => uml.toPlantUML
          case "svg" => uml.toSVG
          case _ => uml.toPlantUML
        }
        if (opts.outputFile.isDefined) {
          val outPath = baseFolder.resolve(opts.outputFile())
          val outName = outPath.toFile.getAbsolutePath
          FileUtils.writeFile(outName, outputStr)
          println(s"Output written to file: $outName")
        } else {
          println(outputStr)
        }
      }
    }
  }

  private def errorDriver(e: Throwable, scallop: Scallop) = e match {
    case Help(s) => {
      println("Help: " + s)
      scallop.printHelp
      sys.exit(0)
    }
    case _ => {
      println("Error: %s".format(e.getMessage))
      scallop.printHelp
      sys.exit(1)
    }
  }

  def getSchema(opts: MainOpts, baseFolder: Path, rdf: RDFReader): Either[String, Schema] = {
    val base = Some(FileUtils.currentFolderURL)
    if (opts.schema.isDefined) {
      val path = baseFolder.resolve(opts.schema())
      Schemas.fromFile(path.toFile(), opts.schemaFormat(), opts.engine(), base)
    } else if (opts.schemaUrl.isDefined) {
      val str = Source.fromURL(opts.schemaUrl()).mkString
      Schemas.fromString(str,opts.schemaFormat(),opts.engine(),base)
    }
    else {
      logger.info("Schema not specified. Extracting schema from data")
      Schemas.fromRDF(rdf, opts.engine())
    }

  }
}

