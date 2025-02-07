package EShop.lab2
import EShop.lab2.EShopUtils._
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.event.LoggingReceive

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object CartActor {

  sealed trait Command
  case class AddItem(item: Any) extends Command
  case class RemoveItem(item: Any) extends Command
  case object ExpireCart extends Command
  case object StartCheckout extends Command
  case object CancelCheckout extends Command
  case object CloseCheckout extends Command

  sealed trait Event
  case class CheckoutStarted(checkoutRef: ActorRef) extends Event

  def props: Props = Props(new CartActor())
}

class CartActor extends Actor with ActorLogging {
  import CartActor._

  implicit val ec: ExecutionContext = context.system.dispatcher

  val cartTimerDuration: FiniteDuration = 5 seconds

  private def scheduleTimer: Cancellable = context.system.scheduler.scheduleOnce(cartTimerDuration, self, ExpireCart)

  def receive: Receive = empty

  def empty: Receive = LoggingReceive.withLabel("empty") {
    case AddItem(item) => context become nonEmpty(Cart.empty.addItem(item), scheduleTimer)
  }

  def nonEmpty(cart: Cart, timer: Cancellable): Receive = LoggingReceive.withLabel("nonEmpty") {
    case AddItem(item) =>
      action(timer) {
        context become nonEmpty(cart.addItem(item), scheduleTimer)
      }
    case RemoveItem(item) if cart.contains(item) && cart.size == 1 =>
      action(timer) {
        context become empty
      }
    case RemoveItem(item) if cart.contains(item) =>
      action(timer) {
        context become nonEmpty(cart.removeItem(item), scheduleTimer)
      }
    case StartCheckout =>
      action(timer) {
//        val checkoutRef = context.actorOf(Checkout.props(self))
//        checkoutRef ! Checkout.StartCheckout
//        sender ! CheckoutStarted(checkoutRef)
        context become inCheckout(cart)
      }
    case ExpireCart =>
      action(timer) {
        context become empty
      }
  }

  def inCheckout(cart: Cart): Receive = LoggingReceive.withLabel("inCheckout") {
    case CancelCheckout => context become nonEmpty(cart, scheduleTimer)
    case CloseCheckout  => context become empty
  }

}
