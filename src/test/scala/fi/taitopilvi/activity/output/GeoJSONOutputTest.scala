/**
  * Copyright (C) 2016 Taitopilvi Oy - All Rights Reserved
  *
  * Unauthorized copying of this file, via any medium is strictly prohibited
  * Proprietary and confidential
  *
  * Written by Jukka Loukkaanhuhta
  */
package fi.taitopilvi.activity.output

import fi.taitopilvi.activity.Activity
import fi.taitopilvi.activity.input.TcxInput
import fi.taitopilvi.activity.model.{GeoLineString, GeoPoint}
import org.joda.time.DateTime
import org.json4s._
import org.json4s.native.Serialization.write
import org.scalatest.FunSuite

class ActivityGeoJSONOutputTest extends FunSuite {
  implicit val formats = DefaultFormats

  val startTime = DateTime.now

  def test1tcx1 = {
    val tcxInput = new TcxInput("src/test/input/test1.tcx")
    val activities: Seq[Activity] = tcxInput.toActivities
    activities.head
  }

  test("test1.tcx to String") {
    val activity = test1tcx1
    val output = new ActivityGeoJSONOutput(activity)
    println(output)
  }

  test("GeoPoint to String") {
    val point = GeoPoint(62.88808476179838, 27.694242177531123)
    println(write(point))
  }

  test("GeoLineString to String") {
    val line = GeoLineString(
      List(62.88808476179838, 62.88231206126511),
      List(27.694242177531123, 27.672076150774956)
    )
    println(write(line))
  }
}
