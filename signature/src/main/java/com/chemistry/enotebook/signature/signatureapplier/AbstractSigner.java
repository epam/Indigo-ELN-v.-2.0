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

import com.chemistry.enotebook.signature.entity.DocumentSignatureBlock;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class AbstractSigner implements SignatureApplier {

    private static final Logger log = LoggerFactory.getLogger(AbstractSigner.class);

    protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss zzz");
    protected String signatureApprovedImage = "approved.jpg";
    protected SignatureVerifier signatureVerifier = new SignatureVerifier();

    public byte[] signDocument(byte[] documentContent, Certificate[] chain,
        DocumentSignatureBlock documentSignatureBlock, ExternalSignature externalSignature, String text, byte[] image) throws Exception {
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        PdfSignatureAppearance sap = getPdfSignatureAppearance(documentContent, documentSignatureBlock, text, image, fout);
        MakeSignature.signDetached(sap, new BouncyCastleDigest(), externalSignature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
        return fout.toByteArray();
    }

    protected PdfSignatureAppearance getPdfSignatureAppearance(byte[] documentContent, DocumentSignatureBlock documentSignatureBlock, String text, byte[] image, ByteArrayOutputStream fout) throws IOException, DocumentException {
        int i = documentSignatureBlock.getIndex();
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

        sap.setReason(documentSignatureBlock.getReason().reason());
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

    public String getSignatureApprovedText(DocumentSignatureBlock signatureTemplateBlock) {
        StringBuilder sb = new StringBuilder();
//        sb.append("Digitally signed by ").append(signatureTemplateBlock.getSigner().getFirstName()).append(" ").append(signatureTemplateBlock.getSigner().getLastName()).append("\n");
        sb.append("\n\n\n\n\n");
        sb.append("Date: ").append(simpleDateFormat.format(System.currentTimeMillis())).append("\n");
        sb.append("Reason: ").append(cutLongText(signatureTemplateBlock.getReason().reason())).append("\n");
        if(!"".equals(signatureTemplateBlock.getComment()) && signatureTemplateBlock.getComment() != null) {
            sb.append("Comment: ").append(cutLongText(signatureTemplateBlock.getComment().replace("\n", " ").replace("\r", " "))).append("\n");
        }
        return sb.toString();
    }

    public String getSignatureRejectedText(DocumentSignatureBlock signatureTemplateBlock) {
        StringBuilder sb = new StringBuilder();
        sb.append("Rejected by ").append(signatureTemplateBlock.getSigner().getFirstName()).append(" ").append(signatureTemplateBlock.getSigner().getLastName()).append("\n");
        sb.append("Date: ").append(simpleDateFormat.format(System.currentTimeMillis())).append("\n");
        sb.append("Comment: ").append(cutLongText(signatureTemplateBlock.getComment())).append("\n");
        return sb.toString();
    }

    protected String cutLongText(String text) {
        if (text.length() <= 35) {
            return text;
        } else if (text.length() > 35 && text.length() < 70) {
            return text.substring(0, 35) + "\n" + text.substring(35);
        } else {
            return text.substring(0, 35) + "\n" + text.substring(35, 70) + "...";
        }
    }

    protected byte[] getImageBytes(String imageName) {
        try {
            return IOUtils.toByteArray(AbstractSigner.class.getClassLoader().getResourceAsStream(imageName));
        } catch (IOException e) {
            log.error("Error loading image: ", e);
        }
        return new byte[0];
    }
}
