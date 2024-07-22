/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.chemistry.sdf;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.epam.indigoeln.core.util.EqualsUtil.doubleEqZero;

public final class SdUnit implements Serializable, Externalizable {

    private static final String OK = "OK";
    private static final String OK_3D = "OK 3D";

    static final long serialVersionUID = 42L;
    private static final String MOLECULE_PATTERN = "M  END";
    private boolean is3D;
    private int numAtoms;
    private boolean upperCase;
    private String molPortion;
    private Map<String, String> infoPortion;
    private List<String> keyList;
    private boolean valid;
    private String validString;
    private static final Log LOGGER = LogFactory.getLog(SdUnit.class);

    public SdUnit(String molecule, boolean molFilePortionOnly) {
        this(molecule, true,
                molFilePortionOnly);
    }

    SdUnit(String molecule, boolean allKeysToUpperCase,
           boolean molFilePortionOnly) {
        molPortion = "";
        infoPortion = null;
        keyList = new ArrayList<>();
        valid = true;
        validString = OK;
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
        validString = OK;
        is3D = false;
        numAtoms = -1;
        upperCase = true;
    }

    private static String createConsistentLineTermination(String in) throws IOException {
        StringReader sr = new StringReader(in);
        BufferedReader br = new BufferedReader(sr);
        String line;
        StringBuilder outStringBuilder = new StringBuilder();
        while ((line = br.readLine()) != null) {
            outStringBuilder.append(line).append("\n");
        }
        return outStringBuilder.toString();
    }

    private static String validateDetail(String mol) {
        if (!StringUtils.contains(mol, MOLECULE_PATTERN)) {
            return "Does not contain \"M  END\"";
        }

        String returnString;

        int numAtoms;
        int numBonds;

        try (StringReader sr = new StringReader(mol); BufferedReader br = new BufferedReader(sr)) {
            String line = null;

            for (int i = 0; i < 4; ++i) {
                line = br.readLine();
                if (Objects.isNull(line)) {
                    return "Molecule has too few lines";
                }
            }

            int impossibleNumber = -1;

            numAtoms = validateDetailGetInteger(line, 0, 3);
            if (numAtoms <= impossibleNumber) {
                return "Number of atoms is invalid";
            }

            numBonds = validateDetailGetInteger(line, 3, 6);
            if (numBonds <= impossibleNumber) {
                return "Number of bonds is invalid";
            }

            returnString = validateDetailCoordinates(numAtoms, br);
            if (!StringUtils.equals(returnString, OK_3D)) {
                return returnString;
            }

            returnString = validateDetailAtoms(numBonds, br);
            if (Objects.nonNull(returnString)) {
                return returnString;
            } else {
                returnString = OK_3D;
            }

            line = br.readLine();

            if (Objects.isNull(line)) {
                return "Molecule has too few lines";
            }
        } catch (IOException e) {
            LOGGER.error("Unexpected error parsing molecule", e);
            return "Unexpected error parsing molecule";
        }

        return returnString + " " + Integer.toString(numAtoms);
    }

    private static String validateDetailCoordinates(int numAtoms, BufferedReader br) throws IOException {
        String returnString = OK;
        double improbablyLargeValue = 10000.0D;

        for (int x = 0; x <= numAtoms - 1; ++x) {
            String line = br.readLine();

            if (Objects.isNull(line)) {
                return "Incorrect number of atoms and/or bonds";
            }

            if (line.indexOf(".") != 5) {
                return "X coordinate decimal in wrong place";
            }

            double test;

            test = validateDetailGetDouble(line, 0, 10);
            if (Math.abs(test) >= improbablyLargeValue) {
                return "Invalid X coordinate";
            }

            test = validateDetailGetDouble(line, 10, 20);
            if (Math.abs(test) >= improbablyLargeValue) {
                return "Invalid Y coordinate";
            }

            test = validateDetailGetDouble(line, 20, 30);
            if (Math.abs(test) >= improbablyLargeValue) {
                return "Invalid Z coordinate";
            }

            if (doubleEqZero(test)) {
                continue;
            }

            returnString = OK_3D;
        }

        return returnString;
    }

