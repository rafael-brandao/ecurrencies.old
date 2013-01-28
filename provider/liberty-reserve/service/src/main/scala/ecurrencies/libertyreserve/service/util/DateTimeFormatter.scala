package ecurrencies.libertyreserve.service.util

import org.joda.time.format.DateTimeFormat.forPattern
import org.joda.time.DateTimeZone.UTC

trait DateTimeFormatter {

  private val dateTimeFormatter = forPattern( "yyyy-MM-dd HH:mm:ss" ).withZone( UTC )

  def parseMillis( dateTime: String ) = dateTimeFormatter.parseMillis( dateTime )

  def printDate( dateTime: Long ) = dateTimeFormatter.print( dateTime )

}