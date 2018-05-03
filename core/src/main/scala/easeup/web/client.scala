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

import cats.effect.IO
import result._

sealed trait Method

object Method {
  final case class Get(params: Seq[(String, String)]) extends Method
  final case class Post(body: String) extends Method
}

final case class PreparedRequest[A](
  headers: Seq[(String, String)],
  method: Method,
  url: String)

trait Client[S] {
  def execute[A](req: PreparedRequest[A]): ResultT[IO, S, Error, A]
}