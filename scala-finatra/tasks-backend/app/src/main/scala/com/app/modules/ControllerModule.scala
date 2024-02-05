package com.app.modules

import com.app.controllers.{GetTaskController, ListTasksController, NewTaskController, UpdateTaskController}
import com.app.repository.TasksRepository
import com.google.inject
import com.google.inject.Provides
import com.twitter.inject.TwitterModule

import javax.inject.Singleton

object ControllerModule extends TwitterModule {

  override def modules: Seq[inject.Module] = Seq(RepositoryModule)

  @Provides
  @Singleton
  def getTaskController(repository: TasksRepository): GetTaskController =
    new GetTaskController(tasksRepository = repository)

  @Provides
  @Singleton
  def listTasksController(repository: TasksRepository): ListTasksController =
    new ListTasksController(tasksRepository = repository)

  @Provides
  @Singleton
  def newTaskController(repository: TasksRepository): NewTaskController =
    new NewTaskController(tasksRepository = repository)

  @Provides
  @Singleton
  def updateTaskController(repository: TasksRepository): UpdateTaskController =
    new UpdateTaskController(tasksRepository = repository)

}
