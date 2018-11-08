package demo

import com.twitter.finagle.{Parallel, Serial, Service}
import com.twitter.util._

object Main extends com.twitter.app.App {

  val serviceA = Service.mk[String, String](s =>
    Future.value(s)
  )

  val client = Parallel.client[String, String]().newService("localhost:9999")

  val serviceB = Service.mk[String, String](s =>
    client(s + "\n")
  )


  val serverA = Serial.server[String, String]().serve(":9080", serviceA)
  val serverB = Serial.server[String, String]().serve(":9081", serviceB)

  closeOnExit(serverB)

  sys.addShutdownHook(serverA.close())

  def main(): Unit = {
    Await.ready(serverA)
  }
}
