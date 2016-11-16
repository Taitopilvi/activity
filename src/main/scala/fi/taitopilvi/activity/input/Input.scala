/**
  * Copyright (C) 2016 Taitopilvi Oy - All Rights Reserved
  *
  * Unauthorized copying of this file, via any medium is strictly prohibited
  * Proprietary and confidential
  *
  * Written by Jukka Loukkaanhuhta
  */
package fi.taitopilvi.activity.input

import fi.taitopilvi.activity.{Activity, Route}

trait Input {
  def toActivities: Seq[Activity]
  def toRoutes: Seq[Route]
}
