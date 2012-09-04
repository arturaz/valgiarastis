package net.arturaz.valgiarastis

import models._
import models.RawMenu
import scalax.io.{Input, Resource}
import com.codahale.jerkson.Json
import java.io.File

object Runner {
  private[this] val dbFile = "data/db.json"

  def main(args: Array[String]) {
    args match {
      case Array("predict", menuFile, peopleCount) =>
        predict(menuFile, peopleCount.toInt)
      case Array("correct", oldFile, newPeopleCount) =>
        correct(oldFile, newPeopleCount.toInt)
      case _ =>
        help()
    }
  }

  private[this] def readDb(): Database = {
    println("Reading DB from '"+dbFile+"'...")
    val dbJson = Resource.fromFile(dbFile).string
    println("Parsing DB from JSON...")
    val db = Json.parse[Database](dbJson)

    db
  }

  private[this] def writeFile(file: File, data: String) {
    if (file.exists()) file.delete()
    val out = Resource.fromFile(file)
    out.write(data)
  }

  private[this] def predict(menuFile: String, peopleCount: Int) {
    val db = readDb()

    println("Reading menu from '"+menuFile+"'...")
    val menuJson = Resource.fromFile(menuFile).string
    println("Parsing menu from JSON...")
    val menu = Json.parse[RawMenu](menuJson)

    val totals = OrderCalculator.
      calculate(menu.toMenu(db), db.packs, peopleCount)

    val basename = menuFile.replaceAll("\\.json$", "")

    val htmlFile = new File("%s-prediction.html".format(basename))
    println("Writing orders to '"+htmlFile.getPath+"'...")
    writeFile(htmlFile, totals.toHtml)

    val jsonFile = new File("%s-prediction.json".format(basename))
    println("Writing orders JSON to '"+jsonFile.getPath+"'...")
    writeFile(jsonFile, Json.generate(totals))

    println("Done.")
  }

  private[this] def correct(oldFile: String, newPeopleCount: Int) {
    println("Reading old orders JSON from '"+oldFile+"'...")
    val oldOrdersJson = Resource.fromFile(oldFile).string
    println("Parsing old orders from JSON...")
    val totals = Json.parse[Totals](oldOrdersJson)

    println("Recalculating orders from " + totals.peopleCount + " to " +
      newPeopleCount + " people.")
    val newTotals = OrderCalculator.recalculate(totals, newPeopleCount)

    val basename = oldFile.replaceAll("-prediction\\.json$", "")

    val htmlFile = new File("%s-correction.html".format(basename))
    println("Writing orders to '"+htmlFile.getPath+"'...")
    writeFile(htmlFile, newTotals.toHtml)

    println("Done.")
  }

  private[this] def help() {
    println(
      """
Usage: java -jar valgiarastis.jar args

  Prediction mode:
    java -jar valgiarastis.jar predict menu_file people_count
    I.e.: java -jar valgiarastis.jar predict data/menu_file.json 34

  This generates data/menu_file-prediction.html (for viewing) and
  data/menu_file-prediction.json (for correction mode).

  Correction mode:
    java -jar valgiarastis.jar correct prediction_file new_people_count
    I.e.: java -jar valgiarastis.jar correct data/menu_file-prediction.json 16

  This generates data/menu_file-correction.html (for viewing) with corrected
  values.
      """.trim
    )
  }
}