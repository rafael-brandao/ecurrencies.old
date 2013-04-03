package ecurrencies.libertyreserve.util

import scala.language.{ implicitConversions, postfixOps }

import com.google.protobuf.GeneratedMessage

import ecurrencies.libertyreserve.domain._

sealed trait RequestHeaderGenerator[ M <: GeneratedMessage ] {

  protected val listFunction: M => Seq[ String ] = message => Seq.empty[ String ]

  def create( payload: M, api: Api, securityWord: Array[ Char ], id: String = generateId( now() ), date: Long = now() ): RequestHeader = {
    RequestHeader.newBuilder
      .setId( id )
      .setApi( api )
      .setToken( TokenBuilder build ( securityWord, id, listFunction( payload ), date ) )
      .build
  }
}

object RequestHeaderGenerator {

  implicit object AccountNameRequestHeaderGenerator extends RequestHeaderGenerator[ AccountNameRequest.Payload ]

  implicit object BalanceRequestHeaderGenerator extends RequestHeaderGenerator[ BalanceRequest.Payload ]

  implicit object FindTransactionHeaderGenerator extends RequestHeaderGenerator[ FindTransactionRequest.Payload ] {
    override implicit val listFunction: FindTransactionRequest.Payload => Seq[ String ] =
      payload => Seq( payload.getBatchNumber.toString )
  }

  implicit object HistoryRequestHeaderGenerator extends RequestHeaderGenerator[ HistoryRequest.Payload ] {
    override implicit val listFunction: HistoryRequest.Payload => Seq[ String ] =
      payload =>
        Seq(
          DateTimeFormatter print payload.getSpecification.getFrom,
          DateTimeFormatter print payload.getSpecification.getTill
        )
  }

  implicit object TransferRequestGenerator extends RequestHeaderGenerator[ TransferRequest.Payload ] {
    override implicit val listFunction: TransferRequest.Payload => Seq[ String ] =
      payload =>
        Seq(
          payload.getMerchantReference,
          payload.getPayeeAccountId,
          payload.getCurrency.toString.toLowerCase,
          payload.getAmount
        )
  }

  implicit def requestHeaderFor[ M <: GeneratedMessage ]( tuple: (M, Api, Array[Char], String, Long) )( implicit generator: RequestHeaderGenerator[ M ] ): RequestHeader =
    generator.create( tuple _1, tuple _2, tuple _3, tuple _4, tuple _5 )

  implicit def requestHeaderFor[ M <: GeneratedMessage ]( tuple: (M, Api, Array[Char]) )( implicit generator: RequestHeaderGenerator[ M ] ): RequestHeader =
    generator.create( tuple _1, tuple _2, tuple _3 )

}