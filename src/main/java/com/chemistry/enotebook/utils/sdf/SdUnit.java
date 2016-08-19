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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SdUnit implements Serializable, Externalizable {
    static final long serialVersionUID = 42L;
    boolean is3D;
    int numAtoms;
    boolean upperCase;
    private String molPortion;
    private Map<String, String> infoPortion;
    private List<String> keyList;
    private boolean valid;
    private String validString;

    public SdUnit(String molecule, boolean molFilePortionOnly) {
        this(molecule, true, molFilePortionOnly);
    }

    public SdUnit(String molecule, boolean allKeysToUpperCase,
                  boolean molFilePortionOnly) {
        molPortion = "";
        infoPortion = null;
        keyList = new ArrayList<>();
        valid = true;
        validString = "OK";
        is3D = false;
        numAtoms = -1;
        upperCase = true;
        init(molecule, allKeysToUpperCase, molFilePortionOnly);
    }

    public SdUnit() {
        molPortion = "";
        infoPortion = null;
        keyList = new ArrayList<>();
        valid = true;
        validString = "OK";
        is3D = false;
        numAtoms = -1;
        upperCase = true;
    }

    private static String createConsistentLineTermination(String in) throws IOException {
        StringReader sr = new StringReader(in);
        BufferedReader br = new BufferedReader(sr);
        String line;
        String out = "";
        while ((line = br.readLine()) != null) {
            out += line + "\n";
        }
        return out;
    }

    static String validateDetail(String mol) {
        String returnString = "OK";
        int numAtoms;
        try {
            if (mol.indexOf("M  END") <= 0)
                return "Does not contain \"M  END\"";
            StringReader sr = new StringReader(mol);
            BufferedReader br = new BufferedReader(sr);
            String line = br.readLine();
            if (line == null)
                return "Molecule has too few lines";
            line = br.readLine();
            if (line == null)
                return "Molecule has too few lines";
            line = br.readLine();
            if (line == null)
                return "Molecule has too few lines";
            line = br.readLine();
            if (line == null)
                return "Molecule has too few lines";
            int impossibleNumber = -1;
            int numBonds;
            try {
                numAtoms = Integer.parseInt(line.substring(0, 3).trim());
            } catch (NumberFormatException e) {
                return "Number of atoms is invalid";
            }
            if (numAtoms <= impossibleNumber)
                return "Number of atoms is invalid";
            try {
                numBonds = Integer.parseInt(line.substring(3, 6).trim());
            } catch (NumberFormatException e) {
                return "Number of bonds is invalid";
            }
            if (numAtoms <= impossibleNumber)
                return "Number of bonds is invalid";
            double improbablyLargeValue = 10000.0D;
            for (int x = 0; x <= numAtoms - 1; ++x) {
                line = br.readLine();
                if (line == null)
                    return "Incorrect number of atoms and/or bonds";
                double test;
                try {
                    test = Double.parseDouble(line.substring(0, 10).trim());
                } catch (NumberFormatException e) {
                    return "Invalid X coordinate";
                }
                if (line.indexOf(".") != 5)
                    return "X coordinate decimal in wrong place";
                if (Math.abs(test) >= improbablyLargeValue)
                    return "Invalid X coordinate";
                try {
                    test = Double.parseDouble(line.substring(10, 20).trim());
                } catch (NumberFormatException e) {
                    return "Invalid Y coordinate";
                }
                if (Math.abs(test) >= improbablyLargeValue)
                    return "Invalid Y coordinate";
                try {
                    test = Double.parseDouble(line.substring(20, 30).trim());
                } catch (NumberFormatException e) {
                    return "Invalid Z coordinate";
                }
                if (Math.abs(test) >= improbablyLargeValue)
                    return "Invalid Z coordinate";
                if (test == 0.0D)
                    continue;
                returnString = "OK 3D";
            }
            int impossibleAtomNumber = 0;
            for (int x = 0; x <= numBonds - 1; ++x) {
                line = br.readLine();
                if (line == null)
                    return "Incorrect number of atoms and/or bonds";
                if (line.startsWith("M")) {
                    System.out.println(mol);
                    return "Incorrect number of atoms and/or bonds";
                }
                if (line.length() < 12)
                    return "Invalid Bond Line - too short";
                int test;
                try {
                    test = Integer.parseInt(line.substring(0, 3).trim());
                } catch (NumberFormatException e) {
                    return "Invalid Bond Line - invalid atom1 number";
                }
                if (test <= impossibleAtomNumber)
                    return "Invalid Bond Line - invalid atom1 number";
                try {
                    test = Integer.parseInt(line.substring(3, 6).trim());
                } catch (NumberFormatException e) {
                    return "Invalid Bond Line - invalid atom2 number";
                }
                if (test <= impossibleAtomNumber)
                    return "Invalid Bond Line - invalid atom2 number";
            }
            line = br.readLine();
            if (line == null)
                return "Molecule has too few lines";
        } catch (Exception e) {
            return "Unexpected error parsing molecule";
        }
        if (returnString.startsWith("OK"))
            returnString = returnString + " " + Integer.toString(numAtoms);
        return returnString;
    }

    public void init(String molecule, boolean allKeysToUpperCase, boolean molFilePortionOnly) {
        try {
            if (molecule == null) {
                valid = false;
                validString = "Input molecule was NULL";
                return;
            }
            upperCase = allKeysToUpperCase;
            if (molecule.contains("\r"))
                molecule = createConsistentLineTermination(molecule);
            if (molFilePortionOnly) {
                molecule = molecule.substring(0, molecule.indexOf("M  END") + 6);
                molecule = molecule + "\n\n$$$$";
            }
            if (!molecule.contains("M  END") || !molecule.contains("$$$$")) {
                valid = false;
                validString = "Does not contain \"M  END\" or \"$$$$\"";
                molPortion = "Not a valid molecule!";
            }
            if (molecule.contains("M  END"))
                setMol(molecule.substring(0, molecule.indexOf("M  END") + 6) + "\n");
            validString = validateDetail(molPortion);
            if (!validString.startsWith("OK"))
                valid = false;
            infoPortion = parseInfo(molecule, keyList);
        } catch (IllegalArgumentException e) {
            valid = false;
            if (validString.startsWith("OK"))
                validString = e.getMessage();
            else
                validString = validString + " AND " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getInfoPortion() {
        return infoPortion;
    }

    public String getValue(String key) {
        if (valid)
            return infoPortion.get(key.toUpperCase());
        else
            return "";
    }

    public void setValue(String key, String value) {
        if (valid)
            if (value == null || "".equals(value.trim())) {
                removeKey(key);
                infoPortion.remove(key.toUpperCase());
            } else {
                infoPortion.put(key.toUpperCase(), value);
                replaceKey(key);
            }
    }

    private void removeKey(String key) {
        int len = keyList.size();
        for (int x = len - 1; x >= 0; x--) {
            String s = keyList.get(x);
            if (s.equalsIgnoreCase(key))
                keyList.remove(x);
        }

    }

    private void replaceKey(String key) {
        int len = keyList.size();
        boolean replaced = false;
        int x = 0;
        do {
            if (x > len - 1)
                break;
            String s = keyList.get(x);
            if (s.equalsIgnoreCase(key)) {
                keyList.set(x, key);
                replaced = true;
                break;
            }
            x++;
        } while (true);
        if (!replaced)
            keyList.add(key);
    }

    public void removeItem(String key) {
        if (valid) {
            infoPortion.remove(key.toUpperCase());
            removeKey(key);
        }
    }

    public String getMol() {
        if (valid)
            return molPortion;
        else
            return "";
    }

    public void setMol(String mol) {
        String mol1;
        if (mol.contains("M  END")) {
            mol1 = mol.substring(0, mol.indexOf("M  END") + "M  END".length())
                    + "\n";
        } else {
            mol1 = mol;
        }
        String tmp = validateDetail(mol1);
        if (tmp.startsWith("OK")) {
            if (tmp.contains("3D"))
                is3D = true;
            String num = tmp.substring(tmp.lastIndexOf(" ") + 1).trim();
            try {
                numAtoms = Integer.parseInt(num);
            } catch (NumberFormatException nfe) {
                numAtoms = -1;
            }
            tmp = "OK";
        }
        if (!"OK".equals(tmp))
            if (validString.startsWith("OK"))
                validString = tmp;
            else
                validString = validString + " AND UPON MOL MODIFICATION " + tmp;
        if (valid)
            try {
                molPortion = createConsistentLineTermination(mol);
            } catch (Exception e) {
            }
    }

    public boolean isValidMol() {
        return valid;
    }

    public String getInvalidDescription() {
        return validString;
    }

    public String[] getKeys() {
        Object[] o = keyList.toArray();
        String[] out = new String[o.length];
        for (int x = 0; x <= o.length - 1; x++)
            out[x] = (String) o[x];

        return out;
    }

    public String getUnit() {
        String[] keys = getKeys();
        StringBuilder out = new StringBuilder();
        for (int x = 0; x <= keys.length - 1; x++) {
            String value = getValue(keys[x]);
            out.append(">  <");
            out.append(keys[x]);
            out.append(">\n");
            out.append(value);
            out.append("\n\n");
        }

        if (keys.length == 0) {
            out.append("\n");
        }
        out.append("$$$$\n");
        out.insert(0, molPortion);
        return out.toString();
    }

    private Map<String, String> parseInfo(String sdInfo, List<String> origNames) {
        Map<String, String> out = new HashMap<>();
        try {
            String attrPortion = sdInfo.substring(sdInfo.indexOf("M  END") + 6,
                    sdInfo.indexOf("$$$$") + 4).trim();
            String thisName;
            String thisOrigName;
            String thisValue;
            do {
                if (attrPortion.indexOf(">") != 0
                        || attrPortion.indexOf("<") <= 0)
                    break;
                attrPortion = attrPortion
                        .substring(attrPortion.indexOf("<") + 1);
                thisName = attrPortion.substring(0, attrPortion.indexOf(">"))
                        .trim();
                thisOrigName = thisName;
                thisName = thisName.toUpperCase();
                attrPortion = attrPortion.substring(attrPortion.indexOf("\n"))
                        .trim();
                if (attrPortion.contains(">"))
                    thisValue = attrPortion.substring(0,
                            attrPortion.indexOf("\n>")).trim();
                else if (attrPortion.trim().indexOf(">") == 0
                        && attrPortion.contains("<"))
                    thisValue = "";
                else
                    thisValue = attrPortion.substring(0,
                            attrPortion.indexOf("$$$$")).trim();
                if (!"".equals(thisValue.trim())) {
                    if (out.containsKey(thisName)) {
                        String tmp = out.get(thisName);
                        if (tmp != null && !"".equals(tmp)) {
                            out.put(thisName, tmp + "\n" + thisValue);
                        } else {
                            out.put(thisName, thisValue);
                            origNames.add(thisOrigName);
                        }
                    } else {
                        out.put(thisName, thisValue);
                        origNames.add(thisOrigName);
                    }
                }
                if (attrPortion.indexOf(">  <") != 0
                        && attrPortion.indexOf("> <") != 0
                        && attrPortion.contains("\n>"))
                    attrPortion = attrPortion.substring(attrPortion
                            .indexOf("\n>") + 1);
            } while (true);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing sdFile attributes", e);
        }
        return out;
    }

    @Override
    public String toString() {
        return getUnit();
    }

    public String toString(String lineTermination) throws IOException {
        StringBuilder out;
        StringReader sr = new StringReader(getUnit());
        BufferedReader br = new BufferedReader(sr);
        out = new StringBuilder();
        for (String line; (line = br.readLine()) != null; ) {
            out.append(line);
            out.append(lineTermination);
        }

        br.close();
        sr.close();
        return out.toString();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeExternal(out);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        readExternal(in);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4000);
        for (int byt; (byt = in.read()) != -1; )
            baos.write(byt);

        baos.close();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gis = new GZIPInputStream(bais);
        ByteArrayOutputStream unit = new ByteArrayOutputStream();
        int chunk;
        while ((chunk = gis.read()) >= 0)
            unit.write(chunk);
        unit.close();
        String info = new String(unit.toByteArray());
        bais.close();
        gis.close();
        init(info, true, false);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        String text = getUnit();
        byte[] btext = text.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(btext.length);
        GZIPOutputStream gzo = new GZIPOutputStream(baos);
        gzo.write(btext);
        gzo.finish();
        baos.close();
        byte[] bytes = baos.toByteArray();
        out.write(bytes);
        out.flush();
    }


}
