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

trait QueryStringEncoder[A] {
  def encode(x: A): QueryString
}

object QueryStringEncoder {
  def apply[A](implicit enc: QueryStringEncoder[A]): QueryStringEncoder[A] = enc

  def instance[A](f: A => QueryString): QueryStringEncoder[A] = (x: A) => f(x)

  implicit val hnil: QueryStringEncoder[HNil] =
    instance(_ => QueryString(List.empty[QueryStringItem]))

  implicit def hlist[K <: Symbol, H, T <: HList](implicit
    witness: Witness.Aux[K],
    henc: Lazy[ParamValueEncoder[H]],
    tenc: QueryStringEncoder[T]): QueryStringEncoder[FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
    instance(hlist => {
      val head = henc.value.encode(hlist.head)
      val tail = tenc.encode(hlist.tail)
      QueryString(QueryStringItem(fieldName, head) :: tail.items)
    })
  }

  implicit def generic[A, H](implicit
    generic: LabelledGeneric.Aux[A, H],
    henc: Lazy[QueryStringEncoder[H]]): QueryStringEncoder[A] =
    QueryStringEncoder.instance(x => henc.value.encode(generic.to(x)))
}