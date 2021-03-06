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

package ccf.transport.http

import org.specs.Specification
import org.specs.mock.Mockito

import java.io.IOException
import java.net.URL

import ccf.transport.json.{JsonEncoder, JsonDecoder}
import ccf.transport._

object HttpConnectionSpec extends Specification with Mockito {
  "Invalid request" should {
    val url = new URL("http://www.url")
    "cause an InvalidRequestException" in {
      val client = mock[HttpClient]
      val decoder = mock[Decoder]
      val encoder = mock[Encoder]
      val conn = new HttpConnection(url, client, decoder, encoder, None, None) 
      conn.send(new TransportRequest(Map[String, String](), None)) must throwA[InvalidRequestException]
    }
  }
  "IOException thrown from HttpClient#post" should {
    val requestData = "data"
    val url = new URL("http://www.url")
    "cause a ConnectionException" in {
      val request = basicRequest
      val decoder = mock[Decoder]
      val encoder = mock[Encoder]
      encoder.encodeRequest(request) returns requestData
      val client = mock[HttpClient]
      client.post(any[URL], any[String]) throws new IOException
      val conn = new HttpConnection(url, client, decoder, encoder, None, None)
      conn.send(request) must throwA[ConnectionException]
    }
  }
  "HttpConnection with header contributor" should {
    val url = new URL("http://www.url")
    val originalHeaders = Map("type" -> "sometype", "originalHeader" -> "originalValue")
    val originalContent = Some("content")
    val originalRequest = TransportRequest(originalHeaders, originalContent)

    "add and replace headers to request" in {
      val contributedHeaders = Map(
        "originalHeader" -> "contributedValue",
        "newHeader" -> "newValue")
      val expectedHeaders = Map(
        "type" -> "sometype",
        "originalHeader" -> "contributedValue",
        "newHeader" -> "newValue")
      val expectedRequest = TransportRequest(expectedHeaders, originalContent)
      val encodedExpectedRequest = JsonEncoder.encodeRequest(expectedRequest)

      val contributor = new HttpTransportHeaderContributor {
        def getHeaders = contributedHeaders
      }

      val response = JsonEncoder.encodeResponse(TransportResponse(Map(), None))
      val client = mock[HttpClient]
      client.post(any[URL], any[String]) returns response
      val conn = new HttpConnection(url, client, JsonDecoder, JsonEncoder, None, Some(contributor))
      conn.send(originalRequest)
      there was one(client).post(any[URL], org.mockito.Matchers.eq(encodedExpectedRequest))
    }

  "HttpConnection create" should {
    "create a HttpConnection having http client with configured timeouts" in {
      val connection = HttpConnection.create(mock[URL], timeoutMillis = 5000)
      val client = connection.client.asInstanceOf[DispatchHttpClient]
      client.timeoutMillis must equalTo(5000)
    }
    "create a HttpConnection having http client with default timeouts" in {
      val connection = HttpConnection.create(mock[URL])
      val client = connection.client.asInstanceOf[DispatchHttpClient]
      client.timeoutMillis must equalTo(3000)
    }
   }
  }

  private def basicRequest: TransportRequest = {
    TransportRequest(Map("type" -> "spec"), None)
  }
}
