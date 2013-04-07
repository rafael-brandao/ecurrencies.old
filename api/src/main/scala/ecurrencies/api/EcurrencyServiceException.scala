package ecurrencies.api

@SerialVersionUID(1L)
class EcurrencyServiceException(recoverable: Boolean, cause: Throwable) extends Exception(cause) with Serializable {

  def this(recoverable: Boolean) = this(recoverable, null)

  lazy val isRecoverable = recoverable
}

object EcurrencyServiceException {
  def apply(recoverable: Boolean, cause: Throwable) = new EcurrencyServiceException(recoverable, cause)

  def apply(recoverable: Boolean) = new EcurrencyServiceException(recoverable)
}