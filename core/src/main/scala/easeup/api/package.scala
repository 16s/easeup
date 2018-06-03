/**
 * Copyright 2018 Tamas Neltz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easeup

import ingot._
import ingot.state._
import cats.effect._
import easeup.encoding._
import easeup.web.{ ClientError, HttpClientExecutor, Method, Request }
import cats.~>

package object api {
  implicit val evalToIo: (cats.Eval ~> IO) = new (cats.Eval ~> IO) {
    def apply[A](x: cats.Eval[A]): IO[A] = IO.apply(x.value)
  }

  implicit def compositeUnitState[S]: CompositeState[Unit, S] = new CompositeState[Unit, S] {
    override def inspect(ss: S): Unit = ()

    override def update(ss: S, s: Unit): S = ss
  }

  private def processBody[JsonS, Q](
    method: Method,
    query: Q)(
    implicit
    encJs: JsonValueEncoder[Q],
    encoder: JsonEncoder[JsonS]): Ingot[IO, JsonS, Unit, Option[String]] = {
    method match {
      case Method.Post =>
        encoder.run(encJs.encode(query)).map { body =>
          Some(body)
        }
      case Method.Get => Ingot.pure(None)
    }
  }

  def runQuery[ClientS, JsonS, QueryS, Q, R](
    endpoint: Endpoint[Q, R], query: Q)(
    implicit
    encQ: QueryStringEncoder[Q],
    encJs: JsonValueEncoder[Q],
    dec: ResponseFieldDecoder[R],
    client: HttpClientExecutor[ClientS],
    encoder: JsonEncoder[JsonS],
    parser: JsonParser[JsonS],
    cs: CompositeState[ClientS, QueryS],
    js: CompositeState[JsonS, QueryS]): Ingot[IO, QueryS, ApiError, R] = {
    for {
      body <- processBody(endpoint.method, query).transformS[QueryS].leftMap {
        case () => ApiError.Request(endpoint.uri, "Invalid body"): ApiError
      }
      params = endpoint.method match {
        case Method.Post => QueryString(List.empty[QueryStringItem])
        case Method.Get => encQ.encode(query)
      }
      req = Request(
        headers = Seq.empty[(String, String)],
        method = endpoint.method,
        params = params,
        body = body,
        url = endpoint.uri)
      resp <- client.run(req).transformS[QueryS].leftMap {
        case ClientError.InvalidUri(uri: String, error: String) => ApiError.Request(uri, error): ApiError
      }
      parsed <- parser.run(resp.body).transformS[QueryS].leftMap {
        case JsonParseError.InvalidData() => ApiError.JsonParse(endpoint.uri, resp.body): ApiError
      }
      respObject <- dec.decodeJson(parsed).transformS[QueryS].withMonad[IO].leftMap {
        case DecodeError.InvalidDataType => ApiError.DecodeError(endpoint.uri, parsed): ApiError
      }
    } yield respObject
  }
}
