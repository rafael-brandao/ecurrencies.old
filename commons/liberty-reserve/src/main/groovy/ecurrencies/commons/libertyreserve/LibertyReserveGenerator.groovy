package ecurrencies.commons.libertyreserve

import static java.lang.Long.toHexString
import static java.lang.Math.round
import static java.lang.String.valueOf
import static java.lang.System.currentTimeMillis
import static org.joda.time.DateTime.now
import static org.joda.time.DateTimeZone.UTC

import java.text.DecimalFormat
import java.text.NumberFormat

import ecurrencies.domain.libertyreserve.LibertyReserve.Account
import ecurrencies.domain.libertyreserve.LibertyReserve.AccountNameRequest
import ecurrencies.domain.libertyreserve.LibertyReserve.AccountNameResponse
import ecurrencies.domain.libertyreserve.LibertyReserve.Balance
import ecurrencies.domain.libertyreserve.LibertyReserve.BalanceRequest
import ecurrencies.domain.libertyreserve.LibertyReserve.BalanceResponse
import ecurrencies.domain.libertyreserve.LibertyReserve.Currency
import ecurrencies.domain.libertyreserve.LibertyReserve.Error
import ecurrencies.domain.libertyreserve.LibertyReserve.FindTransactionRequest
import ecurrencies.domain.libertyreserve.LibertyReserve.HistoryRequest
import ecurrencies.domain.libertyreserve.LibertyReserve.HistoryResponse
import ecurrencies.domain.libertyreserve.LibertyReserve.HistorySpecification
import ecurrencies.domain.libertyreserve.LibertyReserve.PaymentPurpose
import ecurrencies.domain.libertyreserve.LibertyReserve.RequestHeader
import ecurrencies.domain.libertyreserve.LibertyReserve.ResponseHeader
import ecurrencies.domain.libertyreserve.LibertyReserve.ResponseStatus
import ecurrencies.domain.libertyreserve.LibertyReserve.Transaction
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionDirection
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionPrivacy
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionResponse
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionSource
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionType
import ecurrencies.domain.libertyreserve.LibertyReserve.TransferRequest

abstract class LibertyReserveGenerator {

    private static final String EMPTY_STRING = ''
    private static final Random RANDOM = new Random()
    private static final char[] ACCOUNT_ID_CHARS = ['M', 'U', 'X'] as char[]
    private static final NumberFormat AMOUNT_NUMBER_FORMAT = new DecimalFormat('0000')
    private static final NumberFormat ID_NUMBER_FORMAT = new DecimalFormat('00000000000000000000')

    static AccountNameRequest accountNameRequest() {
        next(AccountNameRequest.class) {
            requestHeader   = nextRequestHeader()
            searchAccountId = nextAccountId()
        }
    }

    static BalanceRequest balanceRequest() {

        def optionalEntries = ['currency' : { nextEnumType(Currency.class) }]

        next(BalanceRequest.class) { builder ->
            requestHeader        = nextRequestHeader()
            setOptionalEntries(builder, optionalEntries)
        }
    }

    static FindTransactionRequest findTransactionRequest() {
        next(FindTransactionRequest.class) {
            requestHeader = nextRequestHeader()
            batchNumber   = nextBatchNumber()
        }
    }

    static HistoryRequest historyRequest() {
        def optionalEntries = [
                    'pageIndex' : { RANDOM.nextInt(30) + 1 },
                    'pageSize'  : { RANDOM.nextInt(20) + 1 }
                ]
        next(HistoryRequest.class) { builder ->
            requestHeader        = nextRequestHeader()
            historySpecification = nextHistorySpecification()
            setOptionalEntries(builder, optionalEntries)
        }
    }

    static TransferRequest transferRequest() {
        def optionalEntries = [
                    'type'    : { nextEnumType(TransactionType.class) },
                    'purpose' : { nextEnumType(PaymentPurpose.class) }
                ]
        next(TransferRequest.class) { builder ->
            requestHeader     = nextRequestHeader()
            payeeAccountId    = nextAccountId()
            currency          = nextEnumType(Currency.class)
            amount            = nextAmount(15_000)
            merchantReference = nextId()
            setPrivate(nextBoolean())
            setOptionalEntries(builder, optionalEntries)
        }
    }

    static AccountNameResponse accountNameResponse() {
        nextResponse(AccountNameResponse.class) {
            account = nextAccount()
        }
    }

    static BalanceResponse balanceResponse() {
        def containsBalance = { balances, balance ->
            balances.any {
                it.currency.name() == balance.currency.name()
            }
        }
        nextResponse(BalanceResponse.class) {
            Balance b = nextBalance()
            0.upto(RANDOM.nextInt(Currency.values().length)) {
                while(containsBalance(balancesList, b))
                    b = nextBalance()
                addBalances(b)
            }
        }
    }

    static HistoryResponse historyResponse() {
        nextResponse(HistoryResponse.class) {
            hasMore = nextBoolean()
            0.upto(RANDOM.nextInt(20)) {
                addTransactions(nextTransaction())
            }
        }
    }

    static TransactionResponse transactionResponse() {
        nextResponse(TransactionResponse.class) {
            transaction = nextTransaction()
        }
    }

