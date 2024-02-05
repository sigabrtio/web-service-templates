package com.app.repository

import com.app.models.{Task, TaskStatus, TaskStatusEntry}
import com.twitter.util.{Future, FuturePool}

import java.sql.Connection
import java.util.UUID

trait TasksRepository {
  def getTask(userId: String, id: String): Future[Option[Task]]

  def listTasks(userId: String): Future[Seq[String]]

  def getTaskStatusHistory(userId: String, taskId: String): Future[Seq[TaskStatusEntry]]

  def insertTask(userId: String, task: Task): Future[String]

  def updateTask(userId: String, task: Task): Future[Unit]

  def updateTaskStatusHistory(userId: String, taskId: String, newStatus: String): Future[Unit]
}

class TasksRepositoryJdbc(val conn: Connection) extends TasksRepository {
  private val TasksTable = "tasks";
  private val TaskStatusHistoryTable = "task_status_history"
  override def getTask(userId: String, id: String): Future[Option[Task]] = {
    FuturePool.unboundedPool {
      val statement = conn.prepareStatement(
        s"""
           |SELECT id,description,status,create_time FROM $TasksTable WHERE userid=? AND id=?
           |""".stripMargin
      )
      statement.setString(1, userId)
      statement.setString(2, id)

      val results = statement.executeQuery()
      if (results.next()) {
         Some(
          Task(
            id = results.getString("id"),
            description = results.getString("description"),
            status = results.getString("status"),
          )
        )
      } else {
        Option.empty[Task]
      }
    }
  }

  override def listTasks(userId: String): Future[Seq[String]] = {
    var taskIds = Seq[String]()
    FuturePool.unboundedPool {
      val statement = conn.prepareStatement(
        s"""
           |
           |SELECT id FROM $TasksTable WHERE userid=?;
           |
           |""".stripMargin
      )
      statement.setString(1, userId)
      val results = statement.executeQuery()
      while (results.next()) {
        taskIds = taskIds :+ results.getString(1)
      }
      taskIds
    }
  }

  override def getTaskStatusHistory(userId: String, taskId: String): Future[Seq[TaskStatusEntry]] = {
    var statuses = Seq[TaskStatusEntry]()
    FuturePool.unboundedPool {
      val statement = conn.prepareStatement(
        s"""
           |
           |SELECT status FROM $TaskStatusHistoryTable WHERE user_id=? AND task_id=?;
           |
           |""".stripMargin
      )
      statement.setString(1, userId)
      statement.setString(2, taskId)
      val results = statement.executeQuery()
      while (results.next()) {
        statuses = statuses :+ TaskStatusEntry(status = results.getString(1))
      }
      statuses
    }
  }

  override def insertTask(userId: String, task: Task): Future[String] = {
    FuturePool.unboundedPool {
      val taskId = UUID.randomUUID().toString
      val statement = conn.prepareStatement(
        s"""
           |BEGIN;
           |
           |INSERT INTO $TasksTable (userid,id,description,status) VALUES
           |  (?,?,?,?);
           |
           |INSERT INTO $TaskStatusHistoryTable (user_id,task_id,status) VALUES
           |  (?,?,?);
           |
           |END;
           |""".stripMargin
      )
      statement.setString(1, userId)
      statement.setString(2,taskId)
      statement.setString(3, task.description)
      statement.setString(4, task.status)

      statement.setString(5, userId)
      statement.setString(6, task.id)
      statement.setString(7, TaskStatus.Created)

      statement.execute()
      taskId
    }


  }

  override def updateTask(userId: String, task: Task): Future[Unit] = {
    FuturePool.unboundedPool {
      val statement = conn.prepareStatement(
        s"""
           |UPDATE $TasksTable SET description=?,status=? WHERE
           |  userid=? AND id=?
           |""".stripMargin
      )
      statement.setString(1, task.description)
      statement.setString(2,task.status)
      statement.setString(3, userId)
      statement.setString(4, task.id)
      statement.execute()
    }
  }

  override def updateTaskStatusHistory(userId: String, taskId: String, newStatus: String): Future[Unit] = {
    FuturePool.unboundedPool {
      val statement = conn.prepareStatement(
        s"""
           |
           |INSERT INTO $TaskStatusHistoryTable (user_id,task_id,status) VALUES
           |  (?, ?, ?)
           |""".stripMargin
      )
      statement.setString(1, userId)
      statement.setString(2,taskId)
      statement.setString(3, newStatus)
      statement.execute()
    }
  }
}
