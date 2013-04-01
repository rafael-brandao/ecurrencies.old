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
class TransferSpec
  extends TestKit(ActorSystem())
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll {

  implicit val timeout = Timeout(5 seconds)

  val request: TransferRequest = classOf[TransferRequest]
  val response: TransactionResponse = classOf[TransactionResponse]
  val invalidRequest: AccountNameRequest = classOf[AccountNameRequest]

  val httpActor = system.actorOf(Props(HttpServiceMock[TransferRequest, TransactionResponse](response)))
  val parser = system.actorOf(Props(ProtobuffParser[TransferRequest](httpActor)))

  "Transfer service" when {
    "receives a valid TransferRequest" must {
      "return a valid TransactionResponse" in {
        val future = (parser ? request.toByteArray).mapTo[TransactionResponse]
        val result = Await.result(future, timeout.duration)
        result must be(response)
      }
    }
  }

  "Transfer service" when {
    "receives an invalid TransferRequest" must {
      "return a non recoverable EcurrencyServiceException" in {
        val future = (parser ? invalidRequest.toByteArray).mapTo[TransactionResponse]
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