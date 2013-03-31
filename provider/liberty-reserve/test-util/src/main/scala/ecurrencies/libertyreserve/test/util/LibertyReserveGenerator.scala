package ecurrencies.libertyreserve.test.util

import java.lang.System.currentTimeMillis

import scala.math.BigDecimal.RoundingMode.HALF_UP
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.Random

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.GeneratedMessage.Builder

import org.joda.time.{ DateTime, DateTimeZone }
import DateTimeZone.UTC

import ecurrencies.libertyreserve.domain._
import ecurrencies.libertyreserve.util._
import ecurrencies.libertyreserve.util.RequestHeaderGenerator
import ecurrencies.libertyreserve.util.RequestHeaderGenerator._

object Valid extends Enumeration {
  type Valid = Value
  val Yes, No, Any = Value
}

object LibertyReserveGenerator {

  import Valid._

  private lazy val empty = ""
  private lazy val random = Random
  private lazy val accountIdFormat = "%c%d"
  private lazy val accountIdChars = Array( 'M', 'U', 'X' )

  implicit def class2AccountNameRequest( clazz: Class[ AccountNameRequest ] ): AccountNameRequest = {
    import AccountNameRequest.Payload

    implicit val payload = next[ Payload, Payload.Builder ]() { _.setSearchAccountId( accountId ) }

    nextRequest[ AccountNameRequest, Payload ]
  }

  implicit def class2BalanceRequest( clazz: Class[ BalanceRequest ] ): BalanceRequest = {
    import BalanceRequest.Payload

    val optional = Array( { builder: Payload.Builder => builder.setCurrency( nextEnumType( classOf[ Currency ] ) ) } )

    implicit val payload = next[ Payload, Payload.Builder ]( optional )()

    nextRequest[ BalanceRequest, Payload ]
  }

  implicit def class2FindTransactionRequest( clazz: Class[ FindTransactionRequest ] ): FindTransactionRequest = {
    import FindTransactionRequest.Payload

    implicit val payload = next[ Payload, Payload.Builder ]() { _.setBatchNumber( batchNumber ) }

    nextRequest[ FindTransactionRequest, Payload ]
  }

  implicit def class2HistoryRequest( clazz: Class[ HistoryRequest ] ): HistoryRequest = {
    import HistoryRequest.Payload

    val optional = Array(
      { builder: Payload.Builder => builder.setPageIndex( random.nextInt( 30 ) + 1 ) },
      { builder: Payload.Builder => builder.setPageSize( random.nextInt( 20 ) + 1 ) }
    )

    implicit val payload = next[ Payload, Payload.Builder ]( optional ) { _.setSpecification( historySpecification ) }

    nextRequest[ HistoryRequest, Payload ]
  }

  implicit def class2TransferRequest( clazz: Class[ TransferRequest ] ): TransferRequest = {
    import TransferRequest.Payload

    implicit val payload = next[ Payload, Payload.Builder ]() {
      _.setPayeeAccountId( accountId )
        .setAmount( amount( 50000 ) )
        .setCurrency( nextEnumType( classOf[ Currency ] ) )
        .setMemo( "Some transaction" )
        .setMerchantReference( id )
        .setType( nextEnumType( classOf[ TransactionType ] ) )
        .setPrivate( nextBoolean )
        .setPurpose( nextEnumType( classOf[ PaymentPurpose ] ) )
    }
    nextRequest[ TransferRequest, Payload ]
  }

  implicit def class2AccountNameResponse( clazz: Class[ AccountNameResponse ] ): AccountNameResponse = ( clazz, Any )

  implicit def class2AccountNameResponse( tuple: Tuple2[ Class[ AccountNameResponse ], Valid ] ): AccountNameResponse = {
    import AccountNameResponse.Payload

    nextResponse[ AccountNameResponse, Payload ]( tuple._2 ) {
      next[ Payload, Payload.Builder ]() { _.setAccount( account ) }
    }
  }

  implicit def class2BalanceResponse( clazz: Class[ BalanceResponse ] ): BalanceResponse = ( clazz, Any )

  implicit def class2BalanceResponse( tuple: Tuple2[ Class[ BalanceResponse ], Valid ] ): BalanceResponse = {
    import BalanceResponse.Payload

    nextResponse[ BalanceResponse, Payload ]( tuple._2 ) {
      next[ Payload, Payload.Builder ]() {
        Currency.values.foldLeft( _ ) { ( b, currency ) => b.addBalances( balance( currency ) ) }
      }
    }
  }

  implicit def class2HistoryResponse( clazz: Class[ HistoryResponse ] ): HistoryResponse = ( clazz, Any )

  implicit def class2HistoryResponse( tuple: Tuple2[ Class[ HistoryResponse ], Valid ] ): HistoryResponse =
    ( tuple._1, tuple._2, Some( random.nextInt( 20 ) ) )

  implicit def class2HistoryResponse2( tuple: Tuple2[ Class[ HistoryResponse ], Option[ Int ] ] ): HistoryResponse =
    ( tuple._1, Yes, tuple._2 )

