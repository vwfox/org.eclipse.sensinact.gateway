/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.packet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;

/**
 * A {@link PacketReader} factory service
 *
 * @param <P> extended {@link BasisPacket} type
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface PacketReaderFactory {
    /**
     * Returns true if this factory handles the {@link Packet}
     * type passed as parameter; otherwise returns false
     *
     * @param packetType the {@link Packet} type
     * @return <ul>
     * <li>true if the specified {@link Packet} type
     * is handled by this factory</li>
     * <li>false otherwise</li>
     * </ul>
     */
    boolean handle(Class<? extends Packet> packetType);

    /**
     * Returns a new {@link PacketReader} instance
     *
     * @param mediator the {@link Mediator} allowing to interact
     *                 with the OSGi host environment
     * @param manager  the {@link ExtModelConfiguration} managing
     *                 the set of accessible {@link ExtResourceConfig}s
     * @param packet   the {@link Packet} to parse
     * @return a new {@link PacketReader} instance
     * @throws InvalidPacketException
     */
    <P extends Packet> PacketReader<P> newInstance(ExtModelConfiguration manager, P packet) throws InvalidPacketException;
}
