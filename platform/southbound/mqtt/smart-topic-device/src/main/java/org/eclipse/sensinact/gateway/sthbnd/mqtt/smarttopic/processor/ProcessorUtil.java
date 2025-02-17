/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.selector.Selector;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.selector.SelectorIface;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility method used by the processor to interpolate messages
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorUtil {
    /**
     * This method transforms processor string property  in a list of selectors with its respective expression to be interpreted. e.g. 'array$0,json$name,'
     *
     * @param payload
     * @return
     */
    public static List<SelectorIface> transformProcessorListInSelector(String payload) {
        List<SelectorIface> result = new ArrayList<SelectorIface>();
        StringTokenizer stProcessorTuple = new StringTokenizer(payload, ",");
        while (stProcessorTuple.hasMoreTokens()) {
            String processorTuple = stProcessorTuple.nextToken();
            StringTokenizer stProcessor = new StringTokenizer(processorTuple, "$");
            String processor[] = new String[]{"", ""};
            Integer index = 0;
            while (stProcessor.hasMoreTokens()) {
                processor[index++] = stProcessor.nextToken();
            }
            result.add(new Selector(processor[0], processor[1]));
        }
        return result;
    }
}
