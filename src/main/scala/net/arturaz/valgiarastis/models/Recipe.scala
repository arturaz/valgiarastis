package net.arturaz.valgiarastis.models

/**
 * Created with IntelliJ IDEA.
 * User: arturas
 * Date: 9/4/12
 * Time: 10:37 AM
 * To change this template use File | Settings | File Templates.
 */
case class Recipe(
  name: String, ingredients: Seq[Ingredient]
)