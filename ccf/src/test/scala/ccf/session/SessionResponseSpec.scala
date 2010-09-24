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

package ccf.session

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.transport.{TransportRequestType, TransportResponse}

class SessionResponseSpec extends Specification with Mockito {
  "Session response factory" should {

    "fail without type in transport response" in {
      val responseHeaders = Map[String, String]()
      val transportResponse = TransportResponse(responseHeaders, None)
      SessionResponse(transportResponse, mock[SessionRequest]) must throwAn(new RuntimeException("No response type found"))
    }

    "create successful join response from" in {
      successCreationSpec(mock[JoinRequest], TransportRequestType.join,
        (transportResponse: TransportResponse, result: Either[Success, Failure]) => JoinResponse(transportResponse, result))
    }

    "create failure join response from" in {
      failureCreationSpec(mock[JoinRequest], TransportRequestType.join,
        (transportResponse: TransportResponse, result: Either[Success, Failure]) => JoinResponse(transportResponse, result))
    }

    "create successful part response from" in {
      successCreationSpec(mock[PartRequest], TransportRequestType.part,
        (transportResponse: TransportResponse, result: Either[Success, Failure]) => PartResponse(transportResponse, result))
    }

    "create failure part response from" in {
      failureCreationSpec(mock[PartRequest], TransportRequestType.part,
        (transportResponse: TransportResponse, result: Either[Success, Failure]) => PartResponse(transportResponse, result))
    }

    "create successful in channel response from" in {
      successCreationSpec(mock[InChannelRequest], "testRequestType",
        (transportResponse: TransportResponse, result: Either[Success, Failure]) => InChannelResponse(transportResponse, result))
    }

    "create failure in channel response from" in {
      failureCreationSpec(mock[InChannelRequest], "testRequestType",
        (transportResponse: TransportResponse, result: Either[Success, Failure]) => InChannelResponse(transportResponse, result))
    }

    "create successful operation context response from" in {
      successCreationSpec(mock[OperationContextRequest], TransportRequestType.context,
        (transportResponse: TransportResponse, result: Either[Success, Failure]) => OperationContextResponse(transportResponse, result))
    }

    "create failure operation context response from" in {
      failureCreationSpec(mock[OperationContextRequest], TransportRequestType.context,
        (transportResponse: TransportResponse, result: Either[Success, Failure]) => OperationContextResponse(transportResponse, result))
    }
  }

  def successCreationSpec(request: SessionRequest, requestType: String,
                                  expectedResponse: (TransportResponse, Either[Success, Failure]) => SessionResponse) {
    val responseHeaders = Map("type" -> requestType)

    "successful transport response" in {
      val transportResponse = TransportResponse(responseHeaders, SessionRequest.successResponseContent)
      SessionResponse(transportResponse, request) mustEqual expectedResponse(transportResponse, Left(Success(request, None)))
    }
  }

  def failureCreationSpec(request: SessionRequest, requestType: String,
                                  expectedResponse: (TransportResponse, Either[Success, Failure]) => SessionResponse) {
    val responseHeaders = Map("type" -> requestType)

    "failure transport response" in {
      val reason = "test reason"
      val transportResponse = TransportResponse(responseHeaders, SessionRequest.failureResponseContent(reason))
      SessionResponse(transportResponse, request) mustEqual expectedResponse(transportResponse, Right(Failure(request, reason)))
    }

    "transport response without content" in {
      val transportResponse = TransportResponse(responseHeaders, None)
      SessionResponse(transportResponse, request) mustEqual expectedResponse(transportResponse, Right(Failure(request, "Response missing content")))
    }

    "transport response with mistyped content" in {
      class UnsupportedResponseContent
      val transportResponse = TransportResponse(responseHeaders, Some(new UnsupportedResponseContent))
      SessionResponse(transportResponse, request) mustEqual expectedResponse(transportResponse, Right(Failure(request, "Unrecognized response content")))
    }

    "transport response with empty content" in {
      val transportResponse = TransportResponse(responseHeaders, Some(Map()))
      SessionResponse(transportResponse, request) mustEqual expectedResponse(transportResponse, Right(Failure(request, "Empty response content")))
    }

    "transport response with mistyped content map" in {
      val transportResponse = TransportResponse(responseHeaders, Some(Map[String,Int]("result" -> 42)))
      SessionResponse(transportResponse, request) mustEqual expectedResponse(transportResponse, Right(Failure(request, "Mistyped response content")))
    }

    "transport response with missing status" in {
      val transportResponse = TransportResponse(responseHeaders, Some(Map[String,String]("reason" -> "test reason")))
      SessionResponse(transportResponse, request) mustEqual expectedResponse(transportResponse, Right(Failure(request, "Unkown response status")))
    }

    "transport response with unrecognized status" in {
      val transportResponse = TransportResponse(responseHeaders, Some(Map[String,String]("result" -> "TEST")))
      SessionResponse(transportResponse, request) mustEqual expectedResponse(transportResponse, Right(Failure(request, "Unkown response status")))
    }
  }
}