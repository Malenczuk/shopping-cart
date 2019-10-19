package EShop.lab2

case class Cart(items: Seq[Any]) {
  def contains(item: Any): Boolean = items.contains(item)
  def addItem(item: Any): Cart = copy(items = item +: items)
  def removeItem(item: Any): Cart = {
    val idx = items.indexOf(item)
    copy(items = items.take(idx - 1) ++ items.drop(idx + 1))
  }
  def size: Int = items.size
}

object Cart {
  def empty: Cart = Cart(Nil)
}
