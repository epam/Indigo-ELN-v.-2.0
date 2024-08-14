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
package com.chemistry.enotebook.signature;

public class SignatureProperties {
    private String finalStatus;
    private String isUploadDocumentsAllowed;
    private String signingMethod;
    private String indigoElnAddress;

    private SignatureProperties(String finalStatus, String isUploadDocumentsAllowed, String signingMethod, String indigoElnAddress) {
        this.finalStatus = finalStatus;
        this.isUploadDocumentsAllowed = isUploadDocumentsAllowed;
        this.signingMethod = signingMethod;
        this.indigoElnAddress = indigoElnAddress;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public String isUploadDocumentsAllowed() {
        return isUploadDocumentsAllowed;
    }

    public String getSigningMethod() {
        return signingMethod;
    }

    public String getIndigoElnAddress() {
        return indigoElnAddress;
    }
}
