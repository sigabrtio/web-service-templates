package com.app.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.exceptions.NotFoundException

class ErrorController extends Controller {

  get("/:*") { _: Request =>
    throw new NotFoundException("")
  }
}
