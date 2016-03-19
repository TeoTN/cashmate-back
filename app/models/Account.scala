package models

case class Account(id: Option[Long], login: String, passwordHash: String, email: String, points: Int, token: String) {

  def this(login: String, passwordHash: String, email: String) = this(None, login, passwordHash, email, 0, java.util.UUID.randomUUID.toString)

}
