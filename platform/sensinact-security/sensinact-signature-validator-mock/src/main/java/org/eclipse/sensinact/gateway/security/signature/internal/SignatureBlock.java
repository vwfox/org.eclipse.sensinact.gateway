/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.signature.internal;


import java.io.IOException;
import java.net.URL;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.IOUtils;

/**
 * A Signature block is part of the meta-data of signed Jar Files. It
 * encapsulates a CMS (Cryptographic Message Standard) file.
 *
 * @author pierre parrend
 */
public class SignatureBlock  {
    private Mediator mediator;
	private byte[] bytes;

    public SignatureBlock(Mediator mediator, URL url) throws IOException {
        this.bytes= IOUtils.read(url.openStream());
        this.mediator = mediator;
    }

    /**
     * A method for retrieving the signature block in a given Jar File for a
     * given signer.
     *
     * @param sjar
     * @param signerName
     * @return SignatureBlock
     * @throws IOException
     */
    public static SignatureBlock getInstance(Mediator mediator, SignedBundle sjar, String signerName) throws IOException {
        SignatureBlock block = null;
        final URL rsaEntry = sjar.getEntry("/META-INF/" + signerName + ".RSA");
        final URL dsaEntry = sjar.getEntry("/META-INF/" + signerName + ".DSA");
        if (dsaEntry == null) {
            if (rsaEntry != null) {
                block = new SignatureBlock(mediator, rsaEntry);
            }
        } else {
            block = new SignatureBlock(mediator, dsaEntry);
        }
        return block;
    }

    /**
     * A method for retrieving signature from the CMS data of a signed jar.<br>
     * the archive id supposed to be signed only once by the signer.
     *
     * @param pkcs7
     * @return byte[]
     * @throws Exception
     */
    public static byte[] getSignatureFromSignedData() {        
        byte[] signature = new byte[] {};
        return signature;
    }
}
