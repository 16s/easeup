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

package easeup.api

import easeup.encoding.JsonParser
import easeup.web.HttpClientExecutor

sealed trait JsonDependency
case object MissingJsonDependency extends JsonDependency
final case class FulfilledJsonDependency[A](jsonParser: JsonParser[A]) extends JsonDependency

sealed trait HttpDependency
case object MissingHttpDependency extends HttpDependency
final case class FulfilledHttpDependency[A](httpExecutor: HttpClientExecutor[A]) extends HttpDependency

trait ApiClient[HttpState <: HttpDependency, JsonState <: JsonDependency] {
  val httpClient: HttpState
  val jsonParser: JsonState
}

object ApiClient {
  type EmptyApiClient = ApiClient[MissingHttpDependency.type, MissingJsonDependency.type]

  val emptyApiClient: EmptyApiClient = new ApiClient[MissingHttpDependency.type, MissingJsonDependency.type] {
    override val httpClient = MissingHttpDependency
    override val jsonParser = MissingJsonDependency
  }

  implicit class MissingHttpApiClient[JsonState <: JsonDependency](val v: ApiClient[MissingHttpDependency.type, JsonState]) {
    type Result[A] = ApiClient[FulfilledHttpDependency[A], JsonState]

    def setHttpClient[A](newHttpClient: HttpClientExecutor[A]): Result[A] = new ApiClient[FulfilledHttpDependency[A], JsonState] {
      override val httpClient = FulfilledHttpDependency(newHttpClient)
      override val jsonParser = v.jsonParser
    }
  }

  implicit class MissingJsonApiClient[HttpState <: HttpDependency](val v: ApiClient[HttpState, MissingJsonDependency.type]) {
    type Result[A] = ApiClient[HttpState, FulfilledJsonDependency[A]]

    def setJsonParser[A](newJsonParser: JsonParser[A]): Result[A] = new ApiClient[HttpState, FulfilledJsonDependency[A]] {
      override val httpClient = v.httpClient
      override val jsonParser = FulfilledJsonDependency(newJsonParser)
    }
  }

  type PreparedApiClient[A, B] = ApiClient[FulfilledHttpDependency[B], FulfilledJsonDependency[A]]

  implicit class FulfilledJsonApiClient[A, B](v: PreparedApiClient[A, B]) {
    def run(): Unit = {
    }
  }
}