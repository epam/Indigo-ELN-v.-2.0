/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of CRS.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.epam.indigo.crs.classes;

public class FullCompoundInfo extends CompoundInfo {
	
	private static final long serialVersionUID = -5258996699592541932L;
	
	private String compoundNumber;
    private String conversationalBatchNumber;
    private CompoundRegistrationStatus registrationStatus;

    public FullCompoundInfo() {
    }

    public FullCompoundInfo(String data, String batchNumber, String casNumber, String saltCode, double saltEquivalents,
                            String comments, String hazardComments, String storageComments, String stereoIsomerCode,
                            String compoundNumber, String conversationalBatchNumber, CompoundRegistrationStatus registrationStatus) {
        this.data = data;
        this.batchNumber = batchNumber;
        this.casNumber = casNumber;
        this.saltCode = saltCode;
        this.saltEquivalents = saltEquivalents;
        this.comments = comments;
        this.hazardComments = hazardComments;
        this.storageComments = storageComments;
        this.stereoIsomerCode = stereoIsomerCode;
        this.compoundNumber = compoundNumber;
        this.conversationalBatchNumber = conversationalBatchNumber;
        this.registrationStatus = registrationStatus;
    }

    public String toString() {
        return String.format("[%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s]", data, batchNumber, casNumber, saltCode, saltEquivalents,
                comments, hazardComments, storageComments, stereoIsomerCode, compoundNumber, conversationalBatchNumber);
    }
    public String getCompoundNumber() {
        return compoundNumber;
    }

    public void setCompoundNumber(String compoundNumber) {
        this.compoundNumber = compoundNumber;
    }

    public String getConversationalBatchNumber() {
        return conversationalBatchNumber;
    }

    public void setConversationalBatchNumber(String conversationalBatchNumber) {
        this.conversationalBatchNumber = conversationalBatchNumber;
    }

    public void setRegistrationStatus(CompoundRegistrationStatus registrationStatus) {
		this.registrationStatus = registrationStatus;
	}

    public CompoundRegistrationStatus getRegistrationStatus() {
		return registrationStatus;
	}
}
