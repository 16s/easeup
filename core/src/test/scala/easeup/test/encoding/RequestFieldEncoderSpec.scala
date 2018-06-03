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

package easeup.test.encoding

import easeup.encoding._
import org.scalatest._
import Matchers._

class RequestFieldEncoderSpec extends FlatSpec with EitherValues {
  import DataTypes._

  "JsonValueEncoder" should "correctly encode a string" in {
    assert(JsonValueEncoder[String].encode("string") === JsonString("string"))
  }

  it should "correctly encode a list of strings" in {
    assert(JsonValueEncoder[List[String]].encode(List("string", "string2")) === JsonArray(List(JsonString("string"), JsonString("string2"))))
  }

  it should "correctly encode a TestClass as object" in {
    JsonValueEncoder[TestClass].encode(TestClass(5L)) shouldBe JsonObject(List(("id", JsonLong(5L))))
  }

  it should "correctly encode a list of TestClasses" in {
    val data = List(TestClass(5), TestClass(6))
    val response = JsonArray(Seq(JsonObject(List(("id", JsonLong(5L)))), JsonObject(List(("id", JsonLong(6L))))))
    assert(JsonValueEncoder[List[TestClass]].encode(data) === response)
  }

  it should "correctly encode a vector of TestClasses" in {
    val data = Vector(TestClass(5), TestClass(6))
    val response = JsonArray(Seq(JsonObject(List(("id", JsonLong(5L)))), JsonObject(List(("id", JsonLong(6L))))))
    assert(JsonValueEncoder[Vector[TestClass]].encode(data) === response)
  }

  it should "correctly encode NestedClass" in {
    val data = NestedClass(TestClass(5L))
    val response = JsonObject(List(("item", JsonObject(List(("id", JsonLong(5L)))))))
    assert(JsonValueEncoder[NestedClass].encode(data) === response)
  }

  "ResponseFieldDecoder" should "correctly decode a string" in {
    assert(ResponseFieldDecoder[String].decodeJson(JsonString("abc")).runA().right.value === "abc")
  }

  it should "correctly decode a TestClass object" in {
    val json = JsonObject(List(("id", JsonLong(5L))))
    assert(ResponseFieldDecoder[TestClass].decodeJson(json).runA().right.value === TestClass(5L))
  }

  it should "correctly decode a list of TestClasses" in {
    val data = List(TestClass(5), TestClass(6))
    val response = JsonArray(Seq(JsonObject(List(("id", JsonLong(5L)))), JsonObject(List(("id", JsonLong(6L))))))
    assert(ResponseFieldDecoder[List[TestClass]].decodeJson(response).runA().right.value === data)
  }

  it should "correctly decode NestedClass" in {
    val data = NestedClass(TestClass(5L))
    val response = JsonObject(List(("item", JsonObject(List(("id", JsonLong(5L)))))))
    assert(ResponseFieldDecoder[NestedClass].decodeJson(response).runA().right.value === data)
  }

}