  implicit private def class2HistoryResponse( tuple: Tuple3[ Class[ HistoryResponse ], Valid, Option[ Int ] ] ): HistoryResponse = {
    import HistoryResponse.Payload

    nextResponse[ HistoryResponse, Payload ]( tuple._2 ) {
      next[ Payload, Payload.Builder ]() {
        builder =>
          val numberOfTransactions =
            tuple._3 match {
              case Some( value ) if ( value >= 0 && value <= 20 ) => value
              case _ => 0
            }
          val range = 0 until numberOfTransactions

          range.end match {
            case 0 => builder.setHasMore( false )
            case _ =>
              range.map( i => transaction ).foldLeft( builder ) { _.addTransactions( _ ) }.setHasMore( nextBoolean )
          }
      }
    }
  }

  implicit def class2TransactionResponse( clazz: Class[ TransactionResponse ] ): TransactionResponse = ( clazz, Any )

  implicit def class2TransactionResponse( tuple: Tuple2[ Class[ TransactionResponse ], Option[ Boolean ] ] ): TransactionResponse =
    ( tuple._1, Yes, tuple._2 )

  implicit def class2TransactionResponse2( tuple: Tuple2[ Class[ TransactionResponse ], Valid ] ): TransactionResponse =
    random.nextFloat match {
      case value if value <= 0.8 => ( tuple._1, tuple._2, Some( true ) )
      case _                     => ( tuple._1, tuple._2, Some( false ) )
    }

  implicit def class2TransactionResponse( tuple: Tuple3[ Class[ TransactionResponse ], Valid, Option[ Boolean ] ] ): TransactionResponse = {
    import TransactionResponse.Payload

    nextResponse[ TransactionResponse, Payload ]( tuple._2 ) {
      next[ Payload, Payload.Builder ]() {
        builder =>
          tuple._3 match {
            case Some( true ) => builder.setTransaction( transaction )
            case _            => builder
          }
      }
    }
  }

  private implicit def requestHeader[ Payload <: GeneratedMessage ]( payload: Payload )(
    implicit headerGenerator: RequestHeaderGenerator[ Payload ] ): RequestHeader = {

    val requestHeader: RequestHeader = ( payload, api, "password".toCharArray )
    requestHeader
  }

  private def api(): Api = Api.newBuilder.setAccountId( accountId ).setName( "MyApi_" + id ).build

  private def account(): Account = Account.newBuilder
    .setAccountId( accountId )
    .setAccountName( empty )
    .build

  private def balance(): Balance = balance( nextEnumType( classOf[ Currency ] ) )

  private def balance( currency: Currency ): Balance = Balance.newBuilder
    .setCurrency( currency )
    .setBalance( amount( 50000 ) )
    .build

  private def historySpecification() = {
    import HistorySpecification.Builder

    lazy val minimumYearsOffset = random.nextInt( 10 )
    lazy val maximumYearsOffset = random.nextInt( 10 ) + minimumYearsOffset + 1
    lazy val amountFrom = random.nextInt( 10000 )
    lazy val amountTo = random.nextInt( 10000 ) + amountFrom + 1

    val optional = Array(
      { builder: Builder => builder.setFrom( stripTimestampMillis( timestamp( maximumYearsOffset ) ) ) },
      { builder: Builder => builder.setCurrency( nextEnumType( classOf[ Currency ] ) ) },
      { builder: Builder => builder.setTransactionDirection( nextEnumType( classOf[ TransactionDirection ] ) ) },
      { builder: Builder => builder.setRelatedAccountId( accountId ) },
      { builder: Builder => builder.setMerchantReference( id ) },
      { builder: Builder => builder.setTransactionPrivacy( nextEnumType( classOf[ TransactionPrivacy ] ) ) },
      { builder: Builder => builder.setAmountFrom( amount( amountFrom ) ) },
      { builder: Builder => builder.setAmountTo( amount( amountTo ) ) }
    )
    next[ HistorySpecification, Builder ]( optional ) {
      _.setTill( stripTimestampMillis( timestamp( minimumYearsOffset, maximumYearsOffset ) ) )
    }
  }

  private def transaction() = Transaction.newBuilder
    .setBatchNumber( batchNumber )
    .setDate( stripTimestampMillis( timestamp( 15 ) ) )
    .setMerchantReference( id )
    .setType( nextEnumType( classOf[ TransactionType ] ) )
    .setPayerAccountId( accountId )
    .setPayerAccountName( empty )
    .setPayeeAccountId( accountId )
    .setPayeeAccountName( empty )
    .setCurrency( nextEnumType( classOf[ Currency ] ) )
    .setAmount( amount( 10000 ) )
    .setFee( amount( 2 ) )
    .setClosingBalance( amount( 1000, 50000 ) )
    .setMemo( empty )
    .setPrivate( nextBoolean )
    .setSource( nextEnumType( classOf[ TransactionSource ] ) )
    .build

