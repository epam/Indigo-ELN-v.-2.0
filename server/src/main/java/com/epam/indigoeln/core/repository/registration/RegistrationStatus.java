/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.repository.registration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class RegistrationStatus {

    private Status status;

    private String message;

    private Date date;

    private Map<String, String> compoundNumbers;

    private Map<String, String> conversationalBatchNumbers;

    private RegistrationStatus(Status status, String message, Date date) {
        this.status = status;
        this.message = message;
        this.date = date;
        compoundNumbers = new HashMap<>();
        conversationalBatchNumbers = new HashMap<>();
    }

    private RegistrationStatus(Status status) {
        this(status, null, null);
    }

    public static RegistrationStatus passed(Date date) {
        return new RegistrationStatus(Status.PASSED, null, date);
    }

    public static RegistrationStatus inProgress() {
        return new RegistrationStatus(Status.IN_PROGRESS);
    }

    public static RegistrationStatus failed() {
        return new RegistrationStatus(Status.FAILED);
    }

    public static RegistrationStatus failed(String message) {
        return new RegistrationStatus(Status.FAILED, message, null);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public Map<String, String> getCompoundNumbers() {
        return compoundNumbers;
    }

    public void setCompoundNumbers(Map<String, String> compoundNumbers) {
        this.compoundNumbers = compoundNumbers;
    }

    public Map<String, String> getConversationalBatchNumbers() {
        return conversationalBatchNumbers;
    }

    public void setConversationalBatchNumbers(Map<String, String> conversationalBatchNumbers) {
        this.conversationalBatchNumbers = conversationalBatchNumbers;
    }

    public enum Status {
        PASSED, FAILED, IN_PROGRESS, IN_CHECK
    }
}
