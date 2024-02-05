package com.app.controllers

import com.app.models.{Task, TaskStatus}
import com.app.repository.TasksRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.annotations.Header
import com.twitter.finatra.jackson.modules.ScalaObjectMapperModule
import com.twitter.util.Future

case class NewTaskRequest(
  @Header("user-id") userId: String,
  @JsonProperty("description") description: String,
)

case class NewTaskResponse(
  @JsonProperty("id") id: String
)

class NewTaskController(tasksRepository: TasksRepository) extends Controller {
  private val objectMapper = (new ScalaObjectMapperModule).objectMapper

  post("/tasks/new") { newTaskRequest: NewTaskRequest =>
    Future.value(newTaskRequest.userId).flatMap { userId =>
      tasksRepository.insertTask(
        userId = userId,
        task = Task(
          id = "",
          description = newTaskRequest.description,
          status = TaskStatus.Created
        )
      ).map { taskId =>
        val resp = Response(Status.Ok).content(
          objectMapper.writeValueAsBuf(
            NewTaskResponse(taskId)
          )
        )
        resp.headerMap.add("Content-Type", "application/json")
        Option(resp)
      }
    }

  }

}
