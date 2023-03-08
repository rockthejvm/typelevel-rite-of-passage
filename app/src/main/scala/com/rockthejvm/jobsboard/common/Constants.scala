package com.rockthejvm.jobsboard.common

object Constants {
  val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""

  object Endpoints {
    val root   = "http://localhost:4041"
    val signup = s"$root/api/auth/users"
  }
}
