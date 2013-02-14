package ecurrencies.client

import com.typesafe.config.ConfigFactory

object Settings {

  val config = ConfigFactory.load()

  config.checkValid( ConfigFactory.defaultReference(), "ecurrencies-client" )

  import config._

  val `message-fire-interval` : Long = getMilliseconds( "ecurrencies-client.message-fire-interval" )

  val `connection.host` = getString( "ecurrencies-client.amqp.connection.host" )
  val `connection.port` = getInt( "ecurrencies-client.amqp.connection.port" )
  val `connection.virtual-host` = getString( "ecurrencies-client.amqp.connection.virtual-host" )
  val `connection.username` = getString( "ecurrencies-client.amqp.connection.username" )
  val `connection.password` = getString( "ecurrencies-client.amqp.connection.password" )

  val `channel.prefetch-count` = getInt( "ecurrencies-client.amqp.channel.prefetch-count" )

  val `exchange-name` = getString( "ecurrencies-client.amqp.exchange-name" )
  
  val `consumer-queue.name` = getString( "ecurrencies-client.amqp.consumer-queue.name" )
  val `consumer-queue.durable` = getBoolean( "ecurrencies-client.amqp.consumer-queue.durable" )
  val `consumer-queue.exclusive` = getBoolean( "ecurrencies-client.amqp.consumer-queue.exclusive" )
  val `consumer-queue.auto-delete` = getBoolean( "ecurrencies-client.amqp.consumer-queue.auto-delete" )

  val `non-blocking-producer.instances` = getInt( "ecurrencies-client.non-blocking-producer.instances" )
  val `rpc-client.instances` = getInt( "ecurrencies-client.rpc-client.instances" )
  val `rpc-client.timeout`: Long = getMilliseconds( "ecurrencies-client.rpc-client.timeout" )
  val `event-consumer.instances` = getInt( "ecurrencies-client.event-consumer.instances" )

  val `liberty-reserve.ecurrency-id` =
    getString( "ecurrencies-client.liberty-reserve.ecurrency-id" )
  val `liberty-reserve.account-name-service-id` =
    getString( "ecurrencies-client.liberty-reserve.account-name-service-id" )
  val `liberty-reserve.balance-service-id` =
    getString( "ecurrencies-client.liberty-reserve.balance-service-id" )
  val `liberty-reserve.find-transaction-service-id` =
    getString( "ecurrencies-client.liberty-reserve.find-transaction-service-id" )
  val `liberty-reserve.history-service-id` =
    getString( "ecurrencies-client.liberty-reserve.history-service-id" )
  val `liberty-reserve.transfer-service-id` =
    getString( "ecurrencies-client.liberty-reserve.transfer-service-id" )

}