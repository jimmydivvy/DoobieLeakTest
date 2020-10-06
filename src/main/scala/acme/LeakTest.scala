package acme

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource, Sync}
import com.zaxxer.hikari.HikariDataSource
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.syntax._
import doobie.implicits._

import scala.concurrent.duration._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.instances.either._

import scala.concurrent.ExecutionContext

object LeakTest extends IOApp {


  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO]    // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.h2.Driver",                        // driver classname
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",   // connect URL
        "sa",                                   // username
        "",                                     // password
        ce,                                     // await connection here
        be                                      // execute JDBC operations here
      )

      _ <- Resource.liftF {
        xa.configure { ds =>
          IO {
            ds.setLeakDetectionThreshold(5000)
            ds.setRegisterMbeans(true)
          }
        }
      }
    } yield xa


  override def run(args: List[String]): IO[ExitCode] = {

    transactor.use { tx =>

      for {
        f1 <- sql"SELECT 1".query[String].stream.transact(tx).compile.toVector.start
        _ <- IO.sleep(10 millis)
        _ <- f1.cancel
        _ <- IO.never
      } yield ExitCode.Success
    }
  }

}
