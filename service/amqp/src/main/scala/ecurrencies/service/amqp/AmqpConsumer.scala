package ecurrencies.service.amqp

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.Future

import akka.actor.{ Actor, ActorLogging }
import akka.pattern.ask
import akka.util.Timeout

import com.google.protobuf.GeneratedMessage
import com.rabbitmq.client.{ AMQP, Channel, DefaultConsumer, Envelope }

import AmqpConsumer.{ idGenerator, pathFormat }
import ecurrencies.api.EcurrencyServiceException

private[ amqp ] class AmqpConsumer( createChannel: () => Channel, queue: String )( implicit val timeout: Timeout )
    extends Actor with ActorLogging {

  import AmqpConsumer._
  import context._

  private val channel: Channel = createChannel()
  private val id: String = "AmqpConsumer_" + idGenerator.incrementAndGet

  private val consumer: DefaultConsumer =
    new DefaultConsumer( channel ) {
      override def handleDelivery( consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[ Byte ] ) {
        self ! ( consumerTag, envelope, properties, body )
      }
    }

  channel.basicConsume( queue, false, consumer )

  log.info( "{} started", id )

  def close {
    if ( channel ne null )
      channel.basicCancel( consumer.getConsumerTag )
    log.info( "{} is closing channel", id )
    channel.close
    log.info( "Closed {}", id )
  }

  override def preRestart( reason: Throwable, message: Option[ Any ] ) {
    close
  }

  override def postStop {
    close
  }

  def receive = {
    case ( consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[ Byte ] ) =>
      handleDelivery( consumerTag, envelope, properties, body )
  }

  private def handleDelivery( consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[ Byte ] ) {

    import envelope.{ getDeliveryTag => deliveryTag }
    import properties.{ getHeaders => headers, getMessageId => messageId }

    log.info( "{} received a message. MessageId = {}", id, messageId )

    try {
      val ecurrencyId = headers.get( "ecurrency-id" ).toString()
      val serviceId = headers.get( "service-id" ).toString()

      val serviceActor = actorFor( pathFormat format ( ecurrencyId, serviceId ) )

      val future: Future[ GeneratedMessage ] = ( serviceActor ? body ).mapTo[ GeneratedMessage ]

      future onSuccess {
        case response => {
          ack( deliveryTag, messageId ) {
            Option( properties.getReplyTo ) match {
              case Some( replyTo ) => {

                val replyPropertiesBuilder =
                  new AMQP.BasicProperties.Builder()
                    .contentType( properties.getContentType )
                    .messageId( UUID.randomUUID().toString() )
                    .headers( Map( "ecurrency-id" -> ecurrencyId, "service-id" -> serviceId ) )

                val replyProperties =
                  Option( properties.getCorrelationId ) match {
                    case Some( correlationId ) =>
                      log.info( "message {} processed -> sending response to queue {} | correlationId -> {}", messageId, replyTo, correlationId )
                      replyPropertiesBuilder.correlationId( correlationId ).build
                    case _ =>
                      log.info( "message {} processed -> sending response to queue {}", messageId, replyTo )
                      replyPropertiesBuilder.build
                  }
                channel.basicPublish( "", replyTo, replyProperties, response.toByteArray )
              }
              case _ => log.info( "message {} processed", messageId )
            }
          }
        }
      }

      future onFailure {
        case exception: EcurrencyServiceException if ( !exception.isRecoverable ) =>
          reject( deliveryTag, messageId ) {
            log.error( exception, "Caught an unrecoverable exception processing message {}", messageId )
          }
        case exception: Throwable =>
          reject( deliveryTag, messageId, true ) {
            log.error( exception, "Caught a recoverable exception processing message {}", messageId )
          }
      }
    } catch {
      case t: NullPointerException =>
        reject( deliveryTag, messageId ) {
          log.info( "Message {} did not provide valid 'ecurrency-id' and 'service-id' headers", messageId )
        }
    }

  }

  private def ack( deliveryTag: Long, messageId: String )( closure: => Unit = {} ) {
    try {
      closure
      log.info( "{} is confirming message {}", id, messageId )
    } finally
      channel.basicAck( deliveryTag, false )
  }

  private def reject( deliveryTag: Long, messageId: String, requeue: Boolean = false )( closure: => Unit = {} ) {
    try {
      closure
      requeue match {
        case true  => log.warning( "{} is rejecting and requeueing message {}", id, messageId )
        case false => log.warning( "{} is rejecting and discarding message {}", id, messageId )
      }
    } finally
      channel.basicReject( deliveryTag, requeue )
  }

}

private[ amqp ] object AmqpConsumer {

  private lazy val idGenerator = new AtomicInteger
  private lazy val pathFormat = "/user/%s/%s"

  def apply( createChannel: () => Channel, queue: String )( implicit timeout: Timeout ) = new AmqpConsumer( createChannel, queue )

}