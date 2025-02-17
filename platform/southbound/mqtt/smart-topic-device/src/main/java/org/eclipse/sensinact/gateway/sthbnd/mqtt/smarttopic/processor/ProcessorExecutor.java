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

import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.exception.ProcessorException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.selector.SelectorIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processor is the entity that will execute the Selector requests based on the supported Processors.
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorExecutor {
    private final Map<String, ProcessorFormatIface> processors = new HashMap<String, ProcessorFormatIface>();
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorExecutor.class);

    public ProcessorExecutor(final List<ProcessorFormatIface> processors) {
        for (ProcessorFormatIface processor : processors) {
            addProcessorFormatSupport(processor);
        }
    }

    public void addProcessorFormatSupport(ProcessorFormatIface processorFormat) {
        this.processors.put(processorFormat.getName(), processorFormat);
    }

    public String execute(final String inData, List<SelectorIface> selectors) throws ProcessorException {
        String incompleteProcessedInData = inData;
        for (SelectorIface selector : selectors) {
            try {
                //LOG.info("Selector {} IN Data {}  Expression {}", selector.getName(), incompleteProcessedInData, selector.getExpression());
                incompleteProcessedInData = processors.get(selector.getName()).process(incompleteProcessedInData, selector);
                //LOG.info("Selector {} OUT {}", selector.getName(), incompleteProcessedInData);
            } catch (Exception e) {
                throw new ProcessorException("Failed to execute processor " + selector.getName(), e);
            }
        }
        return incompleteProcessedInData;
    }
}
