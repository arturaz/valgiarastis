package net.arturaz.valgiarastis.models

import xml.Node
import java.text.Collator
import java.util.{Calendar, Locale}

/**
 * Created with IntelliJ IDEA.
 * User: arturas
 * Date: 9/4/12
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */

case class IngredientWithSource(source: Recipe, ingredient: Ingredient)
case class IngredientEntry(
  pack: Option[Pack], subparts: List[IngredientWithSource]
) {
  def totalBrutto = subparts.foldLeft(0.0f) { case (sum, pair) =>
    sum + pair.ingredient.bruto
  }
}
case class Totals(
  header: String, peopleCount: Int,
  // ingredient name -> entry
  data: Map[String, IngredientEntry]=Map.empty
) {
  def toHtml = {
    val collator = Collator.getInstance(new Locale("lt_LT"))

    var rows = List.empty[Node]
    data.keys.toSeq.sortWith {
      (a, b) => collator.compare(a, b) == -1
    }.foreach { name =>
      val entry = data(name)
      val totalPerPerson = entry.totalBrutto

      var bruttoTd = <td>{s(totalPerPerson)}</td>
      val bruttoTotal = totalPerPerson * peopleCount
      entry.pack match {
        case None => ()
        case Some(pack) =>
          bruttoTd = bruttoTd.copy(
            child = bruttoTd.child :+ <span>{pack.sign}= {
              pack.round(bruttoTotal)} ({pack.packs(bruttoTotal)
            } packs)</span>
          )
      }

      rows = rows :+ <tr>
        <td>{name}</td>
        {bruttoTd}
      </tr>

      entry.subparts.foreach { pair =>
        val i = pair.ingredient
        rows = rows :+ <tr class="details">
          <td>{pair.source.name}</td>
          <td>{s(i.bruto)}</td>
        </tr>
      }
    }

    val html =
    <html>
      <head>
        <title>{header}</title>
        <meta charset="utf-8" />
        <style type="text/css">
        <![CDATA[
          .details { font-size: 70%; }
          .details td { padding-left: 25px; }
        ]]>
        </style>
      </head>
      <body>
        <h1>{header}</h1>
        <p>People count: {peopleCount}</p>
        <p>Generated at: {"%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS".
          format(Calendar.getInstance())}</p>
        <table border="1" cellpadding="6">
          <tr style="font-weight: bold">
            <td width="400px">Name</td>
            <td>Brutto (g)</td>
          </tr>
          {rows}
        </table>
      </body>
    </html>.toString()

    "<!DOCTYPE HTML>\n" + html
  }

  private[this] def s(value: Float) =
    <span>
      {"%.3f".format(value)} * {peopleCount} = <b>{
        "%.1f".format(value * peopleCount)
      }</b>
    </span>
}

object OrderCalculator {
  def calculate(
    menu: Menu, packs: Map[String, Pack], peopleCount: Int
  ): Totals = {
    var totals = Totals(menu.comment, peopleCount)
    menu.recipes.foreach { recipe =>
      recipe.ingredients.foreach { ingredient =>
        val pair = IngredientWithSource(recipe, ingredient)
        val pack = packs.get(ingredient.name)
        val entry = totals.data.getOrElse(ingredient.name, IngredientEntry(
          pack, List.empty[IngredientWithSource]
        ))

        totals = totals.copy(
          data = totals.data + (
            ingredient.name -> entry.copy(subparts = entry.subparts :+ pair)
          )
        )
      }
    }

    totals
  }

  def recalculate(totals: Totals, newPeopleCount: Int): Totals = {
    if (totals.peopleCount == newPeopleCount) return totals

    var newTotals = totals
    totals.data.foreach { case (name, ingredientEntry) =>
      val newSubparts = ingredientEntry.subparts.map { ingredientWS =>
        ingredientWS.copy(ingredient = ingredientWS.ingredient.copy(
          bruto = (ingredientWS.ingredient.bruto * totals.peopleCount) /
            newPeopleCount
        ))
      }
      val newEntry = ingredientEntry.copy(subparts = newSubparts)

      newTotals = newTotals.copy(
        data = newTotals.data + (name -> newEntry)
      )
    }

    newTotals.copy(peopleCount = newPeopleCount)
  }
}
