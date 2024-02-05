package com.app.controllers

import com.app.models.{Task, TaskStatusEntry}
import com.app.repository.TasksRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.annotations.{Header, RouteParam}
import com.twitter.finatra.jackson.modules.ScalaObjectMapperModule
import com.twitter.util.Future

case class GetTaskRequest(
  @Header("user-id") userId: String,
  @RouteParam("id") taskId: String
)

case class GetTaskResponse(
  @JsonProperty("task") task: Task,
  @JsonProperty("status-history") statusHistory: Seq[TaskStatusEntry]
)

class GetTaskController(tasksRepository: TasksRepository) extends Controller  {
  private val objectMapper = (new ScalaObjectMapperModule).objectMapper

  get("/task/:id") { request: GetTaskRequest =>
    Future.value(request.userId).flatMap { userId =>
        tasksRepository.getTask(
          userId = userId,
          id = request.taskId
        ).flatMap {
          case Some(task) => tasksRepository.getTaskStatusHistory(
              userId = userId,
              taskId = task.id
            ).map { statusHistory =>
              val resp = Response(Status.Ok).content(
                objectMapper.writeValueAsBuf(
                  GetTaskResponse(
                    task = task,
                    statusHistory = statusHistory
                  )
                )
              )
              resp.headerMap.add(
                "Content-Type", "application/JSON"
              )
              Some(resp)
            }
          case None => Future.value(None)
        }
    }
  }
}
