package com.app.controllers

import com.google.inject.Singleton
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.http.exceptions.{ExceptionMapper, NotFoundException}


@Singleton
class NotFoundExceptionMapper extends ExceptionMapper[NotFoundException] {
  override def toResponse(request: Request, throwable: NotFoundException): Response = Response(Status.NotFound)

}
