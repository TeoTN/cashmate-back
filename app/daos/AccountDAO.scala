/**
  * Copyright (C) Bright IT 2016. All rights reserved.
  * Author: Michal Kielbowicz
  */
package daos

import com.google.inject.Inject
import models.Account
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

import scala.concurrent.Future

trait AccountComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "account") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def login = column[String]("login")

    def passwordHash = column[String]("password")

    def email = column[String]("email")

    def points = column[Int]("points")

    def * = (id.?, login, passwordHash, email, points) <>((Account.apply _).tupled, Account.unapply)
  }

}

class AccountDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with AccountComponent {

  import driver.api._

  private val accounts = TableQuery[Accounts]

  def findAll(): Future[Seq[Account]] =
    db.run(accounts.result)

  def findByLogin(login: String): Future[Option[Account]] =
    db.run(accounts.filter(_.login === login).result.headOption)

  def insert(account: Account): Future[Account] =
    db.run(accounts returning accounts += account)

}