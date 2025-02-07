package EShop.lab2

import EShop.lab2.CartFSM.Status
import akka.actor.{LoggingFSM, Props}
import enumeratum._

import scala.concurrent.duration._
import scala.language.postfixOps

object CartFSM {

  sealed trait Status extends EnumEntry

  object Status extends Enumeration {
    case object Empty extends Status
    case object NonEmpty extends Status
    case object InCheckout extends Status
  }

  def props(): Props = Props(new CartFSM())
}

class CartFSM extends LoggingFSM[Status, Cart] {
  import CartActor._
  import CartFSM.Status._

  // useful for debugging, see: https://doc.akka.io/docs/akka/current/fsm.html#rolling-event-log
  override def logDepth = 12

  val cartTimerDuration: FiniteDuration = 1 seconds

  startWith(Empty, Cart.empty)

  when(Empty) {
    case Event(AddItem(item), cart @ Cart(Nil)) => goto(NonEmpty) using cart.addItem(item)
  }

  when(NonEmpty, stateTimeout = cartTimerDuration) {
    case Event(AddItem(item), cart: Cart)                                             => stay using cart.addItem(item)
    case Event(RemoveItem(item), cart: Cart) if cart.contains(item) && cart.size == 1 => goto(Empty) using Cart.empty
    case Event(RemoveItem(item), cart: Cart) if cart.contains(item)                   => stay using cart.removeItem(item)
    case Event(StartCheckout, cart: Cart)                                             => goto(InCheckout) using cart
    case Event(StateTimeout, _)                                                       => goto(Empty) using Cart.empty
  }

  when(InCheckout) {
    case Event(CancelCheckout, cart: Cart) => goto(NonEmpty) using cart
    case Event(CloseCheckout, _)           => goto(Empty) using Cart.empty
  }

//  onTransition {
//    case NonEmpty -> InCheckout =>
//      val checkoutRef = context.actorOf(CheckoutFSM.props(self))
//      checkoutRef ! Checkout.StartCheckout
//      sender ! CheckoutStarted(checkoutRef)
//  }
}
