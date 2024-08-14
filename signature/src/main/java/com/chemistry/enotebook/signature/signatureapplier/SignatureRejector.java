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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignatureRejector extends EasySignatureApplier {

    private static final Logger log = LoggerFactory.getLogger(SignatureRejector.class);

    public byte[] rejectDocument(byte[] documentContent, DocumentSignatureBlock signatureTemplateBlock) throws Exception {
        signatureVerifier.verifySignatures(documentContent);
        return addRejectionField(documentContent, signatureTemplateBlock);
    }

    @Override
    public byte[] signDocument(byte[] documentContent, DocumentSignatureBlock signatureTemplateBlock, byte[] keyStorage, String keyStoragePassword) throws Exception {
        throw new UnsupportedOperationException("use SignatureRejector.rejectDocument(byte[] documentContent, DocumentSignatureBlock signatureTemplateBlock)");
    }

    public byte[] breakDocumentIntegrity(byte[] documentContent) {
        try {
            PdfReader reader = new PdfReader(documentContent);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, bos);

            PdfContentByte content = stamper.getUnderContent(1);

            Rectangle rectangle = createNewRectangle(10, (int)reader.getPageSize(1).getHeight());
            content.rectangle(rectangle);

            content.beginText();
            content.setFontAndSize(BaseFont.createFont(), 50);
            content.setColorFill(BaseColor.RED);
            content.moveText(100, reader.getPageSize(1).getHeight()/2);
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

    public byte[] addRejectionField(byte[] documentContent, DocumentSignatureBlock documentSignatureBlock) throws IOException, DocumentException {
        int i = documentSignatureBlock.getIndex();
        boolean first = i == 1;

        PdfReader reader = new PdfReader(documentContent);
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, fout, '\0', true);
        PdfFormField field = PdfFormField.createSignature(stamper.getWriter());
        field.setFieldName("Document rejected by " + documentSignatureBlock.getSigner().getFirstName() + " " + documentSignatureBlock.getSigner().getLastName());

        int pageNum = reader.getNumberOfPages();
        if(first) {
            stamper.insertPage(++pageNum, reader.getPageSizeWithRotation(1));
        }
        field.setWidget(createNewRectangle(i, (int) reader.getPageSize(pageNum).getHeight()), PdfAnnotation.HIGHLIGHT_OUTLINE);
        field.setFlags(PdfAnnotation.FLAGS_PRINT);

        stamper.addAnnotation(field, pageNum );
        stamper.close();
        return fout.toByteArray();
    }

}