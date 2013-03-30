package ecurrencies.libertyreserve

import scala.language.implicitConversions

import com.google.protobuf.GeneratedMessage.Builder

package object util {

  lazy val IdFormatter = new java.text.DecimalFormat( "00000000000000000000" )

  lazy val ServerTimeZone = org.joda.time.DateTimeZone.UTC

  lazy val DateTimeFormatter = org.joda.time.format.DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss" ).withZone( ServerTimeZone )

  private[ util ] def now() = System.currentTimeMillis

  def generateId: String = generateId( now )

  def generateId[ L <% Long ]( date: L ) = IdFormatter format date

}

package util {
  // Writer Functor
  case class Writer[ A <: Builder[ A ] ]( val builder: A ) {
    def map[ B <: Builder[ B ] ]( f: A => B ): Writer[ B ] = new Writer( f( builder ) )
  }
}