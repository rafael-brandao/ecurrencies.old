package ecurrencies.libertyreserve

import scala.collection.JavaConversions.asJavaIterable
import scala.language.{ implicitConversions, postfixOps }

import com.google.protobuf.GeneratedMessage.Builder

import ecurrencies.libertyreserve.domain._
import ecurrencies.libertyreserve.util.Writer

package object service {

  private[ service ] implicit lazy val DateTimeFormatter = ecurrencies.libertyreserve.util.DateTimeFormatter

  // Implicit Conversions

  // AccountName Request
  private[ service ] implicit def fromDomainAccountNameRequest( request: AccountNameRequest ) =
    new AccountNameRequestMessage( request.getHeader, request.getPayload.getSearchAccountId )

  // Balance Request
  private[ service ] implicit def fromDomainBalanceRequest( request: BalanceRequest ) =
    new BalanceRequestMessage( request.getHeader, Option( request.getPayload.getCurrency ) map { _.toString.toLowerCase } )

  // Find Transaction Request
  private[ service ] implicit def fromDomainFindTransactionRequest( request: FindTransactionRequest ) =
    new FindTransactionRequestMessage( request.getHeader, request.getPayload.getBatchNumber )

  // History Request
  private[ service ] implicit def fromDomainHistoryRequest( request: HistoryRequest ) = {
    import request.{ getPayload => payload }
    val header: Tuple4[ String, String, String, String ] = request.getHeader
    val specification = payload.getSpecification
    import specification._

    HistoryRequestMessage( header _1, header _2, header _3, header _4,
      Option { getFrom } map { DateTimeFormatter print _ }, Option { getTill } map { DateTimeFormatter print _ },
      Option { getCurrency } map { _.toString }, Option { getTransactionDirection } map { _.toString },
      Option { getRelatedAccountId }, Option { getMerchantReference }, Option { getTransactionSource } map { _.toString },
      Option { getTransactionPrivacy } map { _.toString }, Option { getAmountFrom }, Option { getAmountTo },
      Option { payload.getPageIndex }, Option { payload.getPageSize } )
  }

  // Transfer Request
  private[ service ] implicit def fromDomainTransferRequest( request: TransferRequest ) = {
    val payload = request.getPayload
    import payload._

    new TransferRequestMessage( request.getHeader, getPayeeAccountId, getAmount, getCurrency.toString.toLowerCase, getMemo,
      getMerchantReference, getType.toString.toLowerCase, getPrivate, getPurpose.toString.toLowerCase )
  }

  // AccountName Response
  private[ service ] implicit def toDomainAccountNameResponse( response: AccountNameResponseMessage ) = {
    val payload =
      Writer( AccountNameResponse.Payload.newBuilder )
        .map { builder => response.Accounts.getOrElse( List.empty[ AccountMessage ] ).foldLeft( builder ) { _.setAccount( _ ) } }
        .builder.build

    AccountNameResponse.newBuilder.setHeader( response ).setPayload( payload ).build
  }

  // Balance Response
  private[ service ] implicit def toDomainBalanceResponse( response: BalanceResponseMessage ) = {
    val payload =
      Writer( BalanceResponse.Payload.newBuilder )
        .map {
          val balances: Iterable[ Balance ] = response.Balance
          _.addAllBalances( balances )
        }
        .builder.build

    BalanceResponse.newBuilder.setHeader( response ).setPayload( payload ).build
  }

  // History Response
  private[ service ] implicit def toDomainHistoryResponse( response: HistoryResponseMessage ) = {
    val payload =
      Writer( HistoryResponse.Payload.newBuilder() )
        .map { response.HasMore.foldLeft( _ ) { _.setHasMore( _ ) } }
        .map { builder => response.Transactions.getOrElse( List.empty[ TransactionMessage ] ).foldLeft( builder ) { _.addTransactions( _ ) } }
        .builder.build

    HistoryResponse.newBuilder.setHeader( response ).setPayload( payload ).build
  }

  // Transaction Response
  private[ service ] implicit def toDomainTransactionResponse( response: TransactionResponseMessage ) = {
    val payload =
      Writer( TransactionResponse.Payload.newBuilder() )
        .map { response.Transaction.foldLeft( _ ) { _.setTransaction( _ ) } }
        .builder.build

    TransactionResponse.newBuilder.setHeader( response ).setPayload( payload ).build
  }

  // RequestHeader
  private implicit def fromDomailRequestHeader( header: RequestHeader ): Tuple4[ String, String, String, String ] =
    ( header.getId, header.getApi.getAccountId, header.getApi.getName, header.getToken )

  // Response Header
  private implicit def toDomainResponseHeader( response: ResponseMessage ): ResponseHeader =
    Writer( ResponseHeader.newBuilder )
      .map { response.Id.foldLeft( _ ) { _.setId( _ ) } }
      .map { _.setTimestamp( DateTimeFormatter parseMillis response.Timestamp ) }
      .map { _.setStatus( ResponseStatus valueOf response.Status ) }
      .map { response.Error.foldLeft( _ ) { _.setError( _ ) } }
      .builder.build

  // Account
  private implicit def toDomainAccount( account: AccountMessage ): Account =
    Account.newBuilder
      .setAccountId( account.Account )
      .setAccountName( account.Name )
      .build

  // Balance
  private implicit def toDomainBalance( balance: Option[ BalanceMessage ] ): Iterable[ Balance ] = {
    import Balance.newBuilder
    import Currency._

    balance map { balance =>
      Seq(
        balance.USD.map { newBuilder.setCurrency( USD ).setBalance( _ ).build },
        balance.EURO.map { newBuilder.setCurrency( EURO ).setBalance( _ ).build },
        balance.GOLD.map { newBuilder.setCurrency( GOLD ).setBalance( _ ).build }
      ) flatten
    } getOrElse ( Seq.empty[ Balance ] )
  }

  // History Specification
  private implicit def toDomainHistorySpecification( request: HistoryRequestMessage ): HistorySpecification =
    Writer( HistorySpecification.newBuilder )
      .map { b => request.From.map { DateTimeFormatter parseMillis _ }.foldLeft( b ) { _ setFrom _ } }
      .map { b => request.Till.map { DateTimeFormatter parseMillis _ }.foldLeft( b ) { _ setTill _ } }
      .map { b => request.Currency.map { Currency valueOf _ }.foldLeft( b ) { _ setCurrency _ } }
      .map { b => request.Direction.map { TransactionDirection valueOf _ }.foldLeft( b ) { _ setTransactionDirection _ } }
      .map { request.RelatedAccount.foldLeft( _ ) { _ setRelatedAccountId _ } }
      .map { request.Reference.foldLeft( _ ) { _ setMerchantReference _ } }
      .map { b => request.Source.map { TransactionSource valueOf _ }.foldLeft( b ) { _ setTransactionSource _ } }
      .map { b => request.Private.map { TransactionPrivacy valueOf _ }.foldLeft( b ) { _ setTransactionPrivacy _ } }
      .map { request.AmountFrom.foldLeft( _ ) { _ setAmountFrom _ } }
      .map { request.AmountTo.foldLeft( _ ) { _ setAmountTo _ } }
      .builder.build

  // Transaction
  private implicit def toDomainTransaction( transaction: TransactionMessage ): Transaction = {
    import transaction._
    Transaction.newBuilder
      .setBatchNumber( Batch )
      .setDate( DateTimeFormatter parseMillis ( Date ) )
      .setMerchantReference( Reference )
      .setType( TransactionType.valueOf( Type.toUpperCase ) )
      .setPayerAccountId( Payer )
      .setPayerAccountName( PayerName )
      .setPayeeAccountId( Payee )
      .setPayeeAccountName( PayeeName )
      .setCurrency( ecurrencies.libertyreserve.domain.Currency.valueOf( Currency.toInt ) )
      .setAmount( Amount.toString )
      .setFee( Fee.toString )
      .setClosingBalance( Balance.toString )
      .setMemo( Memo )
      .setPrivate( Private )
      .setSource( TransactionSource.valueOf( Source.toInt ) )
      .build
  }

  // Error
  private implicit def fromDomainError( error: Error ): ErrorMessage =
    ErrorMessage( error.getCode, Option( error.getMessage ) )

  private implicit def toDomainError( error: ErrorMessage ): Error =
    Writer( Error.newBuilder )
      .map { _.setCode( error.ErrorCode ) }
      .map { error.ErrorMessage.foldLeft( _ ) { _.setMessage( _ ) } }
      .builder.build

}

