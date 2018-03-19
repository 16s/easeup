package easeup.web

import shapeless._
import shapeless.labelled.FieldType

sealed trait Request {
  val endpoint: String
}

sealed trait JsonValue

case object JsonNull extends JsonValue
final case class JsonBoolean(x: Boolean) extends JsonValue
final case class JsonString(x: String) extends JsonValue
final case class JsonNumber(x: Float) extends JsonValue
final case class JsonArray(xs: Seq[JsonValue]) extends JsonValue
final case class JsonObject(xs: List[(String, JsonValue)]) extends JsonValue

trait RequestFieldEncoder[A] {
  def asJsonValue(x: A): JsonValue
  def asString(x: A): String
}

object RequestFieldEncoder {
  def apply[A](implicit enc: RequestFieldEncoder[A]): RequestFieldEncoder[A] = enc

  def instance[A](f: A => JsonValue, g: A => String): RequestFieldEncoder[A] = new RequestFieldEncoder[A] {
    override def asJsonValue(x: A): JsonValue = f(x)
    override def asString(x: A): String = g(x)
  }

  implicit val string: RequestFieldEncoder[String] = instance(JsonString(_), identity)

  implicit val int: RequestFieldEncoder[Int] = instance(x => JsonNumber(x.toFloat), _.toString)

  implicit val long: RequestFieldEncoder[Long] = instance(x => JsonNumber(x.toFloat), _.toString)

  implicit val double: RequestFieldEncoder[Double] = instance[Double](x => JsonNumber(x.toFloat), _.toString)

  implicit val boolean: RequestFieldEncoder[Boolean] = instance[Boolean](JsonBoolean(_), _.toString)
}

trait RequestObjectEncoder[A] extends RequestFieldEncoder[A] {
  override def asJsonValue(x: A): JsonObject
  def asString(x: A): String
}

object RequestObjectEncoder {
  def apply[A](implicit enc: RequestObjectEncoder[A]): RequestObjectEncoder[A] = enc

  def instance[A](f: A => JsonObject, g: A => String): RequestObjectEncoder[A] = new RequestObjectEncoder[A] {
    override def asJsonValue(x: A): JsonObject = f(x)
    override def asString(x: A): String = g(x)
  }

  implicit val hnil: RequestObjectEncoder[HNil] = instance(_ => JsonObject(List.empty[(String, JsonValue)]), _ => "")

  implicit def hlist[K <: Symbol, H, T <: HList](implicit
    witness: Witness.Aux[K],
    henc: Lazy[RequestFieldEncoder[H]],
    tenc: RequestObjectEncoder[T]): RequestObjectEncoder[FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
    instance(hlist => {
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
    henc: Lazy[RequestObjectEncoder[H]]): RequestObjectEncoder[A] = instance(x => {
    henc.value.asJsonValue(generic.to(x))

  }, x => {
    henc.value.asString(generic.to(x))
  })
}

sealed trait GetRequest extends Request

sealed trait PostRequest extends Request

sealed trait JsonPostRequest extends PostRequest

final case class AuthorizedRequest(bearer: String, request: Request)
