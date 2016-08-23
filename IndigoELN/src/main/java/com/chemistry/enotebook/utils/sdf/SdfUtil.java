/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * <p>
 * This file is part of Indigo ELN.
 * <p>
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * <p>
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.utils.sdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

class SdfUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdfUtil.class);

    private SdfUtil() {
    }

    public static boolean isNoStructure(String sduString) {
        String countLine = getNthLine(sduString, 4);
        return countLine.startsWith("0");
    }

    public static boolean isFieldKey(String line) {
        String line1;
        boolean isField;
        int is;
        int ib;
        isField = line.startsWith(">");
        if (isField && line.length() > 1) {
            line1 = line.substring(1).trim();
            is = line1.indexOf("<");
            ib = line1.lastIndexOf(">");
            isField = is >= 0 && ib > 0;
            isField &= is < ib;
        }
        return isField;
    }

    public static String getFieldKey(String line) {
        boolean isField;
        String keyField = "";
        String line1;
        int is;
        int ib;
        isField = line.startsWith(">");
        if (isField && line.length() > 1) {
            line1 = line.substring(1).trim();
            is = line1.indexOf("<");
            ib = line1.lastIndexOf(">");
            isField = is >= 0 && ib > 0;
            isField &= is < ib;
            if (isField)
                keyField = line1.substring(is + 1, ib).trim();
        }
        return keyField;
    }

    public static String getNthLine(String s, int n) {
        String nthLine = "";
        LineNumberReader lnr = new LineNumberReader(new StringReader(s));
        int line = n;
        line--;
        for (int i = 0; i < line; i++) {
            try {
                lnr.readLine();
            } catch (IOException ignored) {
                LOGGER.error("Error occurred.", ignored);
            }
        }
        try {
            nthLine = lnr.readLine().trim();
        } catch (IOException ignored) {
            LOGGER.error("Error occurred.", ignored);
        }
        return nthLine;
    }
    
}
