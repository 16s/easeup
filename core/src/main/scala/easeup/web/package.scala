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

import cats.effect.IO
import result._

package object web {
  type HttpClientExecutor[S] = Request => ResultT[IO, S, Error, Response]

  final case class Request(
    headers: Seq[(String, String)],
    method: Method,
    params: Seq[(String, String)],
    body: Option[String],
    url: String)

  sealed trait Method

  object Method {
    final case object Get extends Method
    final case object Post extends Method
  }

  final case class Response(req: Request, status: Int, headers: Seq[(String, String)], body: String)

  sealed trait Error

  object Error {
    final case class InvalidUri(uri: String, error: String) extends Error
  }
}
