package EShop.lab2

import EShop.lab2.EShopUtils._
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.event.LoggingReceive

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object Checkout {

  sealed trait Command
  case object StartCheckout extends Command
  case class SelectDeliveryMethod(method: String) extends Command
  case object CancelCheckout extends Command
  case object ExpireCheckout extends Command
  case class SelectPayment(payment: String) extends Command
  case object ExpirePayment extends Command
  case object ReceivePayment extends Command

  sealed trait Event
  case object CheckOutClosed extends Event

  def props(cartRef: ActorRef): Props = Props(new Checkout(cartRef))
}

class Checkout(cartRef: ActorRef) extends Actor with ActorLogging {
  import Checkout._

  implicit val ec: ExecutionContext = context.system.dispatcher

  val checkoutTimerDuration: FiniteDuration = 1 seconds
  val paymentTimerDuration: FiniteDuration = 1 seconds

  def scheduleCheckoutTimer: Cancellable =
    context.system.scheduler.scheduleOnce(checkoutTimerDuration, self, ExpireCheckout)

  def schedulePaymentTimer: Cancellable =
    context.system.scheduler.scheduleOnce(paymentTimerDuration, self, ExpirePayment)

  def receive: Receive = LoggingReceive.withLabel("receive") {
    case StartCheckout => context become selectingDelivery(scheduleCheckoutTimer)
  }

  def selectingDelivery(timer: Cancellable): Receive = LoggingReceive.withLabel("selectingDelivery") {
    case SelectDeliveryMethod(_)         => action(timer)(context become selectingPaymentMethod(scheduleCheckoutTimer))
    case ExpireCheckout | CancelCheckout => cancel(timer)
  }

  def selectingPaymentMethod(timer: Cancellable): Receive = LoggingReceive.withLabel("selectingPaymentMethod") {
    case SelectPayment(_)                => action(timer)(context become processingPayment(schedulePaymentTimer))
    case ExpireCheckout | CancelCheckout => cancel(timer)
  }

  def processingPayment(timer: Cancellable): Receive = LoggingReceive.withLabel("processingPayment") {
    case ReceivePayment => close(timer)
    case ExpirePayment | CancelCheckout => cancel(timer)
  }

  def cancelled: Receive = LoggingReceive.withLabel("cancelled") {
    case _ => log.info("Checkout already cancelled")
  }

  def closed: Receive = LoggingReceive.withLabel("closed") {
    case _ => log.info("Checkout already closed")
  }

  private def cancel(timer: Cancellable): Unit = action(timer) {
//    cartRef ! CartActor.CancelCheckout
    context become cancelled
  }

  private def close(timer: Cancellable): Unit = action(timer) {
//    cartRef ! CartActor.CloseCheckout
//    sender ! CheckOutClosed
    context become closed
  }
}
