package com.app.modules

import com.app.controllers.ErrorController
import com.google.inject.Provides
import com.twitter.inject.TwitterModule

import javax.inject.Singleton

object ErrorModule extends TwitterModule {
  @Provides
  @Singleton
  def getErrorController: ErrorController = new ErrorController
}
