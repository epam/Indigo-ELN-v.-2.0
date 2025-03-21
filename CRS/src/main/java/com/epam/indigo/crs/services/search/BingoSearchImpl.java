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

import com.epam.indigo.crs.classes.BingoService;
import com.epam.indigo.crs.classes.CompoundRegistrationStatus;
import com.epam.indigo.crs.classes.FullCompoundInfo;
import com.epam.indigo.crs.exceptions.CRSException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/crs/search")
public class BingoSearchImpl extends BingoService implements BingoSearch {
	
//  final private int compoundsColumndsCount = 11;
    
	private final String getQuery = "select data, batchNumber, casNumber, saltCode, saltEquivalents, " +
            "comments, hazardComments, storageComments, stereoIsomerCode, compoundNumber, conversationalBatchNumber from " + getDbSchema() + ".compounds";
    
    public BingoSearchImpl() {
        super();
    }

    protected List<Integer> doBingoSearch(String searchFunctionName, String compound, String options) throws CRSException {
        Connection dbConnection = getConnection();

        try {
            ArrayList<Integer> result = new ArrayList<Integer>();
            String query = "select id from " + getDbSchema() + ".compounds where " + getBingoQuery(searchFunctionName, "data", null, null);

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps = setStringToPreparedStatement(ps, 1, dbConnection, compound);
            ps.setString(2, options);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(rs.getInt(1));
            }

            return result;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    @PostMapping("exact")
    public List<Integer> searchExact(@RequestParam String compound, @RequestParam String options) throws CRSException {
        return doBingoSearch("exact", compound, options);
    }

    @PostMapping("sub")
    public List<Integer> searchSub(@RequestParam String compound, @RequestParam String options) throws CRSException {
        return doBingoSearch("sub", compound, options);
    }

    @PostMapping("smarts")
    public List<Integer> searchSmarts(@RequestParam String compound) throws CRSException {
        Connection dbConnection = getConnection();

        try {
            List<Integer> result = new ArrayList<Integer>();
            String query = "select id from " + getDbSchema() + ".compounds where " + getBingoQuery("smarts", "data", null, null);

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps = setStringToPreparedStatement(ps, 1, dbConnection, compound);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(rs.getInt(1));
            }

            return result;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    @PostMapping("sim")
    public List<Integer> searchSim(@RequestParam String compound, @RequestParam String metric, @RequestParam Double bottomValue, @RequestParam Double topValue) throws CRSException {
        Connection dbConnection = getConnection();

        try {
            if (bottomValue == null && topValue == null) {
                String errorMessage = "Both bottomValue and topValue are null in similarity search";
                throw new CRSException(errorMessage);
            }

            List<Integer> result = new ArrayList<Integer>();

            String query = "select id from " + getDbSchema() + ".compounds where " + getBingoQuery("sim", "data", bottomValue, topValue);

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps = setStringToPreparedStatement(ps, 1, dbConnection, compound);
            ps.setString(2, metric);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(rs.getInt(1));
            }

            return result;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    @PostMapping("/compound-by-id")
    public FullCompoundInfo getCompoundById(@RequestParam int id) throws CRSException {
        Connection dbConnection = getConnection();

        try {
            String query = getQuery + " where id = ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new FullCompoundInfo(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getString(10), rs.getString(11), CompoundRegistrationStatus.SUCCESSFUL);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    @PostMapping("/compound-by-number")
    public List<FullCompoundInfo> getCompoundByNumber(@RequestParam String compoundNumber) throws CRSException {
        Connection dbConnection = getConnection();
        try {
            String query = getQuery + " where compoundNumber like ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps.setString(1, compoundNumber + "%");

            ResultSet rs = ps.executeQuery();

            List<FullCompoundInfo> resultList = new ArrayList<FullCompoundInfo>();
            while (rs.next()) {
                resultList.add(new FullCompoundInfo(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getString(10), rs.getString(11), CompoundRegistrationStatus.SUCCESSFUL));

            }

            return resultList;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    @PostMapping("/compound-by-conversational-batch-number")
    public FullCompoundInfo getCompoundByConversationalBatchNumber(@RequestParam String conversationalBatchNumber) throws CRSException {
        Connection dbConnection = getConnection();

        try {
            String query = getQuery + " where conversationalBatchNumber = ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps.setString(1, conversationalBatchNumber);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new FullCompoundInfo(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getString(10), rs.getString(11), CompoundRegistrationStatus.SUCCESSFUL);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    @PostMapping("/compound-by-cas-number")
    public List<FullCompoundInfo> getCompoundByCasNumber(@RequestParam String casNumber) throws CRSException {
        Connection dbConnection = getConnection();

        try {
            String query = getQuery + " where casNumber = ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps.setString(1, casNumber);

            ResultSet rs = ps.executeQuery();

            List<FullCompoundInfo> resultList = new ArrayList<FullCompoundInfo>();
            while (rs.next()) {
                resultList.add(new FullCompoundInfo(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getString(10), rs.getString(11), CompoundRegistrationStatus.SUCCESSFUL));
            }

            return resultList;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    @PostMapping("/compound-by-job-id")
    public List<FullCompoundInfo> getCompoundByJobId(@RequestParam String jobId) throws CRSException {
        Connection dbConnection = getConnection();

        try {
            String query = getQuery + " where jobId = ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps.setInt(1, Integer.parseInt(jobId));

            ResultSet rs = ps.executeQuery();

            List<FullCompoundInfo> resultList = new ArrayList<FullCompoundInfo>();
            while (rs.next()) {
                resultList.add(new FullCompoundInfo(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getString(10), rs.getString(11), CompoundRegistrationStatus.SUCCESSFUL));
            }

            return resultList;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    @PostMapping("/compound-by-batch-number")
    public FullCompoundInfo getCompoundByBatchNumber(@RequestParam String batchNumber) throws CRSException {
        Connection dbConnection = getConnection();

        try {
            String query = getQuery + " where batchNumber = ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps.setString(1, batchNumber);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new FullCompoundInfo(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getString(10), rs.getString(11), CompoundRegistrationStatus.SUCCESSFUL);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }
}
