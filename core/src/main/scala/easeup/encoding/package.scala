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

package object encoding {
  type JsonEncoder = JsonValue => String

  sealed trait JsonValue

  case object JsonNull extends JsonValue
  final case class JsonBoolean(x: Boolean) extends JsonValue
  final case class JsonString(x: String) extends JsonValue
  final case class JsonFloat(x: Float) extends JsonValue
  final case class JsonLong(x: Long) extends JsonValue
  final case class JsonArray(xs: Traversable[JsonValue]) extends JsonValue
  final case class JsonObject(xs: List[(String, JsonValue)]) extends JsonValue
}
