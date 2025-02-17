/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.api.persistence.listener;

public abstract class ApplicationAvailabilityListenerAbstract implements ApplicationAvailabilityListener {
    @Override
    public void serviceOffline() {
    }

    @Override
    public void serviceOnline() {
    }

    @Override
    public void applicationFound(String applicationName, String content) {
    }

    @Override
    public void applicationRemoved(String applicationName) {
    }

    @Override
    public void applicationChanged(String applicationName, String content) {
    }
}
