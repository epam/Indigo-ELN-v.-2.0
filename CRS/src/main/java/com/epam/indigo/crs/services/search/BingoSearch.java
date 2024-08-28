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
package com.epam.indigo.crs.services.search;

import com.epam.indigo.crs.classes.FullCompoundInfo;
import com.epam.indigo.crs.exceptions.CRSException;

import java.util.List;

public interface BingoSearch {

    List<Integer> searchExact(String compound, String options) throws CRSException;
    List<Integer> searchSub(String compound, String options) throws CRSException;
    List<Integer> searchSmarts(String compound) throws CRSException;
    List<Integer> searchSim(String compound, String metric, Double bottomValue, Double topValue) throws CRSException;

    List<FullCompoundInfo> getCompoundByNumber(String compoundNumber) throws CRSException;
    List<FullCompoundInfo> getCompoundByCasNumber(String casNumber) throws CRSException;
    List<FullCompoundInfo> getCompoundByJobId(String jobId) throws CRSException;

    FullCompoundInfo getCompoundById(int id) throws CRSException;
    FullCompoundInfo getCompoundByConversationalBatchNumber(String conversationalBatchNumber) throws CRSException;
    FullCompoundInfo getCompoundByBatchNumber(String batchNumber) throws CRSException;

}
