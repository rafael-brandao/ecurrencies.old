package ecurrencies.common.akka

import scala.reflect.ClassTag

import akka.actor.{ Actor, ActorLogging, ActorRef, Status }

import com.google.protobuf.{ GeneratedMessage, InvalidProtocolBufferException }

import ecurrencies.api.EcurrencyServiceException

class ProtobuffParser[ T <: GeneratedMessage: ClassTag ]( next: ActorRef ) extends Actor with ActorLogging {

  def receive = {
    case payload: Array[ Byte ] =>
      try {
        next forward ( ProtobuffParser parse payload )
      } catch {
        case e if e.getCause.getClass.isAssignableFrom( classOf[ InvalidProtocolBufferException ] ) =>
          sender ! Status.Failure( new EcurrencyServiceException( false, e ) )
        case e: Exception =>
          sender ! Status.Failure( new EcurrencyServiceException( true, e ) )
      }
  }
}

object ProtobuffParser {

  def apply[ T <: GeneratedMessage: ClassTag ]( next: ActorRef ) = new ProtobuffParser[ T ]( next )

  private[ akka ] def parse[ T <: GeneratedMessage: ClassTag ]( payload: Array[ Byte ] ): T = {
    implicitly[ ClassTag[ T ] ].runtimeClass
      .getDeclaredMethod( "parseFrom", classOf[ Array[ Byte ] ] )
      .invoke( null, payload )
      .asInstanceOf[ T ]
  }

}