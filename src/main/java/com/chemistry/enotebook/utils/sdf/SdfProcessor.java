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
import java.util.*;

public class SdfProcessor {
    public static String STRUCT_KEY = "molStructure";
    protected List sduStringList;
    protected List maplist;
    protected Properties fieldMapping;
    protected boolean skipNostruct;
    protected SdfModifier sm;

    public SdfProcessor(String sdfile, String mappingFile, SdfModifier sm, boolean skipNostruct, int start, int stop) {
        sduStringList = new ArrayList();
        maplist = new ArrayList();
        this.skipNostruct = skipNostruct;
        this.sm = sm;
        fieldMapping = calFieldMapping(mappingFile);
        try {
            BufferedReader br = new BufferedReader(new FileReader(sdfile));
            sduStringList = calSduStringList(br, start, stop);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error trying to open sdf File.");
            System.exit(0);
        }
    }

    public SdfProcessor(String sdfile, Properties fieldMap, SdfModifier sm, boolean skipNostruct, int start, int stop) {
        sduStringList = new ArrayList();
        maplist = new ArrayList();
        this.skipNostruct = skipNostruct;
        this.sm = sm;
        fieldMapping = fieldMap;
        try {
            BufferedReader br = new BufferedReader(new FileReader(sdfile));
            sduStringList = calSduStringList(br, start, stop);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error trying to open sdf File.");
            System.exit(0);
        }
    }

    public SdfProcessor(String sdfile, String mappingFile, SdfModifier sm, boolean skipNostruct) {
        this(sdfile, mappingFile, sm, skipNostruct, -1, -1);
    }

    public SdfProcessor(String sdfile, Properties fieldMap, SdfModifier sm, boolean skipNostruct) {
        this(sdfile, fieldMap, sm, skipNostruct, -1, -1);
    }

    public SdfProcessor(String sdfile, String mappingFile, boolean skipNostruct, int start, int stop) {
        this(sdfile, mappingFile, new SdfModifier(), skipNostruct, start, stop);
    }

    public SdfProcessor(String sdfile, Properties fieldMap, boolean skipNostruct, int start, int stop) {
        this(sdfile, fieldMap, new SdfModifier(), skipNostruct, start, stop);
    }

    public SdfProcessor(String sdfile, String mappingFile, boolean skipNostruct) {
        this(sdfile, mappingFile, new SdfModifier(), skipNostruct);
    }

    public SdfProcessor(String sdfile, Properties fieldMap, boolean skipNostruct) {
        this(sdfile, fieldMap, new SdfModifier(), skipNostruct);
    }

    public SdfProcessor(String sdfile, String mappingFile, SdfModifier sm, int start, int stop) {
        this(sdfile, mappingFile, sm, false, start, stop);
    }

    public SdfProcessor(String sdfile, Properties fieldMap, SdfModifier sm, int start, int stop) {
        this(sdfile, fieldMap, sm, false, start, stop);
    }

    public SdfProcessor(String sdfile, String mappingFile, SdfModifier sm) {
        this(sdfile, mappingFile, sm, false);
    }

    public SdfProcessor(String sdfile, Properties fieldMap, SdfModifier sm) {
        this(sdfile, fieldMap, sm, false);
    }

    public SdfProcessor(String sdfile, String mappingFile, int start, int stop) {
        this(sdfile, mappingFile, new SdfModifier(), false, start, stop);
    }

    public SdfProcessor(String sdfile, Properties fieldMap, int start, int stop) {
        this(sdfile, fieldMap, new SdfModifier(), false, start, stop);
    }

    public SdfProcessor(String sdfile, String mappingFile) {
        this(sdfile, mappingFile, new SdfModifier(), false);
    }

    public SdfProcessor(String sdfile, Properties fieldMap) {
        this(sdfile, fieldMap, new SdfModifier(), false);
    }

