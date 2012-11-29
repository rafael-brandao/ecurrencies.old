package ecurrencies.integration.amqp

import static ecurrencies.integration.amqp.Constants.CONNECTION_FACTORY_CHANNEL_CACHE_SIZE
import static ecurrencies.integration.amqp.Constants.CONNECTION_FACTORY_HOSTNAME
import static ecurrencies.integration.amqp.Constants.CONNECTION_FACTORY_PASSWORD
import static ecurrencies.integration.amqp.Constants.CONNECTION_FACTORY_PORT
import static ecurrencies.integration.amqp.Constants.CONNECTION_FACTORY_USERNAME
import static ecurrencies.integration.amqp.Constants.CONNECTION_FACTORY_VIRTUAL_HOST
import static ecurrencies.integration.amqp.Constants.MESSAGE_LISTENER_CONCURRENT_CONSUMERS
import static ecurrencies.integration.amqp.Constants.MESSAGE_LISTENER_PREFETCH_COUNT
import static java.lang.String.format
import static java.lang.invoke.MethodHandles.lookup
import static org.slf4j.LoggerFactory.getLogger
import static org.springframework.amqp.core.AcknowledgeMode.MANUAL
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON

import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

import org.slf4j.Logger
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.core.env.Environment
import org.springframework.integration.MessageChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.core.MessageHandler
import org.springframework.integration.endpoint.AbstractEndpoint
import org.springframework.scheduling.annotation.EnableAsync

@Configuration
@EnableAsync
@PropertySource("classpath:service.properties")
class AmqpConfiguration {

    private static final String QUEUE_NAME_FORMAT = 'ecurrencies.%s.%s'

    private static final String CONNECTION_FACTORY_SETUP_MESSAGE_FORMAT = "Setting up ConnectionFactory -> {}"
    private static final String MESSAGE_LISTENER_SETUP_FORMAT = "Setting up MessageListenerContainer for queue {}"

    private static final Logger LOGGER = getLogger(lookup().lookupClass())

    @Autowired
    private ApplicationContext applicationContext

    @Autowired
    private Environment env

    @Autowired
    private ExecutorService executorService

    @Override
    Executor getAsyncExecutor() {
        executorService
    }

    @Bean
    @Scope(SCOPE_SINGLETON)
    ConnectionFactory connectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory(
                env.getRequiredProperty(CONNECTION_FACTORY_HOSTNAME))

        connectionFactory.username = env.getRequiredProperty(CONNECTION_FACTORY_USERNAME)
        connectionFactory.password = env.getRequiredProperty(CONNECTION_FACTORY_PASSWORD)

        def property = env.getProperty(CONNECTION_FACTORY_VIRTUAL_HOST)
        if(property)
            connectionFactory.virtualHost = property

        property = env.getProperty(CONNECTION_FACTORY_PORT, Integer.class)
        if (property) {
            connectionFactory.port = property
        }

        property = env.getProperty(CONNECTION_FACTORY_CHANNEL_CACHE_SIZE, Integer.class)
        if (property) {
            connectionFactory.channelCacheSize = property
        }

        connectionFactory.setExecutor(executorService)

        LOGGER.info(CONNECTION_FACTORY_SETUP_MESSAGE_FORMAT, connectionFactory)

        connectionFactory
    }

    @Bean
    @Scope(SCOPE_SINGLETON)
    MessageAcknowledgeManager messageAcknowledgeManager() {
        new MessageAcknowledgeManager()
    }

    @Bean(name = "amqp-outbound")
    @Scope(SCOPE_SINGLETON)
    MessageChannel amqpOutboundChannel() {
        new QueueChannel(100)
    }

    @Bean(name = "amqp-error")
    @Scope(SCOPE_SINGLETON)
    MessageChannel amqpErrorChannel() {
        new QueueChannel(100)
    }

    @Bean
    @Scope(SCOPE_SINGLETON)
    RabbitAdmin amqpAdmin() {
        new RabbitAdmin(connectionFactory())
    }

    @Bean
    @Scope(SCOPE_SINGLETON)
    AmqpTemplate amqpTemplate() {
        amqpAdmin().getRabbitTemplate()
    }

    @Bean
    @Scope(SCOPE_SINGLETON)
    MessageHandler amqpOutboundEndpoint() {
        new EcurrenciesAmqpOutboundEndpoint(amqpTemplate(), messageAcknowledgeManager())
    }

    @Bean(initMethod = "start")
    @Scope(SCOPE_PROTOTYPE)
    AbstractEndpoint amqpInboundChannelAdapter(MessageChannel outboundChannel, String ecurrencyName, String serviceName) {

        EcurrenciesAmqpInboundChannelAdapter adapter = new EcurrenciesAmqpInboundChannelAdapter(
                messageListenerContainer(format(QUEUE_NAME_FORMAT, ecurrencyName, serviceName)),
                messageAcknowledgeManager())
        adapter.setErrorChannel(amqpErrorChannel())

        adapter
    }

    private AbstractMessageListenerContainer messageListenerContainer(final String queueName) {
        LOGGER.info(MESSAGE_LISTENER_SETUP_FORMAT, queueName)
        amqpAdmin().declareQueue(new Queue(queueName))

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
                connectionFactory())
        container.taskExecutor = executorService
        container.queueNames = queueName
        container.acknowledgeMode = MANUAL
        container.concurrentConsumers = env.getRequiredProperty(MESSAGE_LISTENER_CONCURRENT_CONSUMERS, Integer.class)
        container.prefetchCount = env.getRequiredProperty(MESSAGE_LISTENER_PREFETCH_COUNT, Integer.class)

        container
    }
}
