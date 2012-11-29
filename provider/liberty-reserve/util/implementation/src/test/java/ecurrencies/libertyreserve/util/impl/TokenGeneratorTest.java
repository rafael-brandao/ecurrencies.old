package ecurrencies.libertyreserve.util.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import ecurrencies.libertyreserve.util.TokenGenerator;

@ContextConfiguration(classes = UtilConfiguration.class)
public class TokenGeneratorTest extends AbstractTestNGSpringContextTests {

    private static final String EXPECTED_TOKEN_HASH = "9A0EFBDCE4F4126C9F1EDD38AA39F3817B9C479C4A1F80B7409597F5403CA860";

    @Autowired
    private TokenGenerator tokenGenerator1;

    @Autowired
    private TokenGenerator tokenGenerator2;

    @Autowired
    private UtilConfiguration configuration;

    @Test
    public void assertNotNull() {
        assertThat(tokenGenerator1, is(notNullValue()));
    }

    @Test
    public void ensureSingletonScope() {
        assertThat(tokenGenerator1 == tokenGenerator2, is(true));
        assertThat(tokenGenerator1, is(tokenGenerator2));
    }

    /*
     * From the LibertyReserve API site we know that the SHA-256 hash of the
     * line MySecWord:20070201:14 results in the following String output:
     * 9A0EFBDCE4F4126C9F1EDD38AA39F3817B9C479C4A1F80B7409597F5403CA860
     */
    @Test
    public void tokenGeneratorValidity() {

        final DateTimeFormatter tokenDateTimeFormatter = configuration.tokenDateTimeFormatter();

        final long timestamp = tokenDateTimeFormatter.parseMillis("20070201:14");

        final String apiSecurityWordStr = "MySecWord";

        final char[] apiSecurityWord = apiSecurityWordStr.toCharArray();

        final String tokenHash = tokenGenerator1.createToken(apiSecurityWord, timestamp);

        assertThat(tokenHash, is(EXPECTED_TOKEN_HASH));

        // ensure that apiSecurityWord was zeroed after it's use
        for (final char c : apiSecurityWord) {
            assertThat(c, is('0'));
        }
    }

}
