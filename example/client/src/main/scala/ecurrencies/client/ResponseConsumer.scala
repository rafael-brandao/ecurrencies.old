package ecurrencies.client

import java.util.concurrent.atomic.AtomicInteger
import akka.actor.{ Actor, ActorLogging, ActorRef, actorRef2Scala }
import akka.event.LoggingAdapter
import com.google.protobuf.GeneratedMessage
import com.rabbitmq.client.{ AMQP, Channel, DefaultConsumer, Envelope }
import ecurrencies.libertyreserve.domain._
import scala.concurrent.duration.Duration
import akka.actor.ActorContext
import com.rabbitmq.client.QueueingConsumer
import scala.concurrent.Await
import scala.concurrent.Future

trait ResponseHandler {

  import Settings._
  import ResponseHandler._

  protected val channel: Channel
  protected val resultAggregator: ActorRef
  protected val id: String
  protected val log: LoggingAdapter

  protected def onDelivery( consumerTag: String,
                            envelope: Envelope,
                            properties: AMQP.BasicProperties,
                            body: Array[ Byte ] ) {

    import envelope.{ getDeliveryTag => deliveryTag }
    import properties.{ getHeaders => headers, getMessageId => messageId }

    log.debug( "Consumer received a response message -> {}", messageId )

    try {

      val ecurrencyId = headers.get( "ecurrency-id" ).toString
      val serviceId = headers.get( "service-id" ).toString

      ecurrencyId match {
        case `liberty-reserve.ecurrency-id` =>
          `liberty-reserve-parsers`.get( serviceId ) match {
            case Some( parse ) =>
              ack( deliveryTag, messageId ) {
                log.info( "{} processed message id {}\n\n{}\n", id, messageId, parse( body ) )
              }
            case None =>
              reject( deliveryTag, messageId ) {
                log.warning( "{} received an invalid message." +
                  " It did not provide a valid 'service-id'" +
                  "MessageId : {} - ServiceId : {}", id, messageId, serviceId )
              }
          }
      }

    } catch {
      case t: NullPointerException =>
        reject( deliveryTag, messageId ) {
          log.warning( "{} received an invalid response." +
            " It did not provide required headers 'ecurrency-id' and 'service-id'" +
            "MessageId : {}", id, messageId )
        }
    }

  }

  protected def ack( deliveryTag: Long, messageId: String )( closure: => Unit = {} ) {
    try {
      closure
      log.info( "{} is confirming message {}", id, messageId )
      resultAggregator ! ProcessedMessage
    } finally
      channel.basicAck( deliveryTag, false )
  }

  protected def reject( deliveryTag: Long, messageId: String, requeue: Boolean = false )( closure: => Unit = {} ) {
    try {
      closure
      requeue match {
        case true  => log.warning( "{} is rejecting and requeueing message {}", id, messageId )
        case false => log.warning( "{} is rejecting and discarding message {}", id, messageId )
      }
      resultAggregator ! RejectedMessage
    } finally
      channel.basicReject( deliveryTag, requeue )
  }

}

object ResponseHandler {

  import Settings._

  private[ client ] val `liberty-reserve-parsers`: Map[ String, Function1[ Array[ Byte ], GeneratedMessage ] ] =
    Map(
      `liberty-reserve.account-name-service-id` -> ( ( response: Array[ Byte ] ) => AccountNameResponse.parseFrom( response ) ),
      `liberty-reserve.balance-service-id` -> ( ( response: Array[ Byte ] ) => BalanceResponse.parseFrom( response ) ),
      `liberty-reserve.find-transaction-service-id` -> ( ( response: Array[ Byte ] ) => TransactionResponse.parseFrom( response ) ),
      `liberty-reserve.history-service-id` -> ( ( response: Array[ Byte ] ) => HistoryResponse.parseFrom( response ) ),
      `liberty-reserve.transfer-service-id` -> ( ( response: Array[ Byte ] ) => TransactionResponse.parseFrom( response ) )
    );

}

class EventBasedConsumer( val createChannel: () => Channel, val queue: String, val resultAggregator: ActorRef )
    extends Actor with ActorLogging with ResponseHandler {

  import EventBasedConsumer._

  val id = "EventBasedConsumer" + idCounter.incrementAndGet
  val channel = createChannel()
  val consumer =
    new DefaultConsumer( channel ) {
      override def handleDelivery( consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[ Byte ] ) {
        self ! ( consumerTag, envelope, properties, body )
      }
    }

  channel.basicConsume( queue, false, consumer )

  log.info( "Started {}", id )

  def close {
    if ( channel != null )
      channel.basicCancel( consumer.getConsumerTag )
    log.info( "{} is closing channel", id )
    channel.close
    log.info( "Closed {}", id )
  }

  def receive = {
    case ( consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[ Byte ] ) =>
      onDelivery( consumerTag, envelope, properties, body )
  }

  override def preRestart( reason: Throwable, message: Option[ Any ] ) {
    close
  }

  override def postStop {
    close
  }
}

object EventBasedConsumer {
  val idCounter = new AtomicInteger

  def apply( createChannel: () => Channel, queue: String, resultAggregator: ActorRef ) =
    new EventBasedConsumer( createChannel, queue, resultAggregator )
}

