/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.rest.dto.notification;

import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;

/**
 * Notification of the new value of a resource
 */
public class ResourceDataNotificationDTO {

    public String provider;

    public String service;

    public String resource;

    public long timestamp;

    public Object oldValue;

    public Object newValue;

    public ResourceDataNotificationDTO() {
        // Do nothing
    }

    public ResourceDataNotificationDTO(ResourceDataNotification notif) {
        this.provider = notif.provider;
        this.service = notif.service;
        this.resource = notif.resource;
        this.timestamp = notif.timestamp.toEpochMilli();
        this.oldValue = notif.oldValue;
        this.newValue = notif.newValue;
    }
}
