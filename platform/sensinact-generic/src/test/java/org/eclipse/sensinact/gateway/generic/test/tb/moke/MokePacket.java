/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.moke;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.TaskManager;
import org.eclipse.sensinact.gateway.generic.packet.Packet;

public class MokePacket implements Packet {

    private String serviceId;
    private String resourceId;
    private String processorId = null;
    private String taskId = null;
    private Object data;

    private CommandType command = null;

    /**
     * Constructor
     *
     * @param mediator    the associated {@link Mediator}
     * @param processorId the targeted {@link PacketProcessor} string
     *                    identifier
     * @param taskId      the associated {@link Task} string identifier
     * @param data        the embedded data array
     */
    public MokePacket(String processorId, String taskId, String serviceId, String resourceId, Object data) {
        this.processorId = processorId;
        this.taskId = taskId;
        this.resourceId = resourceId;
        this.data = data;
        this.serviceId = serviceId;
    }

    /**
     * @param taskId
     * @param mediator2
     * @param string
     * @param strings
     */
    public MokePacket(String processorId, String taskId, String[] serviceIds) {
        this.processorId = processorId;
        this.data = serviceIds;
        this.taskId = taskId;
    }

    /**
     * Returns the string identifier of the targeted
     * {@link PacketProcessor}
     *
     * @return the string identifier of the targeted
     * {@link PacketProcessor}
     */
    public String getServiceProviderIdentifier() {
        return this.processorId;
    }

    /**
     * Returns the string identifier of the
     * associated {@link Task} if any
     *
     * @return the string identifier of the
     * associated {@link Task} if any
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * Returns the data array
     *
     * @return the data array
     */
    public Object getData() {
        return this.data;
    }

    /**
     * @return
     */
    public String getResourceId() {
        return this.resourceId;
    }

    /**
     * @return
     */
    public String getServiceId() {
        return this.serviceId;
    }

    @Override
    public byte[] getBytes() {
        return null;
    }

    CommandType getCommand() {
        if (this.command == null && this.taskId != null) {
            if (taskId.endsWith("SERVICES_ENUMERATION")) {
                return CommandType.SERVICES_ENUMERATION;
            }
            String[] taskIdElements = this.taskId.split(new String(new char[]{TaskManager.IDENTIFIER_SEP_CHAR}));

            this.command = CommandType.valueOf(taskIdElements[1]);
        }
        return this.command;
    }
}