package service {

  sealed trait RequestMessage { val Id: String; val Account: String; val Api: String; val Token: String }

  case class AccountNameRequestMessage( Id: String, Account: String, Api: String, Token: String, Search: String )
      extends RequestMessage {
    def this( header: Tuple4[ String, String, String, String ], Search: String ) = this( header _1, header _2, header _3, header _4, Search )
  }

  case class BalanceRequestMessage( Id: String, Account: String, Api: String, Token: String, CurrencyId: Option[ String ] )
      extends RequestMessage {
    def this( header: Tuple4[ String, String, String, String ], CurrencyId: Option[ String ] ) =
      this( header _1, header _2, header _3, header _4, CurrencyId )
  }

  case class FindTransactionRequestMessage( Id: String, Account: String, Api: String, Token: String, Batch: Long )
      extends RequestMessage {
    def this( header: Tuple4[ String, String, String, String ], Batch: Long ) = this( header _1, header _2, header _3, header _4, Batch )
  }

  case class HistoryRequestMessage(
    Id: String, Account: String, Api: String, Token: String,
    From: Option[ String ] = None, Till: Option[ String ] = None, Currency: Option[ String ] = None,
    Direction: Option[ String ] = None, RelatedAccount: Option[ String ] = None, Reference: Option[ String ] = None,
    Source: Option[ String ] = None, Private: Option[ String ] = None, AmountFrom: Option[ String ] = None,
    AmountTo: Option[ String ] = None, Page: Option[ Int ] = None, Size: Option[ Int ] = None ) extends RequestMessage

  case class TransferRequestMessage(
      Id: String, Account: String, Api: String, Token: String,
      Payee: String, Amount: String, Currency: String, Memo: String,
      Reference: String, Type: String, Private: Boolean, Purpose: String ) extends RequestMessage {

    def this( header: Tuple4[ String, String, String, String ],
              payee: String, amount: String, Currency: String, Memo: String,
              Reference: String, Type: String, Private: Boolean, Purpose: String ) =
      this( header _1, header _2, header _3, header _4, payee, amount, Currency, Memo, Reference, Type, Private, Purpose )
  }

  sealed trait ResponseMessage { val Id: Option[ String ]; val Timestamp: String; val Status: Int; val Error: Option[ ErrorMessage ] }

  case class AccountNameResponseMessage(
    Id: Option[ String ], Timestamp: String, Status: Int, Error: Option[ ErrorMessage ], Accounts: Option[ List[ AccountMessage ] ] )
      extends ResponseMessage

  case class BalanceResponseMessage(
    Id: Option[ String ], Timestamp: String, Status: Int, Error: Option[ ErrorMessage ], Balance: Option[ BalanceMessage ] )
      extends ResponseMessage

  case class HistoryResponseMessage(
    Id: Option[ String ], Timestamp: String, Status: Int, Error: Option[ ErrorMessage ],
    HasMore: Option[ Boolean ], Transactions: Option[ List[ TransactionMessage ] ] )
      extends ResponseMessage

  case class TransactionResponseMessage(
    Id: Option[ String ], Timestamp: String, Status: Int, Error: Option[ ErrorMessage ], Transaction: Option[ TransactionMessage ] )
      extends ResponseMessage

  case class AccountMessage( Account: String, Name: String )

  case class BalanceMessage( USD: Option[ String ] = None, EURO: Option[ String ] = None, GOLD: Option[ String ] = None )

  case class ErrorMessage( ErrorCode: Int, ErrorMessage: Option[ String ] )

  case class TransactionMessage(
    Batch: Long, Date: String, Reference: String, Type: String, Payer: String,
    PayerName: String, Payee: String, PayeeName: String, Currency: Int, Amount: BigDecimal,
    Fee: BigDecimal, Balance: BigDecimal, Memo: String, Private: Boolean, Source: Int )
}