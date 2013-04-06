package ecurrencies.common.akka

import akka.actor.{Actor, UnhandledMessage, Props, ActorSystem}
import akka.pattern.ask
import akka.testkit.TestKit

import ecurrencies.api.EcurrencyServiceException

import org.junit.runner.RunWith

import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import scala.concurrent.Await
import scala.util.control.NonFatal

@RunWith(classOf[JUnitRunner])
class UnhandledMessageListenerSpec extends TestKit(ActorSystem()) with WordSpec with MustMatchers with BeforeAndAfterAll
with SharedTestFixture {

  val echoActorRef = system.actorOf(Props[EchoActor], "EchoActor")
  val listener = system.actorOf(Props[UnhandledMessageListener], "UnhandledMessageListener")

  override def beforeAll() {
    system.eventStream.subscribe(listener, classOf[UnhandledMessage])
  }

  "ActorSystem" when {
    "EchoActror receives a message that is not an integer" must {
      "signal a non recoverable EcurrencyServiceExeption" in {
        val future = {
          (echoActorRef ? " string message") map {
            case result => fail("EchoActor should not return any message => " + result.toString)
          } recover {
            case e: EcurrencyServiceException => assert(e.isRecoverable == false)
            case NonFatal(e) => fail("Should not receive " + e.getClass.getName)
          }
        }
        Await.result(future, timeout.duration)
        assert(future.isCompleted)
      }
    }
  }
}

class EchoActor extends Actor {
  def receive = {
    case number: Int => sender ! number
  }
}

