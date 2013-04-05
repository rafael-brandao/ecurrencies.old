package ecurrencies.libertyreserve.service

import scala.reflect.ClassTag
import scala.concurrent.duration.DurationLong
import scala.language.postfixOps

import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}

import com.google.protobuf.GeneratedMessage

import spray.can.client.HttpClient
import spray.client.HttpConduit
import spray.httpx.SprayJsonSupport.{sprayJsonMarshaller, sprayJsonUnmarshaller}
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.io.{ClientSSLEngineProvider, IOExtension}

import ecurrencies.common.akka.ProtobuffParser
import ecurrencies.libertyreserve.domain._

class LibertyReserveSupervisor extends Actor {

  def receive = {
    case _ =>
  }

  import LibertyReserveJsonProtocol._
  import Settings._
  import SupervisorStrategy.Restart

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Restart
    }

  implicit val sslEngineProvider = ClientSSLEngineProvider

  val ioBridge = IOExtension(context.system).ioBridge()
  val httpClient = context.actorOf(Props(new HttpClient(ioBridge)))

  implicit val conduit =
    context.actorOf(
      Props(new HttpConduit(httpClient, `api.domain`, `api.port`, sslEnabled = true))
    )

  val accountNameHttp = httpActor[AccountNameRequest, AccountNameResponse](`api.account-name-uri`)
  val accountNameParser = parser[AccountNameRequest]("account-name", accountNameHttp)

  val balanceHttp = httpActor[BalanceRequest, BalanceResponse](`api.balance-uri`)
  val balanceParser = parser[BalanceRequest]("balance", balanceHttp)

  val findTransationHttp = httpActor[FindTransactionRequest, TransactionResponse](`api.find-transaction-uri`)
  val findTransactionParser = parser[FindTransactionRequest]("find-transaction", findTransationHttp)

  val historyHttp = httpActor[HistoryRequest, HistoryResponse](`api.history-uri`)
  val historyParser = parser[HistoryRequest]("history", historyHttp)

  val transferHttp = httpActor[TransferRequest, TransactionResponse](`api.transfer-uri`)
  val transferParser = parser[TransferRequest]("transfer", transferHttp)

  private def httpActor[Request <: GeneratedMessage : ClassTag, Response <: GeneratedMessage : ClassTag]
  (uri: String)(implicit marshaller: Marshaller[Request], unmarshaller: Unmarshaller[Response]) = {

    actorOf[Request]() {
      HttpService[Request, Response](uri)
    }
  }

  private def parser[Request <: GeneratedMessage : ClassTag](name: String, next: ActorRef) =
    actorOf[Request](Some(name)) {
      ProtobuffParser[Request](next)
    }


  private def actorOf[Request <: GeneratedMessage : ClassTag](name: Option[String] = None)(f: => Actor) = {
    val props = Props(f).withRouter(router)
    name.map {
      context.actorOf(props, _)
    }.getOrElse {
      context.actorOf(props)
    }
  }

}