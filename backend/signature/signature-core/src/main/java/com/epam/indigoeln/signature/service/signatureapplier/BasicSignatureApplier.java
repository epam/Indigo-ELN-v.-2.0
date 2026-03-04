package com.epam.indigoeln.signature.service.signatureapplier;

import com.epam.indigoeln.signature.entity.DocumentSignatureBlockEntity;
import com.epam.indigoeln.signature.exception.InvalidInputException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class BasicSignatureApplier implements SignatureApplier {
    protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss zzz");
    protected String signatureApprovedImage = "approved.jpg";
    protected String signatureRejectedImage = "rejected.jpg";
    protected SignatureVerifier signatureVerifier = new SignatureVerifier();

    @Override
    public byte[] signDocument(byte[] documentContent, DocumentSignatureBlockEntity signatureBlockEntity, byte[] keyStorage, String keyStoragePassword) throws Exception {
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

        return stampDocument(documentContent, signatureBlockEntity, getSignatureApprovedText(signatureBlockEntity), signatureApprovedImage, pk, chain);
    }

    @Override
    public byte[] rejectDocument(byte[] documentContent, DocumentSignatureBlockEntity signatureBlockEntity) throws Exception {
        signatureVerifier.verifySignatures(documentContent);
        return stampDocument(documentContent, signatureBlockEntity, getSignatureRejectedText(signatureBlockEntity), signatureRejectedImage, null, null);
    }

    protected byte[] stampDocument(byte[] documentContent, DocumentSignatureBlockEntity signatureBlockEntity,
                                String stampText, String imagePath, PrivateKey key, Certificate[] chain) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(documentContent);
        int i = signatureBlockEntity.getIndex() + 1;
        boolean first = i == 1;
        PdfStamper stamper = PdfStamper.createSignature(reader, outputStream, '\0', null, !first);
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        int pageNum = reader.getNumberOfPages();

        if (first) {
            stamper.insertPage(++pageNum, reader.getPageSizeWithRotation(1));
        }

        prepareSignatureAppearance(appearance, reader, pageNum, i, stampText, imagePath,
                signatureBlockEntity.getReason().getTitle(), key, chain);

        if (key == null) {
            closeSignatureAppearance(appearance);
        } else {
            stamper.close();
        }

        return outputStream.toByteArray();
    }

    protected void prepareSignatureAppearance(PdfSignatureAppearance appearance, PdfReader reader, int pageNum, int index, String layer2Text,
                                           String imagePath, String reason, PrivateKey key, Certificate[] chain) throws IOException {
        Calendar signDate = Calendar.getInstance();

        if (key != null) {
            appearance.setCrypto(key, chain, null, PdfSignatureAppearance.SELF_SIGNED);
            appearance.setRender(PdfSignatureAppearance.SignatureRenderNameAndDescription);
        } else {
            PdfDictionary dic = new PdfDictionary();
            dic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
            dic.put(PdfName.M, new PdfDate(signDate));
            appearance.setCryptoDictionary(dic);
        }

        appearance.setVisibleSignature(createNewRectangle(index, (int) reader.getPageSize(pageNum).getHeight()), pageNum, "Signature " + index);
        appearance.setLayer2Text(layer2Text);
        appearance.setImage(Image.getInstance(getImageBytes(imagePath)));
        appearance.setReason(reason);
        appearance.setSignDate(signDate);
    }

    protected void closeSignatureAppearance(PdfSignatureAppearance appearance) throws IOException {
        Map<PdfName, Integer> exc = new HashMap<>();
        exc.put(PdfName.CONTENTS, 10);
        appearance.preClose(exc);
        PdfDictionary update = new PdfDictionary();
        update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
        appearance.close(update);
    }

    protected static Rectangle createNewRectangle(int position, int pageHeight) {
        int stampHeight = 75;
        int stampWidth = 400;

        int llx = 100;
        int lly = pageHeight - ((position-1) * stampHeight + 250);
        int urx = llx + stampWidth;
        int ury = lly + stampHeight;
        return new Rectangle(llx, lly, urx, ury);
    }

    protected String getSignatureApprovedText(DocumentSignatureBlockEntity signatureTemplateBlock) {
        StringBuilder sb = new StringBuilder();
//        sb.append("Digitally signed by ").append(signatureTemplateBlock.getSigner().getFirstName()).append(" ").append(signatureTemplateBlock.getSigner().getLastName()).append("\n");
        sb.append("\n\n\n\n\n");
        sb.append("Date: ").append(simpleDateFormat.format(System.currentTimeMillis())).append("\n");
        sb.append("Reason: ").append(cutLongText(signatureTemplateBlock.getReason().getTitle())).append("\n");
        if(!"".equals(signatureTemplateBlock.getComment()) && signatureTemplateBlock.getComment() != null) {
            sb.append("Comment: ").append(cutLongText(signatureTemplateBlock.getComment().replace("\n", " ").replace("\r", " "))).append("\n");
        }
        return sb.toString();
    }

    protected String getSignatureRejectedText(DocumentSignatureBlockEntity signatureTemplateBlock) {
        return "Rejected by " + signatureTemplateBlock.getUser().getFullName() + "\n" +
                "Date: " + simpleDateFormat.format(System.currentTimeMillis()) + "\n" +
                "Comment: " + cutLongText(signatureTemplateBlock.getComment()) + "\n";
    }

    protected String cutLongText(String text) {
        if (text == null || text.length() <= 35) {
            return text;
        } else if (text.length() < 70) {
            return text.substring(0, 35) + "\n" + text.substring(35);
        } else {
            return text.substring(0, 35) + "\n" + text.substring(35, 70) + "...";
        }
    }

    protected byte[] getImageBytes(String imageName) {
        try {
            return BasicSignatureApplier.class.getClassLoader().getResourceAsStream(imageName).readAllBytes();
        } catch (IOException e) {
            log.error("Error loading image: ", e);
        }
        return new byte[0];
    }
}
