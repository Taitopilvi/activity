/**
  * Copyright (C) 2016 Taitopilvi Oy - All Rights Reserved
  *
  * Unauthorized copying of this file, via any medium is strictly prohibited
  * Proprietary and confidential
  *
  * Written by Jukka Loukkaanhuhta
  */
package fi.taitopilvi.activity.input

import fi.taitopilvi.activity._
import org.joda.time.{DateTime, Duration}

import scala.xml.{Node, XML}

class TcxInput(xmldoc: scala.xml.Elem) extends Input {
  require(xmldoc != null)

  def this(filename: String) = this(XML load filename)

  private def calculateDuration(startTime: DateTime, trackPointNode: Node): Duration = {
    val time = new DateTime(trackPointNode.\("Time").head.text)
    new Duration(startTime, time)
  }

  private def parseHeartRateSamples(startTime: DateTime, trackPointNodes: Seq[Node]): Seq[Sample[Number]] =
    trackPointNodes.map(trackPointNode => {
      val value = (trackPointNode \ "HeartRateBpm" \ "Value").text.trim
      val duration = calculateDuration(startTime, trackPointNode)
      new Sample[Number](value match {
        case "" => null
        case _ => value.toShort
      }, Dimension.HeartRate, duration)
    }).filter(_.value != null)

  private def parseSpeedSamples(startTime: DateTime, trackPointNodes: Seq[Node]): Seq[Sample[Number]] = {
    val msToKmh = 3.6
    trackPointNodes.map(trackPointNode => {
      val value = (trackPointNode \ "Extensions" \ "TPX" \ "Speed").text.trim
      val duration = calculateDuration(startTime, trackPointNode)
      new Sample[Number](value match {
        case "" => null
        case _ => value.toDouble * msToKmh
      }, Dimension.Speed, duration)
    }).filter(_.value != null)
  }

  private def parseDistanceSamples(startTime: DateTime, trackPointNodes: Seq[Node]): Seq[Sample[Number]] =
    trackPointNodes.map(trackPointNode => {
      val value = (trackPointNode \ "DistanceMeters").text.trim
      val duration = calculateDuration(startTime, trackPointNode)
      new Sample[Number](value match {
        case "" => null
        case _ => value.toDouble
      }, Dimension.Distance, duration)
    }).filter(_.value != null)

  private def parseAltitudeSamples(startTime: DateTime, trackPointNodes: Seq[Node]): Seq[Sample[Number]] =
    trackPointNodes.map(trackPointNode => {
      val value = (trackPointNode \ "AltitudeMeters").text.trim
      val duration = calculateDuration(startTime, trackPointNode)
      new Sample[Number](value match {
        case "" => null
        case _ => value.toDouble
      }, Dimension.Altitude, duration)
    }).filter(_.value != null)

  private def parseLatitudeSamples(startTime: DateTime, trackPointNodes: Seq[Node]): Seq[Sample[Number]] =
    trackPointNodes.map(trackPointNode => {
      val value = (trackPointNode \ "Position" \ "LatitudeDegrees").text.trim
      val duration = calculateDuration(startTime, trackPointNode)
      new Sample[Number](value match {
        case "" => null
        case _ => value.toDouble
      }, Dimension.Latitude, duration)
    }).filter(_.value != null)

  private def parseLongitudeSamples(startTime: DateTime, trackPointNodes: Seq[Node]): Seq[Sample[Number]] =
    trackPointNodes.map(trackPointNode => {
      val value = (trackPointNode \ "Position" \ "LongitudeDegrees").text.trim
      val duration = calculateDuration(startTime, trackPointNode)
      new Sample[Number](value match {
        case "" => null
        case _ => value.toDouble
      }, Dimension.Longitude, duration)
    }).filter(_.value != null)

  private def parseCalories(laps: Seq[Node]): Number =
    laps.map(lap => {
      (lap \ "Calories").text.trim match {
        case "" => 0
        case n: String => n.toInt
      }
    }).sum[Int]

  private def parseAverageHeartRate(laps: Seq[Node]): Number = {
    val totalTime = laps.map(lap => (lap \ "TotalTimeSeconds").text.trim.toDouble).sum[Double]
    laps.map(lap => {
      val time = (lap \ "TotalTimeSeconds").text.trim.toDouble
      val value = (lap \ "AverageHeartRateBpm" \ "Value").text.trim
      value match {
        case "" => 0
        case _ => time * value.toInt
      }
    }).filter(_ != 0).sum[Double] / totalTime
  }

  private def parseActivity(activityNode: Node): Activity = {
    val startTime = new DateTime(activityNode.\("Id").head.text)

    val trackPointNodes = (activityNode \\ "Trackpoint")

    val heartRateSamples = parseHeartRateSamples(startTime, trackPointNodes).toList
    val speedSamples = parseSpeedSamples(startTime, trackPointNodes).toList
    val distanceSamples = parseDistanceSamples(startTime, trackPointNodes).toList
    val altitudeSamples = parseAltitudeSamples(startTime, trackPointNodes).toList
    val latitudeSamples = parseLatitudeSamples(startTime, trackPointNodes).toList
    val longitudeSamples = parseLongitudeSamples(startTime, trackPointNodes).toList

    val laps = (activityNode \ "Lap")
    val calories = new Figure[Number](parseCalories(laps), Dimension.Energy, FigureType.Exact)
    val hrAvg = new Figure[Number](parseAverageHeartRate(laps), Dimension.HeartRate, FigureType.Average)
    val distance = distanceSamples.isEmpty match { // TODO use Option
      case false => new Figure[Number](distanceSamples.last.value, Dimension.Distance, FigureType.Exact)
      case _ => new Figure[Number](0, Dimension.Distance, FigureType.Exact)
    }

    new Activity(startTime).
      addSamples(heartRateSamples).
      addSamples(speedSamples).
      addSamples(distanceSamples).
      addSamples(altitudeSamples).
      addSamples(latitudeSamples).
      addSamples(longitudeSamples).
      addFigure(calories).
      addFigure(hrAvg).
      addFigure(distance)
  }

  private def parseRoute(routeNode: Node): Route = {
    val name = routeNode.\("Name").head.text

    val trackPointNodes = (routeNode \\ "Trackpoint")
    val startTime = new DateTime((trackPointNodes.head \ "Time").head.text)

    val distanceSamples = parseDistanceSamples(startTime, trackPointNodes).toList
    val altitudeSamples = parseAltitudeSamples(startTime, trackPointNodes).toList
    val latitudeSamples = parseLatitudeSamples(startTime, trackPointNodes).toList
    val longitudeSamples = parseLongitudeSamples(startTime, trackPointNodes).toList

    val time = new Figure[Number](startTime.getMillis, Dimension.Time, FigureType.Exact)
    val distance = distanceSamples.isEmpty match { // TODO use Option
      case false => new Figure[Number](distanceSamples.last.value, Dimension.Distance, FigureType.Exact)
      case _ => new Figure[Number](0, Dimension.Distance, FigureType.Exact)
    }

    new Route(name).
      addSamples(distanceSamples).
      addSamples(altitudeSamples).
      addSamples(latitudeSamples).
      addSamples(longitudeSamples).
      addFigure(time).
      addFigure(distance)
  }

  override def toActivities: Seq[Activity] = {
    // TODO check schema versions
    (xmldoc \ "Activities" \ "Activity").map(parseActivity(_))
  }

  override def toRoutes: Seq[Route] = {
    // TODO check schema versions
    (xmldoc \ "Courses" \ "Course").map(parseRoute(_))
  }
}
