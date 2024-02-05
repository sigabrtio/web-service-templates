package com.app.modules

import com.fasterxml.jackson.databind.{PropertyNamingStrategies, PropertyNamingStrategy}
import com.twitter.finatra.jackson.modules.ScalaObjectMapperModule

object ObjectMapperModule extends ScalaObjectMapperModule {

  override val propertyNamingStrategy: PropertyNamingStrategy =
    PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE
}