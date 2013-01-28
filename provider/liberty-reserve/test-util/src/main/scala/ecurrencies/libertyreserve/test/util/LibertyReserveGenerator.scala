package ecurrencies.libertyreserve.test.util

import java.lang.System.currentTimeMillis
import java.text.DecimalFormat

import scala.math.BigDecimal.RoundingMode.HALF_UP
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.util.Random

import org.joda.time.{ DateTime, DateTimeZone }
import DateTimeZone.UTC

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.GeneratedMessage.Builder

import ecurrencies.libertyreserve.domain._

object LibertyReserveGenerator {

  object Valid extends Enumeration {
    type Valid = Value
    val Yes, No, Any = Value
  }
  import Valid._

  lazy val empty = ""
  lazy val random = Random
  lazy val idNumberFormat = new DecimalFormat( "00000000000000000000" )
  lazy val accountIdFormat = "%c%d"
  lazy val accountIdChars = Array( 'M', 'U', 'X' )

  def accountNameRequest() = {
    import AccountNameRequest.Builder
    nextRequest[ AccountNameRequest, Builder ]() { builder =>
      builder.setSearchAccountId( accountId )
    }
  }

  def balanceRequest() = {
    import BalanceRequest.Builder
    val optional = Array( { builder: Builder => builder.setCurrency( nextEnumType( classOf[ Currency ] ) ) } )
    nextRequest[ BalanceRequest, Builder ]( optional )()
  }

  def findTransactionRequest() = {
    import FindTransactionRequest.Builder
    nextRequest[ FindTransactionRequest, Builder ]() { builder =>
      builder.setBatchNumber( batchNumber )
    }
  }

  def historyRequest() = {
    import HistoryRequest.Builder
    val optional = Array(
      { builder: Builder => builder.setPageIndex( random.nextInt( 30 ) + 1 ) },
      { builder: Builder => builder.setPageSize( random.nextInt( 20 ) + 1 ) }
    )
    nextRequest[ HistoryRequest, Builder ]( optional ) { builder =>
      builder.setHistorySpecification( historySpecification )
    }
  }

  def transferRequest() =
    nextRequest[ TransferRequest, TransferRequest.Builder ]() { builder =>
      builder
        .setPayeeAccountId( accountId )
        .setAmount( amount( 50000 ) )
        .setCurrency( nextEnumType( classOf[ Currency ] ) )
        .setMemo( "Some transaction" )
        .setMerchantReference( id )
        .setType( nextEnumType( classOf[ TransactionType ] ) )
        .setPrivate( nextBoolean )
        .setPurpose( nextEnumType( classOf[ PaymentPurpose ] ) )
    }

  def accountNameResponse() = {
    import AccountNameResponse.Builder
    nextResponse[ AccountNameResponse, Builder ]() { builder =>
      builder.setAccount( account )
    }
  }

  def balanceResponse() = {
    import BalanceResponse.Builder
    nextResponse[ BalanceResponse, Builder ]() { builder =>
      Currency.values.foldLeft( builder ) { ( b, currency ) =>
        b.addBalances( balance( currency ) )
      }
    }
  }

  def historyResponse( nrTransactions: Option[ Int ] = None ) = {
    import HistoryResponse.Builder
    var valid: Valid = Any
    var numberOfTransactions = random.nextInt( 20 )
    nrTransactions match {
      case Some( value ) =>
        if ( value >= 0 && value <= 20 ) {
          valid = Yes
          numberOfTransactions = value
        } else if ( value < 0 ) {
          valid = No
        }
      case _ =>
    }
    lazy val range = 0 until numberOfTransactions
    nextResponse[ HistoryResponse, Builder ]( valid ) { builder =>
      range.end match {
        case 0 => builder.setHasMore( false )
        case _ =>
          range.map( i => transaction ).foldLeft( builder ) { ( b, t ) =>
            b.addTransactions( t )
          }.setHasMore( nextBoolean )
      }
    }
  }

  def transactionResponse() = {
    import TransactionResponse.Builder
    nextResponse[ TransactionResponse, Builder ]() { builder =>
      builder.setTransaction( transaction )
    }
  }

