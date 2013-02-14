package ecurrencies.service.amqp

import scala.concurrent.duration.DurationLong
import scala.language.postfixOps

import akka.actor.{ Actor, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy }

import com.rabbitmq.client.ConnectionFactory

class AmqpSupervisor extends Actor {

  def receive = { case _ => }

  import SupervisorStrategy.Restart
  import RabbitMQ._
  import Settings._

  override val supervisorStrategy =
    OneForOneStrategy( maxNrOfRetries = 10, withinTimeRange = 1 minute ) {
      case _: Exception => Restart
    }

  val consumers =
    ( 0 until `channel.instances` )
      .foldLeft( List.empty[ ActorRef ] ) {
        ( list, i ) =>
          context.actorOf( Props( AmqpConsumer( newChannel, `queue.name` ) ) ) :: list
      }.par

  override def postStop {
    if ( connection ne null )
      connection.close
  }

}

private[ amqp ] object RabbitMQ {

  import Settings._

  lazy val connection = {
    val factory = new ConnectionFactory
    factory.setHost( `connection.host` )
    factory.setPort( `connection.port` )
    factory.setVirtualHost( `connection.virtual-host` )
    factory.setUsername( `connection.username` )
    factory.setPassword( `connection.password` )
    factory.newConnection
  }

  val newChannel = { () =>
    val channel = connection.createChannel
    channel.exchangeDeclare( `exchange.name`, `exchange.type`, `exchange.durable`, `exchange.auto-delete`, `exchange.internal`, null )
    channel.queueDeclare( `queue.name`, `queue.durable`, `queue.exclusive`, `queue.auto-delete`, null )
    channel.queueBind( `queue.name`, `exchange.name`, `binding-key` )
    channel.basicQos( `channel.prefetch-count` )
    channel
  }

}