    private static String validateDetailAtoms(int numBonds, BufferedReader br) throws IOException {
        int impossibleAtomNumber = 0;

        for (int x = 0; x <= numBonds - 1; ++x) {
            String line = br.readLine();

            if (Objects.isNull(line) || StringUtils.startsWith(line, "M")) {
                return "Incorrect number of atoms and/or bonds";
            }

            if (line.length() < 12) {
                return "Invalid Bond Line - too short";
            }

            int test;

            test = validateDetailGetInteger(line, 0, 3);
            if (test <= impossibleAtomNumber) {
                return "Invalid Bond Line - invalid atom1 number";
            }

            test = validateDetailGetInteger(line, 3, 6);
            if (test <= impossibleAtomNumber) {
                return "Invalid Bond Line - invalid atom2 number";
            }
        }

        return null;
    }

    private static int validateDetailGetInteger(String s, int beginIndex, int endIndex) {
        try {
            return Integer.parseInt(s.substring(beginIndex, endIndex).trim());
        } catch (Exception e) {
            LOGGER.trace("validateDetailGetInteger: " + s + ", " + beginIndex + ", " + endIndex, e);
        }
        return Integer.MIN_VALUE;
    }

    private static double validateDetailGetDouble(String s, int beginIndex, int endIndex) {
        try {
            return Double.parseDouble(s.substring(beginIndex, endIndex).trim());
        } catch (Exception e) {
            LOGGER.trace("validateDetailGetDouble: " + s + ", " + beginIndex + ", " + endIndex, e);
        }
        return Double.MAX_VALUE;
    }

    public void init(String molecule, boolean allKeysToUpperCase, boolean molFilePortionOnly) {
        try {
            if (molecule == null) {
                valid = false;
                validString = "Input molecule was NULL";
                return;
            }
            upperCase = allKeysToUpperCase;
            String mol = molecule;
            if (mol.contains("\r")) {
                mol = createConsistentLineTermination(mol);
            }
            if (molFilePortionOnly) {
                mol = mol.substring(0, mol.indexOf(MOLECULE_PATTERN) + 6);
                mol = mol + "\n\n$$$$";
            }
            if (!mol.contains(MOLECULE_PATTERN) || !mol.contains("$$$$")) {
                valid = false;
                validString = "Does not contain \"M  END\" or \"$$$$\"";
                molPortion = "Not a valid molecule!";
            }
            if (mol.contains(MOLECULE_PATTERN)) {
                setMol(mol.substring(0, mol.indexOf(MOLECULE_PATTERN) + 6) + "\n");
            }
            validString = validateDetail(molPortion);
            if (!validString.startsWith(OK)) {
                valid = false;
            }
            infoPortion = parseInfo(mol, keyList);
        } catch (IllegalArgumentException e) {
            LOGGER.error("SDUnit init error", e);
            valid = false;
            if (validString.startsWith(OK)) {
                validString = e.getMessage();
            } else {
                validString = validString + " AND " + e.getMessage();
            }
        } catch (Exception e) {
            LOGGER.error("SDUnit init error", e);
        }
    }

    public Map<String, String> getInfoPortion() {
        return infoPortion;
    }

    public String getValue(String key) {
        if (valid) {
            return infoPortion.get(key.toUpperCase(Locale.getDefault()));
        } else {
            return "";
        }
    }

    public void setValue(String key, String value) {
        if (valid) {
            if (value == null || "".equals(value.trim())) {
                removeKey(key);
                infoPortion.remove(key.toUpperCase(Locale.getDefault()));
            } else {
                infoPortion.put(key.toUpperCase(Locale.getDefault()), value);
                replaceKey(key);
            }
        }
    }

    private void removeKey(String key) {
        int len = keyList.size();
        for (int x = len - 1; x >= 0; x--) {
            String s = keyList.get(x);
            if (s.equalsIgnoreCase(key)) {
                keyList.remove(x);
            }
        }

    }

    private void replaceKey(String key) {
        int len = keyList.size();

        boolean replaced = false;

        int x = 0;

        do {
            if (x > len - 1) {
                break;
            }

            String s = keyList.get(x);

            if (s.equalsIgnoreCase(key)) {
                keyList.set(x, key);
                replaced = true;
            }

            x++;
        } while (!replaced);

        if (!replaced) {
            keyList.add(key);
        }
    }

    public void removeItem(String key) {
        if (valid) {
            infoPortion.remove(key.toUpperCase(Locale.getDefault()));
            removeKey(key);
        }
    }

    public String getMol() {
        if (valid) {
            return molPortion;
        } else {
            return "";
        }
    }

