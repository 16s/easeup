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

package easeup.encoding

import org.scalatest._
import Matchers._
import cats.syntax.either._

final case class TestClass(id: Long)

final case class NestedClass(item: TestClass)

class CirceJsonEncoderSpec extends FlatSpec {

  "CirceJsonEncoder" should "encode TestClass" in {
    val encoded: Either[Unit, String] = circe.encode.run(JsonValueEncoder[TestClass].encode(TestClass(5))).runA().unsafeRunSync()
    encoded shouldBe Either.right[Unit, String]("{\"id\":5}")
  }
}

