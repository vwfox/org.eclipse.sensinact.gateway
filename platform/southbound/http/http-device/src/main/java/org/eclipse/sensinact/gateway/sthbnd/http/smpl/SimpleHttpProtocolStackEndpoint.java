/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTaskImpl;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.ChainedHttpTaskDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.ChainedHttpTasksDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.HttpProtocolStackEndpointTasksDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.HttpTasksDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.RecurrentChainedHttpTaskDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.RecurrentHttpTaskDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.SimpleHttpTaskDescription;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */

public class SimpleHttpProtocolStackEndpoint extends HttpProtocolStackEndpoint {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
	
	private static final Logger LOG= LoggerFactory.getLogger(SimpleHttpProtocolStackEndpoint.class);
    public static final Class<? extends HttpTask> GET_TASK = HttpTaskImpl.class;
    public static final Class<? extends HttpTask> SET_TASK = HttpTaskImpl.class;
    public static final Class<? extends HttpTask> ACT_TASK = HttpTaskImpl.class;
    public static final Class<? extends HttpTask> SUBSCRIBE_TASK = HttpTaskImpl.class;
    public static final Class<? extends HttpTask> UNSUBSCRIBE_TASK = HttpTaskImpl.class;
    public static final Class<? extends HttpTask> SERVICES_ENUMERATION_TASK = HttpTaskImpl.class;

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    private String endpointId;
    private Class<? extends HttpTask> getTaskClass = null;
    private Class<? extends HttpTask> setTaskClass = null;
    private Class<? extends HttpTask> actTaskClass = null;
    private Class<? extends HttpTask> subscribeTaskClass = null;
    private Class<? extends HttpTask> unsubscribeTaskClass = null;
    private Class<? extends HttpTask> servicesEnumerationTaskClass = null;
    protected Deque<RecurrentHttpTaskConfigurator> recurrences;
    protected Map<CommandType, HttpTaskBuilder> adapters;
    protected Map<CommandType, HttpTaskUrlConfigurator> builders;
    protected Set<String> recurrenceTasks;
    protected ScheduledExecutorService worker;
	private HttpMediator mediator;

    /**
     * @param mediator
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public SimpleHttpProtocolStackEndpoint(HttpMediator mediator) throws ParserConfigurationException, SAXException, IOException {
       this.mediator=mediator;
        this.recurrences = new LinkedList<>();
        this.adapters = new HashMap<>();
        this.builders = new HashMap<>();
        this.recurrenceTasks = Collections.synchronizedSet(new HashSet<>());
        this.worker = Executors.newScheduledThreadPool(3);
        
        //Mediator classloader because we don't need to retrieve
        //all declared factories in the OSGi environment, but only
        //the one specified in the bundle instantiating this
        //SimpleHttpProtocolStackEndpoint
        ServiceLoader<HttpTaskUrlConfigurator> loader = ServiceLoader.load(HttpTaskUrlConfigurator.class, mediator.getClassLoader());

        Iterator<HttpTaskUrlConfigurator> iterator = loader.iterator();
        while (iterator.hasNext()) {
            HttpTaskUrlConfigurator builder = iterator.next();
            CommandType[] types = builder.handled();

            int index = 0;
            int length = types == null ? 0 : types.length;

            for (; index < length; index++) {
                this.builders.put(types[index], builder);
            }
        }
    }
    
    public void registerAdapters(HttpProtocolStackEndpointTasksDescription config) {
	    HttpTasksDescription taskArray = config.getStandalone();
	    List<SimpleHttpTaskDescription> tasks = taskArray == null ? null : taskArray.getTasks();
	    int index = 0;
	    int length = tasks == null ? 0 : tasks.size();
	    for (; index < length; index++) {
	        this.registerAdapter(tasks.get(index));
	    }
	    List<RecurrentHttpTaskDescription> recurrences = taskArray == null ? null : taskArray.getRecurrences();
	    index = 0;
	    length = recurrences == null ? 0 : recurrences.size();
	    for (; index < length; index++) {
	       this.registerAdapter(recurrences.get(index));
	    }
	    ChainedHttpTasksDescription chainedTaskArray = config.getChained();
	    List<ChainedHttpTaskDescription> chainedTasks = chainedTaskArray == null ? null : chainedTaskArray.getTasks();
	    index = 0;
	    length = chainedTasks == null ? 0 : chainedTasks.size();
	    for (; index < length; index++) {
	        this.registerAdapter(chainedTasks.get(index));
	    }
	    List<RecurrentChainedHttpTaskDescription> recurrentChainedTasks = chainedTaskArray == null ? null : chainedTaskArray.getRecurrences();
	    index = 0;
	    length = recurrentChainedTasks == null ? 0 : recurrentChainedTasks.size();
	    for (; index < length; index++) {
	        this.registerAdapter(recurrentChainedTasks.get(index));
	    }
    }

    /**
     * @param chainedHttpTask
     */
    public void registerAdapter(ChainedHttpTaskDescription chainedHttpTask) {
        List<CommandType> commands = chainedHttpTask.getCommands();
        int length = commands == null ? 0 : commands.size();
        int index = 0;

        for (; index < length; index++) {
            ChainedHttpTaskConfigurator executor = new ChainedHttpTaskConfigurator(this, 
            	chainedHttpTask.getProfile(), commands.get(index), this.builders.get(commands.get(index)), 
            	chainedHttpTask.getConfiguration(), chainedHttpTask.getChain());

            switch (commands.get(index)) {
                case ACT:
                    this.setActTaskType(chainedHttpTask.getChaining());
                    break;
                case GET:
                    this.setGetTaskType(chainedHttpTask.getChaining());
                    break;
                case SERVICES_ENUMERATION:
                    this.setServicesEnumerationTaskType(chainedHttpTask.getChaining());
                    break;
                case SET:
                    this.setSetTaskType(chainedHttpTask.getChaining());
                    break;
                case SUBSCRIBE:
                    this.setSubscribeTaskType(chainedHttpTask.getChaining());
                    break;
                case UNSUBSCRIBE:
                    this.setUnsubscribeTaskType(chainedHttpTask.getChaining());
                    break;
                default:
                    break;
            }
            this.adapters.put(commands.get(index), executor);
        }
    }

