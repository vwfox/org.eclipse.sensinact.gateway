/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.core.filtering.FilteringCollection;
import org.eclipse.sensinact.gateway.util.UriUtils;

public class ServiceRequest extends ServicesRequest {
    private String service;

    public ServiceRequest(String requestIdentifier, String serviceProvider, String service, FilteringCollection filteringCollection) {
        super(requestIdentifier, serviceProvider, filteringCollection);
        this.service = service;
//      if (this.service == null) {
//      throw new NullPointerException("Service missing");
//  }
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#getName()
     */
    public String getName() {
        return service;
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#getPath()
     */
    @Override
    public String getPath() {
        return new StringBuilder().append(super.getPath()).append(UriUtils.PATH_SEPARATOR).append(this.getName()).toString();
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#getMethod()
     */
    @Override
    protected String getMethod() {
        return "serviceDescription";
    }

    /**
     * @inheritDoc
     * @see NorthboundRequest#getExecutionArguments()
     */
    @Override
    protected Argument[] getExecutionArguments() {
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + 1];
        if (length > 0) {
            System.arraycopy(superArguments, 0, arguments, 0, length);
        }
        arguments[length] = new Argument(String.class, service);
        return arguments;
    }
}
