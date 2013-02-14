package ecurrencies.service.amqp

import scala.concurrent.duration.DurationLong
import scala.language.postfixOps

import akka.util.Timeout

import com.typesafe.config.ConfigFactory

object Settings {

  val config = ConfigFactory.load()

  config.checkValid( ConfigFactory.defaultReference(), "amqp-service" )

  import config._

  val `connection.host` = getString( "amqp-service.connection.host" )
  val `connection.port` = getInt( "amqp-service.connection.port" )
  val `connection.virtual-host` = getString( "amqp-service.connection.virtual-host" )
  val `connection.username` = getString( "amqp-service.connection.username" )
  val `connection.password` = getString( "amqp-service.connection.password" )

  val `exchange.name` = getString( "amqp-service.exchange.name" )
  val `exchange.type` = getString( "amqp-service.exchange.type" )
  val `exchange.durable` = getBoolean( "amqp-service.exchange.durable" )
  val `exchange.auto-delete` = getBoolean( "amqp-service.exchange.auto-delete" )
  val `exchange.internal` = getBoolean( "amqp-service.exchange.internal" )

  val `binding-key` = getString( "amqp-service.binding-key" )

  val `queue.name` = getString( "amqp-service.queue.name" )
  val `queue.durable` = getBoolean( "amqp-service.queue.durable" )
  val `queue.exclusive` = getBoolean( "amqp-service.queue.exclusive" )
  val `queue.auto-delete` = getBoolean( "amqp-service.queue.auto-delete" )

  val `channel.instances` = getInt( "amqp-service.channel.instances" )
  val `channel.prefetch-count` = getInt( "amqp-service.channel.prefetch-count" )

  val `consumer.timeout`: Long = getMilliseconds( "amqp-service.consumer.timeout" )

  implicit val consumerTimeout = Timeout( `consumer.timeout` milliseconds )
}