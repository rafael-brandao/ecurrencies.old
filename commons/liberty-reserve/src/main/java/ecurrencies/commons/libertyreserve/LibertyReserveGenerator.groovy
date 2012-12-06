package ecurrencies.commons.libertyreserve;

import static java.lang.Long.toHexString;
import static java.lang.Math.round;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static org.joda.time.DateTime.now;
import static org.joda.time.DateTimeZone.UTC;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.joda.time.DateTime;

import ecurrencies.domain.libertyreserve.LibertyReserve.Account;
import ecurrencies.domain.libertyreserve.LibertyReserve.AccountNameRequest;
import ecurrencies.domain.libertyreserve.LibertyReserve.AccountNameResponse;
import ecurrencies.domain.libertyreserve.LibertyReserve.Balance;
import ecurrencies.domain.libertyreserve.LibertyReserve.BalanceRequest;
import ecurrencies.domain.libertyreserve.LibertyReserve.BalanceResponse;
import ecurrencies.domain.libertyreserve.LibertyReserve.Currency;
import ecurrencies.domain.libertyreserve.LibertyReserve.Error;
import ecurrencies.domain.libertyreserve.LibertyReserve.FindTransactionRequest;
import ecurrencies.domain.libertyreserve.LibertyReserve.HistoryRequest;
import ecurrencies.domain.libertyreserve.LibertyReserve.HistoryResponse;
import ecurrencies.domain.libertyreserve.LibertyReserve.HistorySpecification;
import ecurrencies.domain.libertyreserve.LibertyReserve.PaymentPurpose;
import ecurrencies.domain.libertyreserve.LibertyReserve.RequestHeader;
import ecurrencies.domain.libertyreserve.LibertyReserve.ResponseHeader;
import ecurrencies.domain.libertyreserve.LibertyReserve.ResponseStatus;
import ecurrencies.domain.libertyreserve.LibertyReserve.Transaction;
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionDirection;
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionPrivacy;
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionResponse;
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionSource;
import ecurrencies.domain.libertyreserve.LibertyReserve.TransactionType;
import ecurrencies.domain.libertyreserve.LibertyReserve.TransferRequest;

public abstract class LibertyReserveGenerator {

    private static final String EMPTY_STRING = "";
    private static final Random RANDOM = new Random();

    public static AccountNameRequest accountNameRequest() {
        return AccountNameRequest
                .newBuilder()
                .setRequestHeader(requestHeader())
                .setSearchAccountId(AccountIdGenerator.next())
                .build();
    }

    public static BalanceRequest balanceRequest() {
        BalanceRequest.Builder builder = BalanceRequest.newBuilder().setRequestHeader(
                requestHeader());

        if (RANDOM.nextBoolean()) {
            builder = builder.setCurrency(CurrencyGenerator.next());
        }

        return builder.build();
    }

    public static FindTransactionRequest findTransactionRequest() {
        return FindTransactionRequest
                .newBuilder()
                .setRequestHeader(requestHeader())
                .setBatchNumber(BatchNumberGenerator.next())
                .build();
    }

    public static HistoryRequest historyRequest() {
        HistoryRequest.Builder builder = HistoryRequest
                .newBuilder()
                .setRequestHeader(requestHeader())
                .setHistorySpecification(historySpecification());

        if (RANDOM.nextBoolean()) {
            builder = builder.setPageIndex(RANDOM.nextInt(30) + 1);
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setPageSize(RANDOM.nextInt(20) + 1);
        }

        return builder.build();
    }

    public static TransferRequest transferRequest() {
        TransferRequest.Builder builder = TransferRequest
                .newBuilder()
                .setRequestHeader(requestHeader())
                .setPayeeAccountId(AccountIdGenerator.next())
                .setCurrency(CurrencyGenerator.next())
                .setAmount(AmountGenerator.next(15000))
                .setMerchantReference(IdGenerator.next())
                .setPrivate(RANDOM.nextBoolean());

        if (RANDOM.nextBoolean()) {
            builder = builder.setType(TransactionTypeGenerator.next());
        }

        if (RANDOM.nextBoolean()) {
            builder = builder.setMerchantReference(IdGenerator.next());
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setPurpose(PaymentPurposeGenerator.next());
        }

        return builder.build();
    }

    public static AccountNameResponse accountNameResponse() {
        final AccountNameResponse.Builder builder = AccountNameResponse
                .newBuilder()
                .setResponseHeader(responseHeader());

        if (!builder.getResponseHeader().hasError()) {
            builder.setAccount(account());
        }

        return builder.build();
    }

    public static BalanceResponse balanceResponse() {
        BalanceResponse.Builder builder = BalanceResponse.newBuilder().setResponseHeader(
                responseHeader());

        if (!builder.getResponseHeader().hasError()) {
            final int balancesCount = RANDOM.nextInt(Currency.values().length) + 1;
            final Set<Balance> balances = new HashSet<Balance>(balancesCount);
            Balance balance = balance();
            for (int i = 0; i < balancesCount; i++) {
                while (balances.contains(balance)) {
                    balance = balance();
                }
                balances.add(balance);
            }
            builder = builder.addAllBalances(balances);
        }

        return builder.build();
    }

