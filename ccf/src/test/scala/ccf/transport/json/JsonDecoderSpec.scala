/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ccf.transport.json

import org.specs.Specification

import ccf.transport.MalformedDataException
import ccf.transport.{TransportRequest, TransportResponse}

object JsonDecoderSpec extends Specification {
  "Empty string" should {
    "parse to None" in {
      JsonDecoder.decodeResponse("") must equalTo(None)
    }
  }
  "Invalid JSON message" should {
    val invalidMessage = """{"a":1,"b":"c","d":3"""
    "cause MalformedDataException" in {
      JsonDecoder.decodeResponse(invalidMessage) must throwA[MalformedDataException]
    }
  }
  "A JSON response containing list as top-level element" should {
    "cause MalformedDataException" in {
      JsonDecoder.decodeResponse("[]") must throwA[MalformedDataException]
    }
  }
  "A JSON response containing improper header type" should {
    "cause MalformedDataException" in {
      JsonDecoder.decodeResponse("""{"headers":["foo","bar"]}""") must throwA[MalformedDataException]
    }
  }
  "A JSON response without header" should {
    "cause MalformedDataException" in {
      JsonDecoder.decodeResponse("""{"content":["foo","bar"]}""") must throwA[MalformedDataException]
    }
  }
  "A JSON response with headers but without content" should {
    val jsonResponse = """{"headers":{"aa":"bb","cc":"dd"}}"""
    "parse to equivalent TransportResponse" in {
      val expected = TransportResponse(Map[String, String]("aa" -> "bb", "cc" -> "dd"), None)
      val parsed = JsonDecoder.decodeResponse(jsonResponse).get
      parsed must equalTo(expected)
    }
  }
  "A JSON response with headers and content elements" should {
    val jsonResponse = """{"headers":{"aa":"bb","cc":"dd"},"content":{"b":2}}"""
    "parse to equivalent TransportResponse" in {
      val expected = TransportResponse(Map("aa" -> "bb", "cc" -> "dd"), Some(Map("b" -> 2)))
      val parsed = JsonDecoder.decodeResponse(jsonResponse).get
      parsed must equalTo(expected)
    }
  }
  "TransportResponse with content generated by JsonEncoder" should {
    val expected = TransportResponse(Map("key" -> "value"), Some(Map("a" -> 2)))
    val jsonResponse = JsonEncoder.encodeResponse(expected)
    "be properly parsed by JsonDecoder" in {
      val parsed = JsonDecoder.decodeResponse(jsonResponse).get
      parsed must equalTo(expected)
    }
  }
  "TransportResponse without content generated by JsonEncoder" should {
    val expected = TransportResponse(Map("key" -> "value"), None)
    val jsonResponse = JsonEncoder.encodeResponse(expected)
    "be properly parsed by JsonDecoder" in {
      val parsed = JsonDecoder.decodeResponse(jsonResponse).get
      parsed must equalTo(expected)
    }
  }
  "TransportRequest with content generated by JsonEncoder" should {
    val request = TransportRequest(Map("key" -> "value"), Some(Map("a" -> 1)))
    val jsonRequest = JsonEncoder.encodeRequest(request)
    "must be properly parsed to TransportResponse by JsonDecoder" in {
      val expected = TransportRequest(Map("key" -> "value"), Some(Map("a" -> 1)))
      val parsed = JsonDecoder.decodeRequest(jsonRequest).get
      parsed must equalTo(expected)
    }
  }
  "TransportRequest without content generated by JsonEncoder" should {
    val request = TransportRequest(Map("key" -> "value"), None)
    val jsonRequest = JsonEncoder.encodeRequest(request)
    "must be properly parsed to TransportResponse by JsonDecoder" in {
      val expected = TransportRequest(Map("key" -> "value"), None)
      val parsed = JsonDecoder.decodeRequest(jsonRequest).get
      parsed must equalTo(expected)
    }
  }
}