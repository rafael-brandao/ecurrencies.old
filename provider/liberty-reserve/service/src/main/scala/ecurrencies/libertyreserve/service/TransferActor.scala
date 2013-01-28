package ecurrencies.libertyreserve.service

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.{ Actor, ActorRef }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

import ecurrencies.libertyreserve.domain._

class TransferActor( transferHttpService: ActorRef, historyHttpService: ActorRef ) extends Actor {

  import context.dispatcher

  implicit val timeout = Timeout( 10000 milliseconds )

  def receive = {
    case request: TransferRequest =>
      val originalSender = sender
      val future = ( historyHttpService ? ( TransferActor convert request ) ).mapTo[ HistoryResponse ]

      future onSuccess {
        case response =>
          if ( response.getTransactionsCount > 0 )
            pipe( Future { TransferActor convert response } ) to originalSender
          else
            transferHttpService.tell( request, originalSender )
      }
      future onFailure {
        case exception => pipe( future ) to originalSender
      }
  }

}

object TransferActor {
  def apply( transferHttpService: ActorRef, historyHttpService: ActorRef ) =
    new TransferActor( transferHttpService: ActorRef, historyHttpService: ActorRef )

  private def convert( request: TransferRequest ): HistoryRequest = {
    HistoryRequest.newBuilder
      .setRequestHeader( request.getRequestHeader )
      .setHistorySpecification(
        HistorySpecification.newBuilder
          .setMerchantReference( request.getMerchantReference )
          .build )
      .build
  }
  private def convert( response: HistoryResponse ): TransactionResponse = {
    TransactionResponse.newBuilder
      .setResponseHeader( response.getResponseHeader )
      .setTransaction( response.getTransactions( 0 ) )
      .build
  }
}