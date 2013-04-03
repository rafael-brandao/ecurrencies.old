package ecurrencies.libertyreserve.util

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

import ecurrencies.libertyreserve.domain._
import RequestHeaderGenerator._

@RunWith(classOf[JUnitRunner])
class RequestHeaderGeneratorSpec extends WordSpec with ShouldMatchers {

  /*
   * For AccountName obtain by concatenation the following line:
   * Security Word:ID:Date UTC in YYYYMMDD format:Time UTC in HH format (only hours, not minutes).
   * Get hash of above line by SHA256.
   *
   * For example:
   *     security word - MySecWord, request Id – 20121227175937, date UTC - 2012.12.27 17:55 (24h format).
   *
   * Concatenation of parameters:
   *     MySecWord:20121227175937:20121227:17
   * Hash SHA256 for above created line:
   *     D0EB65045E0FF8A604C6C35AEF715B2D67DE4471A1DC4AFBF21FF7987106D272
   *
   * */
  "AccountNameRequestHeaderGenerator" when {

    "called with 'MySecWord' as securityWord, '20121227175937' as id, '2012.12.27 17:55:08' as date (long) " +
      "and any Api object" should {

      "return a RequestHeader object witch token value is " +
        "D0EB65045E0FF8A604C6C35AEF715B2D67DE4471A1DC4AFBF21FF7987106D272" in {

        val expectedToken = "D0EB65045E0FF8A604C6C35AEF715B2D67DE4471A1DC4AFBF21FF7987106D272"

        val securityWord = "MySecWord".toCharArray
        val id = "20121227175937"
        val date = DateTimeFormatter parseMillis "2012-12-27 17:55:08"

        val api = Api.newBuilder.setAccountId("x$1").setName("x$1").build

        val accountNameRequestPayload =
          AccountNameRequest.Payload.newBuilder
            .setSearchAccountId("x$1")
            .build

        val requestHeader: RequestHeader =
          AccountNameRequestHeaderGenerator.create(accountNameRequestPayload, api, securityWord, id, date)

        requestHeader.getToken should be === expectedToken
      }
    }
  }

  /*
   * For Balance obtain by concatenation the following line:
   * Security Word:ID:Date UTC in YYYYMMDD format:Time UTC in HH format (only hours, not minutes).
   * Get hash of above line by SHA256.
   *
   * For example:
   *     security word - MySecWord, request Id – 20121227175937, date UTC - 2012.12.27 17:55 (24h format).
   *
   * Concatenation of parameters:
   *     MySecWord:20121227175937:20121227:17
   * Hash SHA256 for above created line:
   *     D0EB65045E0FF8A604C6C35AEF715B2D67DE4471A1DC4AFBF21FF7987106D272
   *
   * */
  "BalanceRequestHeaderGenerator" when {

    "called with 'MySecWord' as securityWord, '20121227175937' as id, '2012.12.27 17:55:08' as date (long) " +
      "and any Api object" should {

      "return a RequestHeader object witch token value is " +
        "D0EB65045E0FF8A604C6C35AEF715B2D67DE4471A1DC4AFBF21FF7987106D272" in {

        val expectedToken = "D0EB65045E0FF8A604C6C35AEF715B2D67DE4471A1DC4AFBF21FF7987106D272"

        val securityWord = "MySecWord".toCharArray
        val id = "20121227175937"
        val date = DateTimeFormatter parseMillis "2012-12-27 17:55:08"

        val api = Api.newBuilder.setAccountId("x$1").setName("x$1").build

        val balanceRequestPayload = BalanceRequest.Payload.newBuilder.build

        val requestHeader: RequestHeader =
          BalanceRequestHeaderGenerator.create(balanceRequestPayload, api, securityWord, id, date)

        requestHeader.getToken should be === expectedToken
      }
    }
  }

  /*
   * For FindTransaction obtain by concatenation the following line:
   * Security Word:ID:Transaction ID:Date UTC in YYYYMMDD format:Time UTC in HH format (only hours, not minutes).
   * Get hash of above line by SHA256.
   *
   * For example:
   *     security word - MySecWord, request Id – 20121227171708, Transaction ID (batch number) - 123455678,
   *     date UTC - 2012.12.27 17:27 (24h format).
   *
   * Concatenation of parameters:
   *     MySecWord:20121227171708:123455678:20121227:17
   * Hash SHA256 for above created line:
   *     23184B5BA3C6DE2664FD7BFA4E092E3FF87A6E5321AED2250410A6626E262D6B
   *
   * */
  "FindTransactionRequestHeaderGenerator" when {

    "called with 'MySecWord' as securityWord, '20121227171708' as id,  '2012.12.27 17:27:15' as date (long), + " +
      "a FindTransactionRequestPayload that provides '12345678' as transaction id, and any Api object" should {

      "return a RequestHeader object witch token value is " +
        "23184B5BA3C6DE2664FD7BFA4E092E3FF87A6E5321AED2250410A6626E262D6B" in {

        val expectedToken = "23184B5BA3C6DE2664FD7BFA4E092E3FF87A6E5321AED2250410A6626E262D6B"

        val securityWord = "MySecWord".toCharArray
        val id = "20121227171708"
        val date = DateTimeFormatter parseMillis "2012-12-27 17:27:15"

        val api = Api.newBuilder.setAccountId("x$1").setName("x$1").build

        val findTransactionRequestPayload =
          FindTransactionRequest.Payload.newBuilder
            .setBatchNumber(123455678)
            .build()

        val requestHeader: RequestHeader =
          FindTransactionRequestHeaderGenerator.create(findTransactionRequestPayload, api, securityWord, id, date)

        requestHeader.getToken should be === expectedToken
      }
    }
  }

