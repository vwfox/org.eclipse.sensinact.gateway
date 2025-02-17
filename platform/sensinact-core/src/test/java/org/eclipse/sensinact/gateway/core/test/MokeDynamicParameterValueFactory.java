/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.builder.DynamicParameterValueFactory;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory;
import jakarta.json.JsonObject;

/**
 * 
 */
public class MokeDynamicParameterValueFactory implements DynamicParameterValueFactory {

	/**
	 * @InheritedDoc
	 *
	 * @see AccessMethodTriggerFactory#handle(java.lang.String)
	 */
	@Override
	public boolean handle(String type) {
		return "VARIABLE_PARAMETER_BUILDER".equals(type);
	}

	/**
	 * @InheritedDoc
	 *
	 * @see DynamicParameterValueFactory#newInstance(org.eclipse.sensinact.gateway.util.mediator.AbstractMediator,
	 *      org.eclipse.sensinact.gateway.core.model.ServiceImpl, java.lang.String,
	 *      jakarta.json.JSONObject)
	 */
	@Override
	public DynamicParameterValue newInstance(Mediator mediator, Executable<Void, Object> resourceValueExtractor,
			JsonObject builder) throws InvalidValueException {
		String resourceName = builder.getString(DynamicParameterValue.BUILDER_RESOURCE_KEY, null);
		String parameterName = builder.getString(DynamicParameterValue.BUILDER_PARAMETER_KEY, null);
		MokeDynamicParameterValue moke = new MokeDynamicParameterValue(mediator, parameterName, resourceName);
		moke.setResourceValueExtractor(resourceValueExtractor);
		return moke;
	}

}
