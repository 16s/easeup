package easeup.encoding

import cats.syntax.all._
import cats.instances.all._
import shapeless._
import shapeless.labelled.FieldType
import result._

sealed trait JsonValue

case object JsonNull extends JsonValue
final case class JsonBoolean(x: Boolean) extends JsonValue
final case class JsonString(x: String) extends JsonValue
final case class JsonFloat(x: Float) extends JsonValue
final case class JsonLong(x: Long) extends JsonValue
final case class JsonArray(xs: Traversable[JsonValue]) extends JsonValue
final case class JsonObject(xs: List[(String, JsonValue)]) extends JsonValue

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

sealed trait DecodeError
final case object InvalidDataType extends DecodeError

trait ResponseFieldDecoder[A] {
  def parseJson[AA >: A](o: JsonValue): ResultT[cats.Id, Unit, DecodeError, AA]
}

object ResponseFieldDecoder {
  def apply[A](implicit dec: ResponseFieldDecoder[A]): ResponseFieldDecoder[A] = dec

  def instance[A](f: JsonValue => Either[DecodeError, A]): ResponseFieldDecoder[A] = new ResponseFieldDecoder[A] {
    override def parseJson[AA >: A](o: JsonValue): ResultT[Id, Unit, DecodeError, AA] = f(o) match {
      case Left(err) => ResultT.left[cats.Id, Unit, DecodeError, AA](err)
      case Right(a) => ResultT.right[cats.Id, Unit, DecodeError, AA](a)
    }
  }

  implicit val string: ResponseFieldDecoder[String] = instance({
    case JsonString(s) => Either.right[DecodeError, String](s)
    case _ => Either.left[DecodeError, String](InvalidDataType)
  })

  implicit val int: ResponseFieldDecoder[Int] = instance({
    case JsonLong(l) if l.isValidInt => Either.right[DecodeError, Int](l.toInt)
    case JsonFloat(d) if d.isValidInt => Either.right[DecodeError, Int](d.toInt)
    case _ => Either.left[DecodeError, Int](InvalidDataType)
  })

  implicit val long: ResponseFieldDecoder[Long] = instance({
    case JsonLong(l) => Either.right[DecodeError, Long](l)
    case JsonFloat(d) if d.isValidInt => Either.right[DecodeError, Long](d.toLong)
    case _ => Either.left[DecodeError, Long](InvalidDataType)
  })

  implicit val double: ResponseFieldDecoder[Double] = instance({
    case JsonLong(l) => Either.right[DecodeError, Double](l.toDouble)
    case JsonFloat(d) => Either.right[DecodeError, Double](d.toDouble)
    case _ => Either.left[DecodeError, Double](InvalidDataType)
  })

  implicit val float: ResponseFieldDecoder[Float] = instance({
    case JsonLong(l) => Either.right[DecodeError, Float](l.toFloat)
    case JsonFloat(d) => Either.right[DecodeError, Float](d)
    case _ => Either.left[DecodeError, Float](InvalidDataType)
  })

  implicit val boolean: ResponseFieldDecoder[Boolean] = instance({
    case JsonBoolean(b) => Either.right[DecodeError, Boolean](b)
    case _ => Either.left[DecodeError, Boolean](InvalidDataType)
  })

  implicit def list[A](implicit dec: ResponseFieldDecoder[A]): ResponseFieldDecoder[List[A]] =
    instance({
      case JsonArray(xs) =>
        xs.foldLeft(Either.right[DecodeError, Vector[A]](Vector.empty[A]))({
          case (e @ Left(_), _) => e
          case (Right(acc), item) =>
            dec.parseJson(item).runA(()) match {
              case Left(err) => Either.left[DecodeError, Vector[A]](err)
              case Right(v) => Either.right[DecodeError, Vector[A]](acc :+ v)
            }
        }).right.map(_.toList)
      case _ => Either.left[DecodeError, List[A]](InvalidDataType)
    })

  implicit val hnil: ResponseFieldDecoder[HNil] = instance({
    case JsonObject(_) => Either.right[DecodeError, HNil](HNil)
    case _ => Either.left[DecodeError, HNil](InvalidDataType)
  })

  implicit def hlist[K <: Symbol, H, T <: HList](implicit
    witness: Witness.Aux[K],
    hdec: Lazy[ResponseFieldDecoder[H]],
    tdec: ResponseFieldDecoder[T]): ResponseFieldDecoder[FieldType[K, H] :: T] = instance({
    case obj @ JsonObject(items) =>
      val fieldName: String = witness.value.name
      items.collectFirst({ case (k, v) if k == fieldName => hdec.value.parseJson(v).runA(()) }) match {
        case Some(Right(h)) =>
          tdec.parseJson(obj).runA(()) match {
            case Left(err) => Either.left[DecodeError, FieldType[K, H] :: T](err)
            case Right(t) => Either.right[DecodeError, FieldType[K, H] :: T](labelled.field[K](h) :: t)
          }
        case Some(Left(err)) => Either.left[DecodeError, FieldType[K, H] :: T](err)
        case None => Either.left[DecodeError, FieldType[K, H] :: T](InvalidDataType)
      }
    case _ => Either.left[DecodeError, FieldType[K, H] :: T](InvalidDataType)
  })

  implicit def generic[A, H](implicit
    generic: LabelledGeneric.Aux[A, H],
    hdec: Lazy[ResponseFieldDecoder[H]]): ResponseFieldDecoder[A] = instance({
    case obj @ JsonObject(_) => hdec.value.parseJson(obj).runA(()) match {
      case Right(x) => Either.right[DecodeError, A](generic.from(x))
      case Left(err) => Either.left[DecodeError, A](err)

    }
    case _ => Either.left[DecodeError, A](InvalidDataType)
  })

}

trait JsonEncoder {
  def encode(x: JsonValue): String
}