    public void setMol(String mol) {
        String mol1;
        if (mol.contains(MOLECULE_PATTERN)) {
            mol1 = mol.substring(0, mol.indexOf(MOLECULE_PATTERN) + MOLECULE_PATTERN.length())
                    + "\n";
        } else {
            mol1 = mol;
        }
        String tmp = validateDetail(mol1);
        if (tmp.startsWith(OK)) {
            if (tmp.contains("3D")) {
                is3D = true;
            }
            String num = tmp.substring(tmp.lastIndexOf(" ") + 1).trim();
            try {
                numAtoms = Integer.parseInt(num);
            } catch (NumberFormatException nfe) {
                numAtoms = -1;
            }
            tmp = OK;
        }
        if (!OK.equals(tmp)) {
            if (validString.startsWith(OK)) {
                validString = tmp;
            }
        } else {
            validString = validString + " AND UPON MOL MODIFICATION " + tmp;
        }
        if (valid) {
            try {
                molPortion = createConsistentLineTermination(mol);
            } catch (Exception e) {
                LOGGER.error("SDUnit setMol error", e);
            }
        }
    }

    public boolean isValidMol() {
        return valid;
    }

    public String getInvalidDescription() {
        return validString;
    }

    private String[] getKeys() {
        Object[] o = keyList.toArray();
        String[] out = new String[o.length];
        for (int x = 0; x <= o.length - 1; x++) {
            out[x] = (String) o[x];
        }

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
            String attrPortion = sdInfo.substring(sdInfo.indexOf(MOLECULE_PATTERN) + 6, sdInfo.indexOf("$$$$") + 4).trim();

            String thisName;
            String thisOrigName;
            String thisValue;

            do {
                if (attrPortion.indexOf(">") != 0 || attrPortion.indexOf("<") < 1) {
                    break;
                }

                attrPortion = attrPortion.substring(attrPortion.indexOf("<") + 1);
                thisName = attrPortion.substring(0, attrPortion.indexOf(">")).trim();

                thisOrigName = thisName;
                thisName = thisName.toUpperCase(Locale.getDefault());

                attrPortion = attrPortion.substring(attrPortion.indexOf("\n")).trim();

                if (attrPortion.contains(">")) {
                    thisValue = attrPortion.substring(0, attrPortion.indexOf("\n>")).trim();
                } else if (attrPortion.trim().indexOf(">") == 0 && attrPortion.contains("<")) {
                    thisValue = "";
                } else {
                    thisValue = attrPortion.substring(0, attrPortion.indexOf("$$$$")).trim();
                }

                parseInfoAddOrigNames(out, origNames, thisOrigName, thisName, thisValue);

                if (attrPortion.indexOf(">  <") != 0 && attrPortion.indexOf("> <") != 0
                        && attrPortion.contains("\n>")) {
                    attrPortion = attrPortion.substring(attrPortion.indexOf("\n>") + 1);
                }
            } while (true);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing sdFile attributes", e);
        }

        return out;
    }

    private void parseInfoAddOrigNames(Map<String, String> out, List<String> origNames,
                                       String thisOrigName, String thisName, String thisValue) {
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
        for (int byt; (byt = in.read()) != -1; ) {
            baos.write(byt);
        }
        baos.close();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gis = new GZIPInputStream(bais);
        ByteArrayOutputStream unit = new ByteArrayOutputStream();
        int chunk;
        while ((chunk = gis.read()) >= 0) {
            unit.write(chunk);
        }
        unit.close();
        String info = new String(unit.toByteArray(), StandardCharsets.UTF_8);
        bais.close();
        gis.close();
        init(info, true, false);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        String text = getUnit();
        byte[] btext = text.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(btext.length);
        GZIPOutputStream gzo = new GZIPOutputStream(baos);
        gzo.write(btext);
        gzo.finish();
        baos.close();
        byte[] bytes = baos.toByteArray();
        out.write(bytes);
        out.flush();
    }

    public boolean isIs3D() {
        return is3D;
    }

    public void setIs3D(boolean is3D) {
        this.is3D = is3D;
    }

    public int getNumAtoms() {
        return numAtoms;
    }

    public void setNumAtoms(int numAtoms) {
        this.numAtoms = numAtoms;
    }

    public boolean isUpperCase() {
        return upperCase;
    }

    public void setUpperCase(boolean upperCase) {
        this.upperCase = upperCase;
    }
}

