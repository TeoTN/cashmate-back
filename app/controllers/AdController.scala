package controllers

import com.google.inject.Inject
import daos._
import models.{AccountAd, Ad, Answer, Question}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AdController @Inject()
(accountDAO: AccountDAO)(accountAdDAO: AccountAdDAO)
(adDAO: AdDAO)(questionDAO: QuestionDAO)
(answerDAO: AnswerDAO)
  extends Controller {

  def obtain = Action.async { implicit request =>
    forAuthorizedUser(request, (accountId) => {
      adDAO.findRandomForUser(accountId) flatMap {
        case None => Future.successful(BadRequest(Json.toJson(errorJson)))
        case Some(ad) => questionDAO.findRandomQuestionForAd(ad.id.get) flatMap {
          question => answerDAO.findAnswersForQuestion(question.id.get) map {
            answer => Ok(Json.toJson(okJsonObtain(ad, question, answer)))
          }
        }
      }
    })
  }

  def answer(adId: Long) = Action.async(BodyParsers.parse.json) { implicit request =>
    implicit val answerRequestFormat = Json.format[AnswerRequest]
    request.body.validate[AnswerRequest].fold(
      error => Future.successful(BadRequest(Json.toJson(errorJson))),
      answerRequest => forAuthorizedUser(request, (accountId) => {
        questionDAO.findById(answerRequest.questionId) flatMap {
          case None => Future.successful(BadRequest(Json.toJson(errorJson)))
          case Some(question) => answerDAO.findAnswersForQuestion(question.id.get) flatMap {
            answers => if (checkAnswers(answers, answerRequest.answerIds)) {
              adDAO.findById(adId) map {
                case None =>
                  BadRequest(Json.toJson(errorJson))
                case Some(ad) =>
                  accountDAO.addPoints(accountId, ad.points)
                  accountAdDAO.insert(AccountAd(accountId, ad.id.get))
                  Ok(Json.toJson(okJsonAnswer(ad)))
              }
            } else {
              Future.successful(Ok(Json.toJson(errorJson)))
            }
          }
        }
      })
    )
  }

  private def forAuthorizedUser(request: Request[AnyRef], func: Long => Future[Result]) =
    request.session.get("id") map {
      accountId => func(accountId.toLong)
    } getOrElse {
      Future.successful(Unauthorized(Json.toJson(errorJson)))
    }

  private def checkAnswers(dbAnswerIds: Seq[Answer], answerIds: Seq[Long]): Boolean =
    dbAnswerIds.filter(_.isValid).map(_.id.get).sorted == answerIds.sorted

  private def errorJson = Json.obj(
    "answer" -> "ERR"
  )

  private def okJsonObtain(ad: Ad, question: Question, answers: Seq[Answer]) = Json.obj(
    "answer" -> "OK",
    "id" -> ad.id.get,
    "title" -> ad.title,
    "videoUrl" -> ad.videoUrl,
    "points" -> ad.points,
    "vendor" -> ad.vendor,
    "question" -> Json.obj(
      "id" -> question.id.get,
      "content" -> question.content,
      "answers" -> Json.arr(
        answers map (answer => Json.obj(
          "id" -> answer.id.get,
          "content" -> answer.content
        ))
      )
    )
  )

  private def okJsonAnswer(ad: Ad) = Json.obj(
    "answer" -> "OK",
    "points" -> ad.points
  )

  private def errJsonAnswer = Json.obj(
    "answer" -> "ERR"
  )

  private case class AnswerRequest(questionId: Long, answerIds: Seq[Long])

}
