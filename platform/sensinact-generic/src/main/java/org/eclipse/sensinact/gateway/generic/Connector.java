/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketTypeException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PayloadFragment;
import org.eclipse.sensinact.gateway.generic.packet.TaskIdValuePair;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages IO between a {@link ProtocolStackEndpoint} and
 * a set of {@link ServiceProvider}s
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class Connector<P extends Packet> extends TaskManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(Connector.class);
    /**
     * map of managed {@link ExtModelInstance}s
     */
    protected final Map<String,ExtModelInstance<?>> instances;

    /**
     * The global XML formated sensiNact resource model
     * configuration
     */
    protected final ExtModelConfiguration extModelConfiguration;
    
    /**
     * The model instance builder
     */
    protected final ExtModelInstanceBuilder extModelInstanceBuilder;

    /**
     * the {@link ConnectorCustomizer} handling the {@link PacketReader}
     * initialization
     */
    protected ConnectorCustomizer<P> customizer;

    /**
     * Initial lock status
     */
    protected boolean locked;
	private Mediator mediator;

    /**
     * Constructor
     *
     * @param context The associated {@link BundleContext}
     * @param locked  Defines the initial lock state of the
     *                {@link TokenEventProvider} to instantiate
     */
    public Connector(Mediator mediator,
    		ProtocolStackEndpoint<?> endpoint, 
    		ExtModelConfiguration<P> extModelConfiguration, 
    		ConnectorCustomizer<P> customizer) {
        super( endpoint, extModelConfiguration.isLockedAtInitializationTime(), extModelConfiguration.isDesynchronized());
        this.mediator=mediator;
        this.extModelConfiguration = extModelConfiguration;        
        this.extModelInstanceBuilder = new ExtModelInstanceBuilder<>(mediator);
        this.extModelInstanceBuilder.withConnector(this);
        
        this.locked = extModelConfiguration.isLockedAtInitializationTime();
        this.instances = new ConcurrentHashMap<>();

        this.customizer = customizer;
        this.configureCustomizer();
    }

    /**
     * Constructor
     *
     * @param context The associated {@link BundleContext}
     * @param locked  Defines the initial lock state of the
     *                {@link TokenEventProvider} to instantiate
     */
    public Connector(Mediator mediator,ProtocolStackEndpoint<?> endpoint, ExtModelConfiguration extModelConfiguration) {
        this(mediator,endpoint, extModelConfiguration, null);
    }

    /**
     * Configures this Connector's ConnectorCustomiser according
     * to initial configuration properties
     *
     * @param packetType this Connector's Packet type
     * @throws InvalidPacketTypeException
     */
    protected void configureCustomizer() {
        try {
            if (this.customizer == null)
                this.customizer = new DefaultConnectorCustomizer<P>(mediator,this.extModelConfiguration);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    /**
     * Read the {@link Packet} and extract the data allowing to create
     * of to feed sensiNact's data model
     * 
     * @param packet the extended {@link Packet} wrapping the data to be loaded
     * 
     * @throws InvalidPacketException if the specified {@link Packet} cannot be read 
     */
    public void process(P packet) throws InvalidPacketException {

        if (!this.customizer.preProcessing(packet)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Do not process the received packet : exiting");
            }
            return;
        }
        PacketReader<P> reader = this.customizer.newPacketReader(packet);
        if (reader == null) 
            throw new InvalidPacketException("Unable to create an appropriate reader");
        
        Iterator<PayloadFragment> subPacketIterator = reader.iterator();
        while (subPacketIterator.hasNext()) {        	
            PayloadFragment subPacket = subPacketIterator.next();
            
            List<TaskIdValuePair> taskIdValuePairs = subPacket.getTaskIdValuePairs();
            Iterator<TaskIdValuePair> iterator = 
            	(taskIdValuePairs==null||taskIdValuePairs.isEmpty())
            	?null:taskIdValuePairs.iterator();
            
            if(iterator != null) {
	            while (iterator.hasNext()) {
	                TaskIdValuePair taskIdValuePair = iterator.next();
	                String taskIdentifier = taskIdValuePair.taskIdentifier;
	                // No need to process if the protocol allows
	                // to identify the response according to the
	                // initial Task object
	                List<Task> tasks = super.remove(taskIdentifier);
	                Iterator<Task> taskIterator = tasks.iterator();
	
	                boolean treated = false;
	
	                while (taskIterator.hasNext()) {
	                    Task task = taskIterator.next();
	                    if (task != null && !task.isResultAvailable()) {
	                        task.setResult(taskIdValuePair.getValue(), taskIdValuePair.getTimestamp());
	                        treated = true;
	                    }
	                }
	                if (treated) 
	                	subPacket.treated(taskIdentifier);
	            }
            }
            String serviceProviderName = subPacket.getServiceProviderIdentifier();
            
            if (serviceProviderName == null) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Unable to identify the targeted service provider");
                continue;
            }
            
            ExtModelInstance<?> instance = this.instances.get(serviceProviderName);
            ExtServiceProviderImpl serviceProvider = null;

            if (subPacket.isGoodByeMessage()) {
                this.processGoodbye(instance);
                if (instance != null)
                    this.instances.remove(serviceProviderName, instance);
                continue;
            }
            if (instance == null) {
                try {
                    instance = this.addModelInstance(subPacket.getProfileId(), serviceProviderName);
                    if (instance == null)
                        continue;
                    LOG.debug("Service provider discovered : {}", serviceProviderName);
                } catch (InvalidServiceProviderException e) {
                    throw new InvalidPacketException(e);
                }
            }
            serviceProvider = instance.getRootElement();
            if (subPacket.isHelloMessage())
                this.processHello(serviceProvider);
            serviceProvider.process(subPacket);
            this.customizer.postProcessing(serviceProvider, reader);
        }
    }

    /**
     * Processes an 'hello' message sent by the {@link ServiceProvider}
     * passed as parameter
     *
     * @param serviceProvider the {@link ServiceProvider} joining the network
     */
    protected void processHello(ExtServiceProviderImpl serviceProvider) {
        if (ServiceProvider.LifecycleStatus.INACTIVE.equals(serviceProvider.getStatus())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Service provider {} activated", serviceProvider.getName());
            }
            serviceProvider.start();
        }
    }

    /**
     * Processes a 'goodbye' message sent by the {@link ServiceProvider}
     * passed as parameter
     *
     * @param serviceProvider the {@link ServiceProvider} leaving the network
     */
    protected void processGoodbye(final ExtModelInstance<?> instance) {
        if (instance == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("An unknown model instance is leaving the network");
            }
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuilder().append("Service provider '").append(instance.getName()).append("' is leaving the network").toString());
        }
        instance.unregister();
    }

    /**
     * Creates and returns a new {@link ExtModelInstance} holding 
     * an {@link ExtServiceProviderImpl} instance whose profile and 
     * name string are passed as parameter
     *
     * @param profileId string identifier of the
     * profile of the {@link ExtServiceProviderImpl} held by
     * the {@link ExtModelInstance} to be instantiated
     * @param serviceProviderName string name of the
     * {@link ExtServiceProviderImpl} held by
     * the {@link ExtModelInstance} to be instantiated
     * @return a new {@link ExtModelInstance} instance
     * @throws InvalidServiceProviderException
     */
	protected synchronized ExtModelInstance<?> addModelInstance(String profileId, 
    	final String serviceProviderName) throws InvalidServiceProviderException {

    	ExtModelInstance<?> instance = this.extModelInstanceBuilder
    			.build(serviceProviderName, profileId,this.extModelConfiguration);
        if (instance != null) {
            this.instances.put(serviceProviderName, instance);
        }
        return instance;
    }

    /**
     * Returns the {@link ExtModelInstance} whose name
     * is passed as parameter
     *
     * @param instanceName the name of the {@link ExtModelInstance}
     *                     to return
     * @return the {@link ExtModelInstance} with the specified
     * name
     */
    public ExtModelInstance<?> getModelInstance(String instanceName) {
        return this.instances.get(instanceName);
    }

    /**
     * Stops this factory, the created {@link TokenEventProvider} and all
     * {@link ExtServiceProviderImpl} instances
     */
    public void stop() {
        super.stop();
        if (this.instances == null || this.instances.size() == 0) {
            return;
        }
        
        synchronized (this.instances) {
        	this.instances.values().forEach(e -> {
        		try {
        			e.unregister();
        		} catch (IllegalStateException ex) {
        		}
        	});
            this.instances.clear();
        }
    }
}
