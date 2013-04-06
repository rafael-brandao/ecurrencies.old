package ecurrencies.common.akka

import akka.testkit.TestKit
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


trait SharedTestFixture {
  this: TestKit =>

  implicit val dispatcher = system.dispatcher
  implicit val timeout = Timeout(5 seconds)
}
