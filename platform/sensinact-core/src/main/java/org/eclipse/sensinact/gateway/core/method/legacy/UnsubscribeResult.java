/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.method.legacy;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResult;

/**
 * Extended  {@link AccessMethodResult} dedicated to {@link UnsubscribeMethod}
 * execution
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnsubscribeResult extends AccessMethodResult
{
	/**
	 * @param uri 
	 * @param parameters
	 */
    protected UnsubscribeResult(Mediator mediator, 
    		String uri, Object[] parameters)
    {
	    super(mediator, uri, parameters);
    }

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodResult#
	 * createSnaResponse(org.eclipse.sensinact.gateway.core.model.message.SnaMessage.Status)
	 */
    @Override
    protected AccessMethodResponse createSnaResponse(AccessMethodResponse.Status status)
    {
		return new UnsubscribeResponse(super.mediator, 
				super.getPath(), status);
    }
}
