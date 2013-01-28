package ecurrencies.libertyreserve.service

import scala.concurrent.Future
import scala.reflect.ClassTag

import akka.actor.{ Actor, ActorRef }
import akka.pattern.pipe

import com.google.protobuf.GeneratedMessage

import spray.client.HttpConduit.{ Post, pimpWithResponseTransformation, sendReceive, unmarshal }
import spray.http.HttpRequest
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.util.executionContextFromActorRefFactory

class HttpService[ Request <: GeneratedMessage: ClassTag, Response <: GeneratedMessage: ClassTag ](
    uri: String )( implicit conduit: ActorRef, marshaller: Marshaller[ Request ], unmarshaller: Unmarshaller[ Response ] ) extends Actor {

  val pipeline: HttpRequest => Future[ Response ] = sendReceive( conduit ) ~> unmarshal[ Response ]

  def receive = {
    case request: Request => pipe( pipeline( Post( uri, request ) ) ) to sender
  }

}

object HttpService {
  def apply[ Request <: GeneratedMessage: ClassTag, Response <: GeneratedMessage: ClassTag ](
    uri: String )( implicit conduit: ActorRef, marshaller: Marshaller[ Request ], unmarshaller: Unmarshaller[ Response ] ) =
    new HttpService[ Request, Response ]( uri )
}