    /**
     * @param chainedHttpTask
     */
    public void registerAdapter(RecurrentChainedHttpTaskDescription chainedHttpTask) {
        RecurrentHttpTaskConfigurator executor = new RecurrentChainedTaskConfigurator(this, 
        	chainedHttpTask.getCommand(), this.builders.get(chainedHttpTask.getCommand()), 
        	chainedHttpTask.getChaining(), chainedHttpTask.getPeriod(), chainedHttpTask.getDelay(), 
        	chainedHttpTask.getTimeout(), chainedHttpTask.getConfiguration(), chainedHttpTask.getChain());
        this.recurrences.add(executor);
    }

    /**
     * @param command
     * @param executor
     */
    public void registerAdapter(SimpleHttpTaskDescription httpTaskAnnotation) {
        List<CommandType> commands = httpTaskAnnotation.getCommands();
        int length = commands == null ? 0 : commands.size();
        int index = 0;

        for (; index < length; index++) {
            SimpleTaskConfigurator executor = new SimpleTaskConfigurator(this, 
            	httpTaskAnnotation.getProfile(), commands.get(index), this.builders.get(commands.get(index)), 
            	httpTaskAnnotation.getConfiguration());
            this.adapters.put(commands.get(index), executor);
        }
    }

    /**
     * @param reccurent
     */
    public void registerAdapter(RecurrentHttpTaskDescription reccurent) {
        RecurrentTaskConfigurator executor = new RecurrentTaskConfigurator(this, 
        	reccurent.getCommand(), this.builders.get(reccurent.getCommand()),
        	this.getTaskType(reccurent.getCommand()), reccurent.getPeriod(), 
        	reccurent.getDelay(), reccurent.getTimeout(), 
        	reccurent.getConfiguration());
        this.recurrences.add(executor);
    }

    @Override
    public void connect(ExtModelConfiguration manager) throws InvalidProtocolStackException {
        super.connect(manager);

        Iterator<RecurrentHttpTaskConfigurator> iterator = this.recurrences.iterator();

        while (iterator.hasNext()) {
            final RecurrentHttpTaskConfigurator executable = iterator.next();
            final AtomicReference<ScheduledFuture<?>>  ref = new AtomicReference<>();
            ScheduledFuture<?> future = worker.scheduleWithFixedDelay( new Runnable() {
                private long timeout = 0;
                private String taskId;
                
                @Override
                public void run() {                	
                	if(this.taskId == null)
                		this.taskId = String.format("task_%s",this.hashCode());
                	
                	if(SimpleHttpProtocolStackEndpoint.this.recurrenceTasks.contains(this.taskId))
                		return; 
                	
                	SimpleHttpProtocolStackEndpoint.this.recurrenceTasks.add(this.taskId);         
                	
                    if (timeout == 0) 
                        timeout = executable.getTimeout() == -1 ? -1 : (System.currentTimeMillis() + executable.getTimeout());
                    
                    if (timeout > -1 && System.currentTimeMillis() > timeout) {
                    	SimpleHttpProtocolStackEndpoint.this.recurrenceTasks.remove(this.taskId);
                    	ScheduledFuture<?> future = ref.get();
                    	if(future != null)
                    		future.cancel(true);
                        return;
                    }
                    HttpTask<?, ?> task = ReflectUtils.getInstance(executable.getTaskType(), new Object[]{executable.handled(), SimpleHttpProtocolStackEndpoint.this, SimpleHttpRequest.class, UriUtils.ROOT, 
                    		null, null, null});
                    try {
                        if (ChainedHttpTaskConfigurator.class.isAssignableFrom(executable.getClass()))
                            executable.configure(task);
                       else {
                            HttpTaskProcessingContext context = SimpleHttpProtocolStackEndpoint.this.createContext(executable, task);
                            if (context != null) 
                                ((HttpMediator) mediator).registerProcessingContext(task, context);                            
                        }
                        task.execute();
                    } catch (Exception e) {
                    	LOG.error(e.getMessage(), e);
                    }finally {
                    	SimpleHttpProtocolStackEndpoint.this.recurrenceTasks.remove(this.taskId);
                    }
                }
            }, executable.getDelay(), executable.getPeriod(), TimeUnit.MILLISECONDS);
            ref.set(future);
        }
    }

