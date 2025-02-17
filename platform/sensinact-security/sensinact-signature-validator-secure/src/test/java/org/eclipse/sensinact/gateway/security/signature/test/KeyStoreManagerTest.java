/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.signature.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.eclipse.sensinact.gateway.security.signature.internal.KeyStoreManager;
import org.eclipse.sensinact.gateway.security.signature.internal.KeyStoreManagerException;
import org.junit.jupiter.api.Test;

public class KeyStoreManagerTest {
    static KeyStoreManager ksm = null;
    String alias = "selfsigned";
    String fake_alias = "notselfsigned";
    String passwd = "sensiNact_team";
    String falsePasswd = "keyStore";
    static String defaultKeystoreFile = System.getProperty("org.eclipse.sensinact.gateway.security.jks.filename");

    @Test
    public void testGetCertificateOK() throws KeyStoreManagerException {
        ksm = new KeyStoreManager(defaultKeystoreFile, passwd);
        Certificate cert = null;
        try {
            cert = ksm.getCertificate(alias);
        } catch (Exception e) {
        } finally {
            assertNotNull(cert);
        }
    }

    @Test
    public void testGetCertificateWrongLogin() throws KeyStoreManagerException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        boolean ioexception = false;
        boolean noCert;
        Certificate cert = null;
        try {
            ksm = new KeyStoreManager(defaultKeystoreFile, passwd + "p");
            cert = ksm.getCertificate(alias);
        } catch (Exception e) {
            ioexception = true;
        }
        if (cert == null) {
            noCert = true;
        } else {
            noCert = false;
        }
        assertTrue(ioexception && noCert);
    }

    @Test
    public void testGetCertificateNoSubject() throws KeyStoreManagerException {
        ksm = new KeyStoreManager(defaultKeystoreFile, passwd);
        Certificate cert = null;
        try {
            cert = ksm.getCertificate(fake_alias);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assertNull(cert);
        }
    }

    @Test
    public void testGetPublicKeyOK() throws KeyStoreManagerException {
        ksm = new KeyStoreManager(defaultKeystoreFile, passwd);
        PublicKey pub = null;
        try {
            pub = ksm.getCertificate(alias).getPublicKey();
        } catch (Exception e) {
        } finally {
            assertNotNull(pub);
        }
    }


    @Test
    public void testGetPrivateKeyOK() throws KeyStoreManagerException {
        ksm = new KeyStoreManager(defaultKeystoreFile, passwd);
        PrivateKey priv = null;
        try {
            priv = ksm.getPrivateKey(alias, passwd);
        } catch (Exception e) {
        } finally {
            assertNotNull(priv);
        }
    }

    @Test
    public void testGetPrivateKeyWrongLogin() throws KeyStoreManagerException {
        ksm = new KeyStoreManager(defaultKeystoreFile, passwd);
        PrivateKey priv = null;
        try {
            priv = ksm.getPrivateKey(alias, passwd + "p");
        } catch (Exception e) {
        } finally {
            assertNull(priv);
        }
    }

    @Test
    public void testIsCertificateValidOK() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, FileNotFoundException, KeyStoreManagerException {
        ksm = new KeyStoreManager(defaultKeystoreFile, passwd);
        boolean result = false;
        Certificate cert = ksm.getCertificate(alias);
        result = ksm.isValid(cert);
        assertTrue(result);
    }

    @Test
    public void testIsCertificateValidNoKeystoreOK() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, FileNotFoundException, KeyStoreManagerException {
        ksm = new KeyStoreManager(defaultKeystoreFile, passwd);
        boolean result = false;
        Certificate cert = ksm.getCertificate(alias);
        result = ksm.isTemporallyOK(cert);
        assertTrue(result);
    }

}
