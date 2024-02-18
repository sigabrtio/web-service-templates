package com.app.models

import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.DateTime
import java.util.UUID

case class Task(
  @JsonProperty("id") id: String,
  @JsonProperty("description") description: String,
  @JsonProperty("status") status: String,
)

case class TaskMetadata(
  @JsonProperty("id") id: Long,
  @JsonProperty("name") metadataName: String,
  @JsonProperty("type") metadataType: String,
  @JsonProperty("created_at") createDate: Option[DateTime] = None,

  @JsonProperty("long_value") longValue: Option[Long] = None,
  @JsonProperty("double_value") doubleValue: Option[Double] = None,
  @JsonProperty("short_text_value") shortStringValue: Option[String] = None,
  @JsonProperty("uuid_value") uuidValue: Option[UUID] = None,
  @JsonProperty("boolean_value") booleanValue: Option[Boolean] = None
)

object TaskStatus {
  val MetadataName = "task_status"
  val MetadataType = "short_text_value"

  val Created = "created"
  val InProgress = "in_progress"
  val Blocked = "blocked"
  val Finished = "finished"
}
