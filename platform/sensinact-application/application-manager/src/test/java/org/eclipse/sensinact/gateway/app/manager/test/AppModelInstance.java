/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
/**
 *
 */
package org.eclipse.sensinact.gateway.app.manager.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ResourceBuilder;
import org.eclipse.sensinact.gateway.core.ResourceDescriptor;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AppModelInstance<C extends ModelConfiguration> extends ModelInstance<C> {
    /**
     * @param mediator
     * @param resourceModelConfig
     * @param name
     * @param profileId
     * @throws InvalidServiceProviderException
     */
    public AppModelInstance(Mediator mediator, C resourceModelConfig, String name, String profileId) throws InvalidServiceProviderException {
        super(mediator, resourceModelConfig, name, profileId);
    }

    /**
     * @inheritDoc
     * @see ModelInstance#
     * getResourceBuilder(ResourceDescriptor, byte)
     */
    public ResourceBuilder getResourceBuilder(ResourceDescriptor descriptor, byte buildPolicy) {
        return super.getResourceBuilder(descriptor, buildPolicy);
    }
}
