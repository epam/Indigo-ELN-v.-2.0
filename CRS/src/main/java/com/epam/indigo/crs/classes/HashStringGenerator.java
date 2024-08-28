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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class HashStringGenerator {

    public static String getHashString(String input) throws NoSuchProviderException, NoSuchAlgorithmException {
        String res = "";

        MessageDigest algorithm = MessageDigest.getInstance("MD5");

        algorithm.reset();
        algorithm.update(input.getBytes());

        byte[] hashArray = algorithm.digest();

        for (byte b : hashArray) {
            String tmp = (Integer.toHexString(0xFF & b));
            if (tmp.length() == 1) {
                res += "0" + tmp;
            } else {
                res += tmp;
            }
        }

        return res;
    }
}
