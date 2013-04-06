package ecurrencies.common.akka

import akka.actor.{Actor, Status, UnhandledMessage}
import ecurrencies.api.EcurrencyServiceException

class UnhandledMessageListener extends Actor {

  def receive = {
    case u: UnhandledMessage => u.sender ! Status.Failure(EcurrencyServiceException(recoverable = false))
  }
}
