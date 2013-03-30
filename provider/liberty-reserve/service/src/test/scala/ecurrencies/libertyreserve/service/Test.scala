package ecurrencies.libertyreserve.service

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import ecurrencies.libertyreserve.domain._
import ecurrencies.libertyreserve.test.util.LibertyReserveGenerator._
import com.google.protobuf.GeneratedMessage
import akka.actor.RootActorPath

object Test {

  def main( args: Array[ String ] ) {

    val request: HistoryRequest = classOf[ HistoryRequest ]
    println( request )

    implicit val system = ActorSystem( "ecurrencies", ConfigFactory.load() )
    implicit val timeout = Timeout( 5 seconds )

    val supervisor = system.actorOf( Props[ LibertyReserveSupervisor ], "liberty-reserve" )

    Thread.sleep( 500 )

    val service = system.actorFor( "/user/liberty-reserve/history" )
    println( service.path )

    val future = ( service ? request.toByteArray ).mapTo[ GeneratedMessage ]

    val ready = Await.ready( future, 5 seconds )

    ready onSuccess {
      case response => println( response.toString )
    }
    ready onFailure {
      case exception =>
        println( exception.toString )
        exception.printStackTrace
    }

    Thread.sleep( 500 )
    system.shutdown
  }

}