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

import com.epam.indigo.crs.db.DatabaseConnection;
import com.epam.indigo.crs.exceptions.CRSException;
import org.springframework.beans.factory.annotation.Required;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public abstract class BingoService {

    private DataSource dataSource;

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    protected BingoService() {
    }

    protected String getDbSchema() {
        return DatabaseConnection.getInstance().getDbSchema();
    }

    protected Connection getConnection() throws CRSException {
        try {
            return DatabaseConnection.getInstance().getConnection(dataSource);
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        }
    }

    protected PreparedStatement setStringToPreparedStatement(PreparedStatement ps, int index, Connection dbConnection, String s) throws CRSException {
        try {
            return DatabaseConnection.getInstance().setStringToPreparedStatement(ps, index, dbConnection, s);
        } catch (Exception e) {
            throw new CRSException(e.getMessage(), e);
        }
    }

    protected String getBingoQuery(String searchFunctionName, String fieldName, Double bottomValue, Double topValue) {
        return DatabaseConnection.getInstance().getBingoQuery(searchFunctionName, fieldName, bottomValue, topValue);
    }

    protected String getBingoFlushQuery() {
        return DatabaseConnection.getInstance().getBingoFlushQuery();
    }

    protected String getNextJobIdQuery() {
        return DatabaseConnection.getInstance().getNextJobIdQuery();
    }

    protected String getNextCompoundIdQuery() {
        return DatabaseConnection.getInstance().getNextCompoundIdQuery();
    }

    protected void closeConnection(Connection conn) {
        try { conn.close(); } catch (Exception ignored) { }
    }
}