  def requestHeader() = RequestHeader.newBuilder
    .setAccount( accountId )
    .setApi( random.nextLong.toHexString )
    .build

  def account() = Account.newBuilder
    .setAccountId( accountId )
    .setAccountName( empty )
    .build

  def balance(): Balance = balance( nextEnumType( classOf[ Currency ] ) )

  def balance( currency: Currency ): Balance = Balance.newBuilder
    .setCurrency( currency )
    .setBalance( amount( 50000 ) )
    .build

  def historySpecification() = {
    import HistorySpecification.Builder

    lazy val minimumYearsOffset = random.nextInt( 10 )
    lazy val maximumYearsOffset = random.nextInt( 10 ) + minimumYearsOffset + 1
    lazy val amountFrom = random.nextInt( 10000 )
    lazy val amountTo = random.nextInt( 10000 ) + amountFrom + 1

    val optional = Array(
      { builder: Builder => builder.setFrom( stripTimestampMillis( timestamp( maximumYearsOffset ) ) ) },
      { builder: Builder => builder.setTill( stripTimestampMillis( timestamp( minimumYearsOffset, maximumYearsOffset ) ) ) },
      { builder: Builder => builder.setCurrency( nextEnumType( classOf[ Currency ] ) ) },
      { builder: Builder => builder.setTransactionDirection( nextEnumType( classOf[ TransactionDirection ] ) ) },
      { builder: Builder => builder.setRelatedAccountId( accountId ) },
      { builder: Builder => builder.setMerchantReference( id ) },
      { builder: Builder => builder.setTransactionPrivacy( nextEnumType( classOf[ TransactionPrivacy ] ) ) },
      { builder: Builder => builder.setAmountFrom( amount( amountFrom ) ) },
      { builder: Builder => builder.setAmountTo( amount( amountTo ) ) }
    )
    nextMessage[ HistorySpecification, Builder ]( optional )()
  }

  def transaction() = Transaction.newBuilder
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

