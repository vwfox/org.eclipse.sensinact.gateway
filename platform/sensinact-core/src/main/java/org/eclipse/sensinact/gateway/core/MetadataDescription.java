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

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.PrimitiveDescription;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * {@link Description} of a {@link Metadata}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MetadataDescription extends PrimitiveDescription {
	private static final String EMPTY = "";

	/**
	 * Constructor
	 * 
	 * @param metadata the {@link Metadata} for which to instantiate the
	 * MetadataDescription
	 */
	public MetadataDescription(Metadata metadata) {
		super(metadata);
	}

	@Override
	public String getJSONDescription() {
		JsonObject description = this.getJSONObjectDescription();
		if (description == null) 
			return EMPTY;		
		return description.toString();
	}

	/**
	 * Returns the JSON object representation of the described {@link Metadata}
	 * 
	 * @return the JSON object representation of the described {@link Metadata}
	 */
	protected JsonObject getJSONObjectDescription() {
		if (Modifiable.FIXED.equals(super.modifiable)) {
			JsonObjectBuilder description = super.getJsonObject();
			description.add(VALUE_KEY, this.getJsonValue());
			return description.build();
		}
		return null;
	}
}