    /**
     * @return
     */
    public HttpMediator getMediator() {
        return (HttpMediator) mediator;
    }

    /**
     * Defines the string identifier of this SimpleHttpProtocolStackEndpoint
     *
     * @param endpointId
     */
    public void setEndpointIdentifier(String endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public void send(Task task) {
    	HttpTask<?,?> _task =  (HttpTask<?,?>)task;        
        try {
            ((HttpMediator) mediator).configure(_task);
            super.send(_task);
        } catch (Exception e) {
            SimpleHttpProtocolStackEndpoint.LOG.error(e.getMessage(), e);
        } finally {
            ((HttpMediator) mediator).unregisterProcessingContext(_task);
        }
    }

    @Override
    public Task createTask(CommandType command, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        HttpTaskConfigurator configuration = this.adapters.get(command);
        if (configuration == null) {
            return null;
        }
        HttpTask<?, ?> task = super.wrap(HttpTask.class, ReflectUtils.getInstance(this.getTaskType(command), 
        	new Object[]{command, this, SimpleHttpRequest.class, path, profileId, 
        		resourceConfig, parameters}));
        try {
            if (task.getPacketType() == null) 
                task.setPacketType(packetType);            
            if (ChainedHttpTaskConfigurator.class.isAssignableFrom(configuration.getClass()))
                configuration.configure(task);
            else {
                HttpTaskProcessingContext context = SimpleHttpProtocolStackEndpoint.this.createContext(configuration, task);
                if (context != null)
                    ((HttpMediator) mediator).registerProcessingContext(task, context);
            }
            return task;
        } catch (Exception e) {
        	SimpleHttpProtocolStackEndpoint.LOG.error(e.getMessage(), e);
            ((HttpMediator) mediator).unregisterProcessingContext(task);
        }
        return null;
    }

    /**
     * Build the task processing context, to be used to resolve configuration variables
     *
     * @param task the task for which to build the
     *             processing context
     */
    protected HttpTaskProcessingContext createContext(HttpTaskConfigurator httpTaskConfigurator, HttpTask<?, ?> task) {
        HttpTaskProcessingContextFactory factory = null;
        if ((factory = ((HttpMediator) mediator).getTaskProcessingContextFactory()) != null)
            return factory.newInstance(httpTaskConfigurator, this.endpointId, task);
        return null;
    }

    /**
     * Build the task processing context, to be used to resolve configuration variables
     *
     * @param task the task for which to build the
     *             processing context
     */
    protected <CHAINED extends HttpChainedTask<?>> HttpTaskProcessingContext createChainedContext(HttpTaskConfigurator httpTaskConfigurator, HttpChainedTasks<?, CHAINED> tasks, CHAINED task) {
        HttpChainedTaskProcessingContextFactory factory = null;

        if ((factory = ((HttpMediator) this.mediator).getChainedTaskProcessingContextFactory()) != null) {
            return factory.newInstance(httpTaskConfigurator, this.endpointId, tasks, task);
        }
        return null;
    }

    /**
     * Defines the extended {@link Task.Get} type to be used when
     * instantiating a new GET task
     *
     * @param getTaskClass the extended {@link Task.Get} type to be used
     */
    public void setGetTaskType(Class<? extends HttpTask> getTaskClass) {
        this.getTaskClass = getTaskClass;
    }

    /**
     * Returns the extended {@link Task.Get} type to be used when
     * instantiating a new GET task
     *
     * @return the extended {@link Task.Get} type to be used
     */
    public Class<? extends HttpTask> getGetTaskType() {
        if (this.getTaskClass == null) {
            return GET_TASK;
        }
        return this.getTaskClass;
    }

    /**
     * Defines the extended {@link Task.Set} type to be used when
     * instantiating a new SET task
     *
     * @param setTaskClass the extended {@link Task.Set} type to be used
     */
    public void setSetTaskType(Class<? extends HttpTask> setTaskClass) {
        this.setTaskClass = setTaskClass;
    }

    /**
     * Returns the extended {@link Task.Get} type to be used when
     * instantiating a new GET task
     *
     * @return the extended {@link Task.Get} type to be used
     */
    public Class<? extends HttpTask> getSetTaskType() {
        if (this.setTaskClass == null) {
            return SET_TASK;
        }
        return this.setTaskClass;
    }

    /**
     * Defines the extended {@link Task.Act} type to be used when
     * instantiating a new ACT task
     *
     * @param actTaskClass the extended {@link Task.Act} type to be used
     */
    public void setActTaskType(Class<? extends HttpTask> actTaskClass) {
        this.actTaskClass = actTaskClass;
    }

    /**
     * Returns the extended {@link Task.Act} type to be used when
     * instantiating a new ACT task
     *
     * @return the extended {@link Task.Act} type to be used
     */
    public Class<? extends HttpTask> getActTaskType() {
        if (this.actTaskClass == null) {
            return ACT_TASK;
        }
        return this.actTaskClass;
    }

    /**
     * Defines the extended {@link Task.Subscribe} type to be used when
     * instantiating a new SUBSCRIBE task
     *
     * @param subscribeTaskClass the extended {@link Task.Subscribe} type to be used
     */
    public void setSubscribeTaskType(Class<? extends HttpTask> subscribeTaskClass) {
        this.subscribeTaskClass = subscribeTaskClass;
    }

    /**
     * Returns the extended {@link Task.Subscribe} type to be used when
     * instantiating a new SUBSCRIBE task
     *
     * @return the extended {@link Task.Subscribe} type to be used
     */
    public Class<? extends HttpTask> getSubscribeTaskType() {
        if (this.subscribeTaskClass == null) {
            return SUBSCRIBE_TASK;
        }
        return this.subscribeTaskClass;
    }

    /**
     * Defines the extended {@link Task.Unsubscribe} type to be used when
     * instantiating a new UNSUBSCRIBE task
     *
     * @param unsubscribeTaskClass the extended {@link Task.Unsubscribe} type to be used
     */
    public void setUnsubscribeTaskType(Class<? extends HttpTask> unsubscribeTaskClass) {
        this.unsubscribeTaskClass = unsubscribeTaskClass;
    }

    /**
     * Returns the extended {@link Task.Unsubscribe} type to be used when
     * instantiating a new UNSUBSCRIBE task
     *
     * @return the extended {@link Task.Unsubscribe} type to be used
     */
    public Class<? extends HttpTask> getUnsubscribeTaskType() {
        if (this.unsubscribeTaskClass == null) {
            return UNSUBSCRIBE_TASK;
        }
        return this.unsubscribeTaskClass;
    }

    /**
     * Defines the extended {@link Task.ServicesEnumeration} type to be used when
     * instantiating a new SERVICES_ENUMERATION task
     *
     * @param servicesEnumerationTaskClass the extended {@link
     *                                     Task.ServicesEnumeration} type to be used
     */
    public void setServicesEnumerationTaskType(Class<? extends HttpTask> servicesEnumerationTaskClass) {
        this.servicesEnumerationTaskClass = servicesEnumerationTaskClass;
    }

    /**
     * Returns the extended {@link Task.ServicesEnumeration} type to be used when
     * instantiating a new SERVICES_ENUMERATION task
     *
     * @return the extended {@link Task.ServicesEnumeration} type to be used
     */
    public Class<? extends HttpTask> getServicesEnumerationTaskType() {
        if (this.servicesEnumerationTaskClass == null) {
            return SERVICES_ENUMERATION_TASK;
        }
        return this.servicesEnumerationTaskClass;
    }

    /**
     * @param command
     * @return
     */
    protected Class<? extends HttpTask> getTaskType(CommandType command) {
        switch (command) {
            case ACT:
                return this.getActTaskType();
            case GET:
                return this.getGetTaskType();
            case SERVICES_ENUMERATION:
                return this.getServicesEnumerationTaskType();
            case SET:
                return this.getSetTaskType();
            case SUBSCRIBE:
                return this.getSubscribeTaskType();
            case UNSUBSCRIBE:
                return this.getUnsubscribeTaskType();
            default:
                break;
        }
        return HttpTask.class;
    }
    
    @Override
    public void stop() {
        worker.shutdown();
        try {
			worker.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
        worker.shutdownNow();
        try {
        	worker.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
        super.stop();
    }
}
