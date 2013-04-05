package ecurrencies.libertyreserve.service

import akka.routing.{DefaultResizer, RoundRobinRouter}

import com.typesafe.config.ConfigFactory

private[service] object Settings {

  val config = ConfigFactory.load()

  config.checkValid(ConfigFactory.defaultReference(), "liberty-reserve")

  val `api.domain` = config.getString("liberty-reserve.api.domain")
  val `api.port` = config.getInt("liberty-reserve.api.port")

  val `api.account-name-uri` = config.getString("liberty-reserve.api.account-name-uri")
  val `api.balance-uri` = config.getString("liberty-reserve.api.balance-uri")
  val `api.find-transaction-uri` = config.getString("liberty-reserve.api.find-transaction-uri")
  val `api.history-uri` = config.getString("liberty-reserve.api.history-uri")
  val `api.transfer-uri` = config.getString("liberty-reserve.api.transfer-uri")

  val `router.resizer.lower-bound` = config.getInt("liberty-reserve.akka.router.resizer.lower-bound")
  val `router.resizer.upper-bound` = config.getInt("liberty-reserve.akka.router.resizer.upper-bound")

  val `liberty-reserve-dispatcher-id` = "liberty-reserve.akka.dispatcher"

  def router = new RoundRobinRouter(new DefaultResizer(`router.resizer.lower-bound`, `router.resizer.upper-bound`))
}