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

trait ParamValueEncoder[-A] {
  def encode(x: A): ParamValue
}

trait SingleParamValueEncoder[-A] extends ParamValueEncoder[A] {
  def encode(x: A): SingleParamValue
}

object SingleParamValueEncoder {
  def apply[A](implicit enc: SingleParamValueEncoder[A]): SingleParamValueEncoder[A] = enc

  def instance[A](f: A => SingleParamValue): SingleParamValueEncoder[A] = (x: A) => f(x)

  implicit val string: SingleParamValueEncoder[String] = instance(SingleParamValue.apply)

  implicit val int: SingleParamValueEncoder[Int] = instance(x => SingleParamValue.apply(x.toString))

  implicit val long: SingleParamValueEncoder[Long] = instance(x => SingleParamValue.apply(x.toString))

  implicit val double: SingleParamValueEncoder[Double] = instance[Double](x => SingleParamValue.apply(x.toString))

  implicit val float: SingleParamValueEncoder[Float] = instance[Float](x => SingleParamValue.apply(x.toString))

  implicit val boolean: SingleParamValueEncoder[Boolean] = instance[Boolean](x => SingleParamValue.apply(x.toString))
}

trait MultiParamValueEncoder[-A] extends ParamValueEncoder[A] {
  def encode(x: A): MultiParamValue
}

object MultiParamValueEncoder {
  def apply[A](implicit enc: MultiParamValueEncoder[A]): MultiParamValueEncoder[A] = enc

  def instance[A](f: A => MultiParamValue): MultiParamValueEncoder[A] = (x: A) => f(x)

  implicit def list[A](implicit enc: SingleParamValueEncoder[A]): MultiParamValueEncoder[Traversable[A]] =
    instance(xs => MultiParamValue(xs.map(enc.encode)))
}
