/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.method;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.json.JSONArray;

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
	protected AccessMethodDescription(AccessMethod method) {
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
	public JSONArray getJSONObjectDescription() {
		JSONArray jsonArray = new JSONArray();

		Iterator<Signature> iterator = this.signatures.iterator();
		while (iterator.hasNext()) {
			jsonArray.put(iterator.next().getJSONObjectDescription());
		}
		return jsonArray;
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
