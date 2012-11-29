package ecurrencies.integration.amqpimport org.springframework.amqp.core.Messageimport org.springframework.amqp.rabbit.core.ChannelAwareMessageListenerimport org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainerimport org.springframework.amqp.support.converter.MessageConverterimport org.springframework.amqp.support.converter.SimpleMessageConverterimport org.springframework.integration.amqp.support.AmqpHeaderMapperimport org.springframework.integration.amqp.support.DefaultAmqpHeaderMapperimport org.springframework.integration.endpoint.MessageProducerSupportimport org.springframework.integration.support.MessageBuilderimport org.springframework.util.Assertimport com.rabbitmq.client.Channel
class EcurrenciesAmqpInboundChannelAdapter extends MessageProducerSupport {

    private final AbstractMessageListenerContainer messageListenerContainer
    private final MessageAcknowledgeManager messageAcknowledgeManager

    private volatile MessageConverter messageConverter = new SimpleMessageConverter()
    private volatile AmqpHeaderMapper headerMapper = new DefaultAmqpHeaderMapper()

    EcurrenciesAmqpInboundChannelAdapter(AbstractMessageListenerContainer listenerContainer, MessageAcknowledgeManager messageAcknowledgeManager) {
        Assert.notNull(listenerContainer, "listenerContainer must not be null")
        Assert.isNull(listenerContainer.getMessageListener(),
                "The listenerContainer provided to an AMQP inbound Channel Adapter must not have a "
                + "MessageListener configured since the adapter needs to configure its own "
                + "listener implementation.")
        this.messageListenerContainer = listenerContainer
        this.messageListenerContainer.setAutoStartup(false)
        this.messageAcknowledgeManager = messageAcknowledgeManager    }

    @Override
    protected void onInit() {
        this.messageListenerContainer.messageListener = new ChannelAwareMessageListener() {
                    @Override
                    void onMessage(Message message,Channel channel) throws Exception {
                        messageAcknowledgeManager.put(message.messageProperties.deliveryTag, channel)
                        Object payload = messageConverter.fromMessage(message)
                        Map<String, ?> headers = headerMapper.toHeadersFromRequest(message.messageProperties)
                        sendMessage(MessageBuilder.withPayload(payload).copyHeaders(headers).build())                    }                }
        this.messageListenerContainer.afterPropertiesSet()
        super.onInit()    }

    @Override
    protected void doStart() {
        this.messageListenerContainer.start()    }

    @Override
    protected void doStop() {
        this.messageListenerContainer.stop()    }}