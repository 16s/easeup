package easeup.web

import org.scalatest.FlatSpec

class RequestFieldEncoderSpec extends FlatSpec {

  final case class TestClass(id: Long)

  final case class NestedClass(item: TestClass)

  "RequestFieldEncoder" should "correctly encode a string" in {
    assert(RequestFieldEncoder[String].asJsonValue("string") === JsonString("string"))
  }

  it should "correctly encode a TestClass as object" in {
    assert(RequestFieldEncoder[TestClass].asJsonValue(TestClass(5L)) === JsonObject(List(("id", JsonNumber(5f)))))
  }

  it should "correctly encode a list of TestClasses" in {
    val data = List(TestClass(5), TestClass(6))
    val response = JsonArray(Seq(JsonObject(List(("id", JsonNumber(5f)))), JsonObject(List(("id", JsonNumber(6f))))))
    assert(RequestFieldEncoder[List[TestClass]].asJsonValue(data) === response)
  }

  it should "correctly encode a vector of TestClasses" in {
    val data = Vector(TestClass(5), TestClass(6))
    val response = JsonArray(Seq(JsonObject(List(("id", JsonNumber(5f)))), JsonObject(List(("id", JsonNumber(6f))))))
    assert(RequestFieldEncoder[Vector[TestClass]].asJsonValue(data) === response)
  }

  it should "correctly encode NestedClass" in {
    val data = NestedClass(TestClass(5L))
    val response = JsonObject(List(("item", JsonObject(List(("id", JsonNumber(5f)))))))
    assert(RequestFieldEncoder[NestedClass].asJsonValue(data) === response)
  }
}