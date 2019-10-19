package EShop.lab2

import akka.actor.Cancellable

object EShopUtils {

  def action(timer: Cancellable)(action: => Unit): Unit = {
    timer.cancel()
    action
  }
}
