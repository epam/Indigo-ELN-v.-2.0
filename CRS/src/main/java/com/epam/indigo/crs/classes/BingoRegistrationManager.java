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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BingoRegistrationManager {

    public static final BingoRegistrationManager INSTANCE = new BingoRegistrationManager();

    private final Map<String, Integer> tokenHashDict;
    private final Map<Long, CompoundRegistrationStatus> compoundRegistrationStatusDict;

    private BingoRegistrationManager() {
        tokenHashDict = Collections.synchronizedMap(new HashMap<String, Integer>());
        compoundRegistrationStatusDict = Collections.synchronizedMap(new HashMap<Long, CompoundRegistrationStatus>());
    }

    public synchronized String addTokenHashForUser(int userId) throws NoSuchAlgorithmException, NoSuchProviderException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String now = sdf.format(Calendar.getInstance().getTime());

        String hashString = HashStringGenerator.getHashString(userId + now);

        Iterator<Map.Entry<String,Integer>> iter = tokenHashDict.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Integer> entry = iter.next();
            if (userId == entry.getValue()) {
                iter.remove();
            }
        }

        tokenHashDict.put(hashString, userId);

        return hashString;
    }

    public synchronized int getUserId(String tokenHash) {
        return tokenHashDict.get(tokenHash);
    }

    public synchronized void setCompoundRegistrationStatus(long jobId, CompoundRegistrationStatus crs) {
        compoundRegistrationStatusDict.put(jobId, crs);
    }

    public synchronized CompoundRegistrationStatus getCompoundRegistrationStatus(long jobId) {
        return compoundRegistrationStatusDict.get(jobId);
    }

    public synchronized boolean checkTokenHash(String tokenHash) {
        return tokenHashDict.containsKey(tokenHash);
    }
}
