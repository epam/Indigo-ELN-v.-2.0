package com.epam.indigoeln.signature.service.signatureapplier;

import com.epam.indigoeln.signature.entity.DocumentSignatureBlockEntity;
import com.epam.indigoeln.signature.exception.InvalidInputException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

@Slf4j
@ApplicationScoped
public class EasySignatureApplier extends AbstractSigner {

    static {
        TrustManager[] trustAllCerts = new TrustManager[] { // !!!
                new javax.net.ssl.X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            throw new RuntimeException(e); // !!!
        }
    }

    @Override
    public byte[] signDocument(byte[] documentContent, DocumentSignatureBlockEntity signatureTemplateBlock, byte[] keyStorage, String keyStoragePassword) throws Exception {
        signatureVerifier.verifySignatures(documentContent);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        try {
            ks.load(new ByteArrayInputStream(keyStorage), keyStoragePassword.toCharArray());
        } catch (Exception e) {
            throw new InvalidInputException("Probably password for certificate is wrong: " + e.getMessage(), e);
        }

        String alias = ks.aliases().nextElement();

        PrivateKey pk = (PrivateKey) ks.getKey(alias, keyStoragePassword.toCharArray());

        Certificate[] chain = ks.getCertificateChain(alias);

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-1", "BC");

        return signDocument(documentContent, chain, signatureTemplateBlock, es, getSignatureApprovedText(signatureTemplateBlock), getImageBytes(signatureApprovedImage));
    }

    public byte[] rejectDocument(byte[] documentContent, DocumentSignatureBlockEntity signatureTemplateBlock) throws Exception {
        signatureVerifier.verifySignatures(documentContent);
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        PdfSignatureAppearance sap = getPdfSignatureAppearance(documentContent, signatureTemplateBlock, getSignatureRejectedText(signatureTemplateBlock),
                getImageBytes(signatureRejectedImage), fout);
        return fout.toByteArray();
        //return addRejectionField(documentContent, signatureTemplateBlock);
    }

    public byte[] breakDocumentIntegrity(byte[] documentContent) {
        try {
            PdfReader reader = new PdfReader(documentContent);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, bos);

            PdfContentByte content = stamper.getUnderContent(1);

            Rectangle rectangle = createNewRectangle(10, (int) reader.getPageSize(1).getHeight());
            content.rectangle(rectangle);

            content.beginText();
            content.setFontAndSize(BaseFont.createFont(), 50);
            content.setColorFill(BaseColor.RED);
            content.moveText(100, reader.getPageSize(1).getHeight() / 2);
            content.newlineShowText("roughly modified");
            content.endText();

            bos.close();
            stamper.close();
            documentContent = bos.toByteArray();
        } catch (Exception e) {
            log.error("Error breaking document integrity: ", e);
        }
        return documentContent;
    }

    public byte[] addRejectionField(byte[] documentContent, DocumentSignatureBlockEntity DocumentSignatureBlockEntity) throws IOException, DocumentException {
        int i = DocumentSignatureBlockEntity.getIndex();
        boolean first = i == 1;

        PdfReader reader = new PdfReader(documentContent);
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, fout, '\0', true);
        PdfFormField field = PdfFormField.createSignature(stamper.getWriter());
        field.setFieldName("Document rejected by " + DocumentSignatureBlockEntity.getUser().getFullName());

        int pageNum = reader.getNumberOfPages();
        if (first) {
            stamper.insertPage(++pageNum, reader.getPageSizeWithRotation(1));
        }
        field.setWidget(createNewRectangle(i, (int) reader.getPageSize(pageNum).getHeight()), PdfAnnotation.HIGHLIGHT_OUTLINE);
        field.setFlags(PdfAnnotation.FLAGS_PRINT);

        stamper.addAnnotation(field, pageNum);
        stamper.close();
        return fout.toByteArray();
    }
}