    private static RequestHeader nextRequestHeader() {
        next(RequestHeader.class) {
            apiAccountOwnerId = nextAccountId()
            apiName           = valueOf(toHexString(RANDOM.nextLong()))
            apiSecurityWord   = valueOf(toHexString(RANDOM.nextLong()))
        }
    }

    private static Account nextAccount() {
        next(Account.class) {
            accountId   = nextAccountId()
            accountName = EMPTY_STRING
        }
    }

    private static Balance nextBalance() {
        next(Balance.class) {
            currency = nextEnumType(Currency.class)
            balance  = nextAmount(10_000)
        }
    }

    private static HistorySpecification nextHistorySpecification() {
        def minimumYearsOffset = RANDOM.nextInt(10)
        def maximumYearsOffset = RANDOM.nextInt(10) + minimumYearsOffset + 1
        def amountFrom = RANDOM.nextInt(10_000)
        def amountTo = RANDOM.nextInt(10_000) + amountFrom + 1
        def optionalEntries = [
                    'from'    : { nextTimestamp(maximumYearsOffset) },
                    'till'    : { nextTimestamp(minimumYearsOffset, maximumYearsOffset) },
                    'currency': { nextEnumType(Currency.class) },
                    'transactionDirection' : { nextEnumType(TransactionDirection.class) },
                    'relatedAccountId'     : { nextAccountId() },
                    'merchantReference'    : { nextId() },
                    'transactionPrivacy'   : { nextEnumType(TransactionPrivacy.class) },
                    'amountFrom'           : { nextAmount(amountFrom) },
                    'amountTo'             : { nextAmount(amountFrom, amountTo) }
                ]
        next(HistorySpecification.class) { builder ->
            setOptionalEntries(builder, optionalEntries)
        }
    }

    private static Transaction nextTransaction() {
        next(Transaction.class) {
            batchNumber       = nextBatchNumber()
            date              = nextTimestamp(15)
            merchantReference = nextId()
            type              = nextEnumType(TransactionType.class)
            payerAccountId    = nextAccountId()
            payerAccountName  = EMPTY_STRING
            payeeAccountId    = nextAccountId()
            payeeAccountName  = EMPTY_STRING
            currency          = nextEnumType(Currency.class)
            amount            = nextAmount(500)
            fee               = nextAmount(2)
            closingBalance    = nextAmount(500, 10_000)
            memo              = EMPTY_STRING
            source            = nextEnumType(TransactionSource.class)
            setPrivate(nextBoolean())
        }
    }

    private static ResponseHeader nextResponseHeader() {
        def nextStatus = {
            if(RANDOM.nextFloat() <= 0.8) // 80% probability of success
                ResponseStatus.SUCCESS
            else
                (RANDOM.nextFloat() <= 0.7) ? ResponseStatus.ERROR : ResponseStatus.NONE
        }
        next(ResponseHeader.class) {
            id        = nextId()
            timestamp = currentTimeMillis()
            status    = nextStatus()
            if(status != ResponseStatus.SUCCESS)
                error = nextError()
        }
    }

    private static Error nextError() {
        next(Error.class) {
            code = RANDOM.nextInt(1_000)
        }
    }

    private static def next(Class messageClass, Closure closure = {}) {
        def builder = messageClass.newBuilder()
        builder.with(closure)
        builder.build()
    }

    private static def nextResponse(Class messageClass, Closure closure = {}) {
        def enclosingClosure = {
            responseHeader = nextResponseHeader()
            if(!responseHeader.hasError())
                it.with(closure)
        }
        next(messageClass, enclosingClosure)
    }

    private static void setOptionalEntries(builder, entries, Closure condition = { RANDOM.nextFloat() <= RANDOM.nextFloat() }) {
        entries.each { k, v ->
            if(condition()) {
                builder."$k" = (v instanceof Closure) ? v() : v
            }
        }
    }

    private static def nextAccountId() {
        "${ACCOUNT_ID_CHARS[RANDOM.nextInt(3)]}${RANDOM.nextInt(99_999_999) + 1}"
    }

    private static def nextAmount(maximum) {
        nextAmount(0, maximum)
    }

    private static def nextAmount(minimum, maximum) {
        "${nextNumber(minimum, maximum)}.${AMOUNT_NUMBER_FORMAT.format(RANDOM.nextInt(10_000))}"
    }

    private static long nextBatchNumber() {
        long batchNumber = RANDOM.nextLong()
        (batchNumber > 0) ? batchNumber : (batchNumber * (-1))
    }

    private static String nextId() {
        ID_NUMBER_FORMAT.format(nextBatchNumber())
    }

    private static long nextTimestamp(int maximumYearsOffset) {
        nextTimestamp(0, maximumYearsOffset)
    }

    private static long nextTimestamp(int minimumYearsOffset, int maximumYearsOffset) {
        def now = now(UTC)
        def min = now.minusYears(RANDOM.nextInt(maximumYearsOffset - minimumYearsOffset))
        nextNumber(min.getMillis(), now.getMillis())
    }

    private static def nextEnumType(enumClass) {
        def values = enumClass.enumConstants
        values[RANDOM.nextInt(values.length)]
    }

    private static def nextNumber(minimum, maximum) {
        minimum + round(RANDOM.nextFloat() * (maximum - minimum))
    }

    private static def nextBoolean() {
        RANDOM.nextBoolean()
    }
}