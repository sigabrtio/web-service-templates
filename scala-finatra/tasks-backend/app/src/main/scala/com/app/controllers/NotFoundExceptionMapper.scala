package com.app.controllers

import com.app.models.Error
import com.google.inject.Singleton
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.http.exceptions.{ExceptionMapper, NotFoundException}
import com.twitter.finatra.jackson.modules.ScalaObjectMapperModule

@Singleton
class NotFoundExceptionMapper extends ExceptionMapper[NotFoundException] {
  private val objectMapper = (new ScalaObjectMapperModule).objectMapper
  override def toResponse(request: Request, throwable: NotFoundException): Response = {
    val resp = Response(Status.NotFound).content(
      objectMapper.writeValueAsBuf(
        Error(
          message = throwable.getMessage
        )
      )
    )
    resp.headerMap.add("Content-Type", "application/JSON")
    resp
  }

}
