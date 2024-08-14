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

public enum Reason {
    AUTHOR(1, "I am the author"),
    WHITNESS(2, "Witness");

    private int id;
    private String reason;

    Reason(int id, String reason) {
        this.id = id;
        this.reason = reason;
    }

    public int id() {
        return id;
    }

    public String reason() {
        return reason;
    }

    public static Map<Integer, Reason> reasonMap = new HashMap<Integer, Reason>();
    static {
        reasonMap.put(AUTHOR.id, AUTHOR);
        reasonMap.put(WHITNESS.id, WHITNESS);
    }
}
