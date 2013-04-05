package ecurrencies.client

import scala.concurrent.Await
import scala.concurrent.duration.{ FiniteDuration, DurationLong }
import scala.language.postfixOps
import scala.util.Random

import akka.actor.{ Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy }
import akka.pattern.gracefulStop
import akka.routing.{ Broadcast, RoundRobinRouter }

import com.rabbitmq.client.{ Channel, ConnectionFactory }

import ecurrencies.libertyreserve.domain._
import ecurrencies.libertyreserve.test.util.LibertyReserveGenerator._

case object Start
case object Stop

class ClientSupervisor extends Actor with ActorLogging {

  import RabbitMQ._
  import ClientSupervisor._
  import SupervisorStrategy.Restart
  import context._

  override val supervisorStrategy =
    OneForOneStrategy( maxNrOfRetries = 10, withinTimeRange = 1 minute ) {
      case _: Exception => Restart
    }

  private val nonBlockingProducerResultAggregator = createNonBlockingProducerResultAggregator
  private val rpcClientResultAggregator = createRpcClientResultAggregator
  private val eventConsumerResultAgregator = createEventConsumerResultAggregator

  private val nonBlockingProducer = createNonBlockingProducer( nonBlockingProducerResultAggregator )
  private val rpcClient = createRpcClient( rpcClientResultAggregator )
  private val eventBasedConsumer = createEventBasedConsumer( eventConsumerResultAgregator )

  nonBlockingProducer ! Broadcast( Start )
  rpcClient ! Broadcast( Start )

  def receive = {
    case ProducerReady => sender ! nextMessage
    case Stop =>
      stopActorRef( nonBlockingProducer, 5 seconds, Some( "NonBlockingProducers" ) )
      stopActorRef( rpcClient, 10 seconds, Some( "RpcClients" ) )
      stopActorRef( eventBasedConsumer, 5 seconds, Some( "EventBasedConsumers" ) )
  }

  override def postStop() {
    connection.close()
  }

}

object ClientSupervisor {

  import Settings._
  import RabbitMQ._

  private val random = new Random

  private def nextMessage = {
    random.nextInt( 5 ) match {

      case 0 =>
        ( { val request: AccountNameRequest = classOf[ AccountNameRequest ]; request },
          `liberty-reserve.ecurrency-id`,
          `liberty-reserve.account-name-service-id` )

      case 1 =>
        ( { val request: BalanceRequest = classOf[ BalanceRequest ]; request },
          `liberty-reserve.ecurrency-id`,
          `liberty-reserve.balance-service-id` )

      case 2 =>
        ( { val request: FindTransactionRequest = classOf[ FindTransactionRequest ]; request },
          `liberty-reserve.ecurrency-id`,
          `liberty-reserve.find-transaction-service-id` )

      case 3 =>
        ( { val request: HistoryRequest = classOf[ HistoryRequest ]; request },
          `liberty-reserve.ecurrency-id`,
          `liberty-reserve.history-service-id` )

      case 4 =>
        ( { val request: TransferRequest = classOf[ TransferRequest ]; request },
          `liberty-reserve.ecurrency-id`,
          `liberty-reserve.transfer-service-id` )

    }
  }

  val producerChannelFunction = () => newChannel()

  val consumerChannelFunction =
    () => newChannel {
      channel =>
        channel.queueDeclare( `consumer-queue.name`, `consumer-queue.durable`, `consumer-queue.exclusive`, `consumer-queue.auto-delete`, null )
        channel.basicQos( `channel.prefetch-count` )
        channel
    }

  val producerSleepTime = `message-fire-interval` milliseconds
  implicit val rpcClientTimeout = `rpc-client.timeout` milliseconds

  def createEventBasedConsumer( resultAggregator: ActorRef )( implicit context: ActorContext ) = {
    createActorRef( `event-consumer.instances` ) {
      EventBasedConsumer( consumerChannelFunction, `consumer-queue.name`, resultAggregator )
    }
  }

  def createEventConsumerResultAggregator( implicit context: ActorContext ) =
    createActorRef()( new EventConsumerResultAggregator )

  def createNonBlockingProducerResultAggregator( implicit context: ActorContext ) =
    createActorRef()( new NonBlockingProducerResultAggregator )

  def createNonBlockingProducer( resultAggregator: ActorRef )( implicit context: ActorContext ) =
    createActorRef( `non-blocking-producer.instances` ) {
      new NonBlockingProducer( producerChannelFunction, resultAggregator, `exchange-name`, `consumer-queue.name`, producerSleepTime )
    }

  def createRpcClientResultAggregator( implicit context: ActorContext ) =
    createActorRef()( new RpcClientResultAggregator )

  def createRpcClient( resultAggregator: ActorRef )( implicit context: ActorContext ) =
    createActorRef( `rpc-client.instances` ) {
      new RpcClientProducer( producerChannelFunction, resultAggregator, `exchange-name`, producerSleepTime )
    }

  private def createActorRef[ T <: Actor ]( instances: Int = 1 )( f: => T )( implicit context: ActorContext ) = {
    val props = Props( f )
    instances match {
      case number if number > 1 => context.actorOf( props.withRouter( RoundRobinRouter( number ) ) )
      case _                          => context.actorOf( props )
    }
  }

  def stopActorRef( actorRef: ActorRef, timeout: FiniteDuration, id: Option[ String ] = None )( implicit system: ActorSystem ) {
    val actualId: String =
      id match {
        case Some( identification ) => identification
        case None       => actorRef.path.toString
      }
    import system.log
    try {
      Await.result( gracefulStop( actorRef, timeout ), timeout )
    } catch {
      case e: java.util.concurrent.TimeoutException => log.error( "Could not stop {} in {}", actualId, timeout )
      case e: Throwable                             => log.error( e, "Unknow error stopping " + actualId )
    }
  }

}

private[ client ] object RabbitMQ {

  import Settings._

  lazy val connection = {
    val factory = new ConnectionFactory
    factory.setHost( `connection.host` )
    factory.setPort( `connection.port` )
    factory.setVirtualHost( `connection.virtual-host` )
    factory.setUsername( `connection.username` )
    factory.setPassword( `connection.password` )
    factory.newConnection
  }

  def newChannel( apply: Channel => Channel = ( channel => channel ) ) = {
    apply( connection.createChannel )
  }

}