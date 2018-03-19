package easeup.encoding

import io.circe.{ Encoder, Json }
import io.circe.syntax._

object CirceJsonEncoder extends JsonEncoder {
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

  override def encode(x: JsonValue): String = x.asJson.noSpaces

}