    public SdfProcessor(String sdfile, int nl2Check) {
        sduStringList = new ArrayList();
        maplist = new ArrayList();
        skipNostruct = false;
        sm = new SdfModifier();
        Properties defaultMapping = new Properties();
        try {
            BufferedReader br = new BufferedReader(new FileReader(sdfile));
            sduStringList = calSduStringList(br);
            br.close();
            BufferedReader br_t = new BufferedReader(new FileReader(sdfile));
            int i = 0;
            String line = "";
            String keyField = "";
            do {
                if (i++ >= nl2Check || (line = br_t.readLine()) == null)
                    break;
                keyField = SdfUtil.getFieldKey(line);
                if (keyField.length() > 0)
                    defaultMapping.put(keyField, keyField);
            } while (true);
            br_t.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("Error trying to open properties file: " + fnfe);
            System.exit(0);
        } catch (IOException ioe) {
            System.out.println("Error during I/0 of properties file: " + ioe);
            System.exit(0);
        }
        fieldMapping = defaultMapping;
    }

    public SdfProcessor(String sdfile) {
        sduStringList = new ArrayList();
        maplist = new ArrayList();
        skipNostruct = false;
        sm = new SdfModifier();
        Properties defaultMapping = new Properties();
        try {
            BufferedReader br = new BufferedReader(new FileReader(sdfile));
            sduStringList = calSduStringList(br);
            br.close();
            BufferedReader br_t = new BufferedReader(new FileReader(sdfile));
            int i = 0;
            String line = "";
            String keyField = "";
            do {
                if ((line = br_t.readLine()) == null)
                    break;
                keyField = SdfUtil.getFieldKey(line);
                if (keyField.length() > 0)
                    defaultMapping.put(keyField, keyField);
            } while (true);
            br_t.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("Error trying to open properties file: " + fnfe);
            System.exit(0);
        } catch (IOException ioe) {
            System.out.println("Error during I/0 of properties file: " + ioe);
            System.exit(0);
        }
        fieldMapping = defaultMapping;
    }

    public SdfProcessor() {
        sduStringList = new ArrayList();
        maplist = new ArrayList();
    }

    public static String getMolStructure(String sduString) throws IOException {
        java.io.InputStream ips = new ByteArrayInputStream(sduString.getBytes());
        InputStreamReader ipr = new InputStreamReader(ips);
        BufferedReader br = new BufferedReader(ipr);
        StringBuffer molStructure = new StringBuffer();
        String line = "";
        String tLine = "";
        do {
            line = br.readLine();
            tLine = line.trim();
            if (!tLine.endsWith("END") || !tLine.startsWith("M")) {
                molStructure.append(line + "\n");
            } else {
                molStructure.append(line + "\n");
                return molStructure.toString();
            }
        } while (true);
    }

    public static String getOriFieldValue(String sduString, String oriField) throws IOException {
        java.io.InputStream ips = new ByteArrayInputStream(sduString.getBytes());
        InputStreamReader ipr = new InputStreamReader(ips);
        BufferedReader br = new BufferedReader(ipr);
        StringBuffer fieldValue = new StringBuffer();
        String line = "";
        String tLine = "";
        do {
            if ((line = br.readLine()) == null)
                break;
            tLine = line.trim();
            if (!SdfUtil.isFieldKey(tLine) || tLine.toUpperCase().indexOf(oriField.toUpperCase()) < 0)
                continue;
            String vLine = "";
            String tvLine = "";
            do {
                vLine = br.readLine();
                tvLine = vLine.trim();
                if (tvLine.length() == 0)
                    break;
                fieldValue.append(vLine).append("\n");
            } while (true);
            break;
        } while (true);
        return fieldValue.toString().trim();
    }

