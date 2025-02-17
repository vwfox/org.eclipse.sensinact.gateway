/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.dao;

import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.core.security.entity.AgentEntity;
import org.eclipse.sensinact.gateway.core.security.entity.BundleEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Agent DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AgentDAO extends AbstractMutableSnaDAO<AgentEntity> {

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private BundleDAO bundleDAO;

	/**
	 * Constructor
	 * 
	 * @throws DAOException
	 */
	public AgentDAO(DataStoreService dataStoreService) throws DAOException {
		super( AgentEntity.class, dataStoreService);
		this.bundleDAO = new BundleDAO(dataStoreService);
	}

	/**
	 * Returns the {@link AgentEntity} from the datastore matching the given Long
	 * identifier, otherwise null.
	 * 
	 * @param identifier
	 *            The Long identifier specifying the primary key of the
	 *            {@link AgentEntity} to be returned.
	 * @return the {@link AgentEntity} from the datastore matching the given Long
	 *         identifier, otherwise null.
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	public AgentEntity find(final long identifier) throws DAOException, DataStoreException {
		List<AgentEntity> agentEntities = super.select(Collections.singletonMap("AID", identifier));

		if (agentEntities.size() != 1) {
			return null;
		}
		return agentEntities.get(0);
	}

	/**
	 * Returns the {@link AgentEntity} from the datastore matching the given String
	 * public key, otherwise null.
	 * 
	 * @param publicKey
	 *            The String public key of the {@link AgentEntity} to be returned.
	 * @return the {@link AgentEntity} from the datastore matching the given Long
	 *         identifier, otherwise null.
	 * 
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	public AgentEntity find(final String publicKey) throws DAOException, DataStoreException {
		List<AgentEntity> agentEntities = super.select(Collections.singletonMap("APUBLIC_KEY", publicKey));
		
		if (agentEntities.size() != 1) {
			return null;
		}
		return agentEntities.get(0);
	}

	/**
	 * Returns the {@link AgentEntity} from the datastore, held by the
	 * {@link BundleEntity} matching the given String SHA-1 signature if it exists,
	 * or null otherwise.
	 * 
	 * @param signature
	 *            The String signature (SHA-1) of the {@link BundleEntity} holding
	 *            the {@link AgentEntity}to be returned.
	 * @return the {@link AgentEntity} held by the {@link BundleEntity} matching the
	 *         String signature, otherwise null.
	 * 
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	// IMPLIES THE RESTRICTION OF ONE AGENT BY BUNDLE !!
	public AgentEntity findFromBundle(String signature) throws DAOException, DataStoreException {
		final BundleEntity bundleEntity = this.bundleDAO.find(signature);

		if (bundleEntity == null) {
			return null;
		}
		List<AgentEntity> agentEntities = super.select(Collections.singletonMap("BID", bundleEntity.getIdentifier()));

		if (agentEntities.size() != 1) {
			return null;
		}
		return agentEntities.get(0);
	}
}
