package ecurrencies.libertyreserve.service

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout

import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers

import ecurrencies.api.EcurrencyServiceException
import ecurrencies.common.akka.ProtobuffParser
import ecurrencies.libertyreserve.domain._
import ecurrencies.libertyreserve.service.mock.HttpServiceMock
import ecurrencies.libertyreserve.test.util.LibertyReserveGenerator._

@RunWith(classOf[JUnitRunner])
class AccountNameSpec
  extends TestKit(ActorSystem())
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll {

  implicit val timeout = Timeout(5 seconds)

  val request: AccountNameRequest = classOf[AccountNameRequest]
  val response: AccountNameResponse = classOf[AccountNameResponse]
  val invalidRequest: BalanceRequest = classOf[BalanceRequest]

  val httpActor = system.actorOf(Props(HttpServiceMock[AccountNameRequest, AccountNameResponse](response)))
  val parser = system.actorOf(Props(ProtobuffParser[AccountNameRequest](httpActor)))

  "AccountName service" when {
    "receives a valid AccountNameRequest" must {
      "return a valid AccountNameResponse" in {
        val future = (parser ? request.toByteArray).mapTo[AccountNameResponse]
        val result = Await.result(future, timeout.duration)
        result must be(response)
      }
    }
  }

  "AccountNameService" when {
    "receives an invalid AccountNameRequest" must {
      "return a non recoverable EcurrencyServiceException" in {
        val future = (parser ? invalidRequest.toByteArray).mapTo[AccountNameResponse]
        val exception = Await.result(future.failed, timeout.duration)
        exception.getClass must be(classOf[EcurrencyServiceException])
        exception.asInstanceOf[EcurrencyServiceException].isRecoverable must be === false
      }
    }
  }

  override def afterAll() {
    system.shutdown()
  }

}