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
import cats.syntax.all._
import ingot._

sealed trait DecodeError
object DecodeError {
  final case object InvalidDataType extends DecodeError
}

trait ResponseFieldDecoder[A] {
  def decodeJson[AA >: A](o: JsonValue): Clay[DecodeError, AA]
}

object ResponseFieldDecoder {
  def apply[A](implicit dec: ResponseFieldDecoder[A]): ResponseFieldDecoder[A] = dec

  def instance[A](f: JsonValue => Either[DecodeError, A]): ResponseFieldDecoder[A] = new ResponseFieldDecoder[A] {
    override def decodeJson[AA >: A](o: JsonValue): Clay[DecodeError, AA] = f(o) match {
      case Left(err) => Clay.leftT[AA](err)
      case Right(a) => Clay.rightT[DecodeError](a)
    }
  }

  implicit val string: ResponseFieldDecoder[String] = instance({
    case JsonString(s) => Either.right[DecodeError, String](s)
    case _ => Either.left[DecodeError, String](DecodeError.InvalidDataType)
  })

  implicit val int: ResponseFieldDecoder[Int] = instance({
    case JsonLong(l) if l.isValidInt => Either.right[DecodeError, Int](l.toInt)
    case JsonFloat(d) if d.isValidInt => Either.right[DecodeError, Int](d.toInt)
    case _ => Either.left[DecodeError, Int](DecodeError.InvalidDataType)
  })

  implicit val long: ResponseFieldDecoder[Long] = instance({
    case JsonLong(l) => Either.right[DecodeError, Long](l)
    case JsonFloat(d) if d.isValidInt => Either.right[DecodeError, Long](d.toLong)
    case _ => Either.left[DecodeError, Long](DecodeError.InvalidDataType)
  })

  implicit val double: ResponseFieldDecoder[Double] = instance({
    case JsonLong(l) => Either.right[DecodeError, Double](l.toDouble)
    case JsonFloat(d) => Either.right[DecodeError, Double](d.toDouble)
    case _ => Either.left[DecodeError, Double](DecodeError.InvalidDataType)
  })

  implicit val float: ResponseFieldDecoder[Float] = instance({
    case JsonLong(l) => Either.right[DecodeError, Float](l.toFloat)
    case JsonFloat(d) => Either.right[DecodeError, Float](d)
    case _ => Either.left[DecodeError, Float](DecodeError.InvalidDataType)
  })

  implicit val boolean: ResponseFieldDecoder[Boolean] = instance({
    case JsonBoolean(b) => Either.right[DecodeError, Boolean](b)
    case _ => Either.left[DecodeError, Boolean](DecodeError.InvalidDataType)
  })

  implicit def list[A](implicit dec: ResponseFieldDecoder[A]): ResponseFieldDecoder[List[A]] =
    instance({
      case JsonArray(xs) =>
        xs.foldLeft(Either.right[DecodeError, Vector[A]](Vector.empty[A]))({
          case (e @ Left(_), _) => e
          case (Right(acc), item) =>
            dec.decodeJson(item).runA() match {
              case Left(err) => Either.left[DecodeError, Vector[A]](err)
              case Right(v) => Either.right[DecodeError, Vector[A]](acc :+ v)
            }
        }).right.map(_.toList)
      case _ => Either.left[DecodeError, List[A]](DecodeError.InvalidDataType)
    })

  implicit val hnil: ResponseFieldDecoder[HNil] = instance({
    case JsonObject(_) => Either.right[DecodeError, HNil](HNil)
    case _ => Either.left[DecodeError, HNil](DecodeError.InvalidDataType)
  })

  implicit def hlist[K <: Symbol, H, T <: HList](implicit
    witness: Witness.Aux[K],
    hdec: Lazy[ResponseFieldDecoder[H]],
    tdec: ResponseFieldDecoder[T]): ResponseFieldDecoder[FieldType[K, H] :: T] = instance({
    case obj @ JsonObject(items) =>
      val fieldName: String = witness.value.name
      items.collectFirst({ case (k, v) if k == fieldName => hdec.value.decodeJson(v).runA() }) match {
        case Some(Right(h)) =>
          tdec.decodeJson(obj).runA() match {
            case Left(err) => Either.left[DecodeError, FieldType[K, H] :: T](err)
            case Right(t) => Either.right[DecodeError, FieldType[K, H] :: T](labelled.field[K](h) :: t)
          }
        case Some(Left(err)) => Either.left[DecodeError, FieldType[K, H] :: T](err)
        case None => Either.left[DecodeError, FieldType[K, H] :: T](DecodeError.InvalidDataType)
      }
    case _ => Either.left[DecodeError, FieldType[K, H] :: T](DecodeError.InvalidDataType)
  })

  implicit def generic[A, H](implicit
    generic: LabelledGeneric.Aux[A, H],
    hdec: Lazy[ResponseFieldDecoder[H]]): ResponseFieldDecoder[A] = instance({
    case obj @ JsonObject(_) => hdec.value.decodeJson(obj).runA() match {
      case Right(x) => Either.right[DecodeError, A](generic.from(x))
      case Left(err) => Either.left[DecodeError, A](err)

    }
    case _ => Either.left[DecodeError, A](DecodeError.InvalidDataType)
  })

}
