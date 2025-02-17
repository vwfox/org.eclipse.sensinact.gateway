/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;

/**
 * An {@link AccessMethod} {@link Description}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessMethodDescription implements Description, Iterable<Signature> {
	private final Set<Signature> signatures;

	private String name;

	/**
	 * Constructor
	 * 
	 * @param method
	 *            the {@link AccessMethod} to describe
	 */
	protected AccessMethodDescription(AccessMethod<?, ?> method) {
		this.signatures = method.getSignatures();
		this.name = method.getName();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see JSONable#getJSON()
	 */
	@Override
	public String getJSON() {
		StringBuilder buffer = new StringBuilder();
		// buffer.append(JSONUtils.OPEN_BRACKET);

		boolean moreThanOne = false;

		Iterator<Signature> iterator = this.signatures.iterator();
		while (iterator.hasNext()) {
			String json = iterator.next().getJSON();
			if (json == null) {
				continue;
			}
			buffer.append(moreThanOne ? JSONUtils.COMMA : JSONUtils.EMPTY);
			buffer.append(json);
			moreThanOne = true;
		}
		// buffer.append(JSONUtils.CLOSE_BRACKET);
		return buffer.toString();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see Description#getJSONDescription()
	 */
	@Override
	public String getJSONDescription() {
		return this.getJSON();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Signature> iterator() {
		return this.signatures.iterator();
	}

	/**
	 * Returns the JSON array representation of the described {@link AccessMethod}
	 * 
	 * @return the JSON array representation of the described {@link AccessMethod}
	 */
	public JsonArray getJSONObjectDescription() {
		JsonArrayBuilder jsonArray = JsonProviderFactory.getProvider().createArrayBuilder();

		Iterator<Signature> iterator = this.signatures.iterator();
		while (iterator.hasNext()) {
			jsonArray.add(iterator.next().getJSONObjectDescription());
		}
		return jsonArray.build();
	}

	/**
	 * @InheritedDoc
	 *
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}
}
