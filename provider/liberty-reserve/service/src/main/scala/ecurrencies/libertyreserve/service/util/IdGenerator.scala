/**
 * Classes that implement this interface must return a string that unique
 * identifies a request to the Liberty Reserve API server. This string <b>must
 * be up to 20 characters long</b>.
 * <p>
 * This shouldn't be a concern because each api request is scoped to only one
 * account, so it's unlikely that the same account sends two requests at the
 * very same time.
 * <p>
 *
 * @author Rafael de Andrade
 *
 */

package ecurrencies.libertyreserve.service.util

import java.text.DecimalFormat

/**
 * Default Implementation that receives a NumberFormat object to create a String
 * that contains the current UTC time in milliseconds
 *
 * @author Rafael de Andrade
 */
trait IdGenerator {

  private lazy val numberFormat = new DecimalFormat( "00000000000000000000" )

  def nextId() = numberFormat.format( System.currentTimeMillis )

}

