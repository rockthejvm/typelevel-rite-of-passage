package com.rockthejvm.jobsboard.fixtures

import com.rockthejvm.jobsboard.domain.user.*

/*
rockthejvm => $2a$10$jY60jL/9Lv6./UHhhj2ZvOSm8PQIiTueC4gmsegrD5K.Yi6/mGY.m
riccardorulez => $2a$10$jDPXCNCHkbZLzmiTRuw9A.gBHRDQ1iKnYONCBuskyOln8Aa8eucFa
simplepassword => $2a$10$6LQt4xy4LzqQihZiRZGG0eeeDwDCvyvthICXzPKQDQA3C47LtrQFy
riccardorocks => $2a$10$PUD6CznGVHntJFsOOeV4NezBgBUs6irV3sC9fa6ufc0xp9VLYyHZ.
 */
trait UserFixture {
  val Daniel = User(
    "daniel@rockthejvm.com",
    "$2a$10$jY60jL/9Lv6./UHhhj2ZvOSm8PQIiTueC4gmsegrD5K.Yi6/mGY.m",
    Some("Daniel"),
    Some("Ciocirlan"),
    Some("Rock the JVM"),
    Role.ADMIN
  )
  val danielEmail    = Daniel.email
  val danielPassword = "rockthejvm"

  val Riccardo = User(
    "riccardo@rockthejvm.com",
    "$2a$10$jDPXCNCHkbZLzmiTRuw9A.gBHRDQ1iKnYONCBuskyOln8Aa8eucFa",
    Some("Riccardo"),
    Some("Cardin"),
    Some("Rock the JVM"),
    Role.RECRUITER
  )
  val riccardoEmail    = Riccardo.email
  val riccardoPassword = "riccardorulez"

  val NewUser = User(
    "newuser@gmail.com",
    "$2a$10$6LQt4xy4LzqQihZiRZGG0eeeDwDCvyvthICXzPKQDQA3C47LtrQFy",
    Some("John"),
    Some("Doe"),
    Some("Some company"),
    Role.RECRUITER
  )

  val UpdatedRiccardo = User(
    "riccardo@rockthejvm.com",
    "$2a$10$PUD6CznGVHntJFsOOeV4NezBgBUs6irV3sC9fa6ufc0xp9VLYyHZ.",
    Some("RICCARDO"),
    Some("CARDIN"),
    Some("Adobe"),
    Role.RECRUITER
  )

  val NewUserDaniel = NewUserInfo(
    danielEmail,
    danielPassword,
    Some("Daniel"),
    Some("Ciocirlan"),
    Some("Rock the JVM")
  )

  val NewUserRiccardo = NewUserInfo(
    riccardoEmail,
    riccardoPassword,
    Some("Riccardo"),
    Some("Cardin"),
    Some("Rock the JVM")
  )

}
