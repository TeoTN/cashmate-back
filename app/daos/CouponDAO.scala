package daos

import com.google.inject.Inject
import models.Coupon
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

import scala.concurrent.Future

trait CouponComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class Coupons(tag: Tag) extends Table[Coupon](tag, "coupon") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def location = column[String]("location")

    def imageUrl = column[String]("img_url")

    def title = column[String]("title")

    def points = column[Int]("points")

    def vendor = column[String]("vendor")

    def * = (id.?, location, imageUrl, title, points, vendor) <>((Coupon.apply _).tupled, Coupon.unapply)

  }

}

class CouponDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with CouponComponent {

  import driver.api._

  private val coupons = TableQuery[Coupons]

  def findAll(): Future[Seq[Coupon]] =
    db.run(coupons.result)

  def findById(id: Long): Future[Option[Coupon]] =
    db.run(coupons.filter(_.id === id).result.headOption)

  def findByPattern(pattern: String): Future[Seq[Coupon]] =
    db.run(coupons.filter(coupon => (coupon.location like pattern) || (coupon.title like pattern)).result)

}