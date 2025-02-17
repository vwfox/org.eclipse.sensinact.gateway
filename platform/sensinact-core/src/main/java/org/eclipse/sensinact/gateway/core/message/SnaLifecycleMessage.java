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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;

/**
 * Lifecycle dedicated {@link SnaMessage}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaLifecycleMessage extends SnaNotificationMessage<SnaLifecycleMessage.Lifecycle> {
	public static final SnaMessage.Type TYPE = SnaMessage.Type.LIFECYCLE;

	public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[] {
			new TypedKey<String>(SnaConstants.ADDED_OR_REMOVED, String.class, false) };


	enum Lifecycle implements SnaMessageSubType, KeysCollection {
		PROVIDER_APPEARING, PROVIDER_DISAPPEARING, SERVICE_APPEARING, SERVICE_DISAPPEARING, RESOURCE_APPEARING, RESOURCE_DISAPPEARING;

		final Set<TypedKey<?>> keys;

		Lifecycle() {
			List<TypedKey<?>> list = Arrays.asList(new SnaMessage.KeysBuilder(SnaLifecycleMessage.class).keys());

			Set<TypedKey<?>> tmpKeys = new HashSet<TypedKey<?>>();
			tmpKeys.addAll(list);
			keys = Collections.unmodifiableSet(tmpKeys);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see SnaMessageSubType#getSnaMessageType()
		 */
		@Override
		public SnaMessage.Type getSnaMessageType() {
			return SnaLifecycleMessage.TYPE;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see KeysCollection#keys()
		 */
		@Override
		public Set<TypedKey<?>> keys() {
			return this.keys;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see KeysCollection#key(java.lang.String)
		 */
		@Override
		public TypedKey<?> key(String key) {
			TypedKey<?> typedKey = null;

			Iterator<TypedKey<?>> iterator = this.keys.iterator();
			while (iterator.hasNext()) {
				typedKey = iterator.next();
				if (typedKey.equals(key)) {
					break;
				}
				typedKey = null;
			}
			return typedKey;
		}
	}

}
