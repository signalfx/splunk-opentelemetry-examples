import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object HelloServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "hello-system")
    implicit val ec: ExecutionContextExecutor   = system.executionContext

    val route =
      pathSingleSlash {
        get {
          complete("Hello, world!")
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println("Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}