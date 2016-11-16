/**
  * Copyright (C) 2016 Taitopilvi Oy - All Rights Reserved
  *
  * Unauthorized copying of this file, via any medium is strictly prohibited
  * Proprietary and confidential
  *
  * Written by Jukka Loukkaanhuhta
  */
package fi.taitopilvi.activity.output

import fi.taitopilvi.activity._
import fi.taitopilvi.activity.model.GeoLineString
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods._

class ActivityGeoJSONOutput(activity: Activity) extends Output {
  require(activity != null)

  private def toValue(value: Any): JValue =
    value match {
      case f: Figure[_] => toValue(f.value)
      case n: Number => JDouble(n.doubleValue())
    }

  private def route: List[JArray] = {
    val sorted = activity.sorted
    val latitudeSamples = sorted.getSamples(Dimension.Latitude)
    val longitudeSamples = sorted.getSamples(Dimension.Longitude)
    val route = latitudeSamples zip longitudeSamples
    route.map((point) =>
      new JArray(List[JValue](
        toValue(point._1.value),
        toValue(point._2.value)
      )))
  }

  def toJObject: JObject =
    new JObject(List(
      "type" -> JString("LineString"),
      "coordinates" -> new JArray(route)))

  def toGeoLineString: GeoLineString = {
    val sorted = activity.sorted
    val latitudeSamples = sorted.getSamples(Dimension.Latitude).map(_.value.doubleValue())
    val longitudeSamples = sorted.getSamples(Dimension.Longitude).map(_.value.doubleValue())
    GeoLineString(longitudeSamples, latitudeSamples)
  }

  override def toString: String = pretty(render(toJObject))

}
