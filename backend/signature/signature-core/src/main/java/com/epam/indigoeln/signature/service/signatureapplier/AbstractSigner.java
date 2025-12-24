package com.epam.indigoeln.signature.service.signatureapplier;

import com.epam.indigoeln.signature.entity.DocumentSignatureBlockEntity;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public abstract class AbstractSigner implements SignatureApplier {

    protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss zzz");
    protected String signatureApprovedImage = "approved.jpg";
    protected SignatureVerifier signatureVerifier = new SignatureVerifier();

    public byte[] signDocument(byte[] documentContent, Certificate[] chain,
                               DocumentSignatureBlockEntity DocumentSignatureBlockEntity, ExternalSignature externalSignature, String text, byte[] image) throws Exception {
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        PdfSignatureAppearance sap = getPdfSignatureAppearance(documentContent, DocumentSignatureBlockEntity, text, image, fout);
        MakeSignature.signDetached(sap, new BouncyCastleDigest(), externalSignature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
        return fout.toByteArray();
    }

    protected PdfSignatureAppearance getPdfSignatureAppearance(byte[] documentContent, DocumentSignatureBlockEntity DocumentSignatureBlockEntity, String text, byte[] image, ByteArrayOutputStream fout) throws IOException, DocumentException {
        int i = DocumentSignatureBlockEntity.getIndex();
        boolean first = i == 1;

        PdfReader reader = new PdfReader(documentContent);

        PdfStamper stamper = PdfStamper.createSignature(reader, fout, '\0', null, !first);
        PdfSignatureAppearance sap = stamper.getSignatureAppearance();

        int pageNum = reader.getNumberOfPages();
        if(first) {
            stamper.insertPage(++pageNum, reader.getPageSizeWithRotation(1));
        }
        sap.setVisibleSignature(createNewRectangle(i, (int) reader.getPageSize(pageNum).getHeight()), pageNum, "Signature " + i);
        sap.setLayer2Text(text);
        sap.setImage(Image.getInstance(image));

        sap.setReason(DocumentSignatureBlockEntity.getReason().getTitle());
        sap.setSignDate(Calendar.getInstance());
        sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION);
        return sap;
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
        if (text.length() <= 35) {
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
