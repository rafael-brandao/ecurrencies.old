package ecurrencies.service.amqp

import scala.concurrent.Future

import akka.actor.ActorContext
import akka.pattern.ask
import akka.util.Timeout

import com.google.protobuf.GeneratedMessage
import com.rabbitmq.client.{ AMQP, Channel, DefaultConsumer, Envelope }

import ecurrencies.api.EcurrencyServiceException

private[ amqp ] class AmqpConsumer( channel: Channel )( implicit context: ActorContext, timeout: Timeout )
    extends DefaultConsumer( channel ) {

  import AmqpConsumer._
  import context._
  import channel._

  override def handleDelivery( consumerTag: String,
                               envelope: Envelope,
                               properties: AMQP.BasicProperties,
                               body: Array[ Byte ] ) {

    val deliveryTag = envelope.getDeliveryTag
    val ecurrencyId = properties.getHeaders.get( "ecurrencyId" ).asInstanceOf[ String ]
    val serviceId = properties.getHeaders.get( "serviceId" ).asInstanceOf[ String ]

    ( Option( ecurrencyId ), Option( serviceId ) ) match {
      case ( Some( ecurrency ), Some( service ) ) =>
        val serviceActor = actorFor( pathFormat format ( ecurrency, service ) )
        val future: Future[ GeneratedMessage ] = ( serviceActor ? body ).mapTo[ GeneratedMessage ]

        future onSuccess {
          case response => {

            val correlationId = properties.getCorrelationId
            val replyTo = properties.getReplyTo

            ( Option( correlationId ), Option( replyTo ) ) match {
              case ( Some( correlationId ), Some( replyTo ) ) => {
                val replyProperties =
                  new AMQP.BasicProperties.Builder()
                    .contentType( properties.getContentType )
                    .correlationId( correlationId )
                    .build
                try {
                  basicPublish( "", replyTo, replyProperties, response.toByteArray );
                } finally {
                  basicAck( deliveryTag, false )
                }
              }
              case _ => channel.basicAck( deliveryTag, false )
            }
          }
        }

        future onFailure {
          case exception: EcurrencyServiceException if ( !exception.isRecoverable ) =>
            basicReject( deliveryTag, false )
          case _ =>
            basicReject( deliveryTag, true )
        }

      case _ => basicReject( deliveryTag, false )
    }

  }

}

private[ amqp ] object AmqpConsumer {
  def apply( channel: Channel )( implicit context: ActorContext, timeout: Timeout ) = new AmqpConsumer( channel )
  val pathFormat = "/user/%s/%s"
}