package ecurrencies.libertyreserve.service

import ecurrencies.libertyreserve.domain._

import com.google.protobuf.GeneratedMessage

import spray.json._

object LibertyReserveJsonProtocol extends DefaultJsonProtocol {

  private implicit val accountFormat = jsonFormat2( AccountMessage )
  private implicit val balanceFormat = jsonFormat3( BalanceMessage )
  private implicit val errorFormat = jsonFormat2( ErrorMessage )
  private implicit val transactionFormat = jsonFormat15( TransactionMessage )

  private implicit val accountNameRequestFormat = jsonFormat5( AccountNameRequestMessage )
  private implicit val balanceRequestFormat = jsonFormat5( BalanceRequestMessage )
  private implicit val findTransactionRequestFormat = jsonFormat5( FindTransactionRequestMessage )
  private implicit val transferRequestFormat = jsonFormat12( TransferRequestMessage )

  private implicit val accountNameResponseFormat = jsonFormat5( AccountNameResponseMessage )
  private implicit val balanceResponseFormat = jsonFormat5( BalanceResponseMessage )
  private implicit val historyResponseFormat = jsonFormat6( HistoryResponseMessage )
  private implicit val transactionResponseFormat = jsonFormat5( TransactionResponseMessage )

  implicit object AccountNameRequestJsonWriter extends RootJsonFormat[ AccountNameRequest ] {
    def write( request: AccountNameRequest ) = {
      val requestMessage: AccountNameRequestMessage = request
      requestMessage.toJson
    }
    def read( value: JsValue ) = ???
  }

  implicit object BalanceRequestJson extends RootJsonFormat[ BalanceRequest ] {
    def write( request: BalanceRequest ) = {
      val requestMessage: BalanceRequestMessage = request
      requestMessage.toJson
    }
    def read( value: JsValue ) = ???
  }

  implicit object FindTransactionRequestJson extends RootJsonFormat[ FindTransactionRequest ] {
    def write( request: FindTransactionRequest ) = {
      val requestMessage: FindTransactionRequestMessage = request
      requestMessage.toJson
    }
    def read( value: JsValue ) = ???
  }

  implicit object HistoryRequestJson extends RootJsonFormat[ HistoryRequest ] {
    def write( request: HistoryRequest ) = {
      val requestMessage: HistoryRequestMessage = request
      import requestMessage._
      JsObject(
        "id" -> JsString( Id ),
        "account" -> JsString( Account ),
        "api" -> JsString( Api ),
        "token" -> JsString( Token ),
        "from" -> From.map { JsString( _ ) }.getOrElse { JsNull },
        "till" -> Till.map { JsString( _ ) }.getOrElse { JsNull },
        "currency" -> Currency.map { JsString( _ ) }.getOrElse { JsNull },
        "direction" -> Direction.map { JsString( _ ) }.getOrElse { JsNull },
        "relatedAccount" -> RelatedAccount.map { JsString( _ ) }.getOrElse { JsNull },
        "reference" -> Reference.map { JsString( _ ) }.getOrElse { JsNull },
        "source" -> Source.map { JsString( _ ) }.getOrElse { JsNull },
        "private" -> Private.map { JsString( _ ) }.getOrElse { JsNull },
        "amountFrom" -> AmountFrom.map { JsString( _ ) }.getOrElse { JsNull },
        "amountTo" -> AmountTo.map { JsString( _ ) }.getOrElse { JsNull },
        "page" -> Page.map { JsNumber( _ ) }.getOrElse { JsNull },
        "size" -> Size.map { JsNumber( _ ) }.getOrElse { JsNull }
      )
    }

    def read( value: JsValue ) = ???
  }

  implicit object TransferRequestJson extends RootJsonFormat[ TransferRequest ] {
    def write( request: TransferRequest ) = {
      val requestMessage: TransferRequestMessage = request
      requestMessage.toJson
    }
    def read( value: JsValue ) = ???
  }

  implicit object AccountNameResponseJson extends RootJsonFormat[ AccountNameResponse ] {
    def write( response: AccountNameResponse ) = ???
    def read( value: JsValue ) = {
      val responseMessage = value.convertTo[ AccountNameResponseMessage ]
      responseMessage
    }
  }

  implicit object BalanceResponseJson extends RootJsonFormat[ BalanceResponse ] {
    def write( response: BalanceResponse ) = ???
    def read( value: JsValue ) = {
      val responseMessage = value.convertTo[ BalanceResponseMessage ]
      responseMessage
    }
  }

  implicit object HistoryResponseJson extends RootJsonFormat[ HistoryResponse ] {
    def write( response: HistoryResponse ) = ???
    def read( value: JsValue ) = {
      val responseMessage = value.convertTo[ HistoryResponseMessage ]
      responseMessage
    }

  }

  implicit object TransactionResponseJson extends RootJsonFormat[ TransactionResponse ] {
    def write( response: TransactionResponse ) = ???
    def read( value: JsValue ) = {
      val responseMessage = value.convertTo[ TransactionResponseMessage ]
      responseMessage
    }
  }

}