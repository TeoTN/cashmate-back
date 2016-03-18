/**
  * Copyright (C) Bright IT 2016. All rights reserved.
  * Author: Michal Kielbowicz
  */
package models

case class Account(id: Option[Long], login: String, passwordHash: String, email: String, points: Int)
