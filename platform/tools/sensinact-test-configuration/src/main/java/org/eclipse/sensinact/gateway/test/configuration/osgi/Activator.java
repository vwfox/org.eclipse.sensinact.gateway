/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.test.configuration.osgi;

import java.util.ConcurrentModificationException;
import java.util.List;

import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;


/**
 * Bundle Activator
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        String codeBase = context.getProperty("org.eclipse.sensinact.gateway.test.codeBase");

        if (codeBase == null || codeBase.length() == 0) 
            return;
        
        String[] codeBases = codeBase.split(",");

        ServiceReference<ConditionalPermissionAdmin> sRef = context.getServiceReference(
        		ConditionalPermissionAdmin.class);

        ConditionalPermissionAdmin cpa = null;

        if (sRef == null) 
            throw new BundleException("ConditionalPermissionAdmin services needed");
        
        cpa = context.getService(sRef);

        ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

        int index = 0;
        int length = codeBases.length;
        for (; index < length; index++) {
            piList.add(cpa.newConditionalPermissionInfo(
            		String.format("ALLOW {[org.eclipse.sensinact.gateway.core.security.perm.CodeBaseCondition \"%s\"]" 
            				+ "(java.security.AllPermission \"\" \"\")" 
            				+ "} null", codeBases[index])));
        }
        if (!cpu.commit()) 
            throw new ConcurrentModificationException("Permissions changed during update");
        
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        //nothing to do here
    }

}
