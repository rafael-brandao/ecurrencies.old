package ecurrencies.api;

public class EcurrencyServiceException extends Exception {

	private static final long serialVersionUID = 2323133755277870948L;

	private final boolean recoverable;

	public EcurrencyServiceException(boolean recoverable) {
		super();
		this.recoverable = recoverable;
	}

	public EcurrencyServiceException(boolean recoverable, String message) {
		super(message);
		this.recoverable = recoverable;
	}

	public EcurrencyServiceException(boolean recoverable, Throwable cause) {
		super(cause);
		this.recoverable = recoverable;
	}

	public EcurrencyServiceException(boolean recoverable, String message,
			Throwable cause) {
		super(message, cause);
		this.recoverable = recoverable;
	}

	public EcurrencyServiceException(boolean recoverable, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.recoverable = recoverable;
	}

	public boolean isRecoverable() {
		return recoverable;
	}

}
