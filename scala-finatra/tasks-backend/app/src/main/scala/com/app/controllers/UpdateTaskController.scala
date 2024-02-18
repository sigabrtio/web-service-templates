package com.app.controllers

import com.app.models.{Task, TaskMetadata, TaskStatus}
import com.app.repository.TasksRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.annotations.{Header, RouteParam}
import com.twitter.finatra.http.exceptions.NotFoundException
import com.twitter.finatra.jackson.modules.ScalaObjectMapperModule
import com.twitter.io.Buf
import com.twitter.util.Future

case class UpdateTaskRequest(
  @Header("user-id") userId: String,
  @RouteParam("id") taskId: String,
  @JsonProperty("description") description: String,
  @JsonProperty("status") status: String
)

case class UpdateTaskResponse(
  @JsonProperty("success") success: Boolean
)

class UpdateTaskController(tasksRepository: TasksRepository) extends Controller {
  private val objectMapper = (new ScalaObjectMapperModule).objectMapper
  private def validateStatus(status: String): Unit = {
    if (status != TaskStatus.Created && status != TaskStatus.InProgress && status != TaskStatus.Blocked && status != TaskStatus.Finished) {
      throw new IllegalArgumentException(s"$status is not a valid status.")
    }
  }

  post("/tasks/update/:id") { updateTasksRequest: UpdateTaskRequest => {
      Future.value(updateTasksRequest.userId).flatMap { userId =>
        validateStatus(updateTasksRequest.status)
        tasksRepository.getTask(
          userId = userId,
          id = updateTasksRequest.taskId
        ).flatMap {
          case Some(task) => tasksRepository.updateTask(
            userId = userId,
            task = Task(
              id = task.id,
              description = updateTasksRequest.description,
              status = updateTasksRequest.status
            )
          ).flatMap { _: Unit =>
            val updateMetadataAction: Future[Unit] = if (task.status != updateTasksRequest.status) {
              info(s"Updating status from ${task.status} to ${updateTasksRequest.status}")
              tasksRepository.insertTaskMetadata(
                userId = userId,
                taskId = updateTasksRequest.taskId,
                metadata = TaskMetadata(
                  id = -1,
                  metadataName = TaskStatus.MetadataName,
                  metadataType = TaskStatus.MetadataType,
                  shortStringValue = Some(updateTasksRequest.status)
                )
              ).map(_ => ())
            } else {
              info("No need to update status.")
              Future.Unit
            }
            updateMetadataAction.map { _ =>
              val resp = Response(Status.Ok).content(
                objectMapper.writeValueAsBuf(
                  UpdateTaskResponse(
                    success = true
                  )
                )
              )
              resp.headerMap.add("Content-Type", "application/JSON")
              Option(resp)
            }
          }

          case None => throw new NotFoundException(s"Task with id ${updateTasksRequest.taskId} not found!")
        }
      }
    }
  }

}
