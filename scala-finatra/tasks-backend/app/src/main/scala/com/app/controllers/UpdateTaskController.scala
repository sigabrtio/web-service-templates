package com.app.controllers

import com.app.repository.TasksRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.annotations.Header
import com.twitter.finatra.http.exceptions.NotFoundException
import com.twitter.util.Future

case class UpdateTaskRequest(
  @Header("user-id") userId: String,
  @JsonProperty("id") taskId: String,
  @JsonProperty("description") description: String,
  @JsonProperty("status") status: String
)

class UpdateTaskController(tasksRepository: TasksRepository) extends Controller {

  post("/tasks/update/:id") { updateTasksRequest: UpdateTaskRequest =>
    Future.value(updateTasksRequest.userId).flatMap { userId =>
      tasksRepository.getTask(
        userId = userId,
        id = updateTasksRequest.taskId
      ).map {
        case Some(task) => tasksRepository.updateTask(
            userId = userId,
            task = task
          ).flatMap { _ =>
            if (task.status != updateTasksRequest.status) {
              tasksRepository.updateTaskStatusHistory(
                userId = userId,
                taskId = updateTasksRequest.taskId,
                newStatus = updateTasksRequest.status
              )
            } else {
              Future.Unit
            }
          }
        case None => throw new NotFoundException(s"Task with id ${updateTasksRequest.taskId} not found!")
      }
    }

  }

}
