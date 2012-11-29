package ecurrencies.libertyreserve.util

/**
 * Classes that implement this interface must return a string that unique
 * identifies a request to the Liberty Reserve API server. This string <b>must
 * be up to 20 characters long</b>.
 * <p>
 * This shoudn't be a concern because each api request is scoped to only one
 * account, so it's unlikely that the same account sends two requests at the
 * very same time.
 * <p>
 *
 * @author Rafael de Andrade
 * @since 1.0.0
 *
 */
interface IdGenerator {

    /**
     * @return Line of text, up to 20 characters long (varchar(20))
     */
    String createId()
}
