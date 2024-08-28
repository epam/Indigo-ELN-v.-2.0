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

import com.chemistry.enotebook.signature.Util;
import org.apache.commons.codec.digest.DigestUtils;

import javax.json.JsonObject;
import java.util.UUID;

public abstract class JsonRepresentable {
    public abstract JsonObject asJson();

    public static UUID getUuid(JsonObject row, String propertyName) {
        Object uuidObject = null;
        if(row.containsKey(propertyName)) {
            uuidObject = row.getString(propertyName);
        }
        return Util.uuidFromObject(uuidObject);
    }

    public static String getString(JsonObject row, String propertyName) {
        if(row.containsKey(propertyName)) {
            return row.getString(propertyName);
        }
        return null;
    }

    public static Boolean getBoolean(JsonObject row, String propertyName) {
        if(row.containsKey(propertyName)) {
            return row.getBoolean(propertyName);
        }
        return null;
    }

    public static String getMd5Password(JsonObject row) {
        String propertyName = "password";
        if(row.containsKey(propertyName)) {
            return DigestUtils.md5Hex(row.getString(propertyName));
        }
        return null;
    }
}
