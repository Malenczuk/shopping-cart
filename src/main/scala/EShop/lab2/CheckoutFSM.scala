package EShop.lab2


import EShop.lab2.CheckoutFSM.{Data, Status}
import akka.actor.{ActorRef, LoggingFSM, Props}
import enumeratum._

import scala.concurrent.duration._
import scala.language.postfixOps

object CheckoutFSM {

  sealed trait Data extends EnumEntry

  object Data extends Enum[Data] {
    case object Uninitialized extends Data
    case object SelectingDeliveryStarted extends Data
    case object SelectingPaymentStarted extends Data
    case object ProcessingPaymentStarted extends Data

    override def values: IndexedSeq[Data] = findValues
  }
  sealed trait Status extends EnumEntry

  object Status extends Enum[Status] {
    case object NotStarted extends Status
    case object SelectingDelivery extends Status
    case object SelectingPaymentMethod extends Status
    case object Cancelled extends Status
    case object ProcessingPayment extends Status
    case object Closed extends Status

    override def values: IndexedSeq[Status] = findValues
  }

  def props(cartRef: ActorRef): Props = Props(new CheckoutFSM(cartRef))
}

class CheckoutFSM(cartRef: ActorRef) extends LoggingFSM[Status, Data] {
  import CheckoutFSM.Data._
  import CheckoutFSM.Status._
  import Checkout._

  // useful for debugging, see: https://doc.akka.io/docs/akka/current/fsm.html#rolling-event-log
  override def logDepth = 12

  val checkoutTimerDuration: FiniteDuration = 1 seconds
  val paymentTimerDuration: FiniteDuration = 1 seconds

  startWith(NotStarted, Uninitialized)

  when(NotStarted) {
    case Event(StartCheckout, Uninitialized) => goto(SelectingDelivery) using SelectingDeliveryStarted
  }

  when(SelectingDelivery, stateTimeout = checkoutTimerDuration) {
    case Event(SelectDeliveryMethod(_), SelectingDeliveryStarted) => goto(SelectingPaymentMethod) using SelectingPaymentStarted
    case Event(CancelCheckout | StateTimeout, _) => goto(Cancelled)
  }

  when(SelectingPaymentMethod, stateTimeout = checkoutTimerDuration) {
    case Event(SelectPayment(_), SelectingPaymentStarted) => goto(ProcessingPayment) using ProcessingPaymentStarted
    case Event(CancelCheckout | StateTimeout, _) => goto(Cancelled)
  }

  when(ProcessingPayment, stateTimeout = paymentTimerDuration) {
    case Event(ReceivePayment, ProcessingPaymentStarted) => goto(Closed)
    case Event(CancelCheckout | StateTimeout, _) => goto(Cancelled)
  }

  when(Cancelled) {
    case _ =>
      log.info("Checkout already cancelled")
      stay
  }

  when(Closed) {
    case _ =>
      log.info("Checkout already closed")
      stay
  }

//  onTransition {
//    case ProcessingPayment -> Closed => cartRef ! CartActor.CloseCheckout
//    case _ -> Cancelled => cartRef ! CartActor.CancelCheckout
//  }

}
