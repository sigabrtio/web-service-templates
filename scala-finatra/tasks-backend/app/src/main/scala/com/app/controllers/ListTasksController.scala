package com.app.controllers

import com.app.repository.TasksRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.annotations.Header
import com.twitter.finatra.jackson.modules.ScalaObjectMapperModule
import com.twitter.util.Future

case class ListTasksRequest(
  @Header("user-id") userId: String
)

case class ListTasksResponse(
  @JsonProperty("task-ids") taskIds: Seq[String]
)

class ListTasksController(tasksRepository: TasksRepository) extends Controller {
  private val objectMapper = (new ScalaObjectMapperModule).objectMapper

  get("/tasks") {
    (request: ListTasksRequest) => {
      Future.value(request.userId).flatMap { userId =>
        tasksRepository.listTasks(userId).map { taskIds =>
          val resp = Response(Status.Ok).content(
            objectMapper.writeValueAsBuf(
              ListTasksResponse(
                taskIds = taskIds
              )
            )
          )
          resp.headerMap.add(
            "Content-Type", "application/JSON"
          )
          Option(resp)
        }
      }
    }
  }
}
