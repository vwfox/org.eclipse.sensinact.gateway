/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;

/**
 * Creates {@link ResourceConfig}s for a specific southbound bridge
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ResourceConfigBuilder {
	public static final Class<? extends Resource> RESOURCE_TYPE = PropertyResource.class;
	public static final Class<?> DATA_TYPE = String.class;
	public static final Modifiable MODIFIABLE = Modifiable.MODIFIABLE;
	public static final Resource.UpdatePolicy UPDATE_POLICY = Resource.UpdatePolicy.NONE;

	/**
	 * Returns the {@link ResourceConfig} described by the
	 * {@link ResourceDescriptor} passed as parameter
	 * 
	 * @param resourceConfigDescriptor
	 *            the {@link ResourceDescriptor} describing the
	 *            {@link ResourceConfig} to be returned
	 * 
	 * @return the {@link ResourceConfig} described by the specified
	 *         {@link ResourceDescriptor}
	 */
	<G extends ResourceConfig, D extends ResourceDescriptor> G getResourceConfig(D resourceConfigDescriptor);

	/**
	 * Set the default extended {@link Resource} interface to be used by this
	 * ResourceConfigBuilder
	 * 
	 * @param defaultResourceType
	 *            the default {@link Resource} interface to be used
	 */
	void setDefaultResourceType(Class<? extends Resource> defaultResourceType);

	/**
	 * Set the default data Type to be used by this ResourceConfigBuilder
	 * 
	 * @param defaultDataType
	 *            the default data Type to be used
	 */
	void setDefaultDataType(Class<?> defaultDataType);

	/**
	 * Set the default {@link Modifiable} to be used by this ResourceConfigBuilder
	 * 
	 * @param defaultModifiable
	 *            the default {@link Modifiable} to be used
	 */
	void setDefaultModifiable(Modifiable defaultModifiable);

	/**
	 * Set the default {@link Resource.UpdatePolicy} to be used by this
	 * ResourceConfigBuilder
	 * 
	 * @param defaultUpdatePolicy
	 *            the default {@link Resource.UpdatePolicy} to be used
	 */
	void setDefaultUpdatePolicy(Resource.UpdatePolicy defaultUpdatePolicy);

}
