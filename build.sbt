lazy val scala212 = "2.12.13"
lazy val scala213 = "2.13.5"
lazy val supportedScalaVersions = List(
  scala213,
  scala212
)

val Java11 = "adopt@1.11"

lazy val shaclexVersion        = "0.1.91"
lazy val shaclsVersion         = "0.1.75"
lazy val shexsVersion          = "0.1.91"
lazy val srdfVersion           = "0.1.101"

// Dependency versions
lazy val munitVersion          = "0.7.23"
lazy val munitEffectVersion    = "1.0.1"

lazy val plantumlVersion       = "1.2017.12"
lazy val logbackVersion        = "1.2.3"
lazy val loggingVersion        = "3.9.2"
lazy val scallopVersion        = "3.3.1"


// Compiler plugin dependency versions
// lazy val simulacrumVersion    = "0.19.0"
// lazy val kindProjectorVersion = "0.9.5"
// lazy val scalaMacrosVersion   = "2.1.1"

// Dependency modules
// lazy val logbackClassic    = "ch.qos.logback"             % "logback-classic"      % logbackVersion
lazy val munit             = "org.scalameta"              %% "munit"               % munitVersion
lazy val munitEffect       = "org.typelevel"     %% "munit-cats-effect-3" % munitEffectVersion

lazy val plantuml          = "net.sourceforge.plantuml"   % "plantuml"             % plantumlVersion
lazy val scalaLogging      = "com.typesafe.scala-logging" %% "scala-logging"       % loggingVersion
lazy val scallop           = "org.rogach"                 %% "scallop"             % scallopVersion
// lazy val scalactic         = "org.scalactic"              %% "scalactic"           % scalacticVersion
// lazy val scalaTest         = "org.scalatest"              %% "scalatest"           % scalaTestVersion
lazy val shex              = "es.weso"                    %% "shex"                % shexsVersion
lazy val shacl             = "es.weso"                    %% "shacl"               % shaclsVersion
lazy val schema            = "es.weso"                    %% "schema"              % shaclexVersion
lazy val schemaInfer       = "es.weso"                    %% "schemainfer"         % shaclexVersion
lazy val sgraph            = "es.weso"                    %% "sgraph"              % shaclexVersion
lazy val srdfJena          = "es.weso"                    %% "srdfjena"            % srdfVersion
lazy val utilsTest         = "es.weso"                    %% "utilstest"           % shaclexVersion

lazy val MUnitFramework = new TestFramework("munit.Framework")


// Compiler plugin modules
// lazy val simulacrum          = "com.github.mpilquist" %% "simulacrum"     % simulacrumVersion

ThisBuild / githubWorkflowJavaVersions := Seq(Java11)
// ThisBuild / githubOwner := "weso"
// ThisBuild / githubRepository := "umlShaclex"

lazy val umlShaclex = project
  .in(file("."))
  .enablePlugins(ScalaUnidocPlugin,
     SiteScaladocPlugin,
     AsciidoctorPlugin,
     SbtNativePackager,
     WindowsPlugin,
     JavaAppPackaging
     )
//  .settings(
//    buildInfoKeys := BuildInfoKey.ofN(name, version, scalaVersion, sbtVersion),
//    buildInfoPackage := "es.weso.shaclex.buildinfo"
//  )
  .settings(commonSettings, publishSettings)
  .settings(
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(noDocProjects: _*),
    siteSubdirName in ScalaUnidoc := "scaladoc/latest",
    addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(noDocProjects: _*),
    mappings in makeSite ++= Seq(
      file("src/assets/favicon.ico") -> "favicon.ico"
    ),
    libraryDependencies ++= Seq(
//      logbackClassic,
      scalaLogging,
      scallop,
      shex,
      schema,
      schemaInfer,
      shacl,
      sgraph,
      plantuml,
      srdfJena
    ),
    testFrameworks += MUnitFramework,
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
  publishArtifact := false
)

lazy val sharedDependencies = Seq(
  libraryDependencies ++= Seq(
//    scalactic,
    munitEffect % Test
  )
)

lazy val compilationSettings = Seq(
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
   // Resolver.githubPackages("weso"),
   Resolver.sonatypeRepo("snapshots")
  )
)

lazy val publishSettings = Seq(
  sonatypeProfileName := ("es.weso"),
  homepage        := Some(url("https://github.com/labra/umlShaclex")),
  licenses        := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo         := Some(ScmInfo(url("https://github.com/labra/umlShaclex"), "scm:git:git@github.com:labra/umlShaclex.git")),
  autoAPIMappings := true,
  apiURL          := Some(url("http://labra.github.io/umlShaclex/latest/api/")),
  developers := List(
    Developer(
      id="labra",
      name="Jose Emilio Labra Gayo",
      email="jelabra@gmail.com",
      url=url("https://weso.labra.es")
    )),
  publishMavenStyle := true,
)
