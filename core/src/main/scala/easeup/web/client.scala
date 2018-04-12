package easeup.web

import cats.effect.IO
import result.ResultT

sealed trait Method

object Method {
  final case class Get(params: Seq[(String, String)]) extends Method
  final case class Post(body: String) extends Method
}
final case class PreparedRequest(
  headers: Seq[(String, String)],
  method: Method,
  url: String)

trait Client[S] {
  def execute[A](req: PreparedRequest): ResultT[IO, S, Error, A]
}