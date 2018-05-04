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

import cats.effect._
import easeup.web.{ Client, Error, PreparedRequest, Response }
import result._

sealed trait ApiError

final case class Http4sState()

class Http4sClient extends Client[Http4sState] {
  private def run(request: PreparedRequest)(st: Http4sState): IO[Either[Error, Response]] = ???
  override def execute(req: PreparedRequest): ResultT[IO, Http4sState, Error, Response] = ResultT.inspectF(run(req))
}
