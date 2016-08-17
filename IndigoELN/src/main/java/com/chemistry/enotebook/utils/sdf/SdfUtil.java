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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

class SdfUtil {
    public SdfUtil() {
    }

    public static boolean isNoStructure(String sduString) {
        String countLine = getNthLine(sduString, 4);
        return countLine.startsWith("0");
    }

    public static boolean isFieldKey(String line) {
        String line1 = "";
        boolean isField = false;
        int i_s = -1;
        int i_b = -1;
        if (isField = line.startsWith(">") && line.length() > 1) {
            line1 = line.substring(1).trim();
            i_s = line1.indexOf("<");
            i_b = line1.lastIndexOf(">");
            isField &= i_s >= 0 && i_b > 0;
            isField &= i_s < i_b;
        }
        return isField;
    }

    public static String getFieldKey(String line) {
        boolean isField = false;
        String keyField = "";
        String line1 = "";
        int i_s = -1;
        int i_b = -1;
        if (isField = line.startsWith(">") && line.length() > 1) {
            line1 = line.substring(1).trim();
            i_s = line1.indexOf("<");
            i_b = line1.lastIndexOf(">");
            isField &= i_s >= 0 && i_b > 0;
            isField &= i_s < i_b;
            if (isField)
                keyField = line1.substring(i_s + 1, i_b).trim();
        }
        return keyField;
    }

