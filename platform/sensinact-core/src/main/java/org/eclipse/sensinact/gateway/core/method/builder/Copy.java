/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method.builder;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Extended {@link DynamicParameterValue} returning the value of the Primitive
 * parameter when executed
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Copy extends AbstractDynamicParameterValue {
	public static final String NAME = "COPY";

	/**
	 * Constructor
	 * 
	 * @param index
	 *            the index of the execution parameter to copy
	 */
	public Copy(Mediator mediator, String parameterName, String resourceName) {
		super(mediator, parameterName, resourceName);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see JSONable#getJSON()
	 */
	@Override
	public String getJSON() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(JSONUtils.OPEN_BRACE);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(DynamicParameterValue.BUILDER_TYPE_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(this.getName());
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(DynamicParameterValue.BUILDER_PARAMETER_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(super.parameterName);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(DynamicParameterValue.BUILDER_RESOURCE_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(super.resourceName);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.CLOSE_BRACE);
		return buffer.toString();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see AccessMethodTrigger#getName()
	 */
	@Override
	public String getName() {
		return Copy.NAME;
	}

	/**
	 * @InheritedDoc
	 *
	 * @see DynamicParameterValue#getValue()
	 */
	@Override
	public Object getValue() {
		return this.getResourceValue();
	}
}
