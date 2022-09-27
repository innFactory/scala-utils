package de.innfactory.implicits

trait Showable { this: Product =>
  override def toString: String = this.show
}

object Showable {
  implicit class ShowableProduct(product: Product) {
    def show: String = {
      val className   = product.productPrefix
      val fieldNames  = product.productElementNames.toList
      val fieldValues = product.productIterator.toList
      val fields      = fieldNames.zip(fieldValues).map { case (name, value) =>
        value match {
          case subProduct: Product => s"$name = ${subProduct.show}"
          case _                   => s"$name = $value"
        }
      }
      fields.mkString(s"$className(", ", ", ")")
    }
  }
}
