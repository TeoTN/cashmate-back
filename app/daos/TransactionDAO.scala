package daos

import com.google.inject.Inject
import models.{Coupon, Transaction}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait TransactionComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class Transactions(tag: Tag) extends Table[Transaction](tag, "transaction") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def code = column[Int]("code")

    def timestamp = column[Long]("timestamp")

    def couponId = column[Long]("coupon_id")

    def accountId = column[Long]("account_id")

    def acceptedByVendor = column[Boolean]("accepted_vendor")

    def * = (id.?, code, timestamp, couponId, accountId, acceptedByVendor) <>((Transaction.apply _).tupled, Transaction.unapply)

  }

}

class TransactionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with TransactionComponent {

  import driver.api._

  private val transactions = TableQuery[Transactions]

  def insert(transaction: Transaction): Future[Transaction] =
    db.run(transactions returning transactions += transaction)

  def findById(id: Long): Future[Option[Transaction]] =
    db.run(transactions.filter(_.id === id).result.headOption)

  def findByCode(code: Int): Future[Option[Transaction]] =
    db.run(transactions.filter(_.code === code).result.headOption)

  def acceptTransaction(id: Long): Future[Unit] = {
    val query = for {transaction <- transactions if transaction.id === id} yield transaction.acceptedByVendor
    db.run(query.update(true).map(_ => ()))
  }

  def deleteById(id: Long): Future[Unit] = {
    db.run(transactions.filter(_.id === id).delete.map(_ => ()))
  }


}