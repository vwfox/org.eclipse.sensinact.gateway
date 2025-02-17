/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.oauth2.servlet;

import java.io.IOException;

//import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
//import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.sensinact.gateway.security.oauth2.IdentityServer;
import org.eclipse.sensinact.gateway.security.oauth2.OAuthServer;
import org.eclipse.sensinact.gateway.security.oauth2.UserInfo;

import jakarta.json.JsonObject;

@WebServlet(/*asyncSupported = true*/)
public class SecurityServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private IdentityServer idServer;
	private OAuthServer authServer;

	public SecurityServlet(IdentityServer idServer, OAuthServer authServer) {
		this.authServer = authServer;
		this.idServer = idServer;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doExecute(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doExecute(req,resp);
	}
	
	private final void doExecute(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }
//        final AsyncContext asyncContext;
//        if (request.isAsyncStarted()) {
//            asyncContext = request.getAsyncContext();
//        } else {
//            asyncContext = request.startAsync(request, response);
//        }
//        response.getOutputStream().setWriteListener(new WriteListener() {
//            @Override
//            public void onWritePossible() throws IOException {
//                HttpServletRequest req = (HttpServletRequest) asyncContext.getRequest();
//                HttpServletResponse res = (HttpServletResponse) asyncContext.getResponse();

                String code = request.getParameter("code");
        		if (code != null) {
        			int status = 401;
        			HttpSession session = request.getSession();
        			JsonObject tokens = authServer.verify(code, request);
        			if (tokens != null) {
        				String id_token;
        				String access_token;
        				try {
        					id_token = tokens.getString("id_token");
        					access_token = tokens.getString("access_token");
        					UserInfo user = idServer.getUserInfo(id_token, access_token);
        					authServer.addCredentials(access_token, user);
        					session.setAttribute("token", access_token);
        					status = 200;
        				} catch (Exception e) {
        					e.printStackTrace();
        				}
        			} else {
        				session.setAttribute("token", "");
        			}
    				response.setStatus(status);
        			String uri = (String) session.getAttribute("redirect_uri");
        			if (uri != null) {
        				response.sendRedirect(uri);
        			}
        		} 
//        		if (req.isAsyncStarted()) {
//                    asyncContext.complete();
//                }
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                t.printStackTrace();
//            }
//
//        });
    }
}
