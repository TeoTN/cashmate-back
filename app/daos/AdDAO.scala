package daos

import com.google.inject.Inject
import models.Ad
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

trait AdComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class Ads(tag: Tag) extends Table[Ad](tag, "ad") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title")

    def videoLink = column[String]("video_link")

    def * = (id.?, title, videoLink) <>((Ad.apply _).tupled, Ad.unapply)
  }

}

class AdDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with AdComponent {

  import driver.api._

  private val ads = TableQuery[Ads]

}