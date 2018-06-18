package controllers

import javax.inject.Inject

import play.api.data._
import play.api.i18n._
import play.api.mvc._
import scala.io.Source
import models.Country
import models.Airport
import models.Runway
import scala.collection.mutable.ArrayBuffer
import java.util.concurrent.atomic.AtomicInteger

class HomeController @Inject() (cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import QueryForm._
//load all countries from csv file
  val countries = for {
    line <- Source.fromFile("D:/lunatech/lunatech-query-reports-app/conf/resources/countries.csv", "UTF-8").getLines().drop(1).toList
    values = line.split(",\"").map(_.replaceAll("\"", ""))
  } yield Country(values(0).toInt, values(1), values(2), values(3), values(4), (if (values.length == 6) values(5) else ""))

  //load all airports from csv file
  val airports = for {
    line <- Source.fromFile("D:/lunatech/lunatech-query-reports-app/conf/resources/airports.csv", "UTF-8").getLines().drop(1).toList
    values1 = line.split(",")
    keywrds = values1.filter(_.count(_ == '"') == 1).mkString(",").replaceAll("\"", "")
    values = values1.map(_.replaceAll("\"", ""))
  } yield Airport.apply(values(0).toInt, values(1), values(2), values(3), values(4), values(5), values(6), values(7), values(8),
    values(9), values(10), (if (values.length >= 12) values(11) else ""), (if (values.length >= 13) values(12) else ""), (if (values.length >= 14) values(13) else ""),
    (if (values.length >= 15) values(14) else ""), (if (values.length >= 16) values(15) else ""), (if (values.length >= 17) values(16) else ""),
    (if (values.length == 18) (if (keywrds.length() > 0) keywrds else values(17)) else ""))

    //load all runways from csv file
  val runways = for {
    line <- Source.fromFile("D:/lunatech/lunatech-query-reports-app/conf/resources/runways.csv", "UTF-8").getLines().drop(1).toList
    values = line.split(",").map(_.replaceAll("\"", ""))
  } yield Runway(values(0).toInt, values(1).toInt, values(2), values(3), values(4), values(5), values(6), values(7), (if (values.length >= 9) values(8) else ""),
    (if (values.length >= 10) values(9) else ""), (if (values.length >= 11) values(10) else ""), (if (values.length >= 12) values(11) else ""), (if (values.length >= 13) values(12) else ""),
    (if (values.length >= 14) values(13) else ""), (if (values.length >= 15) values(14) else ""), (if (values.length >= 16) values(15) else ""), (if (values.length >= 17) values(16) else ""),
    (if (values.length >= 18) values(17) else ""), (if (values.length >= 19) values(18) else ""), (if (values.length >= 20) values(19) else ""))

    
  private val resultCountries: ArrayBuffer[Country] = scala.collection.mutable.ArrayBuffer()
  private val resultAirports: ArrayBuffer[Airport] = scala.collection.mutable.ArrayBuffer()
  private val resultRunways: ArrayBuffer[Runway] = scala.collection.mutable.ArrayBuffer()

  private val postUrl = routes.HomeController.getQuery()

 //gets the 4 reports and sends the request to Reports page
  def getReport = Action { implicit request: MessagesRequest[AnyContent] =>
    val airportsByCountry = airports.groupBy(_.iso_country).toList.sortBy(_._2.length)
    val countriesWithlowestNoOfAirports = airportsByCountry.take(10)
    val countriesWithHighestNoOfAirports = airportsByCountry.reverse.take(10)
    val countrie10 = countries.take(10)
    val airportsOf10C = airports.filter(a => countrie10.exists(_.code == a.iso_country))
    val runwaysByAirport = runways.groupBy(_.airport_ref)

    val runwaysByCountry = airportsOf10C.map(a => (a.iso_country, runwaysByAirport.get(a.id).getOrElse(Nil)))
    val runwaysGroupByCountry = runwaysByCountry.toSet
    val countryAndRunways = for {
      f <- runwaysGroupByCountry.groupBy(_._1)
      c = f._1
      rw = f._2.map(_._2.map(_.surface).mkString(", "))
    } yield (c, rw)

    val commonRunwayIdents = runways.groupBy(_.le_ident).toList.sortBy(_._2.length).reverse.take(10).map(_._1)

    Ok(views.html.Reports(countriesWithlowestNoOfAirports, countriesWithHighestNoOfAirports, countryAndRunways.toList, commonRunwayIdents))
  }
  
  
 //home page handler
  def homePage = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.homePage(resultCountries.toList, resultAirports.toList, resultRunways.toList, form, postUrl))
  }

  //returns query results
  def getQuery = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      BadRequest(views.html.homePage(resultCountries.toList, resultAirports.toList, resultRunways.toList, formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>

      val selectedCountries = data.countryNameOrCode.size match {
        case 2 => countries.filter(_.code == data.countryNameOrCode.trim())
        case _ => countries.filter(_.name.toUpperCase().startsWith(data.countryNameOrCode.trim().toUpperCase()))
      }

      val selectedAirports = airports.filter(a => selectedCountries.exists(_.code == a.iso_country))
      val selectedRunways = runways.filter(r => selectedAirports.exists(_.id == r.airport_ref))

      resultCountries.clear()
      resultCountries ++= (selectedCountries)
      resultAirports.clear()
      resultAirports ++= (selectedAirports)
      resultRunways.clear()
      resultRunways ++= (selectedRunways)

      val str = s"${data.countryNameOrCode}: ${selectedCountries.map(_.name).mkString(", ")}"
      Redirect(routes.HomeController.homePage()).flashing("Results For: " -> str)
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
