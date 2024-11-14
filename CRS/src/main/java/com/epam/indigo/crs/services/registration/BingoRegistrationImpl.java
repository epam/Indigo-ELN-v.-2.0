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

import com.epam.indigo.crs.classes.*;
import com.epam.indigo.crs.exceptions.CRSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

@RestController("/crs/registration")
public class BingoRegistrationImpl extends BingoService implements BingoRegistration {

	private static final Logger log = LoggerFactory.getLogger(BingoRegistrationImpl.class);
	
    private static final Object insertLock = new Object();
    private static final Object jobIdLock = new Object();
    
    protected BingoRegistrationManager brm;

    public BingoRegistrationImpl() {
        super(); 
        brm = BingoRegistrationManager.INSTANCE;
    }

    @PostMapping("token-hash")
    public String getTokenHash(@RequestParam String username, @RequestParam String password) throws CRSException {
    	log.info("Returning token hash for user " + username);
    	
        Connection dbConnection = getConnection();

        try {
            String query = "select id from " + getDbSchema() + ".users where username = ? and password = ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, HashStringGenerator.getHashString(password));

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new CRSException("Incorrect username or password");
            } else {
                return brm.addTokenHashForUser(rs.getInt(1));
            }
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    protected boolean checkTokenHash(String tokenHash) {
        return brm.checkTokenHash(tokenHash);
    }

    private PreparedStatement submitCompoundInfoForRegistration(Connection dbConnection, PreparedStatement ps, CompoundInfo compoundInfo, int userId, long jobId) throws CRSException {
        try {
            String newCompoundNumberId = getNewCompoundNumberId(dbConnection, compoundInfo);
            String compoundNumber = getCompoundNumber(dbConnection, compoundInfo, newCompoundNumberId);

            ps = setStringToPreparedStatement(ps, 1, dbConnection, compoundInfo.getData());
            ps.setInt(2, userId);
            ps.setString(3, compoundNumber);
            ps.setString(4, compoundInfo.getBatchNumber());
            ps.setString(5, compoundInfo.getCasNumber());
            ps.setString(6, compoundInfo.getSaltCode());
            ps.setDouble(7, compoundInfo.getSaltEquivalents());
            ps.setString(8, compoundInfo.getComments());
            ps.setString(9, compoundInfo.getHazardComments());
            ps.setString(10, compoundInfo.getStorageComments());
            ps.setLong(11, jobId);
            ps.setString(12, getConversationalBatchNumber(dbConnection, compoundNumber, compoundInfo, newCompoundNumberId));
            ps.setString(13, compoundInfo.getStereoIsomerCode());

            return ps;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        }
    }

    private long getNewJobId() throws CRSException {
    	synchronized (jobIdLock) {
    		log.info("Returning new JobID");
    		
    		Connection dbConnection = getConnection();

    		try {
                String query = getNextJobIdQuery();
                PreparedStatement ps = dbConnection.prepareStatement(query);

                ResultSet result = ps.executeQuery();

                if (result.next()) {
                    return result.getInt(1);
                } else {
                    throw new CRSException("Could not get new jobId from database");
                }
            } catch (Exception e) {
                throw new CRSException(e.getMessage(), e);
    		} finally {
    			closeConnection(dbConnection);
    		}
		}
    }

    private String getCompoundNumberPrefix(Connection dbConnection) throws CRSException {
        try {
            String query = "select value from " + getDbSchema() + ".settings where key = 'COMPOUND_NUMBER_PREFIX'";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ResultSet result = ps.executeQuery();

            if (result.next()) {
                String prefix = result.getString(1);

                result.close();
                ps.close();

                return prefix;
            } else {
                throw new CRSException("Could not get compound number prefix from database");
            }
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        }
    }

    private String getCompoundNumber(Connection dbConnection, CompoundInfo ci, String compoundNumberId) throws CRSException {
        return getCompoundNumberPrefix(dbConnection) + '-' + compoundNumberId + '-' + ci.getSaltCode();
    }

    private String getConversationalBatchNumber(Connection dbConnection, String compoundNumber, CompoundInfo ci, String compoundNumberId) throws CRSException {
        return compoundNumber + '-' + String.format("%03d", getNumberOfSameCompounds(dbConnection, ci) + 1);
    }

    private long doSubmitForRegistration(String tokenHash, List<CompoundInfo> compoundInfo, long jobId) throws CRSException {
    	synchronized (insertLock) {
    		log.info("Processing registration with JobID=" + jobId);
    		
            Connection dbConnection = getConnection();
            
            try {
                if (checkTokenHash(tokenHash)) {
                    int userId = brm.getUserId(tokenHash);

                    String query = "insert into " + getDbSchema() + ".compounds(data, userId, compoundNumber, batchNumber, casNumber, saltCode, " +
                            "saltEquivalents, comments, hazardComments, storageComments, jobId, conversationalBatchNumber, stereoisomerCode) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try {
                        dbConnection.setAutoCommit(false);

                        for (CompoundInfo ci : compoundInfo) {
                            PreparedStatement ps = dbConnection.prepareStatement(query);
                            ps = submitCompoundInfoForRegistration(dbConnection, ps, ci, userId, jobId);
                            ps.executeUpdate();
                            ps.close();
                        }

                        dbConnection.commit();

                        brm.setCompoundRegistrationStatus(jobId, CompoundRegistrationStatus.SUCCESSFUL);
                        log.info("Successfully registered compounds with JobID=" + jobId);
                    } catch (Throwable e) {
                        dbConnection.rollback();

                        brm.setCompoundRegistrationStatus(jobId, CompoundRegistrationStatus.FAILED);
                        log.error("Error registering compounds with JobID=" + jobId, e);
                    }

                    try {
                        dbConnection.setAutoCommit(true);

                        String bingoFlushQuery = getBingoFlushQuery();
                        if (bingoFlushQuery != null && bingoFlushQuery.length() > 0) {
                            PreparedStatement fps = dbConnection.prepareCall(bingoFlushQuery);
                            fps.execute();
                            fps.close();
                        }
                    } catch (Throwable e) {
                        log.warn("Error executing Bing Flush Query!", e);
                    }
                } else {
                    brm.setCompoundRegistrationStatus(jobId, CompoundRegistrationStatus.WRONG_TOKEN_DURING_REGISTRATION);
                    log.warn("Wrong token during registration with JobID=" + jobId);
                }

                return jobId;
            } catch (Throwable e) {
                throw new CRSException(e.getMessage(), e);
            } finally {
            	closeConnection(dbConnection);
            }
    	}
    }

