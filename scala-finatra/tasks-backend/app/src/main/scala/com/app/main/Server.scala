package com.app.main

import com.app.controllers.{ErrorController, GetTaskController, ListTasksController, NewTaskController, NotFoundExceptionMapper, UpdateTaskController}
import com.app.modules.{ControllerModule, ErrorModule}
import com.google.inject
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter


class Server extends HttpServer {
  override def modules: Seq[inject.Module] = Seq(
    ControllerModule,
    ErrorModule
  )

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[GetTaskController]
      .add[ListTasksController]
      .add[NewTaskController]
      .add[UpdateTaskController]
      .add[ErrorController]
      .exceptionMapper[NotFoundExceptionMapper]
  }

  //
  override def failfastOnFlagsNotParsed: Boolean = true
}

object ServerMain extends Server