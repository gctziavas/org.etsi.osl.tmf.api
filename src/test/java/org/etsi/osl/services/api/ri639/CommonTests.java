/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 - 2024 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package org.etsi.osl.services.api.ri639;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.etsi.osl.tmf.ri639.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommonTests {

    @Test
    public void testApiException() {
        int errorCode = 404;
        String errorMessage = "Not Found";

        ApiException exception = new ApiException(errorCode, errorMessage);

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    public void testApiOriginFilter() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        ApiOriginFilter filter = new ApiOriginFilter();
        filter.doFilter(request, response, chain);

        verify(response).addHeader("Access-Control-Allow-Origin", "*");
        verify(response).addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        verify(response).addHeader("Access-Control-Allow-Headers", "Content-Type");
        verify(chain).doFilter(request, response);
    }

    @Test
    public void testApiResponseMessage() {
        int[] codes = {ApiResponseMessage.ERROR, ApiResponseMessage.WARNING, ApiResponseMessage.INFO, ApiResponseMessage.OK, ApiResponseMessage.TOO_BUSY, 6};

        String[] types = {"error", "warning", "info", "ok", "too busy", "unknown"};

        String[] messages = {"An error occured", "This is a warning", "Given info", "ok", "System is too busy", "unknown code"};

        for (int i = 0; i < codes.length; i++) {
            int code = codes[i];
            String type = types[i];
            String message = messages[i];

            ApiResponseMessage responseMessage = new ApiResponseMessage(code, message);

            assertEquals(message, responseMessage.getMessage());
            assertEquals(code, responseMessage.getCode());
            assertEquals(type, responseMessage.getType());

        }

        ApiResponseMessage responseMessage = new ApiResponseMessage();
        responseMessage.setMessage("Error");
        assertEquals("Error", responseMessage.getMessage());
        responseMessage.setType("ok");
        assertEquals("ok", responseMessage.getType());
        responseMessage.setCode(ApiResponseMessage.OK);
        assertEquals(ApiResponseMessage.OK, responseMessage.getCode());
    }

    @Test
    public void testHubApiController() {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpServletRequest request = new MockHttpServletRequest();

        HubApiController controller = new HubApiController(objectMapper, request);

        Optional<ObjectMapper> returnedObjectMapper = controller.getObjectMapper();
        Optional<HttpServletRequest> returnedRequest = controller.getRequest();

        assertTrue(returnedObjectMapper.isPresent());
        assertTrue(returnedRequest.isPresent());

        assertEquals(objectMapper, returnedObjectMapper.get());
        assertEquals(request, returnedRequest.get());
    }

    @Test
    public void testListenerApiController() {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpServletRequest request = new MockHttpServletRequest();

        ListenerApiController controller = new ListenerApiController(objectMapper, request);

        Optional<ObjectMapper> returnedObjectMapper = controller.getObjectMapper();
        Optional<HttpServletRequest> returnedRequest = controller.getRequest();

        assertTrue(returnedObjectMapper.isPresent());
        assertTrue(returnedRequest.isPresent());

        assertEquals(objectMapper, returnedObjectMapper.get());
        assertEquals(request, returnedRequest.get());
    }

    @Test
    public void testNotFoundException() {
        int errorCode = 404;
        String errorMessage = "Not Found";

        NotFoundException exception = new NotFoundException(errorCode, errorMessage);

        assertEquals(errorMessage, exception.getMessage());
    }

}

