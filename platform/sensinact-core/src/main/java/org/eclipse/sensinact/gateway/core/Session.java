/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import java.util.Set;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.filtering.FilteringCollection;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.ResourceIntent;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.method.ActResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeResponse;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * A Session allows to invoke access method on resources, and to access to
 * available service providers, services, and/or resources
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Session {
	/**
	 * Returns the String identifier of this Session
	 * 
	 * @return this Session's String identifier
	 */
	String getSessionId();

	/**
	 * Returns the set of {@link ServiceProvider}s accessible for this Session
	 * 
	 * @return the set of accessible {@link ServiceProvider}s
	 */
	Set<ServiceProvider> serviceProviders();

	/**
	 * Returns the set of {@link ServiceProvider}s compliant to the String LDAP
	 * formated filter passed as parameter
	 * 
	 * @param filter
	 *            the String LDAP formated filter
	 * 
	 * @return the set of accessible {@link ServiceProvider}s according to the
	 *         specified LDAP filter
	 */
	Set<ServiceProvider> serviceProviders(String filter);

	/**
	 * Returns the {@link ServiceProvider} whose String identifier is passed as
	 * parameter
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider
	 * 
	 * @return the {@link ServiceProvider}
	 */
	ServiceProvider serviceProvider(String serviceProviderName);

	/**
	 * Returns the {@link Service} whose String identifier is passed as parameter,
	 * held by the specified service provider
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param servideId
	 *            the String identifier of the service
	 * 
	 * @return the {@link Service}
	 */
	Service service(String serviceProviderName, String serviceName);

	/**
	 * Returns the {@link Resource} whose String identifier is passed as parameter,
	 * held by the specified service provider and service
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource
	 * @param servideId
	 *            the String identifier of the service providing the resource
	 * @param resourceId
	 *            the String identifier of the resource
	 * 
	 * @return the {@link Resource}
	 */
	Resource resource(String serviceProviderName, String serviceName, String resourceName);

	/**
	 * Returns true if the {@link Resource} with the specified name, that belongs to the specified service, 
	 * itself held by the specified service provider is accessible by this Session, because it exists 
	 * and the owner of this Session as enough access rights; otherwise returns false  
	 * 
	 * @param serviceProviderId the String identifier of the service provider holding the service
	 * providing the resource
	 * @param servideId the String identifier of the service providing the resource
	 * @param resourceId the String identifier of the resource
	 * 
	 * @return true if the {@link Resource} whose String identifier is passed as parameter,
	 * provided by the specified service, itself held by the specified service provider, is 
	 * accessible by this Session  
	 */
	boolean isAccessible(String serviceProviderName, String serviceName, String resourceName);
	
	/**
	 * Invokes the GET access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param attributeId
	 *            the String identifier of the resource's attribute targeted by the
	 *            access method call
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the GET access method invocation,
	 *         wrapped into a {@link GetResponse}
	 */
	GetResponse get(String serviceProviderId, String serviceId, String resourceId, String attributeId, 
			Object...args);

	/**
	 * Invokes the GET access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param attributeId
	 *            the String identifier of the resource's attribute targeted by the
	 *            access method call
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the GET access method invocation,
	 *         tagged by the specified request identifier, and wrapped into a
	 *         {@link GetResponse}
	 */
	GetResponse get(String requestId, String serviceProviderId, String serviceId, String resourceId,
			String attributeId, Object...args);

	/**
	 * Invokes the SET access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param attributeId
	 *            the String identifier of the resource's attribute targeted by the
	 *            access method call
	 * @param parameter
	 *            the value object to be set
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the SET access method invocation,
	 *         wrapped into a {@link SetResponse}
	 */
	SetResponse set(String serviceProviderId, String serviceId, String resourceId, String attributeId,
			Object parameter, Object...args);

	/**
	 * Invokes the SET access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param attributeId
	 *            the String identifier of the resource's attribute targeted by the
	 *            access method call
	 * @param parameter
	 *            the value object to be set
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the SET access method invocation,
	 *         tagged by the specified request identifier and , wrapped into a
	 *         {@link SetResponse}
	 */
	SetResponse set(String requestId, String serviceProviderId, String serviceId, String resourceId,
			String attributeId, Object parameter, Object...args);

	/**
	 * Invokes the ACT access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param parameters
	 *            the Objects array parameterizing the call
	 * 
	 * @return the JSON formated response of the ACT access method invocation,
	 *         wrapped into a {@link ActResponse}
	 */
	ActResponse act(String serviceProviderId, String serviceId, String resourceId, Object[] parameters);

	/**
	 * Invokes the ACT access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param parameters
	 *            the Objects array parameterizing the call
	 * 
	 * @return the JSON formated response of the ACT access method invocation,
	 *         tagged by the specified request identifier, and wrapped into a
	 *         {@link ActResponse}
	 */
	ActResponse act(String requestId, String serviceProviderId, String serviceId, String resourceId,
			Object[] parameters);

	/**
	 * Invokes the SUBSCRIBE access method on the resource whose String identifier
	 * is passed as parameter, held by the specified service provider and service
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param recipient
	 *            the {@link Recipient} to which the update events generated by the
	 *            subscription will be transmitted
	 * @param conditions
	 *            the JSON formated set of constraints applying on the subscription
	 *            to be created
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the SUBSCRIBE access method invocation,
	 *         wrapped into a {@link SubscribeResponse}
	 */
	SubscribeResponse subscribe(String serviceProviderId, String serviceId, String resourceId, 
			Recipient recipient, JsonArray conditions, Object...args);

	/**
	 * Invokes the SUBSCRIBE access method on the resource whose String identifier
	 * is passed as parameter, held by the specified service provider and service
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param recipient
	 *            the {@link Recipient} to which the update events generated by the
	 *            subscription will be transmitted
	 * @param conditions
	 *            the JSON formated set of constraints applying on the subscription
	 *            to be created
	 * @param policy
	 *            the policy applying on the ErrorHandler of the callback of the 
	 *            subscription to be created
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the SUBSCRIBE access method invocation,
	 *         wrapped into a {@link SubscribeResponse}
	 */
	SubscribeResponse subscribe(String serviceProviderId, String serviceId, String resourceId, 
		Recipient recipient, JsonArray conditions, String policy, Object...args);

	/**
	 * Invokes the SUBSCRIBE access method on the resource whose String identifier
	 * is passed as parameter, held by the specified service provider and service
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param recipient
	 *            the {@link Recipient} to which the update events generated by the
	 *            subscription will be transmitted
	 * @param conditions
	 *            the JSON formated set of constraints applying on the subscription
	 *            to be created
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the SUBSCRIBE access method invocation,
	 *         tagged by the specified request identifier, and wrapped into a
	 *         {@link SubscribeResponse}
	 */
	SubscribeResponse subscribe(String requestId, String serviceProviderId, String serviceId, 
		String resourceId, Recipient recipient, JsonArray conditions, Object...args);

	/**
	 * Invokes the SUBSCRIBE access method on the resource whose String identifier
	 * is passed as parameter, held by the specified service provider and service
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param recipient
	 *            the {@link Recipient} to which the update events generated by the
	 *            subscription will be transmitted
	 * @param conditions
	 *            the JSON formated set of constraints applying on the subscription
	 *            to be created
	 * @param policy
	 *            the policy applying on the ErrorHandler of the callback of the subscription
	 *            to be created
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the SUBSCRIBE access method invocation,
	 *         tagged by the specified request identifier, and wrapped into a
	 *         {@link SubscribeResponse}
	 */
	SubscribeResponse subscribe(String requestId, String serviceProviderId, String serviceId, String resourceId,
			Recipient recipient, JsonArray conditions, String policy, Object...args);

	/**
	 * Invokes the UNSUBSCRIBE access method on the resource whose String identifier
	 * is passed as parameter, held by the specified service provider and service
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param subscriptionId
	 *            the String identifier of the subscription to be deleted
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the UNSUBSCRIBE access method
	 *         invocation, wrapped into a {@link UnsubscribeResponse}
	 */
	UnsubscribeResponse unsubscribe(String serviceProviderId, String serviceId, String resourceId,
			String subscriptionId, Object...args);

	/**
	 * Invokes the UNSUBSCRIBE access method on the resource whose String identifier
	 * is passed as parameter, held by the specified service provider and service
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param subscriptionId
	 *            the String identifier of the subscription to be deleted
	 * @param args optional extra arguments Object array parameterizing the call
	 * 
	 * @return the JSON formated response of the UNSUBSCRIBE access method
	 *         invocation, tagged by the specified request identifier, and wrapped
	 *         into a {@link UnsubscribeResponse}
	 */
	UnsubscribeResponse unsubscribe(String requestId, String serviceProviderId, String serviceId, 
		String resourceId, String subscriptionId, Object...args);

	/**
	 * Creates and registers an {@link SnaAgent} attached to this Session and that
	 * will be unregistered when this last one will disappear
	 * 
	 * @param callback
	 *            the {@link MidAgentCallback} of the {@link SnaAgent} to be created
	 * @param filter
	 *            the {@link SnaFilter} of the {@link SnaAgent} to be created
	 * 
	 * @return the JSON formated result of the {@link SnaAgent} registration,
	 *         including its String identifier, wrapped into a
	 *         {@link SubscribeResponse}
	 */
	SubscribeResponse registerSessionAgent(MidAgentCallback callback, SnaFilter filter);

	/**
	 * Creates and registers an {@link SnaAgent} attached to this Session and that
	 * will be unregistered when this last one will disappear
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param callback
	 *            the {@link MidAgentCallback} of the {@link SnaAgent} to be created
	 * @param filter
	 *            the {@link SnaFilter} of the {@link SnaAgent} to be created
	 * 
	 * @return the JSON formated result of the {@link SnaAgent} registration,
	 *         including its String identifier, tagged by the specified request
	 *         identifier, wrapped into a {@link SubscribeResponse}
	 */
	SubscribeResponse registerSessionAgent(String requestId, final MidAgentCallback callback, final SnaFilter filter);

	/**
	 * Unregisters the {@link SnaAgent} attached to this Session and whose String
	 * identifier is passed as parameter
	 * 
	 * @param agentId
	 *            the String identifier of the linked {@link SnaAgent} to be
	 *            unregistered
	 * 
	 * @return the JSON formated result of the {@link SnaAgent} unregistration,
	 *         wrapped into a {@link UnsubscribeResponse}
	 */
	UnsubscribeResponse unregisterSessionAgent(String agentId);

	/**
	 * Unregisters the {@link SnaAgent} attached to this Session and whose String
	 * identifier is passed as parameter
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param agentId
	 *            the String identifier of the linked {@link SnaAgent} to be
	 *            unregistered
	 * 
	 * @return the JSON formated result of the {@link SnaAgent} unregistration,
	 *         tagged by the specified request identifier, and wrapped into a
	 *         {@link UnsubscribeResponse}.
	 */
	UnsubscribeResponse unregisterSessionAgent(String requestId, String agentId);

	/**
	 * Registers a new {@link ResourceIntent} attached to this Session targeting the 
	 * {@link Resource} whose String path is passed as parameter and executing the 
	 * {@link Executable} also passed as parameter the availability status of the target
	 * changes 
	 * 
	 * @param requestId the String identifier of the request, to be reported into the response
	 * @param callback the {@link Executable} to be executing when the availability status 
	 * of the specified target changes
	 * @param resourcePath the array of String paths of the targeted {@link Resource}s
	 * 
	 * @return the JSON formated result of the {@link ResourceIntent} registration,
	 * tagged by the specified request identifier, and wrapped into an {@link SubscribeResponse}.
	 */
	SubscribeResponse registerSessionIntent(String requestId, Executable<Boolean, Void> callback, String... resourcePath);
	
	/**
	 * Unregisters the {@link ResourceIntent} attached to this Session and whose String
	 * identifier is passed as parameter
	 * 
	 * @param requestId the String identifier of the request, to be reported into the response
	 * @param intentId the String identifier of the linked {@link ResourceIntent} to be
	 * unregistered
	 * 
	 * @return the JSON formated result of the {@link ResourceIntent} unregistration,
	 * tagged by the specified request identifier, and wrapped into an {@link UnsubscribeResponse}.
	 */
	UnsubscribeResponse unregisterSessionIntent(String requestId, String intentId);
	
	/**
	 * Registers a new {@link ResourceIntent} attached to this Session targeting the 
	 * {@link Resource} whose String path is passed as parameter and executing the 
	 * {@link Executable} also passed as parameter the availability status of the target
	 * changes 
	 * 
	 * @param callback the {@link Executable} to be executing when the availability status 
	 * of the specified target changes
	 * @param resourcePath the array of String paths of the targeted {@link Resource}s 
	 * 
	 * @return the JSON formated result of the {@link ResourceIntent} registration, 
	 * wrapped into an {@link SubscribeResponse}.
	 */
	SubscribeResponse registerSessionIntent(Executable<Boolean, Void> callback, String... resourcePath);
	
	/**
	 * Unregisters the {@link ResourceIntent} attached to this Session and whose String
	 * identifier is passed as parameter
	 * 
	 * @param requestId the String identifier of the request, and to be reported into 
	 * the response
	 * @param intentId the String identifier of the linked {@link ResourceIntent} to be
	 * unregistered
	 * 
	 * @return the JSON formated result of the {@link ResourceIntent} unregistration,
	 * wrapped into an {@link UnsubscribeResponse}.
	 */
	UnsubscribeResponse unregisterSessionIntent(String intentId);
	
	/**
	 * Returns the JSON formated list of all registered resource model instances,
	 * accessible to this Session, from the local sensiNact instance, as well as
	 * from the connected remote ones
	 * 
	 * @return the JSON formated list of the resource model instances for this
	 *         Session, and wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<String> getAll();

	/**
	 * Returns the JSON formated list of all registered resource model instances,
	 * accessible to this Session, from the local sensiNact instance, as well as
	 * from the connected remote ones
	 * 
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of the resource model instances
	 *         for this Session, and wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<String> getAll(FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated list of all registered resource model instances,
	 * accessible to this Session and compliant to the specified String LDAP
	 * formated filter, from the local sensiNact instance, as well as from the
	 * connected remote ones
	 * 
	 * @param filter
	 *            the String LDAP formated filter allowing to discriminate the
	 *            selected elements
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of the resource model instances
	 *         for this Session, compliant to the specified LDAP formated filter,
	 *         and wrapped into a {@link ResultHolder}.
	 */
	DescribeResponse<String> getAll(String filter, FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated list of all registered resource model instances,
	 * accessible to this Session and compliant to the specified String LDAP
	 * formated filter, from the local sensiNact instance, as well as from the
	 * connected remote ones
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param filter
	 *            the String LDAP formated filter allowing to discriminate the
	 *            selected elements
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of the resource model instances
	 *         for this Session and compliant to the specified LDAP formated filter,
	 *         tagged by the specified request identifier, and wrapped into a
	 *         {@link DescribeResponse}.
	 */
	DescribeResponse<String> getAll(String requestId, String filter, FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @return the JSON formated list of available service providers, wrapped into a
	 *         {@link DescribeResponse}
	 */
	DescribeResponse<String> getProviders();

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of available service providers,
	 *         wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<String> getProviders(FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of available service providers,
	 *         tagged by the specified request identifier, and wrapped into a
	 *         {@link DescribeResponse}
	 */
	DescribeResponse<String> getProviders(String requestId, FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated description of the service provider whose String
	 * identifier is passed as parameter
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider
	 * 
	 * @return the JSON formated description of the specified service provider,
	 *         wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<JsonObject> getProvider(String serviceProviderId);

	/**
	 * Returns the JSON formated description of the service provider whose String
	 * identifier is passed as parameter
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider
	 * 
	 * @return the JSON formated description of the specified service provider,
	 *         tagged by the specified request identifier, and wrapped into a
	 *         {@link DescribeResponse}
	 */
	DescribeResponse<JsonObject> getProvider(String requestId, String serviceProviderId);

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the services
	 * 
	 * @return the JSON formated list of available services for the specified
	 *         service providers, wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<String> getServices(String serviceProviderId);

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the services
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of available services for the
	 *         specified service providers, wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<String> getServices(String serviceProviderId, FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the services
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of available services for the
	 *         specified service providers, tagged by the specified request
	 *         identifier, and wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<String> getServices(String requestId, String serviceProviderId,
			FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated description of the service whose String identifier
	 * is passed as parameter, and held by the specified service provider
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param serviceId
	 *            the String identifier of the service to return the description of
	 * 
	 * @return the JSON formated description of the specified service, wrapped into
	 *         a {@link DescribeResponse}
	 */
	DescribeResponse<JsonObject> getService(String serviceProviderId, String serviceId);

	/**
	 * Returns the JSON formated description of the service whose String identifier
	 * is passed as parameter, and held by the specified service provider
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param serviceId
	 *            the String identifier of the service to return the description of
	 * 
	 * @return the JSON formated description of the specified service, tagged by the
	 *         specified request identifier, and wrapped into a
	 *         {@link DescribeResponse}
	 */
	DescribeResponse<JsonObject> getService(String requestId, String serviceProviderId, String serviceId);

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param serviceId
	 *            the String identifier of the service providing the resources
	 * 
	 * @return the JSON formated list of available resources for the specified
	 *         service provider and service, wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<String> getResources(String serviceProviderId, String serviceId);

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param serviceId
	 *            the String identifier of the service providing the resources
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of available resources for the
	 *         specified service provider and service, wrapped into a
	 *         {@link DescribeResponse}
	 */
	DescribeResponse<String> getResources(String serviceProviderId, String serviceId,
			FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated list of available service providers for the user
	 * whose public key is passed as parameter
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param serviceId
	 *            the String identifier of the service providing the resources
	 * @param filterCollection
	 *            the collection of the filters to be applied on the result of the
	 *            call
	 * 
	 * @return the JSON formated and filtered list of available resources for the
	 *         specified service provider and service, tagged by the specified
	 *         request identifier, and wrapped into a {@link DescribeResponse}
	 */
	DescribeResponse<String> getResources(String requestId, String serviceProviderId, String serviceId,
			FilteringCollection filterCollection);

	/**
	 * Returns the JSON formated description of the resource whose String identifier
	 * is passed as parameter, and held by the service provider and service whose
	 * String identifiers are also passed as parameter
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service,
	 *            providing the resource
	 * @param serviceId
	 *            the String identifier of the service providing the resource
	 * @param resourceId
	 *            the String identifier of the resource to return the description of
	 * 
	 * @return the JSON formated description of the specified resource, wrapped into
	 *         a {@link DescribeResponse}
	 */
	DescribeResponse<JsonObject> getResource(String serviceProviderId, String serviceId, String resourceId);

	/**
	 * Returns the JSON formated description of the resource whose String identifier
	 * is passed as parameter, and held by the service provider and service whose
	 * String identifiers are also passed as parameter
	 * 
	 * @param requestId
	 *            the String identifier of the request, and to be reported into the
	 *            response
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service,
	 *            providing the resource
	 * @param serviceId
	 *            the String identifier of the service providing the resource
	 * @param resourceId
	 *            the String identifier of the resource to return the description of
	 * 
	 * @return the JSON formated description of the specified resource, tagged by
	 *         the specified request identifier, and wrapped into a
	 *         {@link DescribeResponse}
	 */
	DescribeResponse<JsonObject> getResource(String requestId, String serviceProviderId, String serviceId,
			String resourceId);
}
