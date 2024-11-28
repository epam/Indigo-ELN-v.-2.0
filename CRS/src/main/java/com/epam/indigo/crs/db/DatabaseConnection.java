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
package com.epam.indigo.crs.db;

import com.epam.indigo.crs.classes.PropertyReader;
import com.epam.indigo.crs.db.connectors.DbConnector;
import com.epam.indigo.crs.db.connectors.oracle.OracleConnector;
import com.epam.indigo.crs.db.connectors.postgresql.PostgreSQLConnector;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

	private static final String ORACLE = "oracle";
	private static final String POSTGRESQL = "postgresql";
	
	private String dbType;
	private String dbSchema;
	
	private DbConnector dbConnector;
	
	private static DatabaseConnection _instance;
	
	public static synchronized DatabaseConnection getInstance() {
		if (_instance == null) {
			_instance = new DatabaseConnection();
		}
		return _instance;
	}
	
	private DatabaseConnection() {
		Properties crsProperties = PropertyReader.getCrsProperties();

		dbType = POSTGRESQL;
		dbSchema = "crs"; //crsProperties.getProperty("DATABASE_USERNAME");
	}

	private synchronized DbConnector getDbConnector() {
		if (dbConnector == null) {
			try {
				if (StringUtils.containsIgnoreCase(dbType, ORACLE)) {
					dbConnector = new OracleConnector();
				} else if (StringUtils.containsIgnoreCase(dbType, POSTGRESQL)) {
					dbConnector = new PostgreSQLConnector();
				} else {
					throw new RuntimeException("Unknown DB: " + dbType);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return dbConnector;
	}
	
	public String getDbSchema() {
		return dbSchema;
	}
	
	public Connection getConnection(DataSource dataSource) throws SQLException {
		Connection dbConnection;

		try {
			dbConnection = dataSource.getConnection();
			dbConnection.setAutoCommit(true);
		} catch (Exception e) {
			dbConnection = null;
			e.printStackTrace();
		}

		if (dbConnection == null) {
			throw new SQLException("Cannot connect to DB!");
		}

		return dbConnection;
	}

	public String getBingoQuery(String searchFunctionName, String fieldName, Double bottomValue, Double topValue) {
		return getDbConnector().getBingoQuery(searchFunctionName, fieldName, bottomValue, topValue);
	}
	
	public String getBingoFlushQuery() {
		return getDbConnector().getBingoFlushQuery();
	}
	
	public String getNextJobIdQuery() {
		return getDbConnector().getNextJobIdQuery();
	}
	
	public String getNextCompoundIdQuery() {
		return getDbConnector().getNextCompoundIdQuery();
	}
	
	public PreparedStatement setStringToPreparedStatement(PreparedStatement ps, int index, Connection conn, String s) throws SQLException {
		return getDbConnector().setStringToPreparedStatement(ps, index, conn, s);
	}
}
