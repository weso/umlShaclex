package es.weso.uml

case class ConversionOptions(separateOrs: Boolean)

object ConversionOptions {
  def default: conversionOptions = ConversionOptions(
    separateOrs = false
  )
}