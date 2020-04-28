lazy val scala212 = "2.12.11"
lazy val scala213 = "2.13.1"
lazy val supportedScalaVersions = List(scala213, scala212)

/*
scalafmt: {
  style = defaultWithAlign
  maxColumn = 150
  align.tokens = [
    { code = "=>", owner = "Case" }
    { code = "⇒", owner = "Case" }
    { code = "extends", owner = "Defn.(Class|Trait|Object)" }
    { code = "//", owner = ".*" }
    { code = "{", owner = "Template" }
    { code = "}", owner = "Template" }
    { code = ":=", owner = "Term.ApplyInfix" }
    { code = "++=", owner = "Term.ApplyInfix" }
    { code = "+=", owner = "Term.ApplyInfix" }
    { code = "%", owner = "Term.ApplyInfix" }
    { code = "%%", owner = "Term.ApplyInfix" }
    { code = "%%%", owner = "Term.ApplyInfix" }
    { code = "->", owner = "Term.ApplyInfix" }
    { code = "→", owner = "Term.ApplyInfix" }
    { code = "<-", owner = "Enumerator.Generator" }
    { code = "←", owner = "Enumerator.Generator" }
    { code = "=", owner = "(Enumerator.Val|Defn.(Va(l|r)|Def|Type))" }
  ]
}
 */

lazy val shaclexVersion        = "0.1.57"
lazy val shaclsVersion         = "0.1.59"
lazy val shexsVersion          = "0.1.60"
lazy val srdfVersion           = "0.1.66"

// Dependency versions
lazy val catsVersion           = "2.1.1"
lazy val scalacticVersion      = "3.1.1"
lazy val scalaTestVersion      = "3.1.1"
lazy val plantumlVersion       = "1.2017.12"
lazy val logbackVersion        = "1.2.3"
lazy val loggingVersion        = "3.9.2"
lazy val scallopVersion        = "3.3.1"


// Compiler plugin dependency versions
lazy val simulacrumVersion    = "0.19.0"
// lazy val kindProjectorVersion = "0.9.5"
lazy val scalaMacrosVersion   = "2.1.1"

// Dependency modules
lazy val catsCore          = "org.typelevel"              %% "cats-core"           % catsVersion
lazy val catsKernel        = "org.typelevel"              %% "cats-kernel"         % catsVersion
lazy val catsMacros        = "org.typelevel"              %% "cats-macros"         % catsVersion
lazy val catsEffect        = "org.typelevel"              %% "cats-effect"         % catsVersion
lazy val logbackClassic    = "ch.qos.logback"             % "logback-classic"      % logbackVersion
lazy val plantuml          = "net.sourceforge.plantuml"   % "plantuml"             % plantumlVersion
lazy val scalaLogging      = "com.typesafe.scala-logging" %% "scala-logging"       % loggingVersion
lazy val scallop           = "org.rogach"                 %% "scallop"             % scallopVersion
lazy val scalactic         = "org.scalactic"              %% "scalactic"           % scalacticVersion
lazy val scalaTest         = "org.scalatest"              %% "scalatest"           % scalaTestVersion
lazy val shex              = "es.weso"                    %% "shex"                % shexsVersion
lazy val shacl             = "es.weso"                    %% "shacl"               % shaclsVersion
lazy val schema            = "es.weso"                    %% "schema"              % shaclexVersion
lazy val schemaInfer       = "es.weso"                    %% "schemainfer"         % shaclexVersion
lazy val sgraph            = "es.weso"                    %% "sgraph"              % shaclexVersion
lazy val srdfJena          = "es.weso"                    %% "srdfjena"            % srdfVersion
lazy val utilsTest         = "es.weso"                    %% "utilstest"           % shaclexVersion


// Compiler plugin modules
// lazy val scalaMacrosParadise = "org.scalamacros"      % "paradise"        % scalaMacrosVersion cross CrossVersion.full
lazy val simulacrum          = "com.github.mpilquist" %% "simulacrum"     % simulacrumVersion
// lazy val kindProjector       = "org.spire-math"       %% "kind-projector" % kindProjectorVersion

lazy val umlShaclex = project
  .in(file("."))
  .enablePlugins(ScalaUnidocPlugin, SbtNativePackager, WindowsPlugin, JavaAppPackaging)
//  .settings(
//    buildInfoKeys := BuildInfoKey.ofN(name, version, scalaVersion, sbtVersion),
//    buildInfoPackage := "es.weso.shaclex.buildinfo" 
//  )
  .settings(commonSettings, publishSettings)
  .settings(
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(noDocProjects: _*),
    libraryDependencies ++= Seq(
      logbackClassic,
      scalaLogging,
      scallop,
      shex,
      schema, 
      schemaInfer,
      shacl,
      sgraph,
      plantuml,
      scalaTest % Test,
      srdfJena % Test
    ),
    cancelable in Global      := true,
    fork                      := true,
    crossScalaVersions := supportedScalaVersions,
    //    crossScalaVersions := Nil,
    // publish / skip := true
  )

/* ********************************************************
 ******************** Grouped Settings ********************
 **********************************************************/

lazy val noDocProjects = Seq[ProjectReference]()

lazy val noPublishSettings = Seq(
//  publish := (),
//  publishLocal := (),
  publishArtifact := false
)

lazy val sharedDependencies = Seq(
  libraryDependencies ++= Seq(
    scalactic,
    scalaTest % Test
  )
)

lazy val compilationSettings = Seq(
  scalaVersion := "2.13.0",
  // format: off
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= Seq(
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-explaintypes",                     // Explain type errors in more detail.
    "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.  "-encoding", "UTF-8",
    "-language:_",
    "-target:jvm-1.8",
    "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
    "-Xlint",
    "-Yrangepos",
//    "-Ylog-classpath",
//    "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
    "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
//    "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
//    "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
//    "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
//    "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
//    "-Ywarn-numeric-widen",              // Warn when numerics are widened.
//    "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
//    "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
//    "-Ywarn-unused:locals",              // Warn if a local definition is unused.
//    "-Ywarn-unused:params",              // Warn if a value parameter is unused.
//    "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
//    "-Ywarn-unused:privates",            // Warn if a private member is unused.
//    "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
//    "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
//    "-Ypartial-unification",
  )
  // format: on
)

lazy val commonSettings = compilationSettings ++ sharedDependencies ++ Seq(
  organization := "es.weso",
  resolvers ++= Seq(
    Resolver.bintrayRepo("labra", "maven"),
    Resolver.bintrayRepo("weso", "weso-releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val publishSettings = Seq(
  maintainer      := "Jose Emilio Labra Gayo <labra@uniovi.es>",
  homepage        := Some(url("https://github.com/labra/umlShaclex")),
  licenses        := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo         := Some(ScmInfo(url("https://github.com/labra/umlShaclex"), "scm:git:git@github.com:labra/umlShaclex.git")),
  autoAPIMappings := true,
  apiURL          := Some(url("http://labra.github.io/umlShaclex/latest/api/")),
  pomExtra        := <developers>
                       <developer>
                         <id>labra</id>
                         <name>Jose Emilio Labra Gayo</name>
                         <url>https://github.com/labra/</url>
                       </developer>
                     </developers>,
  scalacOptions in doc ++= Seq(
    "-diagrams-debug",
    "-doc-source-url",
    scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
    "-sourcepath",
    baseDirectory.in(LocalRootProject).value.getAbsolutePath,
    "-diagrams",
  ),
  publishMavenStyle              := true,
  bintrayRepository in bintray   := "weso-releases",
  bintrayOrganization in bintray := Some("weso")
)
