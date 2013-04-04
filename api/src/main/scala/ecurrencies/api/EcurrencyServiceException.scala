package ecurrencies.api

class EcurrencyServiceException(recoverable: Boolean, cause: Throwable) extends Exception(cause) {

  def this(recoverable: Boolean) = this(recoverable, null)

  def isRecoverable = recoverable

}
