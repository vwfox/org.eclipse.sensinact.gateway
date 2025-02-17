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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.eclipse.sensinact.gateway.security.signature.internal.CryptographicUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class CryptographicUtilsTest {
    static CryptographicUtils cutils = null;
    String fileName4hash = "src/test/resources/textFile.txt.bis";
    String trueHashValue = "c00148f586db109ffaca3724102e69e2e7996bf0";
    String falseHashValue = "swzWklBmNJVD4/8+hpCT6b3L7WY=";
    String defaultAlgo = "SHA-1";
    String fileName4CMS = "src/test/resources/textFile.txt";
    String alias = "selfsigned";
    String passwd = "sensiNact_team";
    String keyStoreType = "jks";
    String defaultKeystoreFile = "../cert/keystore.jks";
    String signatureFileName = "src/test/resources/JUNITTES.SF";
    String signatureBlockName = "src/test/resources/JUNITTES.DSA";
    KeyStore ks = null;

    public CryptographicUtilsTest() throws NoSuchAlgorithmException {
        cutils = new CryptographicUtils();
    }

    KeyStore getKeyStore() throws KeyStoreException, FileNotFoundException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(new FileInputStream(defaultKeystoreFile), passwd.toCharArray());
        return ks;
    }

    /**
     * @param fileName
     * @return byte[]
     * @throws IOException
     */
    byte[] getData(String fileName) throws IOException {
      try(FileInputStream fis = new FileInputStream(fileName);){
        byte[] data = new byte[fis.available()];
        fis.read(data);
        return data;
      }
    }

    /**
     * @param data
     * @param algo
     * @throws NoSuchAlgorithmException
     */
    String getTrueHashValue(byte[] data, String algo) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algo);
        digest.update(data);
        return java.util.Base64.getEncoder().encodeToString(digest.digest());
    }

    /**
     * @param d1
     * @param d2
     * @return boolean
     */
    protected boolean dataWithSameContent(byte[] d1, byte[] d2) {
        boolean result = false;
        if (d1.length == d2.length) {
            byte[] data1 = new byte[d1.length];
            byte[] data2 = new byte[d2.length];
            boolean sameContent = true;
            for (int i = 0; i < d1.length; i++) {
                sameContent = sameContent && (data1[i] == data2[i]);
            }
            result = sameContent;
        }
        return result;
    }

    /**
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testCheckHashValueOK() throws Exception {
        byte[] data = getData(fileName4hash);
        String trueHashValue = this.getTrueHashValue(data, defaultAlgo);
        boolean result = cutils.checkHashValue(data, trueHashValue, "SHA1-Digest");
        Assertions.assertTrue(result);
    }

    /**
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testCheckHashValueFalseAssertion() throws Exception {
        byte[] data = getData(fileName4hash);
        boolean result = cutils.checkHashValue(data, falseHashValue, "SHA1-Digest");
        ;
        Assertions.assertFalse(result);
    }

    @Test
    public void testGetHashValueOK() throws Exception {
        byte[] data = getData(fileName4hash);
        String trueHashValue = getTrueHashValue(data, defaultAlgo);
        String result = cutils.getHashValue(data, "SHA1-Digest");
        ;
        Assertions.assertTrue(result.equals(trueHashValue));
    }

    /**
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testGetHashValueFalseAssertion() throws Exception {
        byte[] data = getData(fileName4hash);
        String result = cutils.getHashValue(data, "SHA1-Digest");
        Assertions.assertFalse(result.equals(falseHashValue));
    }

    /**
     * A method for verifying validity of a given CMS file
     */
    @Disabled
    @Test
    public void testCheckCMSDataValidity() throws Exception {
        try {
            byte[] signatureFileData = this.getData(signatureFileName);
            byte[] signatureBlockData = this.getData(signatureBlockName);
            boolean res = cutils.checkCMSDataValidity(signatureFileData, signatureBlockData, "SHA1-Digest");
            Assertions.assertTrue(res);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
    }
//	@Test
//	public void testGetAndCheckCMSDataOK()
//	{
//		try
//		{
//			KeyStore ks = getKeyStore();
//			Certificate cert = ks.getCertificate(alias);
//			PrivateKey priv = (PrivateKey) ks.getKey(alias,
//			        passwd.toCharArray());
//
//			byte[] data = this.getData(fileName4CMS);
//			byte[] cmsData = cutils.getCMSData(cert, priv, data);
//			boolean valid = cutils.checkCMSDataValidity(data, cmsData);
//			Assert.assertTrue(valid);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
}
