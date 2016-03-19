package controllers

import com.google.inject.Inject
import daos._
import models.{AccountAd, Ad, Answer, Question}
import play.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AdController @Inject()
(accountDAO: AccountDAO)(accountAdDAO: AccountAdDAO)
(adDAO: AdDAO)(questionDAO: QuestionDAO)
(answerDAO: AnswerDAO)
  extends Controller {

  def obtain(token: String) = Action.async { implicit request =>
    Logger.debug("AD")
    forAuthorizedUser(token, (accountId) => {
      adDAO.findRandomForUser(accountId) flatMap {
        case None => Future.successful(BadRequest(Json.toJson(errorJson("No ads available."))))
        case Some(ad) => questionDAO.findRandomQuestionForAd(ad.id.get) flatMap {
          question => answerDAO.findAnswersForQuestion(question.id.get) map {
            answer => Ok(Json.toJson(okJsonObtain(ad, question, answer)))
          }
        }
      }
    })
  }

  def answer(adId: Long, token: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    Logger.debug("ANSWER " + request.body.toString())
    implicit val answerRequestFormat = Json.format[AnswerRequest]
    request.body.validate[AnswerRequest].fold(
      error => Future.successful(BadRequest(Json.toJson(errorJson("Parsing error")))),
      answerRequest => forAuthorizedUser(token, (accountId) => {
        questionDAO.findById(answerRequest.questionId) flatMap {
          case None => Future.successful(BadRequest(Json.toJson(errorJson("Invalid query"))))
          case Some(question) => answerDAO.findAnswersForQuestion(question.id.get) flatMap {
            answers => if (checkAnswers(answers, answerRequest.answerIds)) {
              adDAO.findById(adId) map {
                case None =>
                  BadRequest(Json.toJson(errorJson("Invalid query")))
                case Some(ad) =>
                  accountDAO.addPoints(accountId, ad.points)
                  accountAdDAO.insert(AccountAd(accountId, ad.id.get))
                  Ok(Json.toJson(okJsonAnswer(ad)))
              }
            } else {
              Future.successful(BadRequest(Json.toJson(errorJson("Wrong answer"))))
            }
          }
        }
      })
    )
  }

  private def forAuthorizedUser(token: String, func: Long => Future[Result]) =
    accountDAO.findByToken(token) flatMap {
      case None => Future.successful(Unauthorized(Json.toJson(errorJson("Wrong token"))))
      case Some(account) => func(account.id.get)
    }

  private def checkAnswers(dbAnswerIds: Seq[Answer], answerIds: Seq[Long]): Boolean =
    dbAnswerIds.filter(_.isValid).map(_.id.get).sorted == answerIds.sorted

  private def errorJson(err: String = "ERR") = Json.obj(
    "answer" -> err
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
      "answers" -> {
        answers map (answer => Json.obj(
          "id" -> answer.id.get,
          "content" -> answer.content
        ))
      }
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
