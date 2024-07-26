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
package com.chemistry.enotebook.signature.email;

import com.chemistry.enotebook.signature.IndigoSignatureServiceException;
import com.chemistry.enotebook.signature.Util;
import com.chemistry.enotebook.signature.entity.Document;
import com.chemistry.enotebook.signature.entity.DocumentSignatureBlock;
import com.chemistry.enotebook.signature.entity.Status;
import com.chemistry.enotebook.signature.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Collection;
import java.util.Properties;
import java.util.TreeSet;

public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private static final String TIMEOUT = "300000"; // 5 minutes (5 * 60 * 1000)

    private String emailFrom;
    private String emailUser;
    private String emailPassword;
    private String emailSmtpHost;
    private String emailSmtpPort;
    private String emailAuth;
    private String emailSsl;
    private String emailTls;

    public EmailSender(String emailFrom, String emailUser, String emailPassword, String emailSmtpHost, String emailSmtpPort, String emailAuth, String emailSsl, String emailTls) {
        this.emailFrom = emailFrom;
        this.emailUser = emailUser;
        this.emailPassword = emailPassword;
        this.emailSmtpHost = emailSmtpHost;
        this.emailSmtpPort = emailSmtpPort;
        this.emailAuth = emailAuth;
        this.emailSsl = emailSsl;
        this.emailTls = emailTls;
    }

    public void sendEmailToNextSignerInSeparateThread(final Document document, final String indigoElnAddress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendEmailToNextSigner(document, indigoElnAddress);
            }
        }).start();
    }

    private void sendEmailToNextSigner(Document document, String indigoElnAddress) {
        try {
            Collection<DocumentSignatureBlock> documentSignatureBlocks = document.getDocumentSignatureBlocks();
            documentSignatureBlocks = new TreeSet<DocumentSignatureBlock>(documentSignatureBlocks);
            for(DocumentSignatureBlock documentSignatureBlock : documentSignatureBlocks) {
                if(documentSignatureBlock.getStatus().equals(Status.SIGNING)) {
                    User signer = documentSignatureBlock.getSigner();
                    sendEmail(signer.getEmail(), "Request to sign document",
                            "Dear " + signer.getFirstName() + " " + signer.getLastName() + ",\n" +
                            "\n" +
                            "You have been asked for signing document " + document.getDocumentName() + ". Please go to " + indigoElnAddress + " and sign or reject the document.\n" +
                            "\n" +
                            "Sincerely yours,\n" +
                            "Indigo Signature Service team.");
                    break;
                }
            }
        } catch(Exception e) {
            log.error("Error sending email: ", e);
        }
    }

    private void sendEmail(String email, String subject, String messageBody) {
        sendEmail(email, subject, messageBody, null);
    }

    private void sendEmail(String email, String subject, String messageText, Multipart attachement) {
        final String username = this.emailUser;
        final String password = this.emailPassword;

        Properties props = new Properties();

        props.put("mail.smtp.connectiontimeout", TIMEOUT);
        props.put("mail.smtp.timeout", TIMEOUT);
        props.put("mail.smtp.writetimeout", TIMEOUT);

        props.put("mail.smtp.auth", emailAuth);
        props.put("mail.smtp.host", emailSmtpHost);
        props.put("mail.smtp.port", emailSmtpPort);

        // If send causes "Could not convert socket to TLS" exception,
        // add props.put("mail.smtp.ssl.trust", emailSmtpHost); for SSL or TLS

        if (Boolean.valueOf(emailTls)) {
            props.put("mail.smtp.starttls.enable", "true");
        } else if (Boolean.valueOf(emailSsl)) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        Session session;

        if (Boolean.valueOf(emailAuth)) {
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        } else {
            session = Session.getInstance(props);
        }

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(subject);

            if (attachement != null) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(messageText);

                attachement.addBodyPart(messageBodyPart, 0);

                message.setContent(attachement);
            } else {
                message.setText(messageText);
            }

            Transport.send(message);

            log.info("Email to " + email + " has been sent.");
        } catch (Exception e) {
            log.error("Error sending email to " + email + ": " + e.getMessage());
        }
    }

    public void sendEmailToDocumentAuthorAboutRejectionInSeparateThread(final Document document, final User user, final String comment) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendEmailToDocumentAuthorAboutRejection(document, user, comment);
            }
        }).start();
    }

    private void sendEmailToDocumentAuthorAboutRejection(Document document, User rejector, String comment) {
        String rejectorFullName = rejector.getFirstName() + " " + rejector.getLastName();
        StringBuilder sb = new StringBuilder();
        sendEmail(document.getAuthor().getEmail(), "IndigoSignatureService - Signature Request Declined",
                sb.append("Dear ").append(document.getAuthor().getFirstName()).append(" ").append(document.getAuthor().getLastName()).append(",\n")
                        .append("\n")
                        .append(rejectorFullName).append(" has declined your request to digitally sign the document:\n")
                        .append("\n")
                        .append(document.getDocumentName()).append("\n")
                        .append("\n")
                        .append("with comment: ").append(comment).append("\n")
                        .append("\n")
                        .append("Please contact ").append(rejectorFullName).append(" by ").append(rejector.getEmail()).append(" with any questions you may have as to why they cannot sign the document at this time.\n")
                        .append("\n")
                        .append("Do not reply to this message. This e-mail message has been sent from an unmonitored e-mail address. We are unable to respond to any replies sent to this e-mail address.\n")
                        .append("\n")
                        .append("Sincerely yours,\n")
                        .append("Indigo Signature Service team.").toString());
    }

    public void sendEmailToDocumentAuthorAboutRemovingBrokenDocumentInSeparateThread(final Document document) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendEmailToDocumentAuthorAboutRemovingBrokenDocument(document);
            }
        }).start();
    }

    public void sendEmailToDocumentAuthorAboutRemovingBrokenDocument(Document document) {
        Multipart multipart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();

        DataSource source = new ByteArrayDataSource(document.getContent(), "application/pdf");

        try {
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(document.getDocumentName());
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            multipart = null;
            log.error("Error sending message", e);
        }

        StringBuilder sb = new StringBuilder();

        sendEmail(document.getAuthor().getEmail(), "Indigo Signature Service - document " + document.getDocumentName() + " was removed",
                sb.append("Dear ").append(document.getAuthor().getFirstName()).append(" ").append(document.getAuthor().getLastName()).append(",\n")
                        .append("\n")
                        .append("The document ").append(document.getDocumentName()).append(" which you submitted at ").append(Util.convertToStringDate(document.getCreationDate())).append(" was unexpectedly changed after the last signature and it will be removed from the Indigo signature application.\n")
                        .append("\n")
                        .append("Do not reply to this message. This e-mail message has been sent from an unmonitored e-mail address. We are unable to respond to any replies sent to this e-mail address.\n")
                        .append("\n")
                        .append("Sincerely yours,\n")
                        .append("Indigo Signature Service team.").toString(),
                multipart);
    }
}
