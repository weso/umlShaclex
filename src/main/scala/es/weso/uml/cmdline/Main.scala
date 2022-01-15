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
      either <- schema2Uml(opts, baseFolder, opts.verbose()).attempt
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
        _ <- opts.outputFile.toOption match {
          case Some(outputFile) => {
           val outPath = baseFolder.resolve(opts.outputFile())
           val outName = outPath.toFile.getAbsolutePath
           for {
             _ <- FileUtils.writeFile(outName, outputStr) 
             _ <- IO.println(s"Output written to file: $outName")
           } yield ()
          }
          case None => IO.println(outputStr)
        }
         _ <- if (!warnings.isEmpty) IO.println(s"Warnings: ${warnings.mkString("\n")} ")
              else IO(()) 
       } yield ExitCode.Success
      })
    } yield (exitCode)
  }

  private def schema2Uml(
    opts: MainOpts, 
    baseFolder: Path,
    verbose: Boolean
    ): IO[(UML,List[String])] = 
   RDFAsJenaModel.empty.flatMap(_.use(empty => for {
    schema <- getSchema(opts.schema.toOption, opts.schemaUrl.toOption, opts.schemaFormat(), opts.engine(), baseFolder, empty, verbose)
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

  def getSchema(
    // opts: MainOpts, 
    optSchema: Option[String],
    optSchemaUrl: Option[String],
    schemaFormat: String,
    engine: String,
    baseFolder: Path, 
    rdf: RDFReader,
    verbose: Boolean
    ): IO[Schema] = {
    val base = Some(FileUtils.currentFolderURL)
    (optSchema, optSchemaUrl) match {
      case (Some(schema), _) => {
       val path = baseFolder.resolve(optSchema.get)
       info(s"Schema path $path, schemaFormat: $schemaFormat, engine: $engine", verbose)
       Schemas.fromFile(path.toFile(), schemaFormat, engine, base)
      }
      case (_,Some(schemaUrl)) => {
       val str = Source.fromURL(optSchemaUrl.get).mkString
       info(s"Schema url $schemaUrl, schemaFormat: $schemaFormat, engine: $engine", verbose)
       info(s"First 10 lines: " ++ showNLines(str,10), verbose)
       Schemas.fromString(str,schemaFormat,engine,base)
      }
      case (None, None) => {
       info("Schema not specified. Extracting schema from data", verbose)
       Schemas.fromRDF(rdf, engine)
      }
    }
  }

  private def showNLines(str: String, n: Int): String = {
    val lines = str.split("\n")
    str.take(n).mkString("\n") ++ 
     (if (lines.length > n) "\n..." else "")
  }

  private def info(msg: String, verbose: Boolean): Unit = {
   if (verbose) {
    logger.info(msg)
   } else ()
  }

}