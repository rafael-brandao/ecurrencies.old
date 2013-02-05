package ecurrencies.client

import scala.concurrent.{ Await, TimeoutException }
import scala.concurrent.duration.DurationLong
import scala.language.postfixOps

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.kernel.Bootable
import akka.pattern.gracefulStop

class Application extends Bootable {

  val init = System.currentTimeMillis
  implicit val system = ActorSystem()
  import system.log

  var clientSupervisor: ActorRef = _

  def startup {
    clientSupervisor = system.actorOf( Props[ ClientSupervisor ] )
  }

  def shutdown {
    clientSupervisor ! Stop
    try {
      Await.result( gracefulStop( clientSupervisor, 20 seconds ), 20 seconds )
    } catch {
      case t: Throwable => log.error( t, "Could not shutdown client supervisor properly" )
    }
    system.shutdown
    println( NonBlockingProducerResultAggregator.report )
    println( EventConsumerResultAggregator.report )
    println( RpcClientResultAggregator.report )
    val now = System.currentTimeMillis
    println( "\n System uptime: " + ( ( now - init ) milliseconds ).toSeconds + " seconds\n" )
  }

}