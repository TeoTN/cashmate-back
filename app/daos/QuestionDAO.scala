package daos

import com.google.inject.Inject
import models.Question
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait QuestionComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class Questions(tag: Tag) extends Table[Question](tag, "question") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def adId = column[Long]("ad_id")

    def content = column[String]("content")

    def * = (id.?, adId, content) <>((Question.apply _).tupled, Question.unapply)

  }

}

class QuestionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with QuestionComponent {

  import driver.api._

  private val questions = TableQuery[Questions]

  private val rand = SimpleFunction.nullary[Double]("random")

  def findById(id: Long): Future[Option[Question]] =
    db.run(questions.filter(_.id === id).result.headOption)

  def findRandomQuestionForAd(adId: Long): Future[Question] =
    db.run(questions.filter(_.adId === adId).sortBy(x => rand).take(1).result.head)

}