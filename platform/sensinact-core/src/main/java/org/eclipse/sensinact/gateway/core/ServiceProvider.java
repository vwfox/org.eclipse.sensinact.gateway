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

import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Localizable;
import org.eclipse.sensinact.gateway.common.primitive.Stateful;

/**
 * This class represents a ServiceProvider on the sensiNact gateway.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ServiceProvider
		extends ElementsProxy<Service>, ServiceCollection, Localizable, Stateful<ServiceProvider.LifecycleStatus> {
	public static final String LATITUDE_PROPERTY = "org.eclipse.sensinact.gateway.location.latitude";

	public static final String LONGITUDE_PROPERTY = "org.eclipse.sensinact.gateway.location.longitude";

	public static final double DEFAULT_Kentyou_LOCATION_LATITUDE = 45.19334890078532d;
	public static final double DEFAULT_Kentyou_LOCATION_LONGITUDE = 5.706474781036377d;

	/**
	 * ServiceProvider's lifecycle possible states enumeration
	 */
	public enum LifecycleStatus {
		JOINING, ACTIVE, LEAVING, INACTIVE, UNKNWON;
	}

	/**
	 * the administration service name
	 */
	public static final String ADMINISTRATION_SERVICE_NAME = "admin";

	/**
	 * the status
	 */
	public static final String LIFECYCLE_STATUS = "lifecycleStatus";

	/**
	 * the service provider friendly name
	 */
	public static final String FRIENDLY_NAME = "friendlyName";

	/**
	 * the name of the bridge providing the resource
	 */
	public static final String BRIDGE = "bridge";

	/**
	 * the url to the default icon representing the resource
	 */
	public static final String ICON = "icon";

}
