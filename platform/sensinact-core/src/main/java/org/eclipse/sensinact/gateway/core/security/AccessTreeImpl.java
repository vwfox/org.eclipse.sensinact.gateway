/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
/**
 * 
 */
package org.eclipse.sensinact.gateway.core.security;

import java.util.Iterator;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.util.tree.PathNodeFactory;
import org.eclipse.sensinact.gateway.util.tree.PathTree;

/**
 * {@link AccessTree} implementation whose nodes define access rights to an
 * instance of the sensiNact's data model
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessTreeImpl<N extends AccessNodeImpl<N>> extends PathTree<N>
		implements MutableAccessTree<AccessNodeImpl<N>> {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	/**
	 * {@link PathNodeFactory} dedicated to {@link AccessNodeImpl} instantiation
	 * 
	 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
	 */
	private static class AccessNodeFactory<N extends AccessNodeImpl<N>> implements PathNodeFactory<N> {

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.util.tree.PathNodeFactory#
		 *      createPathNode(java.lang.String)
		 */
		public N createPathNode(String nodeName) {
			return (N) new AccessNodeImpl<N>(nodeName, false);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.util.tree.PathNodeFactory#
		 *      createPatternNode(java.lang.String)
		 */
		public N createPatternNode(String nodeName) {
			return (N) new AccessNodeImpl<N>(nodeName, true);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.util.tree.PathNodeFactory#
		 *      createRootNode()
		 */
		public N createRootNode() {
			return (N) new AccessNodeImpl<N>(UriUtils.PATH_SEPARATOR, false);
		}
	}

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * The {@link Mediator} allowing to interact with the OSGi host environment
	 */
	protected Mediator mediator;

	/**
	 * Constructor
	 * 
	 */
	public AccessTreeImpl() {
		super(new AccessNodeFactory<N>());
	}

	/**
	 * 
	 * @param option the {@link AccessProfileOption} wrapping the
	 *               {@link AccessProfile} applying to the root node of this
	 *               {@link AccessTree}
	 * 
	 * @return this AccessTree instance
	 */
	public MutableAccessTree<AccessNodeImpl<N>> withAccessProfile(AccessProfileOption option) {
		return this.withAccessProfile(option.getAccessProfile());
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.MutableAccessTree#
	 *      withAccessProfile(org.eclipse.sensinact.gateway.core.security.AccessProfile)
	 */
	@Override
	public MutableAccessTree<AccessNodeImpl<N>> withAccessProfile(AccessProfile profile) {
		super.root.withAccessProfile(profile);
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MutableAccessTree<AccessNodeImpl<N>> clone() {
		MutableAccessTree<AccessNodeImpl<N>> tree = new AccessTreeImpl<N>().withAccessProfile(getRoot().getProfile());

		Iterator<N> iterator = getRoot().iterator();
		while (iterator.hasNext()) {
			tree.getRoot().add(iterator.next().clone());
		}
		return tree;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.AccessTree#isMutable()
	 */
	@Override
	public boolean isMutable() {
		return true;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.MutableAccessTree#immutable()
	 */
	@Override
	public ImmutableAccessTree immutable() {
		return super.immutable(ImmutableAccessTree.class, ImmutableAccessNode.class);
	}

}
