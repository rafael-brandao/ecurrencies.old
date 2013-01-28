package ecurrencies.service.amqp

import com.typesafe.config.{ Config, ConfigFactory }

object Settings {

  val config = ConfigFactory.load()

  config.checkValid( ConfigFactory.defaultReference(), "amqp-service" )

  import config._

  val `connection.host` = getString( "amqp-service.connection.host" )
  val `connection.port` = getInt( "amqp-service.connection.port" )
  val `connection.virtual-host` = getString( "amqp-service.connection.virtual-host" )
  val `connection.username` = getString( "amqp-service.connection.username" )
  val `connection.password` = getString( "amqp-service.connection.password" )

  val `queue.name` = getString( "amqp-service.queue.name" )
  val `queue.durable` = getBoolean( "amqp-service.queue.durable" )
  val `queue.exclusive` = getBoolean( "amqp-service.queue.exclusive" )
  val `queue.auto-delete` = getBoolean( "amqp-service.queue.auto-delete" )

  val `channel.instances` = getInt( "amqp-service.channel.instances" )
  val `channel.prefetch-count` = getInt( "amqp-service.channel.prefetch-count" )

  val `consumer.timeout`: Long = getMilliseconds( "amqp-service.consumer.timeout" )

}