package es.weso.uml.cmdline

import org.rogach.scallop._
import org.rogach.scallop.exceptions._
import com.typesafe.scalalogging._
import es.weso.uml._
import scala.io.Source
import es.weso.schema._
import es.weso.rdf.jena.RDFAsJenaModel
import es.weso.utils.FileUtils
//import cats.data._ 
import cats.implicits._
import cats.effect._
import java.nio.file._
import es.weso.rdf.RDFReader
// import es.weso.utils.IOUtils._

object Main extends IOApp with LazyLogging {

  val defaultOutFormat = "uml"

  def run(args: List[String]): IO[ExitCode] = {
    val opts = new MainOpts(args, errorDriver)
    opts.verify()

    val baseFolder: Path = if (opts.baseFolder.isDefined) {
      Paths.get(opts.baseFolder())
    } else {
      Paths.get(".")
    }
    if (args.length==0) for {
      _ <- IO { opts.printHelp() }
    } yield ExitCode.Error
    else for {
     /* exitCode <- schema2Uml(opts, baseFolder).fold(s => IO{
        println(s"Error: $s")
        ExitCode.Error
      }, (pair) => {
       val (uml,warnings) = pair
       IO(ExitCode.Success)
      }) */
      either <- schema2Uml(opts,baseFolder).attempt
      exitCode <- either.fold(s => IO{
        println(s"Error: $s")
        ExitCode.Error
      }, pair => {
       val (uml,warnings) = pair 
       val outFormat = opts.outFormat.getOrElse(defaultOutFormat)
       val popts = PlantUMLOptions(watermark = opts.watermark.toOption)
       for {
        outputStr <- outFormat.toLowerCase match {
          case "uml" => IO(uml.toPlantUML(popts))
          case s => opts.outputFormatsMap.get(s) match {
            case None => IO.raiseError(new RuntimeException(s"Unsupported fileformat: $s"))
            case Some(fileFormat) => uml.toFormat(popts, fileFormat)
          }
        }
        _ <- if (opts.outputFile.isDefined) {
          val outPath = baseFolder.resolve(opts.outputFile())
          val outName = outPath.toFile.getAbsolutePath
          IO {FileUtils.writeFile(outName, outputStr)
              println(s"Output written to file: $outName")
            }
         } else {
          IO (println(outputStr))
         }
         _ <- if (!warnings.isEmpty) IO(println(s"Warnings: ${warnings.mkString("\n")} "))
         else IO(()) 
       } yield ExitCode.Success
      })
    } yield (exitCode)
  }

  private def schema2Uml(opts: MainOpts, baseFolder: Path): IO[(UML,List[String])] = 
   RDFAsJenaModel.empty.flatMap(_.use(empty => for {
    schema <- getSchema(opts, baseFolder, empty)
    uml <- IO.fromEither(Schema2UML.schema2UML(schema).leftMap(s => new RuntimeException(s"s")))
  } yield uml))


  private def errorDriver(e: Throwable, scallop: Scallop) = e match {
    case Help(s) => {
      println("Help: " + s)
      scallop.printHelp()
      sys.exit(0)
    }
    case _ => {
      println("Error: %s".format(e.getMessage))
      scallop.printHelp()
      sys.exit(1)
    }
  }

  def getSchema(opts: MainOpts, baseFolder: Path, rdf: RDFReader): IO[Schema] = {
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

