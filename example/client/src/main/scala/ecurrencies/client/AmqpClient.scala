package ecurrencies.client

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.duration.Duration

import akka.actor.{ Actor, ActorLogging, ActorRef, actorRef2Scala }

import com.google.protobuf.GeneratedMessage
import com.rabbitmq.client.{ AMQP, Channel, QueueingConsumer }

case object ProducerReady

sealed trait AmqpClient extends Actor with ActorLogging {

  implicit protected val createChannel: () => Channel

  protected val channel = createChannel()
  protected val sleepTime: Duration

  protected def handleRequest( request: GeneratedMessage, ecurrencyId: String, serviceId: String ): Unit

  final def receive = start

  final def start: Receive = {
    case Start => context become handleRequest; sender ! ProducerReady
  }

  final def handleRequest: Receive = {
    case ( request: GeneratedMessage, ecurrencyId: String, serviceId: String ) =>
      handleRequest( request, ecurrencyId, serviceId )
      Thread.sleep( sleepTime.toMillis )
      sender ! ProducerReady
  }

  override def preRestart( reason: Throwable, message: Option[ Any ] ) = closeChannel

  override def postStop = closeChannel

  protected def closeChannel {
    if ( channel != null ) channel.close
  }

}

sealed trait BasicPropertiesBuilder {

  private val genericProperties = new AMQP.BasicProperties().builder().contentType( "application/x-protobuf" )

  protected def generateProperties( apply: AMQP.BasicProperties.Builder => AMQP.BasicProperties.Builder = ( b => b ) ) = {

    apply( genericProperties )
      .messageId( System.currentTimeMillis.toString )
      .build()
  }

  protected def generateRoutingKey( ecurrencyId: String, serviceId: String ) = {
    ecurrencyId + '.' + serviceId
  }

}

class NonBlockingProducer( val createChannel: () => Channel, val resultAggregator: ActorRef,
                           exchangeName: String, replyQueue: String, val sleepTime: Duration )
    extends AmqpClient with BasicPropertiesBuilder {

  def handleRequest( request: GeneratedMessage, ecurrencyId: String, serviceId: String ) {
    val properties = generateProperties( _.replyTo( replyQueue ) )
    channel.basicPublish( exchangeName, generateRoutingKey( ecurrencyId, serviceId ), properties, request.toByteArray )
    resultAggregator ! ProducedMessage
  }

}

class RpcClientProducer(
  val createChannel: () => Channel, val resultAggregator: ActorRef, exchangeName: String, val sleepTime: Duration )(
    implicit timeout: Duration )
    extends AmqpClient with BasicPropertiesBuilder with ResponseHandler {

  import RpcClientProducer._
  import context.dispatcher

  val id = "RpcClientProducer-" + idGenerator.incrementAndGet()

  def handleRequest( request: GeneratedMessage, ecurrencyId: String, serviceId: String ) {

    val correlationId = UUID.randomUUID.toString
    val replyQueue = channel.queueDeclare.getQueue
    val consumer = new QueueingConsumer( channel )

    val properties = generateProperties( _.correlationId( correlationId ).replyTo( replyQueue ) )

    channel.basicPublish( exchangeName, generateRoutingKey( ecurrencyId, serviceId ), properties, request.toByteArray() )
    resultAggregator ! ProducedMessage

    channel.basicConsume( replyQueue, false, consumer )

    try {
      val delivery = Option( consumer.nextDelivery( timeout.toMillis ) )

      delivery match {
        case Some( delivery ) =>

          import delivery.{ getBody => body, getEnvelope => envelope, getProperties => responseProperties }

          val messageId = responseProperties.getMessageId
          val deliveryTag = envelope.getDeliveryTag

          Option( responseProperties.getCorrelationId ) match {
            case Some( responseCorrelationId ) =>
              if ( responseCorrelationId equals correlationId )
                onDelivery( consumer.getConsumerTag, envelope, responseProperties, body )
              else
                reject( deliveryTag, messageId ) {
                  log.warning( "{} received a message not matching correlationId -> " +
                    "Message correlationId = {} | Expected correlationId = {}", id, responseCorrelationId, correlationId )
                }
            case None =>
              reject( deliveryTag, messageId ) {
                log.warning( "{} received a message without correlationId. MessageId = {}", id, messageId )
              }
          }

        case None =>
          log.error( "{} timed out", properties.getMessageId )
          resultAggregator ! TimedOutMessage
      }

    } finally {
      if ( consumer ne null ) channel.basicCancel( consumer.getConsumerTag )
    }
  }

}

object RpcClientProducer {
  private val idGenerator = new AtomicInteger
}