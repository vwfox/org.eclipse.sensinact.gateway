/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Describe {@link AccessMethod}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DescribeMethod<T> extends AbstractAccessMethod<T, DescribeResponse<T>> {
	public static enum DescribeType {
		FILTER_LIST("FILTER_LIST", "namespaces"),COMPLETE_LIST("COMPLETE_LIST", "providers"), PROVIDERS_LIST("PROVIDERS_LIST", "providers"), PROVIDER(
				"DESCRIBE_PROVIDER", "response"), SERVICES_LIST("SERVICES_LIST",
						"services"), SERVICE("DESCRIBE_SERVICE", "response"), RESOURCES_LIST("RESOURCES_LIST",
								"resources"), RESOURCE("DESCRIBE_RESOURCE", "response");

		final String typeName;
		final String responseKey;

		DescribeType(String typeName, String responseKey) {
			this.typeName = typeName;
			this.responseKey = responseKey;
		}

		String getTypeName() {
			return this.typeName;
		}

		String getResponseKey() {
			return this.responseKey;
		}
	}

	private DescribeType describeType;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param uri
	 * @param preProcessingExecutor
	 * @param describeType
	 */
	public DescribeMethod(Mediator mediator, String uri, AccessMethodExecutor preProcessingExecutor,
			DescribeType describeType) {
		super(mediator, uri, AccessMethod.DESCRIBE, preProcessingExecutor);
		this.describeType = describeType;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AbstractAccessMethod#
	 *      createAccessMethodResponseBuilder(java.lang.Object[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public DescribeResponseBuilder<T> createAccessMethodResponseBuilder(Object[] parameters) {
		switch (this.describeType) {
		case FILTER_LIST:
		case COMPLETE_LIST:
		case PROVIDERS_LIST:
		case RESOURCES_LIST:
		case SERVICES_LIST:
			return (DescribeResponseBuilder<T>) new DescribeStringResponseBuilder(mediator, uri, describeType, null);
		case PROVIDER:
		case SERVICE:
		case RESOURCE:
			return (DescribeResponseBuilder<T>) new DescribeJSONResponseBuilder(mediator, uri, describeType);
		default:
			break;
		}
		return null;
	}
}
