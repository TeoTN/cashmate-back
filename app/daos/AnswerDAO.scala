package daos

import com.google.inject.Inject
import models.{Answer, Question}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AnswerComponent {

  self: HasDatabaseConfig[PostgresDriver] =>

  import driver.api._

  class Answers(tag: Tag) extends Table[Answer](tag, "answer") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def questionId = column[Long]("question_id")

    def content = column[String]("content")

    def isValid = column[Boolean]("is_valid")

    def * = (id.?, questionId, content, isValid) <>((Answer.apply _).tupled, Answer.unapply)
  }

}

class AnswerDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[PostgresDriver] with AnswerComponent {

  import driver.api._

  private val answers = TableQuery[Answers]

  private val rand = SimpleFunction.nullary[Double]("random")

  def findAnswersForQuestion(questionId: Long): Future[Seq[Answer]] =
    db.run(answers.filter(_.questionId === questionId).result)

}