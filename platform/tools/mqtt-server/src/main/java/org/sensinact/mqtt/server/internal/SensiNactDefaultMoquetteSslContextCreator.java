/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.sensinact.mqtt.server.internal;

import io.moquette.BrokerConstants;
import io.moquette.server.config.IConfig;
import io.moquette.spi.security.ISslContextCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;

/**
 * Moquette server implementation to load SSL certificate from local filesystem path
 * configured in config file.
 * <p>
 * Created by andrea on 13/12/15.
 */
public class SensiNactDefaultMoquetteSslContextCreator implements ISslContextCreator {

    private static final Logger LOG = LoggerFactory.getLogger(SensiNactDefaultMoquetteSslContextCreator.class);

    private IConfig props;

    public SensiNactDefaultMoquetteSslContextCreator(IConfig props) {
        this.props = props;
    }

    @Override
    public SSLContext initSSLContext() {
        final String jksPath = props.getProperty(BrokerConstants.JKS_PATH_PROPERTY_NAME);
        LOG.info("Starting SSL using keystore at {}", jksPath);
        if (jksPath == null || jksPath.isEmpty()) {
            //key_store_password or key_manager_password are empty
            LOG.warn("You have configured the SSL port but not the jks_path, SSL not started");
            return null;
        }

        //if we have the port also the jks then keyStorePassword and keyManagerPassword
        //has to be defined
        final String keyStorePassword = props.getProperty(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME);
        final String keyManagerPassword = props.getProperty(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME);
        if (keyStorePassword == null || keyStorePassword.isEmpty()) {
            //key_store_password or key_manager_password are empty
            LOG.warn("You have configured the SSL port but not the key_store_password, SSL not started");
            return null;
        }
        if (keyManagerPassword == null || keyManagerPassword.isEmpty()) {
            //key_manager_password or key_manager_password are empty
            LOG.warn("You have configured the SSL port but not the key_manager_password, SSL not started");
            return null;
        }

        // if client authentification is enabled a trustmanager needs to be
        // added to the ServerContext
        String sNeedsClientAuth = props.getProperty(BrokerConstants.NEED_CLIENT_AUTH, "false");
        boolean needsClientAuth = Boolean.valueOf(sNeedsClientAuth);

        try {
            InputStream jksInputStream = jksDatastore(jksPath);
            SSLContext serverContext = SSLContext.getInstance("TLS");
            final KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(jksInputStream, keyStorePassword.toCharArray());
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyManagerPassword.toCharArray());
            TrustManager[] trustManagers = null;
            if (needsClientAuth) {
                // use keystore as truststore, as server needs to trust certificates signed by the server certificates
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                trustManagers = tmf.getTrustManagers();
            }
            // init sslContext
            serverContext.init(kmf.getKeyManagers(), trustManagers, null);

            return serverContext;
        } catch (Exception ex) {
            LOG.error("Can't start SSL layer!", ex);
            return null;
        }
    }

    private InputStream jksDatastore(String jksPath) throws FileNotFoundException {
        URL jksUrl = getClass().getClassLoader().getResource(jksPath);
        if (jksUrl != null) {
            LOG.info("Starting with jks at {}, jks normal {}", jksUrl.toExternalForm(), jksUrl);
            return getClass().getClassLoader().getResourceAsStream(jksPath);
        }
        LOG.info("jks not found in bundled resources, try on the filesystem");
        File jksFile = new File(jksPath);
        if (jksFile.exists()) {
            LOG.info("Using {} ", jksFile.getAbsolutePath());
            return new FileInputStream(jksFile);
        }
        LOG.warn("File {} doesn't exists", jksFile.getAbsolutePath());
        return null;
    }
}
