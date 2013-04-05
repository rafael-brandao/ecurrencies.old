package ecurrencies

import java.util.concurrent.TimeoutException

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationLong
import scala.language.postfixOps

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.kernel.Bootable
import akka.pattern.gracefulStop

import com.typesafe.config.ConfigFactory

import ecurrencies.libertyreserve.service.LibertyReserveSupervisor
import ecurrencies.service.amqp.AmqpSupervisor

class Application extends Bootable {

  implicit val system = ActorSystem()

  import system.dispatcher
  import Settings._

  private val ecurrencyActors = mutable.MutableList.empty[ActorRef]
  private var amqpSupervisor: ActorRef = _

  @inline
  final def startup() {
    ecurrencyActors += system.actorOf(Props[LibertyReserveSupervisor], "liberty-reserve")
    amqpSupervisor = system.actorOf(Props[AmqpSupervisor])
  }

  @inline
  final def shutdown() {
    import system.log

    try {
      Await.ready(
        Future {
          shutdownEcurrencies()
          shutdownAmqp()
        }, `system-shutdown-timeout`)
    } catch {
      case t: TimeoutException =>
        log.error("Could not shutdown system in " + `system-shutdown-timeout`, t)
    }
    system.shutdown()
  }

  @inline
  private def shutdownEcurrencies() {
    val stopped = ecurrencyActors.map(gracefulStop(_, `actor-shutdown-timeout`))
    Await.ready(Future.sequence(stopped), `actor-shutdown-timeout`)
  }

  @inline
  private def shutdownAmqp() {
    Await.ready(gracefulStop(amqpSupervisor, `actor-shutdown-timeout`), `actor-shutdown-timeout`)
  }

  private object Settings {
    val config = system.settings.config
    config.checkValid(ConfigFactory.defaultReference(), "ecurrencies")

    val `actor-shutdown-timeout` =
      config.getMilliseconds("ecurrencies.actor-shutdown-timeout").asInstanceOf[Long] milliseconds
    val `system-shutdown-timeout` =
      config.getMilliseconds("ecurrencies.system-shutdown-timeout").asInstanceOf[Long] milliseconds
  }

}