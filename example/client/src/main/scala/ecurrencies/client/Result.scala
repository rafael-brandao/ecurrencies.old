package ecurrencies.client

import scala.collection.mutable
import scala.math.BigInt
import scala.language.postfixOps

import akka.actor.Actor

case class ProducedMessage()

case class ProcessedMessage()

case class SentMessage()

case class RejectedMessage()

case class TimedOutMessage()

trait AggregatorReport extends Actor {

  override def postStop() {
    println(report)
  }

  final def report = doReport(new mutable.StringBuilder).toString()

  protected val doReport: mutable.StringBuilder => mutable.StringBuilder
}

class NonBlockingProducerResultAggregator extends Actor with AggregatorReport {

  private var producedMessages = BigInt(0)

  def receive = {
    case ProducedMessage => producedMessages += 1
  }

  protected val doReport: (mutable.StringBuilder) => mutable.StringBuilder =
    (_ ++= "\n NonBlockingProducer Results:"
      ++= "\n\t Produced messages: " ++= producedMessages.toString += '\n')
}

class RpcClientResultAggregator extends Actor with AggregatorReport {

  private var producedMessages = BigInt(0)
  private var processedMessages = BigInt(0)
  private var rejectedMessages = BigInt(0)
  private var timedoutMessages = BigInt(0)

  def receive = {
    case ProducedMessage => producedMessages += 1
    case ProcessedMessage => processedMessages += 1
    case RejectedMessage => rejectedMessages += 1
    case TimedOutMessage => timedoutMessages += 1
  }

  protected val doReport: (mutable.StringBuilder) => mutable.StringBuilder =
    (_ ++= "\n RpcClient Results:"
      ++= "\n\t Produced messages: " ++= producedMessages.toString
      ++= "\n\t Processed messages: " ++= processedMessages.toString
      ++= "\n\t Rejected messages: " ++= rejectedMessages.toString
      ++= "\n\t Timedout messages: " ++= timedoutMessages.toString += '\n')
}

class EventConsumerResultAggregator extends Actor with AggregatorReport {

  private var processedMessages = BigInt(0)
  private var rejectedMessages = BigInt(0)

  def receive = {
    case ProcessedMessage => processedMessages += 1
    case RejectedMessage => rejectedMessages += 1
  }

  protected val doReport: (mutable.StringBuilder) => mutable.StringBuilder =
    (_ ++= "\n EventConsumer Results:"
      ++= "\n\t Processed messages: " ++= processedMessages.toString
      ++= "\n\t Rejected messages: " ++= rejectedMessages.toString += '\n')

}