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

import java.io.Serializable;

public class CompoundInfo implements Serializable {
	
	private static final long serialVersionUID = -5072865946150154136L;
	
	protected String data;
    protected String batchNumber;
    protected String casNumber;
    protected String saltCode;
    protected double saltEquivalents;
    protected String comments;
    protected String hazardComments;
    protected String storageComments;
    protected String stereoIsomerCode;

    public String toString() {
        return String.format("[%s,%s,%s,%s,%s,%s,%s,%s,%s]", data, batchNumber, casNumber, saltCode, saltEquivalents,
                comments, hazardComments, storageComments, stereoIsomerCode);
    }

    public String getStereoIsomerCode() {
        return stereoIsomerCode;
    }

    public void setStereoIsomerCode(String stereoIsomerCode) {
        this.stereoIsomerCode = stereoIsomerCode;
    }
    public void setData(String data) {
        this.data = data;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public void setCasNumber(String casNumber) {
        this.casNumber = casNumber;
    }

    public void setSaltCode(String saltCode) {
        this.saltCode = saltCode;
    }

    public void setSaltEquivalents(double saltEquivalents) {
        this.saltEquivalents = saltEquivalents;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setHazardComments(String hazardComments) {
        this.hazardComments = hazardComments;
    }

    public void setStorageComments(String storageComments) {
        this.storageComments = storageComments;
    }

    public String getData() {
        return data;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public String getCasNumber() {
        return casNumber;
    }

    public String getSaltCode() {
        return saltCode;
    }

    public double getSaltEquivalents() {
        return saltEquivalents;
    }

    public String getComments() {
        return comments;
    }

    public String getHazardComments() {
        return hazardComments;
    }

    public String getStorageComments() {
        return storageComments;
    }


}
