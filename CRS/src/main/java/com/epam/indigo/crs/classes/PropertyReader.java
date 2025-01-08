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

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
	
	private static final String PROPERTIES_FILE = "application.properties";
	
    public static Properties getProperties(String file) throws RuntimeException {
        Properties prop = null;
        try {
            InputStream ins = PropertyReader.class.getClassLoader().getResourceAsStream(file);
            if (ins == null) {
                ins = ClassLoader.getSystemResourceAsStream(file);
            }
            if (ins != null) {
                prop = new Properties();
                prop.load(ins);
            }
        } catch (Exception ex) {
            throw new RuntimeException("PropertyReader::getProperties Could not open the properties file " + file, ex);
        }
        if (prop == null) {
            throw new RuntimeException("PropertyReader::getProperties Could not open the properties file " + file);
        }
        return prop;
    }

    public static Properties getCrsProperties() throws RuntimeException {
        return getProperties(PROPERTIES_FILE);
    }
}
