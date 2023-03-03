package com.rockthejvm.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import pureconfig.error.CannotConvert

// generates given configReader: ConfigReader[EmberConfig]
final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {
  // need given ConfigReader[Host] + given ConfigReader[Port] => compiler generates ConfigReader[EmberConfig]
  given hostReader: ConfigReader[Host] = ConfigReader[String].emap { hostString =>
    Host
      .fromString(hostString)
      .toRight(
        CannotConvert(hostString, Host.getClass.toString, s"Invalid host string: $hostString")
      )
  }

  given portReader: ConfigReader[Port] = ConfigReader[Int].emap { portInt =>
    Port
      .fromInt(portInt)
      .toRight(
        CannotConvert(portInt.toString, Port.getClass.toString, s"Invalid port number: $portInt")
      )
  }
}
