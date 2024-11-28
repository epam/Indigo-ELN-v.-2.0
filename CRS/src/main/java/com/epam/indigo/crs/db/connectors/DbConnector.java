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
package com.epam.indigo.crs.db.connectors;

import com.epam.indigo.crs.classes.PropertyReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public abstract class DbConnector {

    private static final String BINGO = "bingo";
    
	protected static final String SMARTS = "smarts";
	protected static final String SIMILARITY = "sim";
	
	protected String bingoSchema;
	protected String dbSchema;
	
	protected DbConnector() throws RuntimeException {
		Properties crsProperties = PropertyReader.getCrsProperties();
		
		bingoSchema = DbConnector.BINGO;
		dbSchema = "crs"; //crsProperties.getProperty("DATABASE_USERNAME");
	}

	public abstract String getBingoQuery(String searchFunctionName, String fieldName, Double bottomValue, Double topValue);
	
	public abstract String getBingoFlushQuery();
	
	public abstract String getNextJobIdQuery();
	
	public abstract String getNextCompoundIdQuery();
	
	public abstract PreparedStatement setStringToPreparedStatement(PreparedStatement ps, int index, Connection conn, String s) throws SQLException;
}
