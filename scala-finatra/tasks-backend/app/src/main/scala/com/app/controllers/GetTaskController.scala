package com.app.controllers

import com.app.models.{Task, TaskMetadata}
import com.app.repository.TasksRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finagle.http.{Response, Status}
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
  @JsonProperty("metadata") taskMetadata: Seq[TaskMetadata]
)

class GetTaskController(tasksRepository: TasksRepository) extends Controller  {
  private val objectMapper = (new ScalaObjectMapperModule).objectMapper

  get("/task/:id") { request: GetTaskRequest =>
    Future.value(request.userId).flatMap { userId =>
        tasksRepository.getTask(
          userId = userId,
          id = request.taskId
        ).flatMap {
          case Some(task) => tasksRepository.getAllTaskMetadata(
              userId = userId,
              taskId = task.id
            ).map { tasksMetadata =>
              val resp = Response(Status.Ok).content(
                objectMapper.writeValueAsBuf(
                  GetTaskResponse(
                    task = task,
                    taskMetadata = tasksMetadata
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
