package com.epam.indigoeln.signature.service.signatureapplier;

import com.epam.indigoeln.signature.entity.DocumentSignatureBlockEntity;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractSigner implements SignatureApplier {

    protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss zzz");
    protected String signatureApprovedImage = "approved.jpg";
    protected String signatureRejectedImage = "rejected.jpg";
    protected SignatureVerifier signatureVerifier = new SignatureVerifier();

    public byte[] stampDocument(byte[] documentContent, DocumentSignatureBlockEntity signatureBlockEntity,
                                String stampText, String imagePath, PrivateKey key, Certificate[] chain) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(documentContent);
        int i = signatureBlockEntity.getIndex();
        boolean first = i == 1;
        PdfStamper stamper = PdfStamper.createSignature(reader, outputStream, null, null, !first);
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

    public void prepareSignatureAppearance(PdfSignatureAppearance appearance, PdfReader reader, int pageNum, int index, String layer2Text,
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

        appearance.setVisibleSignature(createNewRectangle(index, (int) reader.getPageSize(pageNum).getHeight()),
                pageNum, "Signature " + index);
        appearance.setLayer2Text(layer2Text);
        appearance.setImage(Image.getInstance(getImageBytes(imagePath)));
        appearance.setReason(reason);
        appearance.setSignDate(signDate);
    }

    public void closeSignatureAppearance(PdfSignatureAppearance appearance) throws IOException {
        Map<PdfName, Integer> exc = new HashMap<>();
        exc.put(PdfName.CONTENTS, 10);
        appearance.preClose(exc);
        PdfDictionary update = new PdfDictionary();
        update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
        appearance.close(update);
    }

    public static Rectangle createNewRectangle(int position, int pageHeight) {
        int stampHeight = 75;
        int stampWidth = 400;

        int llx = 100;
        int lly = pageHeight - ((position-1) * stampHeight + 250);
        int urx = llx + stampWidth;
        int ury = lly + stampHeight;
        return new Rectangle(llx, lly, urx, ury);
    }

    public String getSignatureApprovedText(DocumentSignatureBlockEntity signatureTemplateBlock) {
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

    public String getSignatureRejectedText(DocumentSignatureBlockEntity signatureTemplateBlock) {
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
            return AbstractSigner.class.getClassLoader().getResourceAsStream(imageName).readAllBytes();
        } catch (IOException e) {
            log.error("Error loading image: ", e);
        }
        return new byte[0];
    }
}
