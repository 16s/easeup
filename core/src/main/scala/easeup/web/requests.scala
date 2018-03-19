package easeup.web

sealed trait Request {
  val endpoint: String
}

sealed trait GetRequest extends Request

sealed trait PostRequest extends Request

sealed trait JsonPostRequest extends PostRequest

final case class AuthorizedRequest(bearer: String, request: Request)
