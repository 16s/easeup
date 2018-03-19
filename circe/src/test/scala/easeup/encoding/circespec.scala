package easeup.encoding

import org.scalatest.FlatSpec

class CirceJsonEncoderSpec extends FlatSpec {
  final case class TestClass(id: Long)

  final case class NestedClass(item: TestClass)

  "CirceJsonEncoder" should "encode TestClass" in {
    val encoded = CirceJsonEncoder.encode(RequestFieldEncoder[TestClass].asJsonValue(TestClass(5)))
    assert(encoded === "{\"id\":5}")
  }
}

