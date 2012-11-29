package ecurrencies.integration.control

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.core.SubscribableChannel

@Configuration
class ControlConfiguration {

    @Bean
    @Scope(SCOPE_SINGLETON)
    SubscribableChannel errorChannel() {
        new PublishSubscribeChannel()
    }
}
