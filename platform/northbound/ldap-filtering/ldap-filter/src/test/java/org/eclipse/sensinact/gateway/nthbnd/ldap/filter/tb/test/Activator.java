/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.ldap.filter.tb.test;

import java.util.Collections;

import org.assertj.core.util.Lists;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
    private LocalProtocolStackEndpoint<Packet> connector;

    public void doStart() throws Exception {
        ExtModelConfiguration<Packet> manager = ExtModelConfigurationBuilder.instance(super.mediator
        	).withStartAtInitializationTime(true
        	).withObserved(Lists.list("/service1/humidity/accessible","/service1/temperature"))
        	.build("resources.xml", Collections.<String, String>emptyMap());

        connector = new LocalProtocolStackEndpoint<Packet>(super.mediator);
        connector.connect(manager);
    }

    public void doStop() throws Exception {
        connector.stop();
        connector = null;
    }

    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