    public static HistoryResponse historyResponse() {
        HistoryResponse.Builder builder = HistoryResponse.newBuilder().setResponseHeader(
                responseHeader());

        if (!builder.getResponseHeader().hasError()) {
            builder = builder.setHasMore(RANDOM.nextBoolean());
            for (int i = 0; i < RANDOM.nextInt() + 1; i++) {
                builder = builder.addTransactions(transaction());
            }
        }

        return builder.build();
    }

    public static TransactionResponse transactionResponse() {
        TransactionResponse.Builder builder = TransactionResponse.newBuilder().setResponseHeader(
                responseHeader());

        if (!builder.getResponseHeader().hasError()) {
            builder = builder.setTransaction(transaction());
        }
        return builder.build();
    }

    private static RequestHeader requestHeader() {
        return RequestHeader
                .newBuilder()
                .setApiAccountOwnerId(AccountIdGenerator.next())
                .setApiName(valueOf(toHexString(RANDOM.nextLong())))
                .setApiSecurityWord(valueOf(toHexString(RANDOM.nextLong())))
                .build();
    }

    private static Account account() {
        return Account
                .newBuilder()
                .setAccountId(AccountIdGenerator.next())
                .setAccountName(EMPTY_STRING)
                .build();
    }

    private static Balance balance() {
        return Balance
                .newBuilder()
                .setCurrency(CurrencyGenerator.next())
                .setBalance(AmountGenerator.next(10000))
                .build();
    }

    private static HistorySpecification historySpecification() {

        final int minimumYearsOffset = RANDOM.nextInt(10);
        final int maximumYearsOffset = RANDOM.nextInt(10) + minimumYearsOffset + 1;
        final int amountFrom = RANDOM.nextInt(10000);
        final int amountTo = RANDOM.nextInt(10000) + amountFrom + 1;

        HistorySpecification.Builder builder = HistorySpecification.newBuilder();

        if (RANDOM.nextBoolean()) {
            builder = builder.setFrom(TimestampGenerator.next(maximumYearsOffset));
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setTill(TimestampGenerator.next(minimumYearsOffset,
                    maximumYearsOffset));
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setCurrency(CurrencyGenerator.next());
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setTransactionDirection(TransactionDirectionGenerator.next());
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setRelatedAccountId(AccountIdGenerator.next());
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setMerchantReference(IdGenerator.next());
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setTransactionPrivacy(TransactionPrivacyGenerator.next());
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setAmountFrom(AmountGenerator.next(amountFrom));
        }
        if (RANDOM.nextBoolean()) {
            builder = builder.setAmountTo(AmountGenerator.next(amountFrom, amountTo));
        }

        return builder.build();
    }

    private static Transaction transaction() {
        return Transaction
                .newBuilder()
                .setBatchNumber(BatchNumberGenerator.next())
                .setDate(TimestampGenerator.next(15))
                .setMerchantReference(IdGenerator.next())
                .setType(TransactionTypeGenerator.next())
                .setPayerAccountId(AccountIdGenerator.next())
                .setPayerAccountName(EMPTY_STRING)
                .setPayeeAccountId(AccountIdGenerator.next())
                .setPayeeAccountName(EMPTY_STRING)
                .setCurrency(CurrencyGenerator.next())
                .setAmount(AmountGenerator.next(500))
                .setFee(AmountGenerator.next(2))
                .setClosingBalance(AmountGenerator.next(500, 10000))
                .setMemo(EMPTY_STRING)
                .setPrivate(RANDOM.nextBoolean())
                .setSource(TransactionSourceGenerator.next())
                .build();
    }

    private static Error error() {
        return Error.newBuilder().setCode(RANDOM.nextInt(1000)).build();
    }

    private static ResponseHeader responseHeader() {
        final ResponseHeader.Builder builder = ResponseHeader
                .newBuilder()
                .setId(IdGenerator.next())
                .setTimestamp(currentTimeMillis())
                .setStatus(ResponseStatusGenerator.next());

        if (!builder.getStatus().equals(ResponseStatus.SUCCESS)) {
            builder.setError(error());
        }

        return builder.build();
    }

    /**
     * Liberty Reserve account number. in a format of Unnnnnnn, Mnnnnnnn or Xnnnnnnn and up to 8
     * characters long
     * <p>
     * Regex: (M|U|X)(\\d){7}[1-9]
     * <p>
     * Examples: U4506734 , X1205
     * 
     * @return a random String that matches the regex pattern
     */
    private static abstract class AccountIdGenerator {

        private static final char[] CHARS = { 'M', 'U', 'X' };

