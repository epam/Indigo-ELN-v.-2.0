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
/*
 * GUIDUtil.java
 *
 * Created on Aug 9, 2004
 *
 *
 */
package com.chemistry.enotebook.experiment.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Purpose: Static Utility to generate GUIDs for NotebookPages
 *
 * @date Aug 9, 2004
 */
public class GUIDUtil {
    private static final Log log = LogFactory.getLog(GUIDUtil.class);
    // initialise the secure random instance
    private static final java.security.SecureRandom seeder = new java.security.SecureRandom();
    /**
     * Cached per JVM server IP.
     */
    private static String hexServerIP = null;

    /**
     * Same as generateGUID(Object o, String extraSeedInfo) except that extraSeedInfo is provided by class canonical name
     *
     * @param o
     * @return
     */
    public static final String generateGUID(Object o) {
        return GUIDUtil.generateGUID(o, o.getClass().getCanonicalName());
    }

    /**
     * A 32 byte GUID generator (Globally Unique ID). These artificial keys SHOULD <strong>NOT </strong> be seen by the user, not
     * even touched by the DBA but with very rare exceptions, just manipulated by the database and the programs.
     * <p>
     * Usage: Add an id field (type java.lang.String) to your EJB, and add setId(XXXUtil.generateGUID(this)); to the ejbCreate
     * method.
     *
     * @param o             = object for which we want to generate the GUID.
     * @param extraSeedInfo - a string or null in which you want to add to the GUID generation to increase randomness in the response
     * @return
     */
    public static final String generateGUID(Object o, String extraSeedInfo) {
        StringBuffer tmpBuffer = new StringBuffer(16);

        if (hexServerIP == null) {
            java.net.InetAddress localInetAddress = null;

            try {
                // get the inet address
                localInetAddress = java.net.InetAddress.getLocalHost();
            } catch (java.net.UnknownHostException uhe) {
                log.warn("generateGUID: Could not get the local IP address using InetAddress.getLocalHost()!", uhe);

                return null;
            }
            // //System.out.println("generateGUID.localInetAddress = " + localInetAddress);
            byte[] serverIP = localInetAddress.getAddress();
            hexServerIP = hexFormat(getInt(serverIP), 8);
            // //System.out.println("generateGUID.hexServerIP = " + hexServerIP);
        }

        String hashcode = hexFormat(System.identityHashCode(o), 8);
        String extraSeedHashCode = hexFormat(System.identityHashCode(extraSeedInfo), 8);
        // //System.out.println("System.identityHashCode(Object) = " + System.identityHashCode(o));
        // //System.out.println("generateGUID.hashcode = " + hashcode);
        tmpBuffer.append(hexServerIP);
        tmpBuffer.append(hashcode);
        tmpBuffer.append(extraSeedHashCode);

        long timeNow = System.currentTimeMillis();
        int timeLow = (int) timeNow & 0xFFFFFFFF;
        // TODO:  Should figure out if we want to synchronize access to the seeder to prevent
        int node = seeder.nextInt();

        StringBuffer guid = new StringBuffer(32);
        guid.append(hexFormat(timeLow, 8));
        guid.append(tmpBuffer.toString());
        guid.append(hexFormat(node, 8));

        // //System.out.println("generateGUID.guid = " + guid.toString());
        return guid.toString();
    }

    private static int getInt(byte[] bytes) {
        int i = 0;
        int j = 24;

        for (int k = 0; j >= 0; k++) {
            int l = bytes[k] & 0xff;
            i += (l << j);
            j -= 8;
        }

        return i;
    }

    private static String hexFormat(int i, int j) {
        String s = Integer.toHexString(i);

        return padHex(s, j) + s;
    }

    /**
     * Adds '0' to padd out to 8 characters
     *
     * @param s
     * @param i
     * @return
     */
    private static String padHex(String s, int i) {
        StringBuffer tmpBuffer = new StringBuffer();

        if (s.length() < i) {
            for (int j = 0; j < (i - s.length()); j++) {
                String randomInt = Integer.toString(seeder.nextInt());
                tmpBuffer.append((randomInt).charAt(randomInt.length() - 1));
            }
        }

        return tmpBuffer.toString();
    }

}
