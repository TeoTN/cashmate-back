package daos

import com.google.inject.Inject
import models.AccountAd
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

import scala.concurrent.Future

trait AccountAdComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class AccountAds(tag: Tag) extends Table[AccountAd](tag, "account_ad") {

    def accountId = column[Long]("account_id")

    def adId = column[Long]("ad_id")

    def * = (accountId, adId) <>((AccountAd.apply _).tupled, AccountAd.unapply)
  }

}

class AccountAdDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with AccountAdComponent {

  import driver.api._

  private val accountAds = TableQuery[AccountAds]

  def insert(accountAd: AccountAd): concurrent.Future[AccountAd] =
    db.run(accountAds returning accountAds += accountAd)

}