package ecurrencies.api

class EcurrencyServiceException(recoverable: Boolean, cause: Throwable) extends Exception(cause) {

  def this(recoverable: Boolean) = this(recoverable, null)

  def isRecoverable = recoverable
}

object EcurrencyServiceException {
  def apply(recoverable: Boolean, cause: Throwable) = new EcurrencyServiceException(recoverable, cause)

  def apply(recoverable: Boolean) = new EcurrencyServiceException(recoverable)
}