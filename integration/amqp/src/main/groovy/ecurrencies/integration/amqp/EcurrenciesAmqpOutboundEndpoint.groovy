package ecurrencies.integration.amqp

import static org.springframework.integration.amqp.AmqpHeaders.DELIVERY_TAG

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.integration.Message
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint

class EcurrenciesAmqpOutboundEndpoint  extends AmqpOutboundEndpoint {

    private final MessageAcknowledgeManager messageAcknowledgeManager

    EcurrenciesAmqpOutboundEndpoint(AmqpTemplate amqpTemplate,
    MessageAcknowledgeManager messageAcknowledgeManager) {
        super(amqpTemplate)
        this.messageAcknowledgeManager = messageAcknowledgeManager
    }

    @Override
    protected Object handleRequestMessage(Message<?> requestMessage) {
        try {
            return super.handleRequestMessage(requestMessage)
        } finally {
            Long deliveryTag = (Long) requestMessage?.headers?."$DELIVERY_TAG"
            if (deliveryTag) {
                messageAcknowledgeManager.confirm(deliveryTag)
            }
        }
    }
}
