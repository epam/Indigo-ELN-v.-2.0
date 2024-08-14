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
package com.chemistry.enotebook.signature.database;

public class DatabaseDifferences {

    public static String getQueryWithLimit(String driverClassName, String query, Integer limit) {
        if (driverClassName != null && driverClassName.contains("Oracle")) {
            return "SELECT * FROM ( " + query + " ) WHERE ROWNUM <= " + limit + " ";
        } else {
            return (query + " LIMIT " + limit + " ");
        }
    }
}
