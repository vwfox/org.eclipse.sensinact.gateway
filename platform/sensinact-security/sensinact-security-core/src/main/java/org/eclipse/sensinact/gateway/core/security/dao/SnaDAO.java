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

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.security.dao.AbstractSnaDAO.UserDefinedSelectStatement;
import org.eclipse.sensinact.gateway.core.security.dao.directive.SelectDirective;
import org.eclipse.sensinact.gateway.core.security.entity.SnaEntity;
import org.eclipse.sensinact.gateway.core.security.entity.SnaEntity.Key;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;

/**
 * Data Access Object interface definition
 * 
 * @param <E>
 *            the handled extended {@link SnaEntity} type
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaDAO<E extends SnaEntity> {
	static final String WHERE_DIRECTIVE = "WHERE";
	static final String SELECT_DIRECTIVE = "SELECT";
	static final String INSERT_DIRECTIVE = "INSERT INTO";
	static final String UPDATE_DIRECTIVE = "UPDATE";
	static final String DELETE_DIRECTIVE = "DELETE FROM";
	static final String SET_DIRECTIVE = "SET";
	static final String EQUALS_OPERATOR = "=";
	static final String FROM_DIRECTIVE = "FROM";
	static final String VALUES_DIRECTIVE = "VALUES";
	static final String AND = "AND";
	static final String WILDCARD = "*";
	static final String DOT = ".";
	static final String COMMA = ",";
	static final String SPACE = " ";
	static final String QUOTE = "'";
	static final String OPEN_PARENTHESIS = "(";
	static final String CLOSE_PARENTHESIS = ")";

	/**
	 * Selects the entities from the datastore according to the
	 * {@link SelectDirective} build using the {@link UserDefinedSelectStatement}
	 * whose name is passed as parameter and parameterized by the set of String
	 * variables also passed as parameter.
	 * 
	 * @param name
	 *            the name of {@link UserDefinedSelectStatement} to be executed
	 * @param variables
	 *            the set of variables parameterizing the select statement to be
	 *            executed
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	List<E> select(String name, String... variables) throws DAOException, DataStoreException;

	/**
	 * Selects the entities from the datastore according to the
	 * {@link SelectDirective} build using the set of conditions gathered in the Map
	 * passed as parameter.
	 * 
	 * @param whereDirectives
	 *            the set of conditions to use to create the SelectDirective.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	List<E> select(Map<String, Object> whereDirectives) throws DAOException, DataStoreException;

	/**
	 * Returns the entity from the datastore matching the given List of
	 * {@link SnaEntity.Key}s, otherwise null.
	 * 
	 * @param keyMap
	 *            The List of {@link SnaEntity.Key}s specifying the primary key of
	 *            the entity to be returned.
	 * @return The entity from the datastore matching the given List of
	 *         {@link SnaEntity.Key}s, otherwise null.
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	E select(List<Key> keyMap) throws DAOException, DataStoreException;

	/**
	 * Returns a list of all entities from the datastore ordered by entity ID. The
	 * list is never null and is empty when the datastore does not contain any
	 * entity.
	 * 
	 * @return A list of all entities from the datastore ordered by entity ID.
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	List<E> select() throws DAOException, DataStoreException;

	/**
	 * Create the given entity in the datastore. The entity ID must be null,
	 * otherwise it will throw IllegalArgumentException. After creating, the DAO
	 * will set the obtained ID in the given entity.
	 * 
	 * @param entity
	 *            The entity to be created in the datastore.
	 * 
	 * @throws IllegalArgumentException
	 *             If the entity ID is not null.
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	void create(E entity) throws IllegalArgumentException, DAOException, DataStoreException;

	/**
	 * Update the given entity in the datastore. The entity ID must not be null,
	 * otherwise it will throw IllegalArgumentException.
	 * 
	 * @param entity
	 *            The entity to be updated in the datastore.
	 * 
	 * @throws IllegalArgumentException
	 *             If the entity ID is null.
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	void update(E entity) throws IllegalArgumentException, DAOException, DataStoreException;

	/**
	 * Delete the given entity from the datastore. After deleting, the DAO will set
	 * the ID of the given entity to null.
	 * 
	 * @param entity
	 *            The entity to be deleted from the datastore.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	void delete(E entity) throws DAOException, DataStoreException;

}