    protected Properties calFieldMapping(String mappingFile) {
        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(mappingFile);
            props.load(fis);
            fis.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("Error trying to open properties file: " + fnfe);
            System.exit(0);
        } catch (IOException ioe) {
            System.out.println("Error during I/0 of properties file: " + ioe);
            System.exit(0);
        }
        return props;
    }

    public Properties getFieldMapping() {
        return fieldMapping;
    }

    public Set getFieldKeys() {
        return fieldMapping.keySet();
    }

    protected List calSduStringList(BufferedReader br)
            throws IOException {
        String line = "";
        StringBuffer sduStringBuffer = new StringBuffer();
        while ((line = br.readLine()) != null)
            if (!line.trim().equals("$$$$"))
                sduStringBuffer.append(line).append("\n");
            else if (!skipNostruct) {
                sduStringList.add(sduStringBuffer.toString());
                sduStringBuffer = new StringBuffer();
            } else {
                String sduString0 = sduStringBuffer.toString();
                if (SdfUtil.isNoStructure(sduString0)) {
                    sduStringBuffer = new StringBuffer();
                } else {
                    sduStringList.add(sduStringBuffer.toString());
                    sduStringBuffer = new StringBuffer();
                }
            }
        return sduStringList;
    }

    protected List calSduStringList(BufferedReader br, int start, int stop)
            throws IOException {
        if (start < 0)
            return calSduStringList(br);
        String line = "";
        StringBuffer sduStringBuffer = new StringBuffer();
        int nMol = 1;
        do {
            if ((line = br.readLine()) == null || nMol > stop)
                break;
            if (!line.trim().equals("$$$$")) {
                if (nMol >= start)
                    sduStringBuffer.append(line).append("\n");
            } else {
                if (nMol >= start)
                    if (!skipNostruct) {
                        sduStringList.add(sduStringBuffer.toString());
                        sduStringBuffer = new StringBuffer();
                    } else {
                        String sduString0 = sduStringBuffer.toString();
                        if (SdfUtil.isNoStructure(sduString0)) {
                            sduStringBuffer = new StringBuffer();
                        } else {
                            sduStringList.add(sduStringBuffer.toString());
                            sduStringBuffer = new StringBuffer();
                        }
                    }
                nMol++;
            }
        } while (true);
        return sduStringList;
    }

    public List getSduStringList() {
        return sduStringList;
    }

    public String[] getMolArray() {
        int listSize = sduStringList.size();
        String molArray[] = new String[listSize];
        for (int i = 0; i < listSize; i++) {
            String sduStringList_i = (String) sduStringList.get(i);
            molArray[i] = sduStringList_i.substring(0, sduStringList_i.indexOf(Mol2000.M_END) + Mol2000.M_END.length());
        }

        return molArray;
    }

    public List getMaplist() throws IOException {
        Map molMap;
        for (Iterator it = sduStringList.iterator(); it.hasNext(); maplist.add(molMap)) {
            String sduString = (String) it.next();
            String molStructure = getMolStructure(sduString);
            molMap = getSimpleMolMap(molStructure, sduString);
        }

        return maplist;
    }

    public List getMaplist(String expandsOn) throws IOException {
        for (Iterator sduIt = sduStringList.iterator(); sduIt.hasNext(); ) {
            String sduString = (String) sduIt.next();
            String molStructure = getMolStructure(sduString);
            Map molMap = getSimpleMolMap(molStructure, sduString);
            TreeMap expandedMolMap[] = expandMolMap((TreeMap) molMap, expandsOn);
            int i = 0;
            while (i < expandedMolMap.length) {
                maplist.add(expandedMolMap[i]);
                i++;
            }
        }

        return maplist;
    }

    public Map getKeyStructureMap(String key) throws Exception {
        Map keyStructureMap = new TreeMap();
        String molStructure = "";
        String keyOri = "";
        String keyValue = "";
        if (!getFieldKeys().contains(key))
            throw new Exception("The provided key does not exist.");
        for (Iterator sduIt = sduStringList.iterator(); sduIt.hasNext(); keyStructureMap.put(keyValue, molStructure)) {
            String sduString = (String) sduIt.next();
            try {
                molStructure = getMolStructure(sduString);
            } catch (IOException e) {
                new Exception("Mol String cannot be obtained from sdu.", e);
            }
            keyOri = ((String) fieldMapping.get(key)).trim();
            try {
                keyValue = getFieldValue(sduString, keyOri, key);
            } catch (IOException e) {
                new Exception("key value cannot be obtained from sdu.", e);
            }
        }
        return keyStructureMap;
    }

    public Map getSimpleMolMap(String molStructure, String sduString) throws IOException {
        TreeMap molMap = new TreeMap();
        Set fieldsSet = fieldMapping.keySet();
        molMap.put(STRUCT_KEY, molStructure);
        String fieldKey;
        String fieldValue;
        for (Iterator keyIt = fieldsSet.iterator(); keyIt.hasNext(); molMap.put(fieldKey, fieldValue)) {
            fieldKey = (String) keyIt.next();
            String fieldOri = ((String) fieldMapping.get(fieldKey)).trim();
            if (fieldOri.startsWith("\"") && fieldOri.endsWith("\""))
                fieldValue = fieldOri.substring(1, fieldOri.length() - 1);
            else
                fieldValue = getFieldValue(sduString, fieldOri, fieldKey);
        }

        return molMap;
    }

    public Map getAddedMolMap(String molStructure, String sduString, Map addedMap) throws IOException {
        TreeMap molMap = new TreeMap();
        Set fieldsSet = fieldMapping.keySet();
        molMap.put(STRUCT_KEY, molStructure);
        String fieldKey;
        String fieldValue;
        for (Iterator keyIt = fieldsSet.iterator(); keyIt.hasNext(); molMap.put(fieldKey, fieldValue)) {
            fieldKey = (String) keyIt.next();
            String fieldOri = ((String) fieldMapping.get(fieldKey)).trim();
            if (fieldOri.startsWith("\"") && fieldOri.endsWith("\""))
                fieldValue = fieldOri.substring(1, fieldOri.length() - 1);
            else
                fieldValue = getFieldValue(sduString, fieldOri, fieldKey);
        }

        Set addedSet = addedMap.keySet();
        String addedKey;
        String addedValue;
        for (Iterator addedIt = addedSet.iterator(); addedIt.hasNext(); molMap.put(addedKey, addedValue)) {
            addedKey = (String) addedIt.next();
            addedValue = (String) addedMap.get(addedKey);
        }

        return molMap;
    }

    public Map getNewMolMap(String molStructure, Map addedMap) throws IOException {
        TreeMap molMap = new TreeMap();
        molMap.put(STRUCT_KEY, molStructure);
        molMap.putAll(addedMap);
        return molMap;
    }

    protected String getFieldValue(String sduString, String oriField, String mappedField) throws IOException {
        String oriFieldValue = getOriFieldValue(sduString, oriField);
        String sFieldValue = sm.fieldValueModify(mappedField, oriFieldValue);
        return sFieldValue;
    }

    protected String getAFieldKey(String ExpandsOn) {
        Set fieldsSet = fieldMapping.keySet();
        for (Iterator keyIt = fieldsSet.iterator(); keyIt.hasNext(); ) {
            String fieldKey = (String) keyIt.next();
            String field = ((String) fieldMapping.get(fieldKey)).trim();
            if (field.equalsIgnoreCase(ExpandsOn))
                return fieldKey;
        }
        return null;
    }

    protected TreeMap[] expandMolMap(TreeMap molMap, String expandsOn) {
        String aFieldKey = getAFieldKey(expandsOn);
        String fieldValue = (String) molMap.get(aFieldKey);
        StringTokenizer st = new StringTokenizer(fieldValue, "\n");
        int nt = st.countTokens();
        TreeMap molMapArray[] = new TreeMap[nt];
        label0:
        for (int i = 0; st.hasMoreTokens(); i++) {
            molMapArray[i] = (TreeMap) molMap.clone();
            String fieldValue_i = st.nextToken();
            Set fieldsSet = fieldMapping.keySet();
            Iterator keyIt = fieldsSet.iterator();
            do {
                if (!keyIt.hasNext())
                    continue label0;
                String fieldKey = (String) keyIt.next();
                String field = ((String) fieldMapping.get(fieldKey)).trim();
                if (field.equalsIgnoreCase(expandsOn))
                    molMapArray[i].put(fieldKey, fieldValue_i);
            } while (true);
        }

        return molMapArray;
    }
}
