package ecurrencies.client

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec
import scala.collection.mutable.StringBuilder
import scala.language.postfixOps

import akka.actor.Actor

case class ProducedMessage()
case class ProcessedMessage()
case class SentMessage()
case class RejectedMessage()
case class TimedOutMessage()

class NonBlockingProducerResultAggregator extends Actor {

  import NonBlockingProducerResultAggregator._

  def receive = {
    case ProducedMessage => producedMessages + 1
  }

}

object NonBlockingProducerResultAggregator {
  private val producedMessages = new BigIntAtomicReference

  def report = (
    new StringBuilder
    ++= "\n NonBlockingProducer Results:"
    ++= "\n\t Produced messages: " ++= producedMessages.get.toString
    += '\n'
  ).toString
}

class RpcClientResultAggregator extends Actor {

  import RpcClientResultAggregator._

  def receive = {
    case ProducedMessage  => producedMessages + 1
    case ProcessedMessage => processedMessages + 1
    case RejectedMessage  => rejectedMessages + 1
    case TimedOutMessage  => timedoutMessages + 1
  }

}

object RpcClientResultAggregator {
  private val producedMessages = new BigIntAtomicReference
  private val processedMessages = new BigIntAtomicReference
  private val rejectedMessages = new BigIntAtomicReference
  private val timedoutMessages = new BigIntAtomicReference

  def report = (
    new StringBuilder
    ++= "\n RpcClient Results:"
    ++= "\n\t Produced messages: " ++= producedMessages.get.toString
    ++= "\n\t Processed messages: " ++= processedMessages.get.toString
    ++= "\n\t Rejected messages: " ++= rejectedMessages.get.toString
    ++= "\n\t Timedout messages: " ++= timedoutMessages.get.toString
    += '\n'
  ).toString

}

class EventConsumerResultAggregator extends Actor {

  import EventConsumerResultAggregator._

  def receive = {
    case ProcessedMessage => processedMessages + 1
    case RejectedMessage  => rejectedMessages + 1
  }

}

object EventConsumerResultAggregator {
  private val processedMessages = new BigIntAtomicReference
  private val rejectedMessages = new BigIntAtomicReference

  def report = (
    new StringBuilder
    ++= "\n EventConsumer Results:"
    ++= "\n\t Processed messages: " ++= processedMessages.get.toString
    ++= "\n\t Rejected messages: " ++= rejectedMessages.get.toString
    += '\n'
  ).toString

}

sealed class BigIntAtomicReference( number: BigInt ) extends AtomicReference( number ) {

  def this() {
    this( 0 )
  }

  def +[ N <% BigInt ]( number: N ) = updateAndGet( _ + number )

  def -[ N <% BigInt ]( number: N ) = updateAndGet( _ - number )

  @tailrec
  private def updateAndGet( f: BigInt => BigInt ): BigIntAtomicReference = {
    val oldValue = get()
    val newValue = f( oldValue )
    if ( compareAndSet( oldValue, newValue ) ) this else updateAndGet( f )
  }

}

object BigIntAtomicReference {
  import language.implicitConversions
  def apply[ X <% Long ]( x: X ) = new BigIntAtomicReference( BigInt( x ) )
  implicit def long2BigIntAtomicReference[ X <% Long ]( x: X ) = BigInt( x )
}
