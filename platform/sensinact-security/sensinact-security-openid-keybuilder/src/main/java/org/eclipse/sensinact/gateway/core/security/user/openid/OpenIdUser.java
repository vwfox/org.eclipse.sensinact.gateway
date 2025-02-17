/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.user.openid;

import java.util.Objects;

public final class OpenIdUser  {

	public static final String USER_PUBLIC_KEY_CLAIM = "snaKey";
	private final JsonWebToken idToken;
	private final JsonWebToken accessToken;
	
	private boolean valid;

	public OpenIdUser(OpenIdUserKeyBuilderConfig userKeyBuilderConfig, String decodedId, JsonWebToken access) {
		
		Objects.requireNonNull(decodedId, "A user must have an id");
		Objects.requireNonNull(access, "A user must have an access token");
		
		idToken = new JsonWebToken(decodedId);
		accessToken = access;
		boolean result = false;
		
		Object azp = access.claim("azp");
		if(azp != null) {
			String authzParty = String.valueOf(azp);
			result = authzParty.contains(userKeyBuilderConfig.getClientId());
			if (!result)
				System.out.println("Bad client Id " + authzParty + " / " + userKeyBuilderConfig.getClientId());
		}		
		this.valid = result && idToken.isValid() && accessToken.isValid();
	}

	/**
	 * @return
	 */
	public String getSensiNactPublicKey() {
		Object publicKey = idToken.claim(USER_PUBLIC_KEY_CLAIM);
		if(publicKey == null)
			return null;
		return String.valueOf(publicKey);
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

}