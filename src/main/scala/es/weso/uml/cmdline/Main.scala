package es.weso.uml.cmdline

import org.rogach.scallop._
import org.rogach.scallop.exceptions._
import com.typesafe.scalalogging._
import es.weso.rdf.jena.Endpoint
import es.weso.uml.{Schema2UML, ShEx2UML}

import scala.io.Source
// import es.weso.server._
import es.weso.schema._
import es.weso.rdf.jena.RDFAsJenaModel
import scala.concurrent.duration._
import es.weso.utils.FileUtils
import scala.util._
import java.nio.file._
import es.weso.rdf.RDFReader

object Main extends App with LazyLogging {
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

    val startTime = System.nanoTime()

    val base = Some(FileUtils.currentFolderURL)

    val eitherResult = for {
      schema <- getSchema(opts, baseFolder, RDFAsJenaModel.empty)
      uml <- Schema2UML.schema2UML(schema)
    } yield uml

    eitherResult match {
      case Left(e) => {
        println(s"Error: $e")
      }
      case Right(uml) => {
        val outputStr = uml.toPlantUML
        if (opts.outputFile.isDefined) {
          FileUtils.writeFile(opts.outputFile(), outputStr)
        } else {
          println("No outputFile?")
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

