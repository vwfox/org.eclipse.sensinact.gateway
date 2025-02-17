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

import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileAccessEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Method DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ObjectProfileAccessDAO extends AbstractImmutableSnaDAO<ObjectProfileAccessEntity> {
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

	private ObjectProfileDAO objectProfileDAO;

	/**
	 * Constructor
	 * 
	 */
	public ObjectProfileAccessDAO(DataStoreService dataStoreService) throws DAOException {
		super(ObjectProfileAccessEntity.class, dataStoreService);
		this.objectProfileDAO = new ObjectProfileDAO(dataStoreService);
	}

	/**
	 * @param objectProfile
	 * @return
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	public AccessProfileOption getAccessProfileOption(long objectProfile) throws DAOException, DataStoreException {
		ObjectProfileEntity entity = this.objectProfileDAO.find(objectProfile);
		return AccessProfileOption.valueOf(entity.getName());
	}

	/**
	 * Returns the {@link ObjectEntity} from the datastore matching the given Long
	 * identifier, otherwise null.
	 * 
	 * @param objectProfileEntity
	 * 
	 * @return the {@link ObjectEntity} from the datastore matching the given Long
	 *         identifier, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public List<ObjectProfileAccessEntity> getObjectProfileAccesses(ObjectProfileEntity objectProfileEntity)
			throws DAOException, DataStoreException {
		return getObjectProfileAccesses(objectProfileEntity.getIdentifier());
	}

	/**
	 * Returns the {@link ObjectEntity} from the datastore matching the given Long
	 * identifier, otherwise null.
	 * 
	 * @param objectProfileEntityId
	 *            The Long identifier specifying the primary key of the
	 *            {@link ObjectProfileEntity} to be returned.
	 * @return the {@link ObjectProfileEntity} from the datastore matching the given
	 *         Long identifier, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public List<ObjectProfileAccessEntity> getObjectProfileAccesses(final long identifier)
			throws DAOException, DataStoreException {
		return super.select(Collections.singletonMap("OPID", identifier));
	}

}
