/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedProperties;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessage.Update;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Abstract implementation of an {@link AbstractSnaMessage}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSnaMessage<S extends Enum<S> & KeysCollection & SnaMessageSubType>
		extends TypedProperties<S> implements SnaMessage<S> {
	
	public static SnaMessage<?> fromJSON(final Mediator mediator, String json) {
		final JsonObject jsonMessage = JsonProviderFactory.readObject(json);
		final String typeStr = jsonMessage.getString("type", null);
		final String uri = jsonMessage.getString("uri");
		if (typeStr == null) {
			return null;
		}
		SnaMessage<?> message = null;
		
		switch(typeStr) {
			case "PROVIDER_APPEARING":
			case "PROVIDER_DISAPPEARING":
			case "SERVICE_APPEARING":
			case "SERVICE_DISAPPEARING":
			case "RESOURCE_APPEARING":
			case "RESOURCE_DISAPPEARING":
				Lifecycle l = Lifecycle.valueOf(typeStr);
				message = new SnaLifecycleMessageImpl(uri, l);
				break;
			case "ATTRIBUTE_VALUE_UPDATED":
			case "METADATA_VALUE_UPDATED":
			case "ACTUATED":
				Update u = Update.valueOf(typeStr);
				message = new SnaUpdateMessageImpl(uri, u);
				break;
			case "CONNECTED":
			case "DISCONNECTED":
				SnaRemoteMessage.Remote r = SnaRemoteMessage.Remote.valueOf(typeStr);
				message = new SnaRemoteMessageImpl(uri, r);
				break;
			case "NO_ERROR" :
			case "UPDATE_ERROR": 
			case "RESPONSE_ERROR":
			case "LIFECYCLE_ERROR": 
			case "SYSTEM_ERROR":
				SnaErrorMessage.Error e = SnaErrorMessage.Error.valueOf(typeStr);
				message = new SnaErrorMessageImpl(uri, e);
			default:
				break;
		}
		if (message != null) {
			List<String> names = Arrays.asList("type", "uri");
			for (Entry<String, JsonValue> e : jsonMessage.entrySet()) {
				String name = e.getKey();
				if(!names.contains(name)) {
					((TypedProperties<?>) message).put(name,e.getValue());
				}
			}
		}
		return message;
	}

	/**
	 * Constructor
	 * 
	 * @param uri
	 * @param type
	 */
	protected AbstractSnaMessage(String uri, S type) {
		super(type);
		super.putValue(SnaConstants.URI_KEY, uri);

	}

	/**
	 * @inheritDoc
	 *
	 * @see PathElement#getPath()
	 */
	public String getPath() {
		return super.<String>get(SnaConstants.URI_KEY);
	}

	/**
	 * Returns the {@link SnaMessage.Type} to which this extended
	 * {@link SnaMessage}'s type belongs to
	 */
	public SnaMessage.Type getSnaMessageType() {
		S type = super.getType();
		return type.getSnaMessageType();
	}

}
