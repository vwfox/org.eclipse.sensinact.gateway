/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.exception.ProcessorFormatException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.selector.SelectorIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stateless class that is capable of interprete a given format.
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorFormatToFloat implements ProcessorFormatIface {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorFormatToFloat.class);

    @Override
    public String getName() {
        return "toFloat";
    }

    @Override
    public String process(String inData, SelectorIface selector) throws ProcessorFormatException {
        try {
            Float value = new Float(inData);
            return Float.valueOf(value).toString();
        } catch (Exception e) {
            LOG.error("Failed to apply {} filter. Bypassing filter", getName(), e);
            return inData;
        }
    }
}
