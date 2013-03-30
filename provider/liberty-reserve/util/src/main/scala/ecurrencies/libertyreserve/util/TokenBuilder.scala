package ecurrencies.libertyreserve.util

import java.nio.charset.Charset
import java.security.MessageDigest

import org.joda.time.format.DateTimeFormat

private[ util ] object TokenBuilder {

  private lazy val UTF8 = Charset.forName( "UTF-8" )
  private lazy val `:` = ":"
  private lazy val tokenDigester = MessageDigest.getInstance( "SHA-256" )
  private lazy val tokenDateFormatter = DateTimeFormat.forPattern( "yyyyMMdd:HH" ).withZone( ServerTimeZone )

  def build( securityWord: Array[ Char ], id: String, seq: Seq[ String ], date: Long ) = {

    val token =
      seq.isEmpty match {
        case true  => new String( securityWord ) + `:` + id + `:` + ( tokenDateFormatter print date )
        case false => new String( securityWord ) + `:` + id + `:` + seq.mkString( `:` ) + `:` + ( tokenDateFormatter print date )
      }

    val tokenBytes = token.getBytes( UTF8 )

    try
      Hex valueOf ( tokenDigester digest tokenBytes )
    finally
      Arrays.wipe( tokenBytes )
  }

  private object Arrays {

    def wipe( array: Array[ Byte ] ): Unit = wipe( array, 0x00.toByte )

    def wipe( array: Array[ Char ] ): Unit = wipe( array, ' ' )

    def wipe[ T ]( array: Array[ T ], zero: T ) =
      for ( i <- 0 until array.length )
        array( i ) = zero
  }

  private object Hex {
    private lazy val table = Array(
      '0'.toByte, '1'.toByte, '2'.toByte, '3'.toByte,
      '4'.toByte, '5'.toByte, '6'.toByte, '7'.toByte,
      '8'.toByte, '9'.toByte, 'A'.toByte, 'B'.toByte,
      'C'.toByte, 'D'.toByte, 'E'.toByte, 'F'.toByte
    )

    def valueOf( bytes: Array[ Byte ] ) = {
      val hex: Array[ Byte ] = new Array( 2 * bytes.length )

      for ( i <- 0 until bytes.length ) {
        hex( 2 * i ) = table( ( bytes( i ) & 0xF0 ) >>> 4 )
        hex( 2 * i + 1 ) = table( bytes( i ) & 0x0F )
      }
      new String( hex, UTF8 )
    }
  }
}