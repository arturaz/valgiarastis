package net.arturaz.valgiarastis.models

/**
 * Created with IntelliJ IDEA.
 * User: arturas
 * Date: 9/4/12
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
object Pack {
  object Rounding {
    val Up = "up"
    val Down = "down"
    val Average = "avg"
  }
}

case class Pack(amount: Int, rounding: String) {
  def isRoundingUp = rounding == Pack.Rounding.Up
  def isRoundingDown = rounding == Pack.Rounding.Down
  def isRoundingAverage = rounding == Pack.Rounding.Average

  def sign =
    if (isRoundingUp) "↑"
    else if (isRoundingDown) "↓"
    else "↕"

  def packs(total: Float) = {
    val value = total / amount

    if (isRoundingUp) value.ceil.toInt
    else if (isRoundingDown) value.floor.toInt
    else value.round
  }

  // How much grams we need if get this in packs.
  def round(total: Float) = packs(total) * amount
}
