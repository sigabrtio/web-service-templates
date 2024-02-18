package com.app.repository

import com.app.models.{Task, TaskMetadata, TaskStatus}
import com.twitter.util.{Future, FuturePool, Try}
import org.joda.time.DateTime

import java.sql.{Connection, Types}
import java.util.UUID

trait TasksRepository {
  def getTask(userId: String, id: String): Future[Option[Task]]

  def listTasks(userId: String): Future[Seq[String]]

  def getAllTaskMetadata(userId: String, taskId: String): Future[Seq[TaskMetadata]]

  def insertTask(userId: String, task: Task): Future[String]

  def updateTask(userId: String, task: Task): Future[Unit]

  def insertTaskMetadata(userId: String, taskId: String, metadata: TaskMetadata): Future[Long]

  def deleteMetadata(userId: String, taskId: String, metadataId: Long): Future[Unit]
}

class TasksRepositoryJdbc(val conn: Connection) extends TasksRepository {
  private val TasksTable = "tasks";
  private val TaskMetadataTable = "task_metadata"
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

  override def getAllTaskMetadata(userId: String, taskId: String): Future[Seq[TaskMetadata]] = {
    var statuses = Seq[TaskMetadata]()
    FuturePool.unboundedPool {
      val statement = conn.prepareStatement(
        s"""
           |
           |SELECT
           |  id,
           |  metadata_name,
           |  metadata_type,
           |  create_time,
           |  metadata_long_value,
           |  metadata_double_value,
           |  metadata_short_text_value,
           |  metadata_uuid_value,
           |  metadata_boolean_value
           |FROM $TaskMetadataTable WHERE user_id=? AND task_id=?;
           |
           |""".stripMargin
      )
      statement.setString(1, userId)
      statement.setString(2, taskId)
      val results = statement.executeQuery()
      while (results.next()) {
        statuses = statuses :+ TaskMetadata(
          id = results.getLong("id"),
          metadataName = results.getString("metadata_name"),
          metadataType = results.getString("metadata_type"),
          createDate = Some(new DateTime(results.getDate("create_time"))),
          longValue = Option(results.getLong("metadata_long_value")),
          doubleValue = Option(results.getDouble("metadata_double_value")),
          shortStringValue = Option(results.getString("metadata_short_text_value")),
          uuidValue = Option(results.getString("metadata_uuid_value")).map(UUID.fromString),
          booleanValue = Option(results.getBoolean("metadata_boolean_value"))
        )
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
           |INSERT INTO $TaskMetadataTable (user_id,task_id,metadata_name,metadata_type,short_text_value) VALUES
           |  (?,?,?, ?,?);
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
      statement.setString(7, TaskStatus.MetadataName)
      statement.setString(8, TaskStatus.MetadataType)
      statement.setString(9, TaskStatus.Created)

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

  override def insertTaskMetadata(userId: String, taskId: String, metadata: TaskMetadata): Future[Long] = {
    FuturePool.unboundedPool {
      val statement = conn.prepareStatement(
        s"""
           |
           |INSERT INTO $TaskMetadataTable (
           |  user_id,
           |  task_id,
           |  metadata_name,
           |  metadata_type,
           |  metadata_long_value,
           |  metadata_double_value,
           |  metadata_short_text_value,
           |  metadata_uuid_value,
           |  metadata_boolean_value)
           |VALUES
           |  (?, ?, ?, ?, ?, ?, ?, ?, ?)
           |RETURNING id;
           |""".stripMargin
      )
      statement.setString(1, userId)
      statement.setString(2, taskId)
      statement.setString(3, metadata.metadataName)
      statement.setString(4,metadata.metadataType)

      metadata.longValue.map(statement.setLong(5, _)).getOrElse(statement.setNull(5, Types.BIGINT))
      metadata.doubleValue.map(statement.setDouble(6, _)).getOrElse(statement.setNull(6, Types.DOUBLE))
      metadata.shortStringValue.map(statement.setString(7, _)).getOrElse(statement.setNull(7, Types.VARCHAR))
      metadata.uuidValue.map { uuid => statement.setString(8, f"${uuid}::uuid")}.getOrElse(statement.setNull(8, Types.OTHER))
      metadata.booleanValue.map(statement.setBoolean(9, _)).getOrElse(statement.setNull(9, Types.BOOLEAN))

      val res = statement.executeQuery()
      res.next()
      res.getLong("id")
    }
  }

  override def deleteMetadata(userId: String, taskId: String, metadataId: Long): Future[Unit] = {
    FuturePool.unboundedPool {
      val statement = conn.prepareStatement(
        s"""
           |DELETE FROM $TaskMetadataTable WHERE
           | id=? AND user_id=? AND task_id=?
           |""".stripMargin
      )

      statement.setLong(1, metadataId)
      statement.setString(2, userId)
      statement.setString(3, taskId)

      statement.execute()
    }
  }
}
