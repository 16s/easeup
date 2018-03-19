package easeup.encoding

import shapeless._
import shapeless.labelled.FieldType

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

trait JsonEncoder {
  def encode(x: JsonValue): String
}
