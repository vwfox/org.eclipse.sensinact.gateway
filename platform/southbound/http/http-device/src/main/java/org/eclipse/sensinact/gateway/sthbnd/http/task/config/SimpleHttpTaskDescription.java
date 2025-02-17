/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.task.config;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleHttpTaskDescription {

	public static SimpleHttpTaskDescription toDescription(SimpleHttpTask simpleHttpTask) {
    	SimpleHttpTaskDescription description = new SimpleHttpTaskDescription();
    	description.setCommands(Arrays.asList(simpleHttpTask.commands()));
    	description.setConfiguration(HttpTaskConfigurationDescription.toDescription(simpleHttpTask.configuration()));
    	description.setProfile(simpleHttpTask.profile());
    	return description;
    }
	
	@JsonProperty(value="profile")
	private String profile;
	
	@JsonProperty(value="commands")
	private List<CommandType> commands;

	@JsonProperty(value="configuration")
	private HttpTaskConfigurationDescription configuration;

	public SimpleHttpTaskDescription() {}
	
	public SimpleHttpTaskDescription(String profile,List<CommandType> commands,
		HttpTaskConfigurationDescription configuration) {
		this.profile = profile;
		if(commands != null)
			this.commands = Collections.unmodifiableList(commands);
		this.configuration = configuration;
	}

	/**
	 * @return the profile
	 */
	public String getProfile() {
    	if(this.profile == null)
    		return ResourceConfig.ALL_PROFILES;
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * @return the commands
	 */
	public List<CommandType> getCommands() {
    	if(this.commands == null)
    		return Arrays.asList(CommandType.GET);
		return commands;
	}

	/**
	 * @param commands the commands to set
	 */
	public void setCommands(List<CommandType> commands) {
		if(commands != null)
			this.commands = Collections.unmodifiableList(commands);
	}

	/**
	 * @return the configuration
	 */
	public HttpTaskConfigurationDescription getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(HttpTaskConfigurationDescription configuration) {
		this.configuration = configuration;
	}
}
