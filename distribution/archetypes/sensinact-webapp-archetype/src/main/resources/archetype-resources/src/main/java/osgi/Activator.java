/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package ${package}.osgi;

import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import ${package}.app.component.CallbackServiceImpl;
import ${package}.app.servlet.IndexFilter;
import ${package}.app.servlet.ResourceFilter;
import ${package}.app.servlet.MirrorServlet;
import ${package}.WebAppConstants;
/**
 * Handle the bundle activation / deactivation
 */
public class Activator extends AbstractActivator<Mediator> {
	private static final Logger LOG = LoggerFactory.getLogger(TtnActivationListener.class);

	@Override
    public void doStart() {
    	mediator.register(new Hashtable() {{
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, WebAppConstants.WEBAPP_ROOT);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
            }
        },new IndexFilter(), 
    	new Class<?>[] {Filter.class});
        LOG.info(String.format("%s filter registered", WebAppConstants.WEBAPP_ROOT));
        
        mediator.register(new Hashtable() {{
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, WebAppConstants.WEBAPP_ALIAS);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
            }
        }, new ResourceFilter(super.mediator), 
        new Class<?>[] {Filter.class});
        LOG.info(String.format("%s filter registered",  WebAppConstants.WEBAPP_ALIAS));
               
	    mediator.register(new Hashtable() {{
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, WebAppConstants.WEBAPP_ALIAS);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
        	}
	    }, new MirrorServlet(), 
	    new Class<?>[] {Servlet.class});
    }

    @Override
    public void doStop() {
        LOG.info("Swagger API was unregistered from {} context", WebAppConstants.WEBAPP_ALIAS);
    }
    
	@Override
	public Mediator doInstantiate(BundleContext context) {
		return new Mediator(context);
	}

}
