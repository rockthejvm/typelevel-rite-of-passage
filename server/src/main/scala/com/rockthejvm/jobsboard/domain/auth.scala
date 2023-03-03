package com.rockthejvm.jobsboard.domain

object auth {
  final case class LoginInfo(
      email: String,
      password: String
  )

  final case class NewPasswordInfo(
      oldPassword: String,
      newPassword: String
  )

  final case class ForgotPasswordInfo(email: String)

  final case class RecoverPasswordInfo(
      email: String,
      token: String,
      newPassword: String
  )
}
