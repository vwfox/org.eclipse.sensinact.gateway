/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.ModelElement;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.eclipse.sensinact.gateway.util.tree.ImmutablePathNode;
import org.eclipse.sensinact.gateway.util.tree.PathNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessNodeImpl<N extends AccessNodeImpl<N>> extends PathNode<N> implements MutableAccessNode {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	private static final Logger LOG=LoggerFactory.getLogger(AccessNodeImpl.class);
	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	protected Map<AccessLevelOption, List<MethodAccessibility>> accesses;
	private AccessProfile profile;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param name
	 * @param isPattern
	 */
	public AccessNodeImpl(String name, boolean isPattern) {
		super(name, isPattern);
		this.accesses = new EnumMap<AccessLevelOption, List<MethodAccessibility>>(AccessLevelOption.class);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.AccessNode#getProfile()
	 */
	@Override
	public AccessProfile getProfile() {
		return this.profile;
	}

	/**
	 * Defines the Map of available {@link AccessMethod.Type} for all pre-defined
	 * {@link AccessLevelOption}s according to the one passed as parameter
	 * 
	 * @param option
	 *            the {@link AccessProfileOption} holding the {@link AccessProfile}
	 *            for which to build the Map of available {@link AccessMethod.Type}
	 *            for pre-defined {@link AccessLevelOption}s
	 */
	public void withAccessProfile(AccessProfileOption option) {
		withAccessProfile(option.getAccessProfile());
	}

	/**
	 * Defines the Map of available {@link AccessMethod.Type} for all pre-defined
	 * {@link AccessLevelOption}s according to the {@link AccessProfile} passed as
	 * parameter
	 * 
	 * @param profile
	 *            the {@link AccessProfile} for which to build the Map of available
	 *            {@link AccessMethod.Type} for pre-defined
	 *            {@link AccessLevelOption}s
	 */
	public void withAccessProfile(AccessProfile profile) {
		if (this.profile != null || profile == null) {
			return;
		}
		this.profile = profile;
		try {
			Set<MethodAccess> methodAccesses = profile.getMethodAccesses();
			if (methodAccesses.isEmpty()) {
				return;
			}
			int[] accessLevels = null;

			Iterator<AccessLevelOption> iterator = Arrays.asList(AccessLevelOption.values()).iterator();

			AccessMethod.Type[] types = AccessMethod.Type.values();

			accessLevels = new int[types.length];
			Iterator<MethodAccess> accessesIterator = methodAccesses.iterator();
			while (accessesIterator.hasNext()) {
				MethodAccess methodAccess = accessesIterator.next();
				int level = methodAccess.getAccessLevel().getLevel();
				int ind = methodAccess.getMethod().ordinal();
				accessLevels[ind] = level;
			}
			int index = -1;
			int length = types == null ? 0 : types.length;

			while (iterator.hasNext()) {
				index = 0;
				AccessLevelOption optionLevel = iterator.next();
				for (; index < length; index++) {
					this.setAccessibleMethod(types[index], optionLevel,
							(accessLevels[index] <= optionLevel.getAccessLevel().getLevel()));
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}

	/**
	 * Returns the list of {@link MethodAccessibility} available for the
	 * {@link ModelElement} mapped to this AccessNode and the
	 * {@link AccessLevelOption} passed as parameter
	 * 
	 * @param userAccessEntity
	 *            the {@link AccessLevelOption} for which to retrieve the set of
	 *            {@link MethodAccessibility}
	 * 
	 * @return the set of {@link MethodAccessibility} for the mapped
	 *         {@link ModelElement} and the specified {@link AccessLevelOption}
	 */
	@Override
	public List<MethodAccessibility> getAccessibleMethods(AccessLevelOption accessLevelOption) {
		List<MethodAccessibility> accesses = new ArrayList<MethodAccessibility>();
		AccessMethod.Type[] types = AccessMethod.Type.values();

		int index = 0;
		int length = types.length;

		for (; index < length; index++) {
			accesses.add(new MethodAccessibilityImpl(types[index], accessLevelOption,
					this.isAccessibleMethod(types[index], accessLevelOption)));
		}
		return accesses;
	}

	/**
	 * Defines the availability of the {@link AccessMethod.Type} passed as parameter
	 * for the {@link AccessLevel} also passed as parameter
	 * 
	 * @param accessMethod
	 *            the {@link AccessMethod.Type} to define the availability of for
	 *            the specified {@link AccessLevel}
	 * @param optionLevel
	 *            the {@link AccessLevel} for which to define the specified
	 *            {@link AccessMethod.Type} availability
	 * @param accessibility
	 *            <ul>
	 *            <li>true if the specified {@link AccessMethod.Type} is accessible
	 *            for the specified {@link AccessLevel}</li>
	 *            <li>false otherwise</li>
	 *            </ul>
	 */
	protected void setAccessibleMethod(AccessMethod.Type accessMethod, AccessLevelOption optionLevel,
			boolean accessibility) {
		List<MethodAccessibility> methodAccesses = this.accesses.get(optionLevel);
		if (methodAccesses == null) {
			methodAccesses = new ArrayList<MethodAccessibility>();
			this.accesses.put(optionLevel, methodAccesses);
		}
		MethodAccessibilityImpl methodAccessibilities = new MethodAccessibilityImpl(accessMethod, optionLevel,
				accessibility);
		methodAccesses.add(methodAccessibilities);
	}

	/**
	 * Returns true if the {@link AccessMethod.Type} passed as parameter is
	 * available for the {@link AccessLevel} passed as parameter; otherwise returns
	 * false
	 * 
	 * @param accessMethod
	 *            the {@link AccessMethod.Type} to define the availability of for
	 *            the specified {@link AccessLevel}
	 * @param accessLevel
	 *            the {@link AccessLevel} for which to define the specified
	 *            {@link AccessMethod.Type} availability
	 * @return
	 *         <ul>
	 *         <li>true if the specified {@link AccessMethod.Type} is accessible for
	 *         the specified {@link AccessLevel}</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	protected boolean isAccessibleMethod(AccessMethod.Type accessMethod, AccessLevelOption optionLevel) {
		boolean accessible = false;

		List<MethodAccessibility> methodAccesses = accesses == null ? emptyList() :
			ofNullable(accesses.get(optionLevel)).orElse(emptyList()); 

		Optional<MethodAccessibility> found = methodAccesses.stream()
				.filter(ma -> ma.getName().equals(accessMethod.name()))
				.findFirst();
		
		if (found.isPresent()) {
			accessible = found.get().isAccessible();
		} else {
			accessible = super.parent == null ? false : super.parent.isAccessibleMethod(accessMethod, optionLevel);
		}
		return accessible;
	}

	/**
	 * Returns the 'minimum' required {@link AccessLevelOption} allowing to invoke
	 * the {@link AccessMethod.Type} passed as parameter on a {@link ModelElement}
	 * mapped to this {@link AccessNodeImpl}
	 * 
	 * @param method
	 *            the targeted {@link AccessMethod.Type}
	 * 
	 * @return the minimum required {@link AccessLevel} to invoke the specified the
	 *         {@link AccessMethod.Type}
	 */
	@Override
	public AccessLevelOption getAccessLevelOption(Type method) {
		AccessLevelOption[] accessLevelOptions = AccessLevelOption.values();
		int index = 0;
		int length = accessLevelOptions == null ? 0 : accessLevelOptions.length;
		for (; index < length; index++) {
			if (this.isAccessibleMethod(method, accessLevelOptions[index])) {
				return accessLevelOptions[index];
			}
		}
		return AccessLevelOption.OWNER;
	}

	/**
	 * Creates and returns an immutable clone of this AccessNode
	 * 
	 * @param ic
	 *            the immutable path node type
	 * @param parent
	 *            the immutable clone of this AccessNode's parent
	 * 
	 * @return this AccessNode immutable clone
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P extends ImmutablePathNode<P>> P immutable(Class<P> ic, P parent) {
		return (P) new ImmutableAccessNode((ImmutableAccessNode) parent, this.nodeName, this.isPattern, super.children,
				this.profile);
	}

	/**
	 * Creates and returns a clone of this AccessNode
	 * 
	 * @return this AccessNode clone
	 */
	public N clone() {
		N clone = null;
		try {
			clone = ((Class<N>) getClass())
					.getConstructor(new Class<?>[] { String.class, boolean.class })
					.newInstance(new Object[] { this.nodeName, this.isPattern });

			clone.withAccessProfile(this.profile);
			Iterator<N> iterator = children.iterator();
			while (iterator.hasNext()) {
				clone.add((N) iterator.next().clone());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
		return clone;
	}
}
