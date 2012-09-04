package net.arturaz.valgiarastis.models

/**
 * Created with IntelliJ IDEA.
 * User: arturas
 * Date: 9/4/12
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */

case class Database(recipes: Map[String, Recipe], packs: Map[String, Pack])
