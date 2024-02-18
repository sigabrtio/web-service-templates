package com.app.models

import com.fasterxml.jackson.annotation.JsonProperty

case class Error (
  @JsonProperty("message") message: String
)
