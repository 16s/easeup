package easeup.web

import org.http4s.client.blaze._
import cats.effect._
import org.http4s.{ Response => H4SResponse }
import result.ResultT

sealed trait ApiError

final case class Http4sState()

class Http4sClient extends Client[Http4sState] {
  private def run(request: PreparedRequest)(st: Http4sState): IO[Either[Error, Response]] = ???
  override def execute(req: PreparedRequest): ResultT[IO, Http4sState, Error, Response] = ResultT.inspectF(run(req))
}
