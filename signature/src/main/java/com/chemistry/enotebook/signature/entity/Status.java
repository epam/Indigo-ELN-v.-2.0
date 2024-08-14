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
package com.chemistry.enotebook.signature.entity;

import java.util.HashMap;
import java.util.Map;

public enum Status {
    SUBMITTED(1),
    SIGNING(2),
    SIGNED(3),
    REJECTED(4),
    WAITING(5),
    CANCELLED(6),
    ARCHIVING(7),
    ARCHIVED(8);

    private int code;

    public int code() {
        return code;
    }

    Status(int code) {
        this.code = code;
    }

    public static Map<Integer, Status> statuses;

    static {
        statuses = new HashMap<Integer, Status>();
        statuses.put(SUBMITTED.code, SUBMITTED);
        statuses.put(SIGNING.code, SIGNING);
        statuses.put(SIGNED.code, SIGNED);
        statuses.put(REJECTED.code, REJECTED);
        statuses.put(WAITING.code, WAITING);
        statuses.put(CANCELLED.code, CANCELLED);
        statuses.put(ARCHIVING.code, ARCHIVING);
        statuses.put(ARCHIVED.code, ARCHIVED);
    }
}
