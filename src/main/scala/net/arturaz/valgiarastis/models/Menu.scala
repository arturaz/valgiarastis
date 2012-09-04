package net.arturaz.valgiarastis.models

/**
 * Created with IntelliJ IDEA.
 * User: arturas
 * Date: 9/4/12
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */

case class RawMenu(comment: String, recipes: Seq[String]) {
  def toMenu(db: Database) =
    Menu(comment, recipes.map { id =>
      if (db.recipes.contains(id)) db.recipes(id)
      else throw new NoSuchElementException("Cannot find recipe in DB: "+id)
    })
}

case class Menu(comment: String, recipes: Seq[Recipe])
