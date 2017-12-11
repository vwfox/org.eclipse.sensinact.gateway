/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core;


import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link RemoteCore} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RemoteSensiNact implements RemoteCore
{	
	//********************************************************************//
	//						NESTED DECLARATIONS		    				  //
	//********************************************************************//
		
	/**
	 *
	 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
	 */
	final class RemoteSensiNactCallback extends AbstractMidAgentCallback
	{	
		RemoteSensiNactCallback(String agentId)
		{
			super();
			super.setIdentifier(agentId);
		}
		
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
		 * doHandle(org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl)
		 */
		@Override
		public void doHandle(SnaLifecycleMessageImpl message)
		{
			RemoteSensiNact.this.endpoint().dispatch(super.identifier, message);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
		 * doHandle(org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl)
		 */
		@Override
		public void doHandle(SnaUpdateMessageImpl message)
		{
			RemoteSensiNact.this.endpoint().dispatch(super.identifier, message);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
		 * doHandle(org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl)
		 */
		@Override
		public void doHandle(SnaErrorMessageImpl message)
		{
			RemoteSensiNact.this.endpoint().dispatch(super.identifier, message);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
		 * doHandle(org.eclipse.sensinact.gateway.core.message.SnaResponseMessage)
		 */
		@Override
		public void doHandle(SnaResponseMessage<?> message)
		{
			RemoteSensiNact.this.endpoint().dispatch(super.identifier, message);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
		 * stop()
		 */
		@Override
		public void stop()
		{
			RemoteSensiNact.this.mediator.debug(
				"RemoteSensiNactCallback '%s' stopped", super.identifier);
		}
	}	
	
	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
	
	//********************************************************************//
	//						STATIC DECLARATIONS		      				  //
	//********************************************************************//

	public static final String NAMESPACE_PROP = "namespace";
	
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
	protected Mediator mediator;
	protected LocalEndpoint localEndpoint;
	protected RemoteEndpoint remoteEndpoint;

	protected Map<String, String> localAgents;	
	protected ServiceRegistration<RemoteCore> registration;


	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing to interact with
	 * the OSGi host environment
	 * @param remoteEndpoint the {@link RemoteEndpoint} the {@link 
	 * RemoteCore} to be instantiated will be attached to, in 
	 * manner of communicating with a remote instance of sensiNact
	 * @param localEndpoint the {@link LocalEndpoint} the {@link 
	 * RemoteCore} to be instantiated will be attached to in 
	 * manner of communicating with the local instance of sensiNact
	 */
	protected RemoteSensiNact(
		Mediator mediator,
		AbstractRemoteEndpoint remoteEndpoint, 
		LocalEndpoint localEndpoint)
	{
		this.mediator = mediator;
		this.localAgents = new HashMap<String,String>();
		
		this.remoteEndpoint = remoteEndpoint;
		if(this.remoteEndpoint == null)
		{
			throw new NullPointerException("A remote endpoint is needed");
		}
		this.localEndpoint = localEndpoint;
	}

	/**
	 * Connects the {@link RemoteEndpoint} of this {@link RemoteCore}
	 * @return
	 * <ul>
	 * 		<li>true if the {@link RemoteEndpoint} of this {@link RemoteCore}
	 * 			connected properly
	 * 		</li>
	 * 		<li>false otherwise</li>	
	 * </ul>
	 */
	boolean connect() 
	{
		return this.remoteEndpoint.connect(this);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#open(java.lang.String)
	 */
	@Override
	public void open(final String namespace)
	{
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{		
				Dictionary<String,Object> props = new Hashtable<String,Object>();
		    	props.put(NAMESPACE_PROP, namespace);		    	
				RemoteSensiNact.this.registration = mediator.getContext(
					).registerService(RemoteCore.class, RemoteSensiNact.this, 
						   props);
				mediator.debug("RemoteCore '%s' registration done", namespace);
				return null;
			}
		});
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#close()
	 */
	@Override
	public void close()
	{
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				if(RemoteSensiNact.this.registration != null)
				{
					try
					{
						RemoteSensiNact.this.registration.unregister();
						
					} catch(IllegalStateException e)
					{
						RemoteSensiNact.this.mediator.error(e);
					}
				}
				return null;
			}
		});
		this.localAgents.clear();

		this.localEndpoint.close();
		this.localEndpoint = null;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#endpoint()
	 */
	@Override
	public RemoteEndpoint endpoint()
	{
		return this.remoteEndpoint;
	}
	
  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getLocations(java.lang.String)
  	 */
	@Override
  	public JSONObject getLocations(String publicKey)
  	{
  		return this.localEndpoint.getLocations(publicKey);
  	}

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getAll(java.lang.String)
  	 */
	@Override
  	public JSONObject getAll(String publicKey)
  	{
  		return this.getAll(publicKey, null);
  	}
  	
  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * getAll(java.lang.String, java.lang.String)
  	 */
	@Override
  	public JSONObject getAll(String publicKey, String filter)
  	{
  		return this.localEndpoint.getAll(publicKey, filter);
  	}
  	
  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getProviders(java.lang.String)
  	 */
	@Override
  	public JSONObject getProviders(String publicKey)
    {
  		return this.localEndpoint.getProviders(publicKey);
    }

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * getProvider(java.lang.String, java.lang.String)
  	 */
	@Override
  	public JSONObject getProvider(String publicKey, String serviceProviderId)
    {
  		return this.localEndpoint.getProvider(publicKey, serviceProviderId);
    }

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * getServices(java.lang.String, java.lang.String)
  	 */
	@Override
  	public JSONObject getServices(String publicKey, String serviceProviderId)
    {
  		return this.localEndpoint.getServices(publicKey, serviceProviderId);
    }

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * getService(java.lang.String, java.lang.String, java.lang.String)
  	 */
	@Override
  	public JSONObject getService(String publicKey, 
  			String serviceProviderId,String serviceId)
    {
  		return this.localEndpoint.getService(publicKey, 
  				serviceProviderId, serviceId);
    }

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * getResources(java.lang.String, java.lang.String, java.lang.String)
  	 */
	@Override
  	public JSONObject getResources(String publicKey, 
  			String serviceProviderId, String serviceId)
    {
  		return this.localEndpoint.getResources(publicKey, 
  				serviceProviderId, serviceId);
    }

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * getResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
  	 */
	@Override
  	public JSONObject getResource(String publicKey, 
  			String serviceProviderId, String serviceId, String resourceId)
    {
  		return this.localEndpoint.getResource(publicKey, serviceProviderId, 
  				serviceId, resourceId);
    }
  	
  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * get(java.lang.String, java.lang.String, java.lang.String, java.lang.String, 
  	 * java.lang.String)
  	 */
	@Override
  	public JSONObject get(String publicKey, String serviceProviderId, 
    		String serviceId, String resourceId, 
    		String attributeId)
    {
  		return this.localEndpoint.get(publicKey, serviceProviderId, serviceId,
  				resourceId, attributeId);
    }

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * set(java.lang.String, java.lang.String, java.lang.String, java.lang.String, 
  	 * java.lang.String, java.lang.Object)
  	 */
	@Override
  	public JSONObject set(String publicKey, String serviceProviderId,
           String serviceId, String resourceId, 
           String attributeId, Object parameter)
    {
  		return this.localEndpoint.set(publicKey, serviceProviderId, serviceId,
  				resourceId, attributeId, parameter);
    }

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * act(java.lang.String, java.lang.String, java.lang.String, java.lang.String, 
  	 * java.lang.Object[])
  	 */
	@Override
  	public JSONObject act(String publicKey, String serviceProviderId,
            String serviceId, String resourceId, 
            Object[] parameters )
    {
  		return this.localEndpoint.act(publicKey, serviceProviderId, serviceId,
  				resourceId, parameters);
    }

  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#
  	 * subscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String,
  	 *  org.json.JSONArray)
  	 */
	@Override
  	public JSONObject subscribe(String publicKey, String serviceProviderId,
            String serviceId, String resourceId, JSONArray conditions)
    {  	
  		return this.subscribe(publicKey, serviceProviderId, serviceId,
  				resourceId, this.remoteEndpoint, conditions);  
    }

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * subscribe(java.lang.String, java.lang.String, java.lang.String, 
	 * java.lang.String, org.eclipse.sensinact.gateway.core.message.Recipient, 
	 * org.json.JSONArray)
	 */
	@Override
	public JSONObject subscribe(String publicKey, String serviceProviderId,
	        String serviceId, String resourceId, Recipient recipient,
	        JSONArray conditions)
	{
  		return this.localEndpoint.subscribe(publicKey, serviceProviderId, serviceId,
  				resourceId, recipient, conditions);  
	}
	
  	/**
  	 * @inheritDoc
  	 *
  	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
  	 * unsubscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String, 
  	 * java.lang.String)
  	 */
	@Override
  	public JSONObject unsubscribe(String publicKey, String serviceProviderId,
            String serviceId, String resourceId, 
           String subscriptionId )
    {
  		return this.localEndpoint.unsubscribe(publicKey, 
  			serviceProviderId, serviceId, resourceId, subscriptionId);   
    }

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#namespace()
	 */
	@Override
	public String namespace() 
	{
		ServiceReference<Core> ref = mediator.getContext(
				).getServiceReference(Core.class);
		
		if(ref!=null )
		{
			Core core = mediator.getContext().getService(ref);
			return core.namespace();
		}
		return null;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#localID()
	 */
	@Override
	public int localID() 
	{
		return this.localEndpoint.localID();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#
	 * registerAgent(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaFilter,
	 *  java.lang.String)
	 */
	@Override
	public void registerAgent(final String remoteAgentId, 
			final SnaFilter filter, final String publicKey)
	{
		JSONObject registration = this.localEndpoint.getSession(publicKey
			).registerSessionAgent(new RemoteSensiNactCallback(
				remoteAgentId), filter);
		JSONObject response = registration.optJSONObject("response");
		String localAgentId = null;
		if(!JSONObject.NULL.equals(response) 
		&& (localAgentId=(String) response.opt("subscriptionId"))!=null)
		{
			this.localAgents.put(remoteAgentId, localAgentId);
		}
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#
	 * unregisterAgent(java.lang.String)
	 */
	@Override
	public void unregisterAgent(String remoteAgentId)
	{
		String localAgentId = this.localAgents.remove(remoteAgentId);
		if(localAgentId == null)
		{
			return;
		}
		this.callAgent(localAgentId, false, new Executable<SnaAgent,Void>()
		{
			@Override
			public Void execute(SnaAgent agent) throws Exception
			{
				if(agent != null)
				{
					agent.stop();
				}
				return null;
			}	
		});
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#
	 * dispatch(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaMessage)
	 */
	@Override
	public void dispatch(String agentId, final SnaMessage<?> message)
	{
		this.callAgent(agentId, true, new Executable<SnaAgent,Void>()
		{
			@Override
			public Void execute(SnaAgent agent) throws Exception
			{
				if(agent != null)
				{
					agent.register(message);
				}
				return null;
			}	
		});
	} 

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.RemoteCore#
	 * closeSession(java.lang.String)
	 */
	@Override
	public void closeSession(String publicKey)
	{
		this.localEndpoint.closeSession(publicKey);
	}
	
	/**
	 * @param agentId
	 * @param local
	 * @param executable
	 */
	private void callAgent(final String agentId, final boolean local,
			final Executable<SnaAgent,Void> executable)
	{
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				return RemoteSensiNact.this.mediator.callService(
					SnaAgent.class, new StringBuilder(
					).append("(&(org.eclipse.sensinact.gateway.agent.id="
					).append(agentId
					).append(")(org.eclipse.sensinact.gateway.agent.local="
					).append(Boolean.toString(local)
					).append("))").toString(), executable);
			}
		});
	}
}
