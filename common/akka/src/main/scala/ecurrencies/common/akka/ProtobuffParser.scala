package ecurrencies.common.akka

import scala.reflect.ClassTag
import scala.util.control.NonFatal

import akka.actor.{Actor, ActorLogging, ActorRef, Status}

import com.google.protobuf.{GeneratedMessage, InvalidProtocolBufferException}

import ecurrencies.api.EcurrencyServiceException


object ProtobuffParser {

  private lazy val parseFrom = "parseFrom"

  def apply[T <: GeneratedMessage : ClassTag](next: ActorRef) = new ProtobuffParser[T](next)

  private[akka] def parsed[T <: GeneratedMessage : ClassTag](payload: Array[Byte]): T =
    implicitly[ClassTag[T]].runtimeClass
      .getDeclaredMethod(parseFrom, classOf[Array[Byte]])
      .invoke(null, payload)
      .asInstanceOf[T]
}

class ProtobuffParser[T <: GeneratedMessage : ClassTag](next: ActorRef) extends Actor with ActorLogging {

  import ProtobuffParser._

  def receive = {
    case payload: Array[Byte] =>
      try {
        next forward parsed(payload)
      } catch {
        case e if e.getCause.getClass.isAssignableFrom(classOf[InvalidProtocolBufferException]) =>
          sender ! Status.Failure(EcurrencyServiceException(recoverable = false, e))
        case NonFatal(t) =>
          sender ! Status.Failure(EcurrencyServiceException(recoverable = true, t))
      }
  }
}