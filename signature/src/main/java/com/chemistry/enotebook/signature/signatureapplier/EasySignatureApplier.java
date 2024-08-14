/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature.signatureapplier;

import java.io.ByteArrayInputStream;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.chemistry.enotebook.signature.IndigoSignatureServiceException;
import com.chemistry.enotebook.signature.entity.DocumentSignatureBlock;

import com.itextpdf.text.pdf.security.*;

public class EasySignatureApplier extends AbstractSigner {

    static {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                    }

                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                    }
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    @Override
    public byte[] signDocument(byte[] documentContent, DocumentSignatureBlock signatureTemplateBlock, byte[] keyStorage, String keyStoragePassword) throws Exception {
        signatureVerifier.verifySignatures(documentContent);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        try {
            ks.load(new ByteArrayInputStream(keyStorage), keyStoragePassword.toCharArray());
        } catch (Exception e) {
            throw new IndigoSignatureServiceException("Probably password for certificate is wrong.");
        }

        String alias = ks.aliases().nextElement();

        PrivateKey pk = (PrivateKey) ks.getKey(alias, keyStoragePassword.toCharArray());

        Certificate[] chain = ks.getCertificateChain(alias);

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-1", "BC");

        return signDocument(documentContent, chain, signatureTemplateBlock, es, getSignatureApprovedText(signatureTemplateBlock), getImageBytes(signatureApprovedImage));
    }

}

