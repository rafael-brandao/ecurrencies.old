package ecurrencies.libertyreserve.service

import scala.reflect.ClassTag

import akka.actor.{ Actor, ActorRef, Props, actorRef2Scala }

import com.google.protobuf.GeneratedMessage
import com.typesafe.config.Config

import spray.can.client.HttpClient
import spray.client.HttpConduit
import spray.httpx.SprayJsonSupport.{ sprayJsonMarshaller, sprayJsonUnmarshaller }
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.io.{ ClientSSLEngineProvider, IOExtension }

import ecurrencies.common.akka.ProtobuffParser
import ecurrencies.libertyreserve.domain._

class LibertyReserveSupervisor extends Actor {

  def receive = { case _ => }

  import LibertyReserveJsonProtocol._
  import Settings._

  implicit val sslEngineProvider = ClientSSLEngineProvider

  val ioBridge = IOExtension( context.system ).ioBridge()
  val httpClient = context.actorOf( Props( new HttpClient( ioBridge ) ) )

  implicit val conduit =
    context.actorOf(
      Props( new HttpConduit( httpClient, `api.domain`, `api.port`, sslEnabled = true ) )
    )

  val accountNameHttp = httpActor[ AccountNameRequest, AccountNameResponse ]( `api.account-name-uri` )
  val accountNameParser = parser[ AccountNameRequest ]( "account-name", accountNameHttp )

  val balanceHttp = httpActor[ BalanceRequest, BalanceResponse ]( `api.balance-uri` )
  val balanceParser = parser[ BalanceRequest ]( "balance", balanceHttp )

  val findTransationHttp = httpActor[ FindTransactionRequest, TransactionResponse ]( `api.find-transaction-uri` )
  val findTransactionParser = parser[ FindTransactionRequest ]( "find-transaction", findTransationHttp )

  val historyHttp = httpActor[ HistoryRequest, HistoryResponse ]( `api.history-uri` )
  val historyParser = parser[ HistoryRequest ]( "history", historyHttp )

  val transferHttp = httpActor[ TransferRequest, TransactionResponse ]( `api.transfer-uri` )
  val transferActor = actorOf { new TransferActor( transferHttp, historyHttp ) }
  val transferParser = parser[ TransferRequest ]( "transfer", transferActor )

  private def httpActor[ Request <: GeneratedMessage: ClassTag, Response <: GeneratedMessage: ClassTag ](
    uri: String )( implicit marshaller: Marshaller[ Request ], unmarshaller: Unmarshaller[ Response ] ) = {
    actorOf[ Request ] { HttpService[ Request, Response ]( uri ) }
  }

  private def parser[ Request <: GeneratedMessage: ClassTag ]( name: String, next: ActorRef ) = {
    implicit val n = Some( name )
    actorOf[ Request ] { ProtobuffParser[ Request ]( next ) }
  }

  private def actorOf[ Request <: GeneratedMessage: ClassTag ]( f: => Actor )( implicit name: Option[ String ] = None ) = {
    name match {
      case Some( name ) => context.actorOf( Props( f ).withRouter( router ), name )
      case None         => context.actorOf( Props( f ).withRouter( router ) )
    }
  }

}