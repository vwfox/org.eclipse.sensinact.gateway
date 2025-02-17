/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.http;

import java.io.IOException;

//import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccessConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CORS Filter
 */
@WebFilter()
public class CorsFilter implements Filter {
	
	private static final Logger LOG = LoggerFactory.getLogger(CorsFilter.class);

    /**
     * @inheritDoc
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.info("Init with config [" + config + "]");
        }
    }

    /**
     * @inheritDoc
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        if (res.isCommitted()) {
            return;
        }
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if (RestAccessConstants.OPTIONS.equals(request.getMethod())) {
            ((HttpServletResponse)res).setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

            StringBuilder builder = new StringBuilder();
            String controlRequestHeaders = request.getHeader("Access-Control-Request-Headers");

            if (controlRequestHeaders != null && controlRequestHeaders.length() > 0) {
                builder.append(controlRequestHeaders);
                builder.append(",");
            }
            builder.append("Authorization, X-Auth-Token, X-Requested-With");
            response.setHeader("Access-Control-Allow-Headers", builder.toString());

            final ServletOutputStream output = res.getOutputStream();
            output.println();
        } else {
            try {
                chain.doFilter(req, res);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @inheritDoc
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.info("Destroyed filter");
        }
    }
}
