package ecurrencies.integration.amqp

import static java.lang.String.format
import static org.springframework.integration.amqp.AmqpHeaders.DELIVERY_TAG

import java.util.concurrent.ConcurrentHashMap

import org.springframework.amqp.AmqpException
import org.springframework.integration.annotation.Header

import com.rabbitmq.client.Channel

class MessageAcknowledgeManager {

    private static final String COULD_NOT_ACK_FORMAT = 'Could not ack [deliveryTag=%d]'
    private static final String COULD_NOT_NACK_FORMAT = 'Could not nack [deliveryTag=%d]'

    private Map<Long, Channel> deliveryTagChannelMap = [:] as ConcurrentHashMap

    void put(Long deliveryTag, Channel channel) {
        deliveryTagChannelMap.put(deliveryTag, channel)
    }

    void confirm(@Header(DELIVERY_TAG) Long deliveryTag) {
        action(deliveryTag, COULD_NOT_NACK_FORMAT) { Channel channel ->
            channel?.basicAck(deliveryTag, false)
        }
    }

    void rejectAndRequeue(@Header(DELIVERY_TAG) Long deliveryTag) {
        action(deliveryTag, COULD_NOT_NACK_FORMAT) { Channel channel ->
            channel?.basicNack(deliveryTag, false, true)
        }
    }

    void reject(@Header(DELIVERY_TAG) final Long deliveryTag) {
        action(deliveryTag, COULD_NOT_NACK_FORMAT) { Channel channel ->
            channel?.basicNack(deliveryTag, false, false)
        }
    }

    private void action(deliveryTag, errorMessage, closure) {
        try {
            closure(deliveryTagChannelMap.remove(deliveryTag))
        } catch (final IOException e) {
            throw new AmqpException(format(errorMessage, deliveryTag), e)
        }
    }
}
