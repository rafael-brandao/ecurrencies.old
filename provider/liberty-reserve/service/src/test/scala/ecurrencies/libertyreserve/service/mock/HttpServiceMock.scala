package ecurrencies.libertyreserve.service.mock

import scala.concurrent.Future
import scala.reflect.ClassTag

import akka.actor.{ Actor, Status, actorRef2Scala }
import akka.pattern.pipe

import com.google.protobuf.GeneratedMessage

import ecurrencies.api.EcurrencyServiceException

class HttpServiceMock[ Request <: GeneratedMessage: ClassTag, Response <: GeneratedMessage: ClassTag ](
    mockResponse: Response ) extends Actor {

  import context.dispatcher

  def receive = {
    case request: Request => pipe( Future { mockResponse } ) to sender
    case _                => sender ! Status.Failure( new EcurrencyServiceException( false ) )
  }

}

object HttpServiceMock {
  def apply[ Request <: GeneratedMessage: ClassTag, Response <: GeneratedMessage: ClassTag ](
    mockResponse: Response ) = new HttpServiceMock[ Request, Response ]( mockResponse )
}