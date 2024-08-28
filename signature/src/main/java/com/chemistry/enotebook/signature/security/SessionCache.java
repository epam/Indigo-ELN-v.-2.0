/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature.security;

import java.util.HashMap;
import java.util.Map;

public class SessionCache {

    private static SessionCache _instance;

    private Map<String, String> cache;
    private Object sync = new Object();

    private SessionCache() {
        cache = new HashMap<String, String>();
    }

    public static synchronized SessionCache getInstance() {
        if (_instance == null) {
            _instance = new SessionCache();
        }
        return _instance;
    }

    public void addSession(String sessionId, String username) {
        synchronized (sync) {
            cache.put(sessionId, username);
        }
    }

    public void removeSession(String sessionId) {
        synchronized (sync) {
            cache.remove(sessionId);
        }
    }

    public String getUsername(String sessionId) {
        synchronized (sync) {
            return cache.get(sessionId);
        }
    }
}
