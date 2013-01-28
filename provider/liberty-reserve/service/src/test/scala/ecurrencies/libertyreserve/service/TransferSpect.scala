package ecurrencies.libertyreserve.service

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout

import org.junit.runner.RunWith
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers

import ecurrencies.api.EcurrencyServiceException
import ecurrencies.common.akka.ProtobuffParser
import ecurrencies.libertyreserve.domain._
import ecurrencies.libertyreserve.service.mock.HttpServiceMock
import ecurrencies.libertyreserve.test.util.LibertyReserveGenerator._

@RunWith( classOf[ JUnitRunner ] )
class TransferSpec
    extends TestKit( ActorSystem() )
    with WordSpec
    with MustMatchers
    with BeforeAndAfterAll {

  implicit val timeout = Timeout( 5 seconds )

  val transferRequest = create[ TransferRequest ]
  val transactionResponse = create[ TransactionResponse ]

  val emptyHistoryResponse = historyResponse( Some( 0 ) )
  val nonEmptyHistoryResponse = historyResponse( Some( 1 ) )

  val transferHttpActor =
    system.actorOf(
      Props( HttpServiceMock[ TransferRequest, TransactionResponse ]( transactionResponse ) ),
      "TransferHttp" )

  val emptyHistoryHttpActor =
    system.actorOf(
      Props( HttpServiceMock[ HistoryRequest, HistoryResponse ]( emptyHistoryResponse ) ),
      "EmptyHistoryHttp" )

  val nonEmptyHistoryHttpActor =
    system.actorOf(
      Props( HttpServiceMock[ HistoryRequest, HistoryResponse ]( nonEmptyHistoryResponse ) ),
      "NonEmptyHistoryHttp" )

  val parser = { next: ActorRef => system.actorOf( Props( ProtobuffParser[ TransferRequest ]( next ) ) ) }

  "TransferService" when {
    "receives a non empty HistoryResponse" must {
      "return the Transaction enclosed by HistoryResponse" in {
        val transferActor = system.actorOf( Props( TransferActor( transferHttpActor, nonEmptyHistoryHttpActor ) ) )
        val future = ( parser( transferActor ) ? transferRequest.toByteArray ).mapTo[ TransactionResponse ]
        val result = Await.result( future, timeout.duration )
        result.getTransaction must be( nonEmptyHistoryResponse.getTransactions( 0 ) )
      }
    }
  }

  "TransferService" when {
    "receives an empty HistoryResponse" must {
      "return a TransactionResponse" in {
        val transferActor = system.actorOf( Props( TransferActor( transferHttpActor, emptyHistoryHttpActor ) ) )
        val future = ( parser( transferActor ) ? transferRequest.toByteArray ).mapTo[ TransactionResponse ]
        val result = Await.result( future, timeout.duration )
        result must be( transactionResponse )
      }
    }
  }

  "TransferHttpActor" when {
    "receives a non TransferRequest" must {
      "pipe a non recoverable EcurrencyException to the caller" in {
        val future = ( transferHttpActor ? create[ HistoryRequest ] ).mapTo[ TransactionResponse ]
        val exception = Await.result( future.failed, timeout.duration )
        exception.getClass must be( classOf[ EcurrencyServiceException ] )
        exception.asInstanceOf[ EcurrencyServiceException ].isRecoverable() must be( false )
      }
    }
  }

  override def afterAll = system.shutdown()

}