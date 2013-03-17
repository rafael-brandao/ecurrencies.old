package ecurrencies.libertyreserve.service.util

import org.joda.time.format.DateTimeFormat.forPattern
import org.joda.time.DateTimeZone.UTC
import java.security.MessageDigest.{ getInstance => messageDigest }

/**
 * This contract enforces the creation of a token object, based on the
 * {@link LibertyReserveApi apiSecurityWord} parameter.
 * <p />
 * According to the <a
 * href="https://www.libertyreserve.com/en/help/apiguide#authentication"
 * >LibertyReserve API Reference</a>
 * <p />
 * <h4>Creation of authentication token</h4>
 * <p />
 * You need to create authentication token with the help of the following
 * procedure:
 * <ul>
 * <li>Obtain by concatenation the following line
 *
 * <pre>
 * Security Word:Date UTC in YYYYMMDD format:Time UTC in HH format.
 * </pre>
 *
 * </li>
 * <li>Get hash of above line by SHA256. For example: secret word - MySecWord,
 * date UTC - 01.02.2007 14:55 (24h format)
 *
 * <pre>
 * Concatenation of parameters: MySecWord:20070201:14
 * Hash SHA256 for above created line: 9A0EFBDCE4F4126C9F1EDD38AA39F3817B9C479C4A1F80B7409597F5403CA860
 * </pre>
 *
 * </li>
 * </ul>
 *
 * It is important to zero the apiSecurityWord char array, after all it is
 * sensitive data that should stay in memory for the shortest possible time
 *
 * @author Rafael de Andrade
 */
trait TokenGenerator {

  import TokenGenerator._

  def createToken( securityWord: Array[ Char ], instant: Long ): String = {

    if ( securityWord != null ) {

      var token: Array[ Byte ] = null

      val securityWordBytes: Array[ Byte ] =
        for (
          char <- securityWord
        ) yield char.asInstanceOf[ Byte ]

      try {
        val suffix =
          for (
            char <- ( ':' +: timeFormatter.print( instant ).toCharArray )
          ) yield char.asInstanceOf[ Byte ]

        token = securityWordBytes ++ suffix

        Hex.valueOf( SHA_256 digest token )

      } finally {
        dispose( securityWord, securityWordBytes, token )
      }
    } else
      throw new IllegalArgumentException( "securityWord cannot be null" )

  }

  def createToken( securityWord: Array[ Char ] ): String = {
    createToken( securityWord, System.currentTimeMillis )
  }

}

object TokenGenerator {

  private lazy val timeFormatter = forPattern( "yyyyMMdd:HH" ).withZone( UTC )
  private lazy val SHA_256 = messageDigest( "SHA-256" )

  private def dispose( arrays: Array[ _ <: AnyVal ]* ) {
    arrays foreach { dispose( _ ) }
  }

  private def dispose[ T <: AnyVal ]( array: Array[ T ] ) {
    if ( array != null ) {
      def fill[ T <: AnyVal ]( array: Array[ T ], zero: T ) {
        for ( i <- 0 until array.length ) {
          array( i ) = zero
        }
      }
      array match {
        case a: Array[ Byte ] => fill( array, 0x00.asInstanceOf[ Byte ] )
        case a: Array[ Char ] => fill( array, '0' )
        case _                =>
      }

    }
  }

  private object Hex {
    private lazy val chars = Array( '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'A', 'B', 'C', 'D', 'E', 'F' )

    def valueOf( bytes: Array[ Byte ] ) = {
      val sb = new StringBuilder( bytes.length * 2 )

      for ( byte <- bytes ) {
        // look up high nibble char
        sb append chars( ( byte & 0xf0 ) >>> 4 )
        // look up low nibble char
        sb append chars( byte & 0x0f )
      }
      sb.toString()
    }
  }
}
