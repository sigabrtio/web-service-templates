package com.app.models

import com.fasterxml.jackson.annotation.JsonProperty

case class Task(
  @JsonProperty("id") id: String,
  @JsonProperty("description") description: String,
  @JsonProperty("status") status: String,
)

case class TaskStatusEntry(
  @JsonProperty("status") status: String
)


object TaskStatus {
  val Created = "created"
  val InProgress = "in_progress"
  val Paused = "paused"
  val Complete = "complete"
}