    @PostMapping("/submit")
    public long submitForRegistration(@RequestParam String tokenHash, CompoundInfo compoundInfo) throws CRSException {
    	long jobId = getNewJobId();
    	
    	new RegistrationThread(tokenHash, Arrays.asList(compoundInfo), jobId).start();
        
        return jobId;
    }

    @PostMapping("/submit-list")
    public long submitListForRegistration(@RequestParam String tokenHash, List<CompoundInfo> compoundInfoList) throws CRSException {
    	long jobId = getNewJobId();
    	
    	new RegistrationThread(tokenHash, compoundInfoList, jobId).start();
        
        return jobId;
    }

    @PostMapping("/status")
    public CompoundRegistrationStatus checkRegistrationStatus(@RequestParam String tokenHash, @RequestParam long jobId) throws CRSException {
        try {
            CompoundRegistrationStatus result;

            if (checkTokenHash(tokenHash)) {
                result = brm.getCompoundRegistrationStatus(jobId);
                if (result == null) {
                    result = CompoundRegistrationStatus.NOT_REGISTERED_YET;
                }
            } else {
                result = CompoundRegistrationStatus.WRONG_TOKEN_DURING_CHECK;
            }

            return result;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        }
    }

    @PostMapping("/unique")
    public boolean isUnique(@RequestParam String compound) throws CRSException {
        Connection dbConnection = getConnection();
        try {
            boolean result = false;
            String query = "select id from " + getDbSchema() + ".compounds where " + getBingoQuery("exact", "data", null, null);

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps = setStringToPreparedStatement(ps, 1, dbConnection, compound);
            ps.setString(2, "ALL");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                result = true;
            }

            return result;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        } finally {
            closeConnection(dbConnection);
        }
    }

    private int getNumberOfSameCompounds(Connection dbConnection, CompoundInfo compoundInfo) throws CRSException {
        try {
            int result = 0;

            String query = "select count(*) from " + getDbSchema() + ".compounds where " + getBingoQuery("exact", "data", null, null) + " and saltCode = ? and stereoisomerCode = ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);

            ps = setStringToPreparedStatement(ps, 1, dbConnection, compoundInfo.getData());
            ps.setString(2, "ALL");
            ps.setString(3, compoundInfo.getSaltCode());
            ps.setString(4, compoundInfo.getStereoIsomerCode());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                result = rs.getInt(1);
            }

            rs.close();
            ps.close();

            return result;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        }
    }

    private String getNewCompoundNumberId(Connection dbConnection, CompoundInfo compoundInfo) throws CRSException {
        try {
            String result = null;

            String query = "select compoundNumber from " + getDbSchema() + ".compounds where " + getBingoQuery("exact", "data", null, null) + " and stereoisomerCode = ?";

            PreparedStatement ps = dbConnection.prepareStatement(query);
            ps = setStringToPreparedStatement(ps, 1, dbConnection, compoundInfo.getData());

            ps.setString(2, "ALL");
            ps.setString(3, compoundInfo.getStereoIsomerCode());

            ResultSet rs = ps.executeQuery();

            while (rs.next() && result == null) {
                result = rs.getString(1).substring(4, 12);
            }

            rs.close();
            ps.close();

            if (result == null) {
                query = getNextCompoundIdQuery();

                ps = dbConnection.prepareStatement(query);
                rs = ps.executeQuery();

                if (rs.next()) {
                    result = rs.getString(1);
                } else {
                    throw new CRSException("Could not get new compound number id from database");
                }

                rs.close();
                ps.close();
            }

            return result;
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        }
    }
    
    private class RegistrationThread extends Thread {
    	
    	private String tokenHash;
    	private List<CompoundInfo> compounds;
    	private long jobId;
    	
    	public RegistrationThread(String tokenHash, List<CompoundInfo> compounds, long jobId) {
    		this.tokenHash = tokenHash;
    		this.compounds = compounds;
    		this.jobId = jobId;
		}
    	
    	@Override
    	public void run() {
    		try {
				doSubmitForRegistration(tokenHash, compounds, jobId);
			} catch (Exception e) {
				log.error("Some error occured: ", e);
			}
    	}
    }
}
