/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.filter.geojson.http.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class HttpServiceTestClient {
	private static ObjectMapper mapper = new ObjectMapper();
	
    public static String newRequest(Mediator mediator, String url, String content, String method) {
        SimpleResponse response;
        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
        builder.setUri(url);
        builder.setAccept("application/json");
        /*builder.addHeader("Authorization",
        		"Basic " + Base64.encodeBytes(
                	"cea:sensiNact_team".getBytes()));*/
        try {
            if (method.equals("GET")) {
                builder.setHttpMethod("GET");

            } else if (method.equals("POST")) {
                builder.setContentType("application/json");
                builder.setHttpMethod("POST");
                if (content != null && content.length() > 0) {
                	mapper.readTree(content);
                	builder.setContent(content);
                }
            } else {
                return null;
            }
            SimpleRequest request = new SimpleRequest(builder);
            response = request.send();
            byte[] responseContent = response.getContent();
            String contentStr = (responseContent == null ? null : new String(responseContent));
            return contentStr;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
