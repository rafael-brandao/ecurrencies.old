package ecurrencies.libertyreserve.util

import org.junit.runner.RunWith
import org.scalatest.{ Finders, WordSpec }
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

import RequestHeaderGenerator._
import ecurrencies.libertyreserve.domain._

@RunWith( classOf[ JUnitRunner ] )
class RequestHeaderGeneratorSpec extends WordSpec with ShouldMatchers {
  
  /*
   * For Transfer obtain by concatenation the following line:
   * Security Word:ID:Reference:Payee:Currency:Amount:Date UTC in YYYYMMDD format:Time UTC in HH format (only hours, 
   * not minutes).
   * Get hash of above line by SHA256.
   * 
   * For example:
   *     security word - MySecWord, request Id – 20121227170733, merchant reference – Reference, payee – U1234567, currency – usd, amount – 10.00, date UTC - 2012.12.27 17:07 (24h format).
   *     Note: currency is always in lower case (usd, euro, gold).
   *     
   * Concatenation of parameters:
   *     MySecWord:20121227170733:Reference:U1234567:usd:10.00:20121227:17
   * Hash SHA256 for above created line:
   *     1C93E3EA9FCC172CB747DC5B4B94B86F307414907AF67CFB6A07BB64D5E153EA
   *
   * */

  "TransferRequestHeaderGenerator" when {

    "called with 'MySecWord' as securityWord, '2012.12.27 17:07:56' as date (long), any Api object and a " +
      "TransferRequestPayload that provides 'U1234567' as accountId, '10.00' as amount 'USD' as currency and " +
      "'Reference' as merchant reference" should {

        "Return a RequestHeader object witch token value is " +
          "1C93E3EA9FCC172CB747DC5B4B94B86F307414907AF67CFB6A07BB64D5E153EA" in {

            val expectedToken = "1C93E3EA9FCC172CB747DC5B4B94B86F307414907AF67CFB6A07BB64D5E153EA"

            val securityWord = "MySecWord".toCharArray
            val id = "20121227170733"
            val date = DateTimeFormatter parseMillis "2012-12-27 17:07:55"

            val api = Api.newBuilder.setAccountId( "x$1" ).setName( "x$1" ).build
            val transferPayload =
              TransferRequest.Payload.newBuilder
                .setPayeeAccountId( "U1234567" )
                .setAmount( "10.00" )
                .setCurrency( Currency.USD )
                .setMerchantReference( "Reference" )
                .build

            val requestHeader: RequestHeader = TransferRequestGenerator.create( transferPayload, api, securityWord, id, date )

            requestHeader.getToken() should be === expectedToken
          }
      }
  }

}