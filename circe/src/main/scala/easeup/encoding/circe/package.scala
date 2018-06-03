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

import cats.effect.IO
import ingot._
import io.circe.{ Encoder, Json }
import io.circe.syntax._

package object circe {
  implicit val jvEncoder: Encoder[JsonValue] = new Encoder[JsonValue] {
    private def encodeArrayElements(xs: Traversable[JsonValue]): Iterable[Json] =
      xs.map(apply).toIterable

    private def encodeObjectFields(fields: List[(String, JsonValue)]): List[(String, Json)] =
      fields.map({ case (k, v) => (k, apply(v)) })

    override def apply(a: JsonValue): Json = a match {
      case JsonNull => Json.Null
      case JsonBoolean(b) => Json.fromBoolean(b)
      case JsonFloat(f) => Json.fromFloatOrString(f)
      case JsonLong(l) => Json.fromLong(l)
      case JsonString(s) => Json.fromString(s)
      case JsonArray(xs) => Json.fromValues(encodeArrayElements(xs))
      case JsonObject(fields) => Json.fromFields(encodeObjectFields(fields))
    }
  }

  val encode: JsonEncoder[Unit] = x => Ingot.pure[IO, Unit, Unit, String](x.asJson.noSpaces)
}
