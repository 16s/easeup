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

import shapeless._
import shapeless.labelled.FieldType

trait JsonValueEncoder[-A] {
  def encode(x: A): JsonValue
}

object JsonValueEncoder {
  def apply[A](implicit enc: JsonValueEncoder[A]): JsonValueEncoder[A] = enc

  def instance[A](f: A => JsonValue): JsonValueEncoder[A] = f(_)

  implicit val string: JsonValueEncoder[String] = instance(JsonString(_))

  implicit val int: JsonValueEncoder[Int] = instance(x => JsonLong(x.toLong))

  implicit val long: JsonValueEncoder[Long] = instance(x => JsonLong(x))

  implicit val double: JsonValueEncoder[Double] = instance[Double](x => JsonFloat(x.toFloat))

  implicit val float: JsonValueEncoder[Float] = instance[Float](x => JsonFloat(x))

  implicit val boolean: JsonValueEncoder[Boolean] = instance[Boolean](JsonBoolean(_))

  implicit def list[A](implicit enc: JsonValueEncoder[A]): JsonValueEncoder[Traversable[A]] =
    instance(xs => JsonArray(xs.map(enc.encode)))

  implicit def generic[A, H](implicit
    generic: LabelledGeneric.Aux[A, H],
    henc: Lazy[JsonValueEncoder[H]]): JsonValueEncoder[A] = instance(x => henc.value.encode(generic.to(x)))

  implicit val hnil: JsonValueEncoder[HNil] =
    instance(_ => JsonObject(List.empty[(String, JsonValue)]))

  implicit def hlist[K <: Symbol, H, T <: HList](implicit
    witness: Witness.Aux[K],
    henc: Lazy[JsonValueEncoder[H]],
    tenc: JsonValueEncoder[T]): JsonValueEncoder[FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
    instance(hlist => {
      val head = henc.value.encode(hlist.head)
      val tail = tenc.encode(hlist.tail)
      tail match {
        case JsonObject(xs) => JsonObject((fieldName -> head) :: xs)
        case _ => JsonObject((fieldName -> head) :: Nil)
      }
    })
  }
}
