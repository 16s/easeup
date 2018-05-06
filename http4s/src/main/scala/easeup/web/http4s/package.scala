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

package easeup.web

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import cats.effect._
import cats.syntax.all._
import org.{ http4s => h4s }
import result._

package object http4s {

  final case class Http4sState(client: h4s.client.Client[IO])

  object Http4sClient {
    val execute: HttpClientExecutor[Http4sState] = req => for {
      request <- buildRequest(req)
      resp <- ResultT.inspectF((st: Http4sState) => run(req, request)(st.client).map(r => Either.right[Error, Response](r)))
    } yield resp

    private def buildRequest(req: Request): ResultT[IO, Http4sState, Error, h4s.Request[IO]] = {
      for {
        uri <- ResultT.inspect[IO, Http4sState, Error, h4s.Uri](_ => getUri(req.url))
        uriWithParams = req.params.groupBy(_._1).foldLeft(uri)({ case (uri, (k, vs)) => uri.withQueryParam(k, vs.map(_._2)) })
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

    private def getUri(uri: String): Either[Error, h4s.Uri] = h4s.Uri.fromString(uri) match {
      case Left(err) => Either.left[Error, h4s.Uri](Error.InvalidUri(uri, err.message))
      case Right(uri) => Either.right[Error, h4s.Uri](uri)
    }

    private def buildBody(body: String): h4s.EntityBody[IO] = {
      val is = IO(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)))
      fs2.io.readInputStream[IO](is, 1024, true)
    }

    private def run(
      preq: Request,
      request: h4s.Request[IO]): h4s.client.Client[IO] => IO[Response] = client => {
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

}