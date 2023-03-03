package com.rockthejvm.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*
import scala.concurrent.duration.FiniteDuration

final case class SecurityConfig(secret: String, jwtExpiryDuration: FiniteDuration)
    derives ConfigReader