  def responseHeader( valid: Valid = Any ) = {
    import ResponseHeader.Builder
    def randomStatus() = {
      if ( random.nextFloat < 0.8 )
        ResponseStatus.SUCCESS
      else if ( random.nextFloat < 0.7 )
        ResponseStatus.ERROR
      else ResponseStatus.NONE
    }

    val status = valid match {
      case Any => randomStatus
      case Yes => ResponseStatus.SUCCESS
      case No  => ResponseStatus.ERROR
    }

    nextMessage[ ResponseHeader, Builder ]() { builder =>
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

  def error() = Error.newBuilder.setCode( random.nextInt( 999 ) + 1 ).build

  private def accountId() = {
    accountIdFormat.format( accountIdChars( random.nextInt( 3 ) ), random.nextInt( 9999999 ) + 1 )
  }

  private def amount( maximum: Long ): String = amount( 0, maximum )

  private def amount[ T <% Double ]( minimum: T, maximum: T ): String =
    BigDecimal( nextDecimalNumber( minimum, maximum ) ).setScale( 4, HALF_UP ).toString

  private def batchNumber(): Long =
    random.nextLong match {
      case value if value >= 0 => value
      case value               => value * ( -1 )
    }

  private def id(): String = {
    idNumberFormat.format( nextNumber( 0, 999999999999999999L ) )
  }

  private def timestamp( maximumYearsOffset: Int ): Long = {
    timestamp( 0, maximumYearsOffset )
  }

  private def timestamp[ T <% Int ]( minimumYearsOffset: T, maximumYearsOffset: T ): Long = {
    val now = DateTime.now( UTC )
    val min = now.minusYears( random.nextInt( maximumYearsOffset - minimumYearsOffset ) )
    nextNumber( min.getMillis, now.getMillis )
  }

  private def stripTimestampMillis[ T <% Long ]( timestamp: T ): Long = {
    1000 * ( timestamp / 1000 )
  }

  private def nextEnumType[ T <: Enum[ T ] ]( enumClass: Class[ T ] ): T = {
    val values = enumClass.getEnumConstants
    values( random.nextInt( values.length ) )
  }

  private def nextNumber[ T <% Long ]( minimum: T, maximum: T ) = {
    minimum + Math.round( random.nextFloat * ( maximum - minimum ) )
  }

  private def nextDecimalNumber[ T <% Double ]( minimum: T, maximum: T ) = {
    minimum + random.nextFloat * ( maximum - minimum )
  }

  private def nextBoolean() = random.nextBoolean

  private def next[ M <: GeneratedMessage: ClassTag, B <: Builder[ B ]: ClassTag ](
    builder: B,
    optionalEntries: Seq[ B => B ] = Seq[ B => B ]() )( required: B => B = { b: B => b } ): M = {

    ( required +: ( optionalEntries filter { b => random.nextFloat < random.nextFloat } ) ).foldLeft( builder ) {
      ( b, f ) => f( b )
    }.build.asInstanceOf[ M ]

  }

  private def nextMessage[ M <: GeneratedMessage: ClassTag, B <: Builder[ B ]: ClassTag ](
    optionalEntries: Seq[ B => B ] = Seq[ B => B ]() )( required: B => B = { b: B => b } ): M = {

    next( nextBuilder[ M, B ], optionalEntries )( required )
  }

  private def nextResponse[ M <: GeneratedMessage: ClassTag, B <: Builder[ B ]: ClassTag ](
    valid: Valid = Any, optionalEntries: Seq[ B => B ] = Seq[ B => B ]() )( required: B => B = { b: B => b } ): M = {

    lazy val rh = responseHeader( valid )
    lazy val builder = implicitly[ ClassTag[ B ] ].runtimeClass
      .getDeclaredMethod( "setResponseHeader", classOf[ ResponseHeader ] )
      .invoke( nextBuilder[ M, B ], rh )
      .asInstanceOf[ B ]
    rh.getStatus() match {
      case ResponseStatus.SUCCESS => next( builder, optionalEntries )( required )
      case _                      => builder.build.asInstanceOf[ M ]
    }
  }

  private def nextRequest[ M <: GeneratedMessage: ClassTag, B <: Builder[ B ]: ClassTag ](
    optionalEntries: Seq[ B => B ] = Seq[ B => B ]() )( required: B => B = { b: B => b } ): M = {

    val builder = implicitly[ ClassTag[ B ] ].runtimeClass
      .getDeclaredMethod( "setRequestHeader", classOf[ RequestHeader ] )
      .invoke( nextBuilder[ M, B ], requestHeader )
      .asInstanceOf[ B ]
    next( builder, optionalEntries )( required )
  }

  private def nextBuilder[ M <: GeneratedMessage: ClassTag, B <: Builder[ B ]: ClassTag ](): B = {
    implicitly[ ClassTag[ M ] ].runtimeClass
      .getDeclaredMethod( "newBuilder" )
      .invoke( null )
      .asInstanceOf[ B ]
  }

  def create[ M <: GeneratedMessage: TypeTag ](): M = typeOf[ M ] match {
    case m if m =:= typeOf[ AccountNameRequest ] => accountNameRequest.asInstanceOf[ M ]
    case m if m =:= typeOf[ BalanceRequest ] => balanceRequest.asInstanceOf[ M ]
    case m if m =:= typeOf[ FindTransactionRequest ] => findTransactionRequest.asInstanceOf[ M ]
    case m if m =:= typeOf[ HistoryRequest ] => historyRequest.asInstanceOf[ M ]
    case m if m =:= typeOf[ TransferRequest ] => transferRequest.asInstanceOf[ M ]
    case m if m =:= typeOf[ AccountNameResponse ] => accountNameResponse.asInstanceOf[ M ]
    case m if m =:= typeOf[ BalanceResponse ] => balanceResponse.asInstanceOf[ M ]
    case m if m =:= typeOf[ HistoryResponse ] => historyResponse().asInstanceOf[ M ]
    case m if m =:= typeOf[ TransactionResponse ] => transactionResponse.asInstanceOf[ M ]
    case _ => throw new IllegalArgumentException( typeOf[ M ] + " is not a valid LibertyReserve API message." )
  }

}