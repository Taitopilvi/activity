/**
  * Copyright (C) 2016 Taitopilvi Oy - All Rights Reserved
  *
  * Unauthorized copying of this file, via any medium is strictly prohibited
  * Proprietary and confidential
  *
  * Written by Jukka Loukkaanhuhta
  */
package fi.taitopilvi.activity

import com.github.nscala_time.time.Imports.{DateTime, Duration}
import fi.taitopilvi.activity.Dimension.Dimension
import fi.taitopilvi.activity.FigureType.FigureType

import scala.annotation.tailrec
import scala.language.postfixOps

// Dimension enumeration - values in SI
object Dimension extends Enumeration {
  type Dimension = Value
  val Time, Distance, Speed, HeartRate, Altitude, Latitude, Longitude, Energy = Value
}

// FigureType enumeration
object FigureType extends Enumeration {
  type FigureType = Value
  val Exact, Minimum, Average, Maximum = Value
}

// Figure class
case class Figure[T](value: T, dimension: Dimension, ftype: FigureType) {
  require(dimension != null, "Dimension required")
  require(ftype != null, "Figure type required")

  def this(v: T, d: Dimension) = this(v, d, FigureType.Exact)

  def isDimension(d: Dimension) = (dimension == d)

  def isFigureType(ft: FigureType) = (ftype == ft)
}

// Sample class
case class Sample[T](value: T, dimension: Dimension, time: Duration) {
  require(dimension != null, "Dimension required")
  require(time != null, "Time required")

  def isDimension(d: Dimension) = (dimension == d)

  override def toString(): String = "(" + value + ", " + dimension + ", " + time + ")"
}

case class Reference[T](storageId: Number)

// Activity class
case class Activity(startTime: DateTime,
               figures: List[Figure[Number]],
               samples: List[Sample[Number]]) {
  require(startTime isBeforeNow)

  val id = startTime

  def this(startTime: DateTime) = this(startTime, Nil, Nil)

  private def sampleSort(s1: Sample[Number], s2: Sample[Number]): Boolean = (s1.time isLongerThan s2.time)

  /** Returns an activity with sorted samples. */
  def sorted = new Activity(startTime, figures, samples sortWith sampleSort reverse)

  /** Activity must be sorted for this to work. */
  def duration: Duration = samples.last.time

  /** Activity must be sorted for this to work. */
  def stopTime: DateTime = startTime plus duration

  def addFigure(figure: Figure[Number]) = new Activity(startTime, figure :: figures, samples)

  def addSample(sample: Sample[Number]) = new Activity(startTime, figures, sample :: samples)

  def addSamples(samples: List[Sample[Number]]) = new Activity(startTime, figures, samples ::: this.samples)

  def figureCount() = figures.length

  def sampleCount() = samples.length

  def getFigure(d: Dimension, ft: FigureType) = figures find ((f: Figure[Number]) => (f.isDimension(d) && f.isFigureType(ft)))

  def getFigure(d: Dimension): Option[Figure[Number]] = getFigure(d, FigureType.Exact)

  def getSamples(d: Dimension) = samples filter (_.isDimension(d))

  private val zeroTime = Duration.standardSeconds(0)

  // TODO big interval values cause problems
  @tailrec
  private def nullIntervalledSamples(acc: List[Number], interval: Duration, time: Duration, samples: List[Sample[Number]]): List[Number] =
    if (time isShorterThan zeroTime)
      acc
    else {
      val take = (!samples.isEmpty) && ((time isEqual samples.head.time) || (time isShorterThan samples.head.time))
      val sample = take match {
        case true => samples.head.value
        case _ => null
      }
      val tail = take match {
        case true => samples.tail
        case _ => samples
      }
      nullIntervalledSamples(sample :: acc, interval, time minus interval, tail)
    }

  def getIntervalledSamples(d: Dimension, interval: Duration, duration: Duration): List[Number] = {
    val s = getSamples(d).sortWith(sampleSort)
    nullIntervalledSamples(Nil, interval, duration, s)
  }

}

// Route class
case class Route(name: String,
                 figures: List[Figure[Number]],
                 samples: List[Sample[Number]]) {
  val id = name

  def this(name: String) = this(name, Nil, Nil)

  private def sampleSort(s1: Sample[Number], s2: Sample[Number]): Boolean = (s1.time isLongerThan s2.time)

  /** Returns an activity with sorted samples. */
  def sorted = new Route(name, figures, samples sortWith sampleSort reverse)

  def addFigure(figure: Figure[Number]) = new Route(name, figure :: figures, samples)

  def addSample(sample: Sample[Number]) = new Route(name, figures, sample :: samples)

  def addSamples(samples: List[Sample[Number]]) = new Route(name, figures, samples ::: this.samples)

  def figureCount() = figures.length

  def sampleCount() = samples.length

  def getFigure(d: Dimension, ft: FigureType) = figures find ((f: Figure[Number]) => (f.isDimension(d) && f.isFigureType(ft)))

  def getFigure(d: Dimension): Option[Figure[Number]] = getFigure(d, FigureType.Exact)

  def getSamples(d: Dimension) = samples filter (_.isDimension(d))

}