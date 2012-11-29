package ecurrencies.libertyreserve.util

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
 *
 * @author Rafael de Andrade
 * @since 1.0.0
 */
interface TokenGenerator {

    String createToken(char[] apiSecurityWord)

    String createToken(char[] apiSecurityWord, long timestamp)
}