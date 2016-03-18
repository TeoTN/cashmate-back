package models

case class Answer(id: Option[Long], questionId: Long, content: String, isValid: Boolean)