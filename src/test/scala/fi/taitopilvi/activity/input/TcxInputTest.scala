/**
  * Copyright (C) 2016 Taitopilvi Oy - All Rights Reserved
  *
  * Unauthorized copying of this file, via any medium is strictly prohibited
  * Proprietary and confidential
  *
  * Written by Jukka Loukkaanhuhta
  */
package fi.taitopilvi.activity.input

import fi.taitopilvi.activity.{Dimension, FigureType}
import org.joda.time.DateTime
import org.scalatest.FunSuite

class TcxInputTest extends FunSuite {
  test("test1.tcx") {
    val tcxInput = new TcxInput("src/test/input/test1.tcx")
    val activities = tcxInput.toActivities
    val routes = tcxInput.toRoutes
    activities.foreach(activity => println("Activity: " + activity.id))
    assert(routes.size === 0)
    assert(activities.size === 1)
    assert(activities.head.getSamples(Dimension.HeartRate).size === 0)
    assert(activities.head.getFigure(Dimension.Energy).get.value === 99)
  }

  test("test2.tcx") {
    val tcxInput = new TcxInput("src/test/input/test2.tcx")
    val activities = tcxInput.toActivities
    val routes = tcxInput.toRoutes
    activities.foreach(activity => println("Activity: " + activity.id))
    assert(routes.size === 0)
    assert(activities.size === 1)
    assert(activities.head.getSamples(Dimension.HeartRate).size > 0)
    assert(activities.head.getSamples(Dimension.Speed).size > 0)
    assert(activities.head.getSamples(Dimension.Distance).size > 0)
    assert(activities.head.getSamples(Dimension.Altitude).size > 0)
    assert(activities.head.getSamples(Dimension.Latitude).size > 0)
    assert(activities.head.getSamples(Dimension.Longitude).size > 0)
    assert(activities.head.getFigure(Dimension.HeartRate, FigureType.Average).get.value.intValue === 123)
  }

  test("route1.tcx") {
    val tcxInput = new TcxInput("src/test/input/route1.tcx")
    val activities = tcxInput.toActivities
    val routes = tcxInput.toRoutes
    routes.foreach(route => println("Route: " + route.name))
    assert(routes.size === 1)
    assert(activities.size === 0)
    assert(routes.head.getFigure(Dimension.Time).get.value.longValue ===
      new DateTime("2016-04-12T10:12:30+00:00").getMillis)
    assert(routes.head.getSamples(Dimension.Distance).size === 13)
    assert(routes.head.getSamples(Dimension.Altitude).size === 13)
    assert(routes.head.getSamples(Dimension.Latitude).size === 13)
    assert(routes.head.getSamples(Dimension.Longitude).size === 13)
    assert(routes.head.getFigure(Dimension.Distance).get.value === 462)
  }

}