    public static String getNthLine(String s, int n) {
        String nthLine = "";
        LineNumberReader lnr = new LineNumberReader(new StringReader(s));
        n--;
        for (int i = 0; i < n; i++) {
            try {
                lnr.readLine();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
        try {
            nthLine = lnr.readLine().trim();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return nthLine;
    }
    
    /*
    public static String fieldDeSalt(String fieldValue) {
        String result[] = StringUtil.split(fieldValue, "-");
        String resultedValue = result[0] + "-" + result[1];
        return resultedValue;
    }

    public static int getCompNum(String fieldValue) {
        String result[] = StringUtil.split(fieldValue, "-");
        return Integer.parseInt(result[1]);
    }

    public static void printMaplist(List maplist, String outFile) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
label0:
        for(Iterator it = maplist.iterator(); it.hasNext(); out.write("$$$$\n")) 
            Map sduMap = (Map)it.next();
            String fieldKey = "molStructure";
            String fieldValue = (String)sduMap.get(fieldKey);
            out.write(fieldValue + "\n");
            Set keySet = sduMap.keySet();
            keySet.remove("molStructure");
            Iterator keyIt = keySet.iterator();
            do {
                if(!keyIt.hasNext())
                    continue label0;
                fieldKey = (String)keyIt.next();
                fieldValue = (String)sduMap.get(fieldKey);
                String fieldKey_sd = ">  <" + fieldKey + ">";
                if(fieldValue != null && fieldValue.length() > 0)
                {
                    out.write(fieldKey_sd + "\n");
                    out.write(fieldValue + "\n\n");
                }
            } while(true);
        }

        out.flush();
        out.close();
    }
    public static void printMaplist(List maplist, String outFile, boolean append)
        throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, append)));
label0:
        for(Iterator it = maplist.iterator(); it.hasNext(); out.write("$$$$\n"))
        {
            Map sduMap = (Map)it.next();
            String fieldKey = "molStructure";
            String fieldValue = (String)sduMap.get(fieldKey);
            if(!fieldValue.endsWith("\n"))
                fieldValue = fieldValue + "\n";
            out.write(fieldValue);
            Set keySet = sduMap.keySet();
            keySet.remove("molStructure");
            Iterator keyIt = keySet.iterator();
            do
            {
                if(!keyIt.hasNext())
                    continue label0;
                fieldKey = (String)keyIt.next();
                fieldValue = (String)sduMap.get(fieldKey);
                String fieldKey_sd = ">  <" + fieldKey + ">";
                if(fieldValue != null && fieldValue.length() > 0)
                {
                    out.write(fieldKey_sd + "\n");
                    out.write(fieldValue + "\n\n");
                }
            } while(true);
        }

        out.flush();
        out.close();
    }

    public static void printMaplist(List maplist, String outFileRoot, int max)
        throws IOException
    {
        int nTotal = maplist.size();
        int nOutput = (nTotal - 1) / max + 1;
        Iterator it = maplist.iterator();
        for(int i = 0; i < nOutput; i++)
        {
            String outFile = outFileRoot + "_" + StringUtil.pad3(i) + ".sdf";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
label0:
            for(int j = 0; it.hasNext() && j++ < max; out.write("$$$$\n"))
            {
                Map sduMap = (Map)it.next();
                String fieldKey = "molStructure";
                String fieldValue = (String)sduMap.get(fieldKey);
                out.write(fieldValue + "\n");
                Set keySet = sduMap.keySet();
                keySet.remove("molStructure");
                Iterator keyIt = keySet.iterator();
                do
                {
                    if(!keyIt.hasNext())
                        continue label0;
                    fieldKey = (String)keyIt.next();
                    fieldValue = (String)sduMap.get(fieldKey);
                    String fieldKey_sd = ">  <" + fieldKey + ">";
                    if(fieldValue != null && fieldValue.length() > 0)
                    {
                        System.out.print(fieldKey + ":" + fieldValue);
                        out.write(fieldKey_sd + "\n");
                        out.write(fieldValue + "\n\n");
                    }
                } while(true);
            }

            out.flush();
            out.close();
        }

    }

    public static void printMaplist1(List maplist, String outFileRoot, int max)
        throws IOException
    {
        int nTotal = maplist.size();
        int nOutput = (nTotal - 1) / max + 1;
        Iterator it = maplist.iterator();
        for(int i = 0; i < nOutput; i++)
        {
            String outFile = outFileRoot + "_" + StringUtil.pad3(i) + ".sdf";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
label0:
            for(int j = 0; it.hasNext() && j++ < max; out.write("$$$$\n"))
            {
                Map sduMap = (Map)it.next();
                String fieldKey = "molStructure";
                String fieldValue = (String)sduMap.get(fieldKey);
                out.write(fieldValue);
                Set keySet = sduMap.keySet();
                keySet.remove("molStructure");
                Iterator keyIt = keySet.iterator();
                do
                {
                    if(!keyIt.hasNext())
                        continue label0;
                    fieldKey = (String)keyIt.next();
                    fieldValue = (String)sduMap.get(fieldKey);
                    String fieldKey_sd = ">  <" + fieldKey + ">";
                    if(fieldValue != null && fieldValue.length() > 0)
                    {
                        System.out.print(fieldKey + ":" + fieldValue);
                        out.write(fieldKey_sd + "\n");
                        out.write(fieldValue + "\n\n");
                    }
                } while(true);
            }

            out.flush();
            out.close();
        }

    }

    public static List getValueList(String sdfile, String fieldKey, String valueFile)
        throws IOException
    {
        List valueList = new ArrayList();
        BufferedReader br = new BufferedReader(new FileReader(sdfile));
        do {
            String v_line;
            if((v_line = br.readLine()) == null)
                break;
            String t_line = v_line.trim();
            if(isFieldKey(t_line) && getFieldKey(t_line).equalsIgnoreCase(fieldKey)) {
                StringBuffer valueBuffer = new StringBuffer();
                do {
                    v_line = br.readLine();
                    t_line = v_line.trim();
                    if(t_line.length() == 0)
                        break;
                    valueBuffer.append(t_line).append("\n");
                } while(true);
                valueList.add(valueBuffer.toString().trim());
            }
        } while(true);
        br.close();
        return valueList;
    }

    public static List getValueList11(String sdfile, String fieldKey, String valueFile) throws IOException {
        List valueList = new ArrayList();
        BufferedReader br = new BufferedReader(new FileReader(sdfile));
        do {
            String v_line;
            if((v_line = br.readLine()) == null)
                break;
            String t_line = v_line.trim();
            if(isFieldKey(t_line) && getFieldKey(t_line).equalsIgnoreCase(fieldKey)) {
                StringBuffer valueBuffer = new StringBuffer();
                do {
                    v_line = br.readLine();
                    t_line = v_line.trim();
                    if(t_line.length() == 0)
                        break;
                    valueBuffer.append(t_line).append("\n");
                } while(true);
                valueList.add(valueBuffer.toString().trim().subSequence(0, 11));
            }
        } while(true);
        br.close();
        return valueList;
    }
    */
}
