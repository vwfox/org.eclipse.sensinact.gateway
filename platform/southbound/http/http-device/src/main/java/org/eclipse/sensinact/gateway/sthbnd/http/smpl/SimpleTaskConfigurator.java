/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.HttpChildTaskConfigurationDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.HttpTaskConfigurationDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.KeyValuePairDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.NestedMappingDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.RootMappingDescription;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SimpleTaskConfigurator implements HttpTaskBuilder {
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleTaskConfigurator.class);
    public static final String VALUE_PATTERN = "\\$\\(([^\\)\\@]+)\\)";
    public static final String CONTEXT_PATTERN = "\\@context\\[([^\\]\\$]+)\\]";

    private static List<KeyValuePairDescription> join(List<KeyValuePairDescription> pa, List<KeyValuePairDescription> pb) {       
        List<KeyValuePairDescription> joined = new ArrayList<>();
        if(pa!=null && !pa.isEmpty())
        	joined.addAll(pa);
        if(pb!=null && !pb.isEmpty())
        	joined.addAll(pb);
        return joined;
    }

    protected SimpleHttpProtocolStackEndpoint endpoint;

    private Pattern valuePattern;
    private Pattern contextPattern;

    private String acceptType = null;
    private String contentType = null;
    private String httpMethod = null;

    private String scheme = null;
    private String host = null;
    private String port = null;
    private String path = null;
    private String profile = null;
    private String clientCertificate = null;
    private String clientCertificatePassword = null;
    private String serverCertificate = null;
    private boolean direct = false;
    private int readTimeout = -1;
    private int connectTimeout = -1;
    private CommandType command = null;

    private Map<String, List<String>> query = null;
    private Map<String, List<String>> headers = null;

    private Class<? extends HttpPacket> packetType = null;
    
    private HttpTaskConfigurator contentBuilder = null;
    private HttpTaskConfigurator urlBuilder = null;
	private NestedMappingDescription[] nestedMapping = null;
	private RootMappingDescription[] rootMapping = null;

    /**
     * @param mediator
     * @param profile
     * @param command
     * @param urlBuilder
     * @param annotation
     */
    public SimpleTaskConfigurator(SimpleHttpProtocolStackEndpoint endpoint, String profile, 
    	CommandType command, HttpTaskConfigurator urlBuilder, HttpTaskConfigurationDescription annotation) {
        this(endpoint, profile, command, urlBuilder, annotation, null);
    }

    /**
     * @param mediator
     * @param profile
     * @param command
     * @param urlBuilder
     * @param parent
     * @param child
     */
    public SimpleTaskConfigurator(SimpleHttpProtocolStackEndpoint endpoint, String profile, 
    	CommandType command, HttpTaskConfigurator urlBuilder, HttpTaskConfigurationDescription parent, 
    	HttpChildTaskConfigurationDescription child) {
        this.endpoint = endpoint;

        this.valuePattern = Pattern.compile(VALUE_PATTERN);
        this.contextPattern = Pattern.compile(CONTEXT_PATTERN);
        
        this.acceptType = child != null && !HttpChildTaskConfigurationDescription.DEFAULT_ACCEPT_TYPE.equals(
        	child.getAcceptType()) ? child.getAcceptType() : parent.getAcceptType();
        
        this.contentType = child != null && !HttpChildTaskConfigurationDescription.DEFAULT_CONTENT_TYPE.equals(
        	child.getContentType()) ? child.getContentType() : parent.getContentType();

        this.httpMethod = child != null && !HttpChildTaskConfigurationDescription.DEFAULT_HTTP_METHOD.equals(
        	child.getHttpMethod()) ? child.getHttpMethod() : parent.getHttpMethod();
        
        this.scheme = child != null && !HttpChildTaskConfigurationDescription.DEFAULT_SCHEME.equals(
        	child.getScheme()) ? child.getScheme() : parent.getScheme();
        
        this.host = child != null && !HttpChildTaskConfigurationDescription.DEFAULT_HOST.equals(
        	child.getHost()) ? child.getHost() : parent.getHost();

        this.port = child != null && !HttpChildTaskConfigurationDescription.DEFAULT_PORT.equals(
        	child.getPort()) ? child.getPort() : parent.getPort();

        this.path = child != null && !HttpChildTaskConfigurationDescription.DEFAULT_PATH.equals(
        	child.getPath()) ? child.getPath() : parent.getPath();
        
        this.clientCertificate =  child != null && !HttpTaskConfigurationDescription.DEFAULT_CLIENT_SSL_CERTIFICATE.equals(
        	child.getClientSSLCertificate()) ?child.getClientSSLCertificate():parent.getClientSSLCertificate();

        this.clientCertificatePassword =  child != null && !HttpTaskConfigurationDescription.DEFAULT_CLIENT_SSL_CERTIFICATE_PASSWORD.equals(
        	child.getClientSSLCertificatePassword()) ?child.getClientSSLCertificatePassword():parent.getClientSSLCertificatePassword();
        
        this.serverCertificate =  child != null && !HttpTaskConfigurationDescription.DEFAULT_SERVER_SSL_CERTIFICATE.equals(
        	child.getClientSSLCertificate()) ?child.getClientSSLCertificate():parent.getClientSSLCertificate();

        this.readTimeout =  (child != null && HttpChildTaskConfigurationDescription.DEFAULT_READ_TIMEOUT != child.getReadTimeout()) ?
        		child.getReadTimeout():parent.getReadTimeout();
        
        this.connectTimeout =  (child != null && HttpChildTaskConfigurationDescription.DEFAULT_CONNECTION_TIMEOUT != child.getConnectTimeout()) ?
        		child.getConnectTimeout():parent.getConnectTimeout();

        this.nestedMapping =  (child != null && HttpChildTaskConfigurationDescription.DEFAULT_NESTED_MAPPING != child.getNestedMapping()) ?
        		child.getNestedMapping():parent.getNestedMapping();
        
        this.rootMapping =  (child != null && HttpChildTaskConfigurationDescription.DEFAULT_ROOT_MAPPING != child.getRootMapping()) ?
        		child.getRootMapping():parent.getRootMapping();
        
        this.setProfile(profile);
        this.urlBuilder = urlBuilder;
        this.command = command;
        this.direct = child != null ? child.isDirect() : parent.isDirect();
        this.packetType = child != null && HttpPacket.class != child.getPacket() ? child.getPacket() : parent.getPacket();
        
        List<KeyValuePairDescription> queryParameters = join(child == null ? null : child.getQuery(), parent.getQuery());

        int index = 0;
        int length = queryParameters == null ? 0 : queryParameters.size();

        this.query = new HashMap<String, List<String>>();
        for (; index < length; index++) {
            String key = queryParameters.get(index).getKey();
            String value = queryParameters.get(index).getValue();
            List<String> values = query.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                query.put(key, values);
            }
            values.add(value);
        }        
        this.headers = new HashMap<String, List<String>>();
        List<KeyValuePairDescription> headersParameters = join(child == null ? null : child.getHeaders(), parent.getHeaders());
        index = 0;
        length = headersParameters == null ? 0 : headersParameters.size();

        for (; index < length; index++) {
            String key = headersParameters.get(index).getKey();
            String value = headersParameters.get(index).getValue();
            List<String> values = headers.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                headers.put(key, values);
            }
            values.add(value);
        }        
        Class<? extends HttpTaskConfigurator> taskContentConfigurationType = child != null && HttpTaskConfigurator.class != child.getContent() 
        		? child.getContent() : parent.getContent();

        if (taskContentConfigurationType != null && taskContentConfigurationType != HttpTaskConfigurator.class) {
             try {
	            this.contentBuilder = ReflectUtils.getTheBestInstance(taskContentConfigurationType, new Object[] {this.endpoint.getMediator()});
	        } catch (Exception e) {
	            LOG.error(e.getMessage(), e);
	        }
        }
    }

    
    private String resolve(HttpTask<?, ?> key, String value) {
        String argument = value;
        String resolved = null;

        while (argument != null) {
            Matcher valueMatcher = this.valuePattern.matcher(argument);
            Matcher contextMatcher = this.contextPattern.matcher(argument);
            
            int valueIndex = valueMatcher.find()?valueMatcher.start(1):-1;
            int contextIndex = contextMatcher.find()?contextMatcher.start(1):-1;
            
            if (valueIndex > contextIndex) {            	
                String val = (String) this.endpoint.getMediator().getProperty(valueMatcher.group(1));
                StringBuilder builder = new StringBuilder().append(argument.substring(0, valueMatcher.start(1) - 2)).append(val).append(argument.substring(valueMatcher.end(1) + 1, argument.length()));
                argument = builder.toString();
            } else if (contextIndex > valueIndex) {            	
            	String val = this.endpoint.getMediator().resolve(key, contextMatcher.group(1));
                StringBuilder builder = new StringBuilder().append(argument.substring(0, contextMatcher.start(1) - 9)).append(val).append(argument.substring(contextMatcher.end(1) + 1, argument.length()));
                argument = builder.toString();
            } else {
                resolved = argument;
                break;
            }
        }
        return resolved;
    }

    @Override
    public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
    	try {
        String portStr = this.resolve(task, this.port);
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            port = 80;
        }
        String uri = new URL(this.resolve(task, scheme), this.resolve(task, host), port, this.resolve(task, path)).toExternalForm();

        StringBuilder queryBuilder = new StringBuilder();

        Iterator<Map.Entry<String, List<String>>> iterator = query.entrySet().iterator();
        Map.Entry<String, List<String>> entry = null;
        int position = -1;

        for (; iterator.hasNext(); ) {
            entry = iterator.next();

            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            queryBuilder.append(++position == 0 ? "?" : "");

            int index = 0;
            int length = values == null ? 0 : values.size();
            for (; index < length; index++) {
                queryBuilder.append((position > 0 || index > 0) ? "&" : "");
                queryBuilder.append(this.resolve(task, entry.getKey()));
                queryBuilder.append("=");
                queryBuilder.append(this.resolve(task, values.get(index)));
            }
        }
        String queryRequest = queryBuilder.toString();
        if (queryRequest.length() > 1) {
            uri = new StringBuilder().append(uri).append(queryRequest).toString();
        }
        task.setUri(uri);

        if (this.urlBuilder != null) {
            this.urlBuilder.configure(task);
        }
        task.setClientSSLCertificate(HttpTaskConfigurationDescription.NO_CERTIFICATE.equals(clientCertificate)?null:clientCertificate);
        task.setClientSSLCertificatePassword(HttpTaskConfigurationDescription.NO_CERTIFICATE.equals(clientCertificatePassword)?null:clientCertificatePassword);        
        task.setServerSSLCertificate(HttpTaskConfigurationDescription.NO_CERTIFICATE.equals(serverCertificate)?ConnectionConfiguration.TRUST_ALL:serverCertificate);
        
        task.setDirect(direct);
        iterator = headers.entrySet().iterator();
        entry = null;
        position = -1;

        for (; iterator.hasNext(); ) {
            entry = iterator.next();

            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            int index = 0;
            int length = values == null ? 0 : values.size();
            for (; index < length; index++) {
                task.addHeader(this.resolve(task, entry.getKey()), this.resolve(task, values.get(index)));
            }
        }
        if(this.packetType!=HttpPacket.class) {
        	task.setPacketType(this.packetType);
        }
        task.setAccept(this.resolve(task, acceptType));
        task.setContentType(this.resolve(task, contentType));
        task.setHttpMethod(this.resolve(task, httpMethod));
        task.setReadTimeout(this.readTimeout);
        task.setConnectTimeout(this.connectTimeout);
        if(this.nestedMapping!=null && this.nestedMapping.length > 0)
        	task.setMapping(this.nestedMapping);
        else if(this.rootMapping!=null && this.rootMapping.length > 0)
        	task.setMapping(this.rootMapping);
        
        if (contentBuilder != null) 
            contentBuilder.configure(task);
    	}catch(Exception e) {
        	e.printStackTrace();
        }
    }

    /**
     * Returns the {@link CommandType} of the task to be
     * configured
     *
     * @return the {@link CommandType} of the task to be
     * configured
     */
    public CommandType handled() {
        return this.command;
    }

	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}
}