package daos

import com.google.inject.Inject
import models.{AccountAd, Ad}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

trait AdComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class Ads(tag: Tag) extends Table[Ad](tag, "ad") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title")

    def videoLink = column[String]("video_link")

    def points = column[Int]("points")

    def * = (id.?, title, videoLink, points) <>((Ad.apply _).tupled, Ad.unapply)

  }

}

class AdDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with AdComponent with AccountAdComponent {

  import driver.api._

  private val ads = TableQuery[Ads]
  private val accountAds = TableQuery[AccountAds]

  private val rand = SimpleFunction.nullary[Double]("random")

  def findById(id: Long): Future[Option[Ad]] =
    db.run(ads.filter(_.id === id).result.headOption)

  def findRandomForUser(accountId: Long): Future[Option[Ad]] =
    db.run(findUnseenQuery(accountId).sortBy(x => rand).take(1).result.headOption)

  private def findUnseenQuery(accountId: Long) = for {
    accountAd <- accountAds if accountAd.accountId =!= accountId
    ad <- ads if ad.id === accountAd.adId
  } yield ad


}