  private def responseHeader( valid: Valid = Any ) = {
    import ResponseHeader.Builder
    def randomStatus() =
      random.nextFloat match {
        case value if value <= 0.85 => ResponseStatus.SUCCESS // 85%
        case value if value <= 0.95 => ResponseStatus.ERROR // 10 %
        case _                      => ResponseStatus.NONE // 5%
      }

    val status = valid match {
      case Any => randomStatus
      case Yes => ResponseStatus.SUCCESS
      case No  => ResponseStatus.ERROR
    }

    next[ ResponseHeader, Builder ]() { builder =>
      locally {
        import builder._
        setStatus( status )
        if ( getStatus != ResponseStatus.SUCCESS )
          setError( error )
        setId( id )
        setTimestamp( stripTimestampMillis( currentTimeMillis ) )
      }
    }
  }

  private def error() = Error.newBuilder.setCode( random.nextInt( 999 ) + 1 ).build

  private def accountId() = accountIdFormat.format( accountIdChars( random.nextInt( 3 ) ), random.nextInt( 9999999 ) + 1 )

  private def amount( maximum: Long ): String = amount( 0, maximum )

  private def amount[ T <% Double ]( minimum: T, maximum: T ): String =
    BigDecimal( nextDecimalNumber( minimum, maximum ) ).setScale( 4, HALF_UP ).toString

  private def batchNumber(): Long =
    random.nextLong match {
      case value if value >= 0 => value
      case value               => value * ( -1 )
    }

  private def id(): String = generateId( nextNumber( 0, 999999999 ) )

  private def timestamp( maximumYearsOffset: Int ): Long = timestamp( 0, maximumYearsOffset )

  private def timestamp[ T <% Int ]( minimumYearsOffset: T, maximumYearsOffset: T ): Long = {
    val now = DateTime.now( UTC )
    val min = now.minusYears( random.nextInt( maximumYearsOffset - minimumYearsOffset ) )
    nextNumber( min.getMillis, now.getMillis )
  }

  private def stripTimestampMillis[ T <% Long ]( timestamp: T ): Long = 1000 * ( timestamp / 1000 )

  private def nextEnumType[ T <: Enum[ T ] ]( enumClass: Class[ T ] ): T = {
    val values = enumClass.getEnumConstants
    values( random.nextInt( values.length ) )
  }

  private def nextNumber[ L <% Long ]( minimum: L, maximum: L ): Long =
    minimum + Math.round( random.nextFloat * ( maximum - minimum ) )

  private def nextDecimalNumber[ D <% Double ]( minimum: D, maximum: D ): Double =
    minimum + random.nextFloat * ( maximum - minimum )

  private def nextBoolean() = random.nextBoolean

  private def next[ M <: GeneratedMessage: ClassTag, B <: Builder[ B ]: ClassTag ](
    optionalEntries: Seq[ B => B ] = Seq[ B => B ]() )( required: B => B = { b: B => b } ): M = {

    ( required +: ( optionalEntries filter { b => random.nextFloat < random.nextFloat } ) )
      .foldLeft( nextBuilder[ M ].asInstanceOf[ B ] ) {
        ( b, f ) => f( b )
      }.build.asInstanceOf[ M ]

  }

  private def nextResponse[ M <: GeneratedMessage: ClassTag, P <: GeneratedMessage: ClassTag ](
    valid: Valid = Any )( payloadFunction: => P ): M = {

    val header = responseHeader( valid )

    ProtobufBuilder[ M ]
      .set( "header" )( header )
      .set( "payload" ) {
        header.getStatus match {
          case ResponseStatus.SUCCESS => payloadFunction
          case _                      => { ProtobufBuilder[ P ].build } // empty Payload
        }
      }.build
  }

  private def nextRequest[ M <: GeneratedMessage: ClassTag, P <: GeneratedMessage: ClassTag ](
    implicit payload: P, headerGenerator: RequestHeaderGenerator[ P ] ): M =
    ProtobufBuilder[ M ]
      .set[ RequestHeader ]( "header" )( payload )
      .set[ P ]( "payload" )( payload )
      .build

  private def nextBuilder[ M <: GeneratedMessage: ClassTag ]() =
    implicitly[ ClassTag[ M ] ].runtimeClass
      .getDeclaredMethod( "newBuilder" )
      .invoke( null ).asInstanceOf[ Builder[ Builder[ _ <: AnyRef ] ] ]

  private class ProtobufBuilder[ M <: GeneratedMessage: ClassTag ] private (
      private val builder: Builder[ Builder[ _ <: AnyRef ] ] ) {

    private val builderClass = builder.getClass

    def set[ T <: Object: ClassTag ]( field: String )( f: => T ): ProtobufBuilder[ M ] =
      new ProtobufBuilder(
        builderClass
          .getDeclaredMethod( "set" + field.capitalize, implicitly[ ClassTag[ T ] ].runtimeClass )
          .invoke( builder, f ).asInstanceOf[ Builder[ Builder[ _ <: AnyRef ] ] ]
      )

    def build(): M = builderClass.getDeclaredMethod( "build" ).invoke( builder ).asInstanceOf[ M ]
  }

  private object ProtobufBuilder {
    def apply[ M <: GeneratedMessage: ClassTag ] = new ProtobufBuilder( nextBuilder[ M ] )
  }

}