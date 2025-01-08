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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BingoSearch {

    @PostMapping("exact")
    List<Integer> searchExact(@RequestParam String compound, @RequestParam String options) throws CRSException;
    @PostMapping("sub")
    List<Integer> searchSub(@RequestParam String compound, @RequestParam String options) throws CRSException;
    @PostMapping("smarts")
    List<Integer> searchSmarts(@RequestParam String compound) throws CRSException;
    @PostMapping("sim")
    List<Integer> searchSim(@RequestParam String compound, @RequestParam String metric, @RequestParam Double bottomValue, @RequestParam Double topValue) throws CRSException;

    @PostMapping("/compound-by-number")
    List<FullCompoundInfo> getCompoundByNumber(@RequestParam String compoundNumber) throws CRSException;
    @PostMapping("/compound-by-cas-number")
    List<FullCompoundInfo> getCompoundByCasNumber(@RequestParam String casNumber) throws CRSException;
    @PostMapping("/compound-by-job-id")
    List<FullCompoundInfo> getCompoundByJobId(@RequestParam String jobId) throws CRSException;

    @PostMapping("/compound-by-id")
    FullCompoundInfo getCompoundById(@RequestParam int id) throws CRSException;
    @PostMapping("/compound-by-conversational-batch-number")
    FullCompoundInfo getCompoundByConversationalBatchNumber(@RequestParam String conversationalBatchNumber) throws CRSException;
    @PostMapping("/compound-by-batch-number")
    FullCompoundInfo getCompoundByBatchNumber(@RequestParam String batchNumber) throws CRSException;

}
