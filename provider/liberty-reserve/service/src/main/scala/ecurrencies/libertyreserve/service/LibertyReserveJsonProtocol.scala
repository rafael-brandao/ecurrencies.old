package ecurrencies.libertyreserve.service

import scala.collection.JavaConversions.{ asScalaBuffer, mapAsScalaMap }
import scala.collection.immutable.ListMap

import spray.json.{ DefaultJsonProtocol, JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

import ecurrencies.libertyreserve.domain._
import ecurrencies.libertyreserve.service.util.{ DateTimeFormatter, IdGenerator, TokenGenerator }

object LibertyReserveJsonProtocol extends DefaultJsonProtocol {

  implicit object AccountNameRequestJson extends RootJsonFormat[ AccountNameRequest ] {
    def write( request: AccountNameRequest ) = JsObject(
      request.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "requestHeader"   => map ++ RequestHeaderJson.write( request.getRequestHeader ).fields
            case "searchAccountId" => map + ( "Search" -> JsString( request.getSearchAccountId ) )
          }
      }
    )

    def read( value: JsValue ) = {
      val builder = AccountNameRequest.newBuilder.setRequestHeader( RequestHeaderJson.read( value ) )
      value.asJsObject.fields.foldLeft( builder ) {
        ( builder, entry ) =>
          entry match {
            case ( "Search", JsString( search ) ) => builder.setSearchAccountId( search )
            case _                                => builder
          }
      }.build
    }
  }

  implicit object BalanceRequestJson extends RootJsonFormat[ BalanceRequest ] {
    def write( request: BalanceRequest ) = RequestHeaderJson.write( request.getRequestHeader )
    def read( value: JsValue ) = BalanceRequest.newBuilder.setRequestHeader( RequestHeaderJson.read( value ) ).build
  }

  implicit object FindTransactionRequestJson extends RootJsonFormat[ FindTransactionRequest ] {
    def write( request: FindTransactionRequest ) = JsObject(
      request.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "requestHeader" => map ++ RequestHeaderJson.write( request.getRequestHeader ).fields
            case "batchNumber"   => map + ( "Batch" -> JsString( request.getBatchNumber.toString ) )
          }
      }
    )

    def read( value: JsValue ) = {
      val builder = FindTransactionRequest.newBuilder.setRequestHeader( RequestHeaderJson.read( value ) )
      value.asJsObject.fields.foldLeft( builder ) {
        ( builder, entry ) =>
          entry match {
            case ( "Batch", JsString( search ) ) => builder.setBatchNumber( search.toLong )
            case _                               => builder
          }
      }.build
    }
  }

  implicit object HistoryRequestJson extends RootJsonFormat[ HistoryRequest ] {
    def write( request: HistoryRequest ) = JsObject(
      request.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "requestHeader"        => map ++ RequestHeaderJson.write( request.getRequestHeader ).fields
            case "historySpecification" => map ++ HistorySpecificationJson.write( request.getHistorySpecification ).fields
            case "pageIndex"            => map + ( "Page" -> JsNumber( request.getPageIndex ) )
            case "pageSize"             => map + ( "Size" -> JsNumber( request.getPageSize ) )
          }
      }
    )

    def read( value: JsValue ) = {
      val builder =
        HistoryRequest.newBuilder
          .setRequestHeader( RequestHeaderJson.read( value ) )
          .setHistorySpecification( HistorySpecificationJson.read( value ) )
      value.asJsObject.fields.foldLeft( builder ) {
        ( builder, entry ) =>
          entry match {
            case ( "Page", JsNumber( page ) ) => builder.setPageIndex( page.toInt )
            case ( "Size", JsNumber( size ) ) => builder.setPageSize( size.toInt )
            case _                            => builder
          }
      }.build
    }
  }

  implicit object TransferRequestJson extends RootJsonFormat[ TransferRequest ] {
    def write( request: TransferRequest ) = JsObject(
      RequestHeaderJson.write( request.getRequestHeader ).fields ++
        ListMap[ String, JsValue ](
          "Payee" -> JsString( request.getPayeeAccountId ),
          "Amount" -> JsString( request.getAmount ),
          "Currency" -> JsNumber( request.getCurrency.getNumber ),
          "Memo" -> JsString( request.getMemo ),
          "Reference" -> JsString( request.getMerchantReference ),
          "Type" -> JsString( request.getType.toString.toLowerCase ),
          "Private" -> JsBoolean( request.getPrivate ),
          "Purpose" -> JsNumber( request.getPurpose.getNumber )
        )
    )

    def read( value: JsValue ) = {
      val builder = TransferRequest.newBuilder.setRequestHeader( RequestHeaderJson.read( value ) )
      value.asJsObject.fields.foldLeft( builder ) {
        ( builder, entry ) =>
          entry match {
            case ( "Payee", JsString( payee ) )         => builder.setPayeeAccountId( payee )
            case ( "Amount", JsString( amount ) )       => builder.setAmount( amount )
            case ( "Currency", JsNumber( currency ) )   => builder.setCurrency( Currency.valueOf( currency.toInt ) )
            case ( "Memo", JsString( memo ) )           => builder.setMemo( memo )
            case ( "Reference", JsString( reference ) ) => builder.setMerchantReference( reference )
            case ( "Type", JsString( transferType ) )   => builder.setType( TransactionType.valueOf( transferType.toUpperCase ) )
            case ( "Private", JsBoolean( privacy ) )    => builder.setPrivate( privacy )
            case ( "Purpose", JsNumber( purpose ) )     => builder.setPurpose( PaymentPurpose.valueOf( purpose.toInt ) )
            case _                                      => builder
          }
      }.build
    }
  }

  implicit object AccountNameResponseJson extends RootJsonFormat[ AccountNameResponse ] {
    def write( response: AccountNameResponse ) = JsObject(
      response.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "responseHeader" => map ++ ResponseHeaderJson.write( response.getResponseHeader ).fields
            case "account" =>
              map + ( "Accounts" -> JsArray( AccountJson.write( response.getAccount ) ) )
          }
      }
    )

    def read( value: JsValue ) = {
      val builder = AccountNameResponse.newBuilder.setResponseHeader( ResponseHeaderJson.read( value ) )
      value.asJsObject.fields.foldLeft( builder ) {
        ( builder, entry ) =>
          entry match {
            case ( "Accounts", JsArray( accounts ) ) =>
              accounts.foldLeft( builder ) { ( builder, jsValue ) =>
                builder.setAccount( AccountJson.read( jsValue ) )
              }
            case _ => builder
          }
      }.build
    }
  }

  implicit object BalanceResponseJson extends RootJsonFormat[ BalanceResponse ] {
    def write( response: BalanceResponse ) = JsObject(
      response.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "responseHeader" => map ++ ResponseHeaderJson.write( response.getResponseHeader ).fields
            case "balances"       => map + ( "Balance" -> BalancesJson.write( response.getBalancesList ) )
          }
      }
    )

    def read( value: JsValue ) = {
      val builder = BalanceResponse.newBuilder.setResponseHeader( ResponseHeaderJson.read( value ) )
      value.asJsObject.fields.foldLeft( builder ) {
        ( builder, entry ) =>
          entry match {
            case ( "Balance", JsObject( _ ) ) =>
              BalancesJson.read( entry._2 ).foldLeft( builder ) {
                ( builder, balance ) =>
                  builder.addBalances( balance )
              }
            case _ => builder
          }
      }.build
    }
  }

  implicit object HistoryResponseJson extends RootJsonFormat[ HistoryResponse ] {
    def write( response: HistoryResponse ) = JsObject(
      response.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "responseHeader" => map ++ ResponseHeaderJson.write( response.getResponseHeader ).fields
            case "hasMore"        => map + ( "HasMore" -> JsBoolean( response.getHasMore ) )
            case "transactions" =>
              map + ( "Transactions" ->
                JsArray( response.getTransactionsList.foldLeft( List[ JsValue ]() ) {
                  ( list, transaction ) =>
                    list :+ TransactionJson.write( transaction )
                } )
              )
          }
      }
    )

    def read( value: JsValue ) = {
      val builder = HistoryResponse.newBuilder.setResponseHeader( ResponseHeaderJson.read( value ) )
      value.asJsObject.fields.foldLeft( builder ) {
        ( builder, entry ) =>
          entry match {
            case ( "HasMore", JsBoolean( hasMore ) ) => builder.setHasMore( hasMore )
            case ( "Transactions", JsArray( transactions ) ) =>
              transactions.foldLeft( builder ) {
                ( builder, jsValue ) =>
                  builder.addTransactions( TransactionJson.read( jsValue ) )
              }
            case _ => builder
          }
      }.build
    }
  }

  implicit object TransactionResponseJson extends RootJsonFormat[ TransactionResponse ] {
    def write( response: TransactionResponse ) = JsObject(
      response.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "responseHeader" => map ++ ResponseHeaderJson.write( response.getResponseHeader ).fields
            case "transaction"    => map + ( "Transaction" -> TransactionJson.write( response.getTransaction ) )
          }
      }
    )

    def read( value: JsValue ) = {
      val builder = TransactionResponse.newBuilder.setResponseHeader( ResponseHeaderJson.read( value ) )
      value.asJsObject.fields.foldLeft( builder ) {
        ( builder, entry ) =>
          entry match {
            case ( "Transaction", JsObject( transaction ) ) => builder.setTransaction( TransactionJson.read( entry._2 ) )
            case _ => builder
          }
      }.build
    }
  }

  implicit object RequestHeaderJson extends RootJsonFormat[ RequestHeader ] with IdGenerator with TokenGenerator {
    def write( header: RequestHeader ) = JsObject(
      "Id" -> JsString( nextId ),
      "Account" -> JsString( header.getAccount ),
      "Api" -> JsString( header.getApi ),
      "Token" -> JsString( createToken( header.getSecurityWord.toCharArray ) )
    )

    def read( value: JsValue ) = value.asJsObject.fields.foldLeft( RequestHeader.newBuilder ) {
      ( builder, entry ) =>
        entry match {
          case ( "Account", JsString( account ) ) => builder.setAccount( account )
          case ( "Api", JsString( api ) )         => builder.setApi( api )
          case _                                  => builder
        }
    }.build
  }

  implicit object ResponseHeaderJson extends RootJsonFormat[ ResponseHeader ] with DateTimeFormatter {
    def write( header: ResponseHeader ) = JsObject(
      header.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) { ( map, entry ) =>
        entry._1.getName match {
          case "id"        => map + ( "Id" -> JsString( header.getId ) )
          case "timestamp" => map + ( "Timestamp" -> JsString( printDate( header.getTimestamp ) ) )
          case "status"    => map + ( "Status" -> JsNumber( header.getStatus().getNumber ) )
          case "error"     => map + ( "Error" -> ErrorJson.write( header.getError ) )
        }
      }
    )

    def read( value: JsValue ) = value.asJsObject.fields.foldLeft( ResponseHeader.newBuilder ) {
      ( builder, entry ) =>
        entry match {
          case ( "Id", JsString( id ) )               => builder.setId( id )
          case ( "Timestamp", JsString( timestamp ) ) => builder.setTimestamp( parseMillis( timestamp ) )
          case ( "Status", JsNumber( status ) )       => builder.setStatus( ResponseStatus.valueOf( status.toInt ) )
          case ( "Error", JsObject( error ) )         => builder.setError( ErrorJson.read( entry._2 ) )
          case _                                      => builder
        }
    }.build
  }

  implicit object AccountJson extends RootJsonFormat[ Account ] {
    def write( account: Account ) = JsObject(
      account.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) { ( map, entry ) =>
        entry._1.getName match {
          case "accountId"   => map + ( "Account" -> JsString( account.getAccountId ) )
          case "accountName" => map + ( "Name" -> JsString( account.getAccountName ) )
        }
      }
    )

    def read( value: JsValue ) = value.asJsObject.fields.foldLeft( Account.newBuilder ) {
      ( builder, entry ) =>
        entry match {
          case ( "Account", JsString( account ) ) => builder.setAccountId( account )
          case ( "Name", JsString( name ) )       => builder.setAccountName( name )
          case _                                  => builder
        }
    }.build
  }

  implicit object BalancesJson extends RootJsonFormat[ Seq[ Balance ] ] {
    def write( balances: Seq[ Balance ] ) = JsObject(
      balances.foldLeft( ListMap[ String, JsValue ]() ) { ( map, balance ) =>
        map + ( balance.getCurrency().toString() -> JsNumber( balance.getBalance ) )
      }
    )
    def read( value: JsValue ) = value.asJsObject.fields.foldLeft( Seq[ Balance ]() ) {
      ( seq, entry ) =>
        entry match {
          case ( _, JsNumber( balance ) ) =>
            seq :+ Balance.newBuilder
              .setCurrency( Currency.valueOf( entry._1 ) )
              .setBalance( balance.toString )
              .build()
          case _ => seq
        }
    }
  }

  implicit object ErrorJson extends RootJsonFormat[ Error ] {
    def write( error: Error ) = JsObject(
      error.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "code"    => map + ( "ErrorCode" -> JsNumber( error.getCode.toString ) )
            case "message" => map + ( "ErrorMessage" -> JsString( error.getMessage ) )
          }
      } )

    def read( value: JsValue ) = value.asJsObject.fields.foldLeft( Error.newBuilder ) {
      ( builder, entry ) =>
        entry match {
          case ( "ErrorCode", JsNumber( code ) )       => builder.setCode( code.toInt )
          case ( "ErrorMessage", JsString( message ) ) => builder.setMessage( message )
          case _                                       => builder
        }
    }.build
  }

  implicit object HistorySpecificationJson extends RootJsonFormat[ HistorySpecification ] with DateTimeFormatter {

    def write( specification: HistorySpecification ) = JsObject(
      specification.getAllFields.foldLeft( ListMap[ String, JsValue ]() ) {
        ( map, entry ) =>
          entry._1.getName match {
            case "from"                 => map + ( "From" -> JsString( printDate( specification.getFrom ) ) )
            case "till"                 => map + ( "Till" -> JsString( printDate( specification.getTill ) ) )
            case "currency"             => map + ( "Currency" -> JsNumber( specification.getCurrency.getNumber ) )
            case "transactionDirection" => map + ( "Direction" -> JsNumber( specification.getTransactionDirection.getNumber ) )
            case "relatedAccountId"     => map + ( "RelatedAccount" -> JsString( specification.getRelatedAccountId ) )
            case "merchantReference"    => map + ( "Reference" -> JsString( specification.getMerchantReference ) )
            case "transactionSource"    => map + ( "Source" -> JsNumber( specification.getTransactionSource.getNumber ) )
            case "transactionPrivacy"   => map + ( "Private" -> JsNumber( specification.getTransactionPrivacy.getNumber ) )
            case "amountFrom"           => map + ( "AmountFrom" -> JsString( specification.getAmountFrom ) )
            case "amountTo"             => map + ( "AmountTo" -> JsString( specification.getAmountTo ) )
          }
      } )

    def read( value: JsValue ) = value.asJsObject.fields.foldLeft( HistorySpecification.newBuilder ) {
      ( builder, entry ) =>
        entry match {
          case ( "From", JsString( from ) )              => builder.setFrom( parseMillis( from ) )
          case ( "Till", JsString( till ) )              => builder.setTill( parseMillis( till ) )
          case ( "Currency", JsNumber( currency ) )      => builder.setCurrency( Currency.valueOf( currency.toInt ) )
          case ( "Direction", JsNumber( direction ) )    => builder.setTransactionDirection( TransactionDirection.valueOf( direction.toInt ) )
          case ( "RelatedAccount", JsString( account ) ) => builder.setRelatedAccountId( account )
          case ( "Reference", JsString( reference ) )    => builder.setMerchantReference( reference )
          case ( "Source", JsNumber( source ) )          => builder.setTransactionSource( TransactionSource.valueOf( source.toInt ) )
          case ( "Private", JsNumber( privacy ) )        => builder.setTransactionPrivacy( TransactionPrivacy.valueOf( privacy.toInt ) )
          case ( "AmountFrom", JsString( amountFrom ) )  => builder.setAmountFrom( amountFrom )
          case ( "AmountTo", JsString( amountTo ) )      => builder.setAmountTo( amountTo )
          case _                                         => builder
        }
    }.build
  }

  implicit object TransactionJson extends RootJsonFormat[ Transaction ] with DateTimeFormatter {
    def write( transaction: Transaction ) = JsObject(
      "Batch" -> JsNumber( transaction.getBatchNumber ),
      "Date" -> JsString( printDate( transaction.getDate ) ),
      "Reference" -> JsString( transaction.getMerchantReference ),
      "Type" -> JsString( transaction.getType.toString.toLowerCase ),
      "Payer" -> JsString( transaction.getPayerAccountId ),
      "PayerName" -> JsString( transaction.getPayerAccountName ),
      "Payee" -> JsString( transaction.getPayeeAccountId ),
      "PayeeName" -> JsString( transaction.getPayeeAccountName ),
      "Currency" -> JsNumber( transaction.getCurrency.getNumber ),
      "Amount" -> JsNumber( transaction.getAmount ),
      "Fee" -> JsNumber( transaction.getFee ),
      "Balance" -> JsNumber( transaction.getClosingBalance ),
      "Memo" -> JsString( transaction.getMemo ),
      "Private" -> JsBoolean( transaction.getPrivate ),
      "Source" -> JsNumber( transaction.getSource.getNumber )
    )

    def read( value: JsValue ) = value.asJsObject.fields.foldLeft( Transaction.newBuilder ) {
      ( builder, entry ) =>
        entry match {
          case ( "Batch", JsNumber( batch ) )          => builder.setBatchNumber( batch.toLong )
          case ( "Date", JsString( date ) )            => builder.setDate( parseMillis( date ) )
          case ( "Reference", JsString( reference ) )  => builder.setMerchantReference( reference )
          case ( "Type", JsString( transactionType ) ) => builder.setType( TransactionType.valueOf( transactionType.toUpperCase ) )
          case ( "Payer", JsString( payer ) )          => builder.setPayerAccountId( payer )
          case ( "PayerName", JsString( payerName ) )  => builder.setPayerAccountName( payerName )
          case ( "Payee", JsString( payee ) )          => builder.setPayeeAccountId( payee )
          case ( "PayeeName", JsString( payeeName ) )  => builder.setPayeeAccountName( payeeName )
          case ( "Currency", JsNumber( currency ) )    => builder.setCurrency( Currency.valueOf( currency.toInt ) )
          case ( "Amount", JsNumber( amount ) )        => builder.setAmount( amount.toString )
          case ( "Fee", JsNumber( fee ) )              => builder.setFee( fee.toString )
          case ( "Balance", JsNumber( balance ) )      => builder.setClosingBalance( balance.toString )
          case ( "Memo", JsString( memo ) )            => builder.setMemo( memo )
          case ( "Private", JsBoolean( privacy ) )     => builder.setPrivate( privacy )
          case ( "Source", JsNumber( source ) )        => builder.setSource( TransactionSource.valueOf( source.toInt ) )
          case _                                       => builder
        }
    }.build
  }

}