        public static String next() {
            return new StringBuilder()
                    .append(CHARS[RANDOM.nextInt(3)])
                    .append(RANDOM.nextInt(99999999) + 1)
                    .toString();
        }

    }

    /**
     * Amount of transaction. Fraction with up to 4 digits in denominator, point (.) as a separator.
     * Examples: 1.26 , 456.7895
     */
    private static abstract class AmountGenerator {

        private static final String DECIMAL_PATTERN = "0000";
        private static final NumberFormat NUMBER_FORMAT = new DecimalFormat(DECIMAL_PATTERN);

        public static String next(final int maximum) {
            return next(0, maximum);
        }

        public static String next(final int minimum, final int maximum) {
            return new StringBuilder()
                    .append(NumberGenerator.next(minimum, maximum))
                    .append('.')
                    .append(NUMBER_FORMAT.format(RANDOM.nextInt(10000)))
                    .toString();
        }

    }

    /**
     * Unique number representing a transaction in Liberty Reserve long number (64-bit integer)
     * <p />
     * Examples: 1234567890 , 9994456683762355345868
     * 
     * @return a random long number that matches the specification
     */
    private static abstract class BatchNumberGenerator {

        public static Long next() {
            final Long batchNumber = RANDOM.nextLong();
            if (batchNumber < 0) {
                return next();
            } else {
                return batchNumber;
            }
        }
    }

    /**
     * Currency being transferred. You may use the following parameters:
     * <ul>
     * <li>1 (usd)</li>
     * <li>2 (euro)</li>
     * <li>3 (gold)</li>
     * </ul>
     * 
     * @return a Currency enumeration that matches the specification
     */
    private static abstract class CurrencyGenerator {

        public static Currency next() {
            return EnumTypeGenerator.next(Currency.class);
        }

    }

    private static abstract class IdGenerator {

        private static final String DECIMAL_PATTERN = "00000000000000000000";
        private static final NumberFormat NUMBER_FORMAT = new DecimalFormat(DECIMAL_PATTERN);

        public static String next() {
            return new StringBuilder()
                    .append(NUMBER_FORMAT.format(BatchNumberGenerator.next()))
                    .toString();
        }

    }

    private static abstract class PaymentPurposeGenerator {

        public static PaymentPurpose next() {
            return EnumTypeGenerator.next(PaymentPurpose.class);
        }

    }

    private static abstract class ResponseStatusGenerator {

        public static ResponseStatus next() {
            return EnumTypeGenerator.next(ResponseStatus.class);
        }

    }

    private static abstract class TimestampGenerator {

        public static Long next(final int maximumYearsOffset) {
            return next(0, maximumYearsOffset);
        }

        public static Long next(final int minimumYearsOffset, final int maximumYearsOffset) {
            final DateTime now = now(UTC);
            final DateTime min = now.minusYears(RANDOM.nextInt(maximumYearsOffset
                    - minimumYearsOffset));
            return NumberGenerator.next(min.getMillis(), now.getMillis());
        }

    }

    private static abstract class TransactionDirectionGenerator {

        public static TransactionDirection next() {
            return EnumTypeGenerator.next(TransactionDirection.class);
        }

    }

    private static abstract class TransactionPrivacyGenerator {

        public static TransactionPrivacy next() {
            return EnumTypeGenerator.next(TransactionPrivacy.class);
        }

    }

    /**
     * Source of transaction. Line supports one of these arguments:
     * <ul>
     * <li>1 (transfer performed from Liberty Reserve site)</li>
     * <li>2 (transfer performed from Liberty Reserve Wallet)</li>
     * <li>3 (transfer performed from Liberty Reserve API interface)</li>
     * <li>4 (transfer performed from Liberty Reserve SCI)</li>
     * </ul>
     * <p />
     * Examples: transfer
     * 
     * @return a TransactionSource enumeration that matches this specification
     */
    private static abstract class TransactionSourceGenerator {

        public static TransactionSource next() {
            return EnumTypeGenerator.next(TransactionSource.class);
        }

    }

    /**
     * Type of transaction. Line supports one of these arguments: transfer (transfer between Liberty
     * Reserve accounts)
     * <p />
     * Examples: transfer
     * 
     * @return a TransactionType enumeration that matches this specification
     */
    private static abstract class TransactionTypeGenerator {

        public static TransactionType next() {
            return EnumTypeGenerator.next(TransactionType.class);
        }

    }

    private static abstract class EnumTypeGenerator {

        public static <T extends Enum<T>> T next(final Class<T> enumClass) {
            final T[] values = enumClass.getEnumConstants();
            return values[RANDOM.nextInt(values.length)];
        }

    }

    private static abstract class NumberGenerator {

        public static int next(final int minimum, final int maximum) {
            return minimum + round(RANDOM.nextFloat() * (maximum - minimum));
        }

        public static long next(final long minimum, final long maximum) {
            return minimum + round(RANDOM.nextDouble() * (maximum - minimum));
        }

    }

}
