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
package com.epam.indigoeln.config.crs;

import com.epam.indigo.crs.classes.CompoundInfo;
import com.epam.indigo.crs.classes.CompoundRegistrationStatus;
import com.epam.indigo.crs.exceptions.CRSException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface BingoRegistrationClient {

    @PostExchange("/token-hash")
    String getTokenHash(@RequestParam String username, @RequestParam String password) throws CRSException;

    @PostExchange("/submit")
    long submitForRegistration(@RequestParam String tokenHash, @RequestBody CompoundInfo compoundInfo) throws CRSException;
    @PostExchange("/submit-list")
    long submitListForRegistration(@RequestParam String tokenHash, @RequestBody List<CompoundInfo> compoundInfoList) throws CRSException;

    @PostExchange("/status")
    CompoundRegistrationStatus checkRegistrationStatus(@RequestParam String tokenHash, @RequestParam long jobId) throws CRSException;

    @PostExchange("/unique")
    boolean isUnique(@RequestParam String compound) throws CRSException;

}
