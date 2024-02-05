package com.app.modules

import com.app.repository.{TasksRepository, TasksRepositoryJdbc}
import com.google.inject.Provides
import com.twitter.inject.TwitterModule

import java.sql.{Connection, DriverManager}
import javax.inject.Singleton

object RepositoryModule extends TwitterModule {

  private val url = flag[String]("db.url", "Database url")
  private val user = flag[String]("db.user", "Database user")
  private val password = flag[String]("db.password", "Database password")

  @Provides
  @Singleton
  def getConnection: Connection = DriverManager.getConnection(url.get.get, user.get.get, password.get.get)


  @Provides
  @Singleton
  def getRepository(connection: Connection): TasksRepository = new TasksRepositoryJdbc(conn = connection)
}
