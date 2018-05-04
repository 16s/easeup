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

trait RequestFieldEncoder[-A] {
  def asJsonValue(x: A): JsonValue
  def asString(x: A): String
}

trait RequestObjectEncoder[A] extends RequestFieldEncoder[A] {
  def asJsonValue(x: A): JsonObject
  def asString(x: A): String
}

object RequestFieldEncoder {
  def apply[A](implicit enc: RequestFieldEncoder[A]): RequestFieldEncoder[A] = enc

  def instance[A](f: A => JsonValue, g: A => String): RequestFieldEncoder[A] = new RequestFieldEncoder[A] {
    override def asJsonValue(x: A): JsonValue = f(x)
    override def asString(x: A): String = g(x)
  }

  def objectInstance[A](f: A => JsonObject, g: A => String): RequestObjectEncoder[A] = new RequestObjectEncoder[A] {
    override def asJsonValue(x: A): JsonObject = f(x)
    override def asString(x: A): String = g(x)
  }

  implicit val string: RequestFieldEncoder[String] = instance(JsonString(_), identity)

  implicit val int: RequestFieldEncoder[Int] = instance(x => JsonLong(x), _.toString)

  implicit val long: RequestFieldEncoder[Long] = instance(x => JsonLong(x), _.toString)

  implicit val double: RequestFieldEncoder[Double] = instance[Double](x => JsonFloat(x.toFloat), _.toString)

  implicit val float: RequestFieldEncoder[Float] = instance[Float](x => JsonFloat(x), _.toString)

  implicit val boolean: RequestFieldEncoder[Boolean] = instance[Boolean](JsonBoolean(_), _.toString)

  implicit def list[A](implicit enc: RequestFieldEncoder[A]): RequestFieldEncoder[Traversable[A]] =
    instance(xs => JsonArray(xs.map(enc.asJsonValue)), xs => xs.map(enc.asString).mkString(","))

  implicit val hnil: RequestObjectEncoder[HNil] =
    objectInstance(_ => JsonObject(List.empty[(String, JsonValue)]), _ => "")

  implicit def hlist[K <: Symbol, H, T <: HList](implicit
    witness: Witness.Aux[K],
    henc: Lazy[RequestFieldEncoder[H]],
    tenc: RequestObjectEncoder[T]): RequestObjectEncoder[FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
    objectInstance(hlist => {
      val head = henc.value.asJsonValue(hlist.head)
      val tail = tenc.asJsonValue(hlist.tail)
      JsonObject((fieldName -> head) :: tail.xs)
    }, hlist => {
      val head = henc.value.asString(hlist.head)
      val tail = tenc.asString(hlist.tail)
      s"$fieldName=$head" + (if (tail.nonEmpty) "&" else "") + tail
    })
  }

  implicit def generic[A, H](implicit
    generic: LabelledGeneric.Aux[A, H],
    henc: Lazy[RequestObjectEncoder[H]]): RequestFieldEncoder[A] = RequestFieldEncoder.instance(x => {
    henc.value.asJsonValue(generic.to(x))
  }, x => {
    henc.value.asString(generic.to(x))
  })
}
