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

import org.scalatest._
import Matchers._
import cats.effect.IO
import easeup.api.ApiClient.MissingHttpApiClient
import easeup.encoding.{ JsonParseError, JsonParser, JsonValue }
import easeup.web.{ ClientError, HttpClientExecutor, Request, Response }
import ingot.Ingot

class ApiClientSpec extends FlatSpec {
  "ApiClient" should "be able to be initialized" in {
    val emptyClient = ApiClient.emptyApiClient
    val httpClient: HttpClientExecutor[Unit] = new HttpClientExecutor[Unit] {
      override def run(r: Request): Ingot[IO, Unit, ClientError, Response[String]] =
        Ingot.leftT[IO, Unit, Response[String]](ClientError.InvalidUri("missing", "missing"))
    }
    val clientWithHttp = emptyClient.setHttpClient(httpClient)
    val jsonParser: JsonParser[Unit] = new JsonParser[Unit] {
      override def run(x: String): Ingot[IO, Unit, JsonParseError, JsonValue] =
        Ingot.leftT[IO, Unit, JsonValue](JsonParseError.InvalidData())
    }
    val clientWithJson = clientWithHttp.setJsonParser(jsonParser)
    clientWithJson.run()
  }
}