  /*
   * For History obtain by concatenation the following line:
   * Security Word:ID:From Date in YYYY-MM-DD hh:mm:ss format:Till Date in YYYY-MM-DD hh:mm:ss format:Date UTC in
   * YYYYMMDD format:Time UTC in HH format (only hours, not minutes).
   * Get hash of above line by SHA256.
   *
   * For example:
   *     MySecWord, request Id – 20121227171008, From Date – 2012-12-01 00:00:00, Till Date – 2012-12-15 00:00:00,
   *     date UTC - 2012.12.27 17:10 (24h format).
   *
   * Concatenation of parameters:
   *     MySecWord:20121227171008:2012-12-01 00:00:00:2012-12-15 00:00:00:20121227:17
   * Hash SHA256 for above created line:
   *     E872E4000E186315AB0FC2C0EF9A67B75A8379223A00D65468BC756DEC0A3760
   *
   * */
  "HistoryRequestHeaderGenerator" when {

    "called with 'MySecWord' as securityWord, '20121227171008' as id,  '2012.12.27 17:10' as date (long), + " +
      "a HistoryRequestPayload that provides '2012-12-01 00:00:00' as from date, '2012-12-15 00:00:00' as till date " +
      "and any Api object" should {

      "return a RequestHeader object witch token value is " +
        "E872E4000E186315AB0FC2C0EF9A67B75A8379223A00D65468BC756DEC0A3760" in {

        val expectedToken = "E872E4000E186315AB0FC2C0EF9A67B75A8379223A00D65468BC756DEC0A3760"

        val securityWord = "MySecWord".toCharArray
        val id = "20121227171008"
        val date = DateTimeFormatter parseMillis "2012-12-27 17:10:47"

        val api = Api.newBuilder.setAccountId("x$1").setName("x$1").build

        val specification =
          HistorySpecification.newBuilder
            .setFrom(DateTimeFormatter parseMillis "2012-12-01 00:00:00")
            .setTill(DateTimeFormatter parseMillis "2012-12-15 00:00:00")
            .build

        val historyRequestPayload =
          HistoryRequest.Payload.newBuilder
            .setSpecification(specification)
            .build

        val requestHeader: RequestHeader =
          HistoryRequestHeaderGenerator.create(historyRequestPayload, api, securityWord, id, date)

        requestHeader.getToken should be === expectedToken
      }
    }
  }

  /*
   * For Transfer obtain by concatenation the following line:
   * Security Word:ID:Reference:Payee:Currency:Amount:Date UTC in YYYYMMDD format:Time UTC in HH format (only hours, 
   * not minutes).
   * Get hash of above line by SHA256.
   * 
   * For example:
   *     security word - MySecWord, request Id – 20121227170733, merchant reference – Reference, payee – U1234567,
   *     currency – usd, amount – 10.00, date UTC - 2012.12.27 17:07 (24h format).
   *     Note: currency is always in lower case (usd, euro, gold).
   *     
   * Concatenation of parameters:
   *     MySecWord:20121227170733:Reference:U1234567:usd:10.00:20121227:17
   * Hash SHA256 for above created line:
   *     1C93E3EA9FCC172CB747DC5B4B94B86F307414907AF67CFB6A07BB64D5E153EA
   *
   * */
  "TransferRequestHeaderGenerator" when {

    "called with 'MySecWord' as securityWord, '20121227170733' as id,  '2012.12.27 17:07:56' as date (long), + " +
      "a TransferRequestPayload that provides 'U1234567' as accountId, '10.00' as amount 'USD' as currency and " +
      "'Reference' as merchant reference and any Api object" should {

      "return a RequestHeader object witch token value is " +
        "1C93E3EA9FCC172CB747DC5B4B94B86F307414907AF67CFB6A07BB64D5E153EA" in {

        val expectedToken = "1C93E3EA9FCC172CB747DC5B4B94B86F307414907AF67CFB6A07BB64D5E153EA"

        val securityWord = "MySecWord".toCharArray
        val id = "20121227170733"
        val date = DateTimeFormatter parseMillis "2012-12-27 17:07:55"

        val api = Api.newBuilder.setAccountId("x$1").setName("x$1").build

        val transferPayload =
          TransferRequest.Payload.newBuilder
            .setPayeeAccountId("U1234567")
            .setAmount("10.00")
            .setCurrency(Currency.USD)
            .setMerchantReference("Reference")
            .build

        val requestHeader: RequestHeader = TransferRequestHeaderGenerator.create(transferPayload, api, securityWord,
          id, date)

        requestHeader.getToken should be === expectedToken
      }
    }
  }

}