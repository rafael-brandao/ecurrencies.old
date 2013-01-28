package ecurrencies.service.amqp

import scala.concurrent.duration.DurationLong
import scala.language.postfixOps

import akka.actor.Actor
import akka.util.Timeout

import com.rabbitmq.client.{ Channel, ConnectionFactory }

class AmqpSupervisor extends Actor {

  def receive = { case _ => }

  import Settings._
  import RabbitMQ._

  implicit val consumerTimeout = Timeout( `consumer.timeout` milliseconds )

  val channelConsumerMap =
    ( 0 until `channel.instances` )
      .map( i => newChannel )
      .foldLeft( Map.empty[ Channel, AmqpConsumer ] ) {
        ( map, channel ) =>
          channel.queueDeclare( `queue.name`, `queue.durable`, `queue.exclusive`, `queue.auto-delete`, null )
          channel.basicQos( `channel.prefetch-count` )
          val consumer = AmqpConsumer( channel )
          channel.basicConsume( `queue.name`, false, consumer )
          map + ( channel -> consumer )
      }.par

  override def postStop {
    try {
      for ( ( channel, consumer ) <- channelConsumerMap ) {
        channel.basicCancel( consumer.getConsumerTag )
        channel.close
      }
    } finally {
      connection.close
    }
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

  def newChannel() = {
    connection.createChannel
  }

}