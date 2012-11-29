package ecurrencies.libertyreserve.util.impl

import static ecurrencies.libertyreserve.util.impl.Constants.TOKEN_DATE_PATTERN
import static ecurrencies.libertyreserve.util.impl.Constants.TOKEN_DATE_TIMEZONE
import static ecurrencies.libertyreserve.util.impl.Constants.TOKEN_DIGEST_ALGORITHM
import static java.lang.String.format
import static java.security.MessageDigest.getInstance
import static org.joda.time.DateTimeZone.forID
import static org.joda.time.format.DateTimeFormat.forPattern
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON

import org.joda.time.format.DateTimeFormatter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.core.env.Environment

import ecurrencies.libertyreserve.util.IdGenerator
import ecurrencies.libertyreserve.util.TokenGenerator
import ecurrencies.libertyreserve.util.UtilFactory
import groovy.transform.PackageScope

@Configuration("utilFactory")
@PropertySource("classpath:util.properties")
class UtilConfiguration implements UtilFactory {

    @Autowired
    private Environment env

    @Bean
    @Scope(SCOPE_SINGLETON)
    @Override
    IdGenerator idGenerator() {
        new DefaultIdGenerator()
    }

    @Bean
    @Scope(SCOPE_SINGLETON)
    @Override
    TokenGenerator tokenGenerator() {
        new DefaultTokenGenerator(tokenDateTimeFormatter(), tokenMessageDigest())
    }

    private def tokenMessageDigest() {
        getInstance(env.getRequiredProperty(TOKEN_DIGEST_ALGORITHM))
    }

    @PackageScope
    DateTimeFormatter tokenDateTimeFormatter() {
        forPattern(env.getRequiredProperty(TOKEN_DATE_PATTERN)).withZone(forID(env.getRequiredProperty(TOKEN_DATE_TIMEZONE)))
    }
}