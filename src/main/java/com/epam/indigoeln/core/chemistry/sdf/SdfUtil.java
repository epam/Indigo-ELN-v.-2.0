/****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************************/
package com.epam.indigoeln.core.chemistry.sdf;

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
            if (isField) {
                keyField = line1.substring(is + 1, ib).trim();
            }
        }
        return keyField;
    }

    public static String getNthLine(String s, int n) {
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
            String result = lnr.readLine();
            return result != null ? result.trim() : "";
        } catch (IOException ignored) {
            LOGGER.error("Error occurred.", ignored);
        }
        return "";
    }
}
