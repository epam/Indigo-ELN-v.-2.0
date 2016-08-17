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

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Mol2000 {
    public static String M_CHARGE = "M  CHG";
    public static String M_RADICAL = "M  RAD";
    public static String M_ISOTOPE = "M  ISO";
    public static String M_END = "M  END";
    private String molString;

    public Mol2000(String molString) {
        this.molString = molString;
    }

    public Mol2000(File molFile) throws Exception {
        getString(molFile);
    }

    private static String getString(String fromFile) throws IOException {
        File f = new File(fromFile);
        BufferedReader br = new BufferedReader(new FileReader(f));
        char charbuffer[] = new char[(int) f.length()];
        for (; br.ready(); br.read(charbuffer)) ;
        br.close();
        return new String(charbuffer);
    }

    public static String getString(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        char charbuffer[] = new char[(int) f.length()];
        for (; br.ready(); br.read(charbuffer)) ;
        br.close();
        return new String(charbuffer);
    }

    public Map getAtomPropertyMap(String prop) throws Exception {
        Map atomPropMap = new HashMap();
        if (molString.indexOf(M_END) <= 0)
            throw new Exception("Bad Mol");
        StringReader sr = new StringReader(molString);
        BufferedReader br = new BufferedReader(sr);
        String line = null;
        do {
            if ((line = br.readLine()) == null)
                break;
            int idxProp = line.indexOf(prop);
            if (idxProp == 0) {
                line = line.substring(prop.length());
                int mapSize = Integer.parseInt(line.substring(0, 3).trim());
                line = line.substring(3);
                int i = 0;
                while (i < mapSize) {
                    String b8 = line.substring(i * 8, (i + 1) * 8);
                    int atomNumber = Integer.parseInt(b8.substring(0, 4).trim());
                    int propValue = Integer.parseInt(b8.substring(4).trim());
                    atomPropMap.put(new Integer(atomNumber), new Integer(propValue));
                    i++;
                }
            }
        } while (true);
        br.close();
        sr.close();
        return atomPropMap;
    }
}
