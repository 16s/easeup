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

package easeup.web.http4s

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import cats.Eval
import cats.effect.IO
import easeup.encoding._
import easeup.web._
import ingot.Ingot
import org.{ http4s => h4s }
import cats.syntax.either._

object Http4sClient {
  val execute: HttpClientExecutor[Http4sState] = req => for {
    request <- buildRequest(req)
    resp <- Ingot.inspectF((st: Http4sState) => run(req, request)(st.client).map(r => Either.right[ClientError, Response[String]](r)))
  } yield resp

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def setQueryParams(uri: h4s.Uri, p: QueryParam): Eval[h4s.Uri] = p match {
    case QueryStringItem(k, SingleParamValue(v)) => Eval.later(uri.withQueryParam(k, v))
    case QueryStringItem(k, MultiParamValue(vs)) => Eval.later(uri.withQueryParam(k, vs.map(_.v).toList))
    case QueryString(items) => items.foldLeft(Eval.later(uri)) {
      case (uriE, item) =>
        for {
          uri <- uriE
          updatedUri <- setQueryParams(uri, item)
        } yield updatedUri
    }
  }

  private def buildRequest(req: Request): Ingot[IO, Http4sState, ClientError, h4s.Request[IO]] = {
    for {
      uri <- Ingot.inspect[IO]((_: Http4sState) => getUri(req.url))
      uriWithParams = setQueryParams(uri, req.params).value
      headers = h4s.Headers(req.headers.map({ case (k, v) => h4s.Header(k, v) }).toList)
      method = req.method match {
        case Method.Get => h4s.Method.GET
        case Method.Post => h4s.Method.POST
      }
      body = req.body.map(buildBody).getOrElse(h4s.EmptyBody)
    } yield h4s.Request[IO](
      method = method,
      uri = uriWithParams,
      headers = headers,
      body = body)
  }

  private def getUri(uri: String)(): Either[ClientError, h4s.Uri] = h4s.Uri.fromString(uri) match {
    case Left(err) => Either.left[ClientError, h4s.Uri](ClientError.InvalidUri(uri, err.message))
    case Right(uri) => Either.right[ClientError, h4s.Uri](uri)
  }

  private def buildBody(body: String): h4s.EntityBody[IO] = {
    val is = IO(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)))
    fs2.io.readInputStream[IO](is, 1024, true)
  }

  private def run(
    preq: Request,
    request: h4s.Request[IO]): h4s.client.Client[IO] => IO[Response[String]] = client => {
    client.fetch(request) { resp =>
      resp.body.compile.toList.map(_.map(_.toChar).mkString).map { body =>
        Response(
          req = preq,
          status = resp.status.code,
          headers = resp.headers.toList.map { h =>
            val rh = h.toRaw
            (rh.name.value, rh.value)
          },
          body = body)
      }
    }
  }
}
