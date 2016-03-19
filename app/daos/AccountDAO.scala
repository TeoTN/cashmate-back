package daos

import com.google.inject.Inject
import models.Account
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait AccountComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "account") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def login = column[String]("login")

    def passwordHash = column[String]("password")

    def email = column[String]("email")

    def points = column[Int]("points")

    def token = column[String]("token")

    def * = (id.?, login, passwordHash, email, points, token) <>((Account.apply _).tupled, Account.unapply)
  }

}

class AccountDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with AccountComponent {

  import driver.api._

  private val accounts = TableQuery[Accounts]

  def findAll(): Future[Seq[Account]] =
    db.run(accounts.result)

  def findById(id: Long): Future[Option[Account]] =
    db.run(accounts.filter(_.id === id).result.headOption)

  def findByToken(token: String): Future[Option[Account]] =
    db.run(accounts.filter(_.token === token).result.headOption)

  def findByLogin(login: String): Future[Option[Account]] =
    db.run(accounts.filter(_.login === login).result.headOption)

  def insert(account: Account): Future[Account] =
    db.run(accounts returning accounts += account)

  def updateToken(oldToken: String, newToken: String): Future[Unit] = {
    val query = for {account <- accounts if account.token === oldToken} yield account.token
    db.run(query.update(newToken).map(_ => ()))
  }

  def addPoints(accountId: Long, points: Int): Future[Unit] = {
    val query = for {account <- accounts if account.id === accountId} yield account.points
    findById(accountId) map {
      case None => None
      case Some(account) => db.run(query.update(account.points + points).map(_ => ()))
    }
  }

  def removePoints(accountId: Long, points: Int): Future[Unit] = {
    val query = for {account <- accounts if account.id === accountId} yield account.points
    findById(accountId) map {
      case None => None
      case Some(account) => db.run(query.update(account.points - points).map(_ => ()))
    }
  }

}