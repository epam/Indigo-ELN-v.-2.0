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
package com.epam.indigo.crs.services.registration;

import com.epam.indigo.crs.classes.CompoundInfo;
import com.epam.indigo.crs.classes.CompoundRegistrationStatus;
import com.epam.indigo.crs.exceptions.CRSException;

import java.util.List;

public interface BingoRegistration {

    String getTokenHash(String username, String password) throws CRSException;

    long submitForRegistration(String tokenHash, CompoundInfo compoundInfo) throws CRSException;
    long submitListForRegistration(String tokenHash, List<CompoundInfo> compoundInfoList) throws CRSException;

    CompoundRegistrationStatus checkRegistrationStatus(String tokenHash, long jobId) throws CRSException;

    boolean isUnique(String compound) throws CRSException;

}
