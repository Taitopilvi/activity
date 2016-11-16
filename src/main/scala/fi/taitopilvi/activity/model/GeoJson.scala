/**
  * Copyright (C) 2016 Taitopilvi Oy - All Rights Reserved
  *
  * Unauthorized copying of this file, via any medium is strictly prohibited
  * Proprietary and confidential
  *
  * Written by Jukka Loukkaanhuhta
  */
package fi.taitopilvi.activity.model

object GeoType extends Enumeration {
  val Point, LineString, Polygon, MultiPoint, MultiPolygon, Other = Value
}

trait Geometry

case class GeoPoint
(
  `type`: String,
  coordinates: List[Double]
  ) extends Geometry {
  require(coordinates.size == 2)
}

object GeoPoint {
  def apply(longitude: Double, latitude: Double): GeoPoint =
    apply("Point", List(longitude, latitude))
}

case class GeoLineString
(
  `type`: String,
  coordinates: List[List[Double]]
  ) extends Geometry {
}

object GeoLineString {
  def apply(longitudes: List[Double], latitudes: List[Double]): GeoLineString = {
    val zipped: List[(Double, Double)] = longitudes zip latitudes
    apply("LineString", zipped map (a => List(a._1, a._2)))
  }
}