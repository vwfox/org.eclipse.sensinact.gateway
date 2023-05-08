/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation 
**********************************************************************/
package org.eclipse.sensinact.prototype;

import java.util.List;

public interface SensiNactSessionManager {

    SensiNactSession getDefaultSession(String userToken);

    SensiNactSession getSession(String sessionId);

    List<String> getSessionIds(String userToken);

    SensiNactSession createNewSession(String userToken);

}
