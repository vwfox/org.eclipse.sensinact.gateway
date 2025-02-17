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

import org.eclipse.sensinact.gateway.core.ServiceProviderProcessableContainer;

/**
 * A PacketReader provides access to the semantic units of a {@link
 * Packet} reified as {@link PayloadFragment}s. Its also iterable over all
 * {@link TaskIdValuePair}s that can be build by parsing the treated
 * packet
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface PacketReader<P extends Packet> extends ServiceProviderProcessableContainer<PayloadFragment> {
    /**
     * Loads the {@link Packet} passed as parameter
     *
     * @param packet the {@link Packet} to be loaded
     * 
     * @throws InvalidPacketException if an error occurs while loading the 
     * specified {@link Packet}
     */
    void load(P packet) throws InvalidPacketException;
    
    /**
     * Parses a previously loaded {@link Packet} if any to define
     * fields relative to sensiNact's model instance hierarchies
     * 
     * @throws InvalidPacketException if an error occurs while parsing 
     * a previously loaded {@link Packet}
     */
    void parse() throws InvalidPacketException;
    
    /**
     * Clears all previously defined field
     */
    void reset();

}
