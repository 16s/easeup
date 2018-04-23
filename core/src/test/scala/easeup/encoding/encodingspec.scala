package easeup.encoding

import org.scalatest._

class RequestFieldEncoderSpec extends FlatSpec with EitherValues {

  final case class TestClass(id: Long)

  final case class NestedClass(item: TestClass)

  "RequestFieldEncoder" should "correctly encode a string" in {
    assert(RequestFieldEncoder[String].asJsonValue("string") === JsonString("string"))
  }

  it should "correctly encode a TestClass as object" in {
    assert(RequestFieldEncoder[TestClass].asJsonValue(TestClass(5L)) === JsonObject(List(("id", JsonLong(5L)))))
  }

  it should "correctly encode a list of TestClasses" in {
    val data = List(TestClass(5), TestClass(6))
    val response = JsonArray(Seq(JsonObject(List(("id", JsonLong(5L)))), JsonObject(List(("id", JsonLong(6L))))))
    assert(RequestFieldEncoder[List[TestClass]].asJsonValue(data) === response)
  }

  it should "correctly encode a vector of TestClasses" in {
    val data = Vector(TestClass(5), TestClass(6))
    val response = JsonArray(Seq(JsonObject(List(("id", JsonLong(5L)))), JsonObject(List(("id", JsonLong(6L))))))
    assert(RequestFieldEncoder[Vector[TestClass]].asJsonValue(data) === response)
  }

  it should "correctly encode NestedClass" in {
    val data = NestedClass(TestClass(5L))
    val response = JsonObject(List(("item", JsonObject(List(("id", JsonLong(5L)))))))
    assert(RequestFieldEncoder[NestedClass].asJsonValue(data) === response)
  }

  "ResponseFieldDecoder" should "correctly decode a string" in {
    assert(ResponseFieldDecoder[String].parseJson(JsonString("abc")).runA(()).right.value === "abc")
  }

  it should "correctly decode a TestClass object" in {
    val json = JsonObject(List(("id", JsonLong(5L))))
    assert(ResponseFieldDecoder[TestClass].parseJson(json).runA(()).right.value === TestClass(5L))
  }

  it should "correctly decode a list of TestClasses" in {
    val data = List(TestClass(5), TestClass(6))
    val response = JsonArray(Seq(JsonObject(List(("id", JsonLong(5L)))), JsonObject(List(("id", JsonLong(6L))))))
    assert(ResponseFieldDecoder[List[TestClass]].parseJson(response).runA(()).right.value === data)
  }

  it should "correctly decode NestedClass" in {
    val data = NestedClass(TestClass(5L))
    val response = JsonObject(List(("item", JsonObject(List(("id", JsonLong(5L)))))))
    assert(ResponseFieldDecoder[NestedClass].parseJson(response).runA(()).right.value === data)
  }

}
