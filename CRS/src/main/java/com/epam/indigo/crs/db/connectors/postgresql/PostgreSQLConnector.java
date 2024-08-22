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
package com.epam.indigo.crs.db.connectors.postgresql;

import com.epam.indigo.crs.db.connectors.DbConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgreSQLConnector extends DbConnector {
	
	public PostgreSQLConnector() throws SQLException {
		super();
	}

	@Override
	public String getBingoQuery(String searchFunctionName, String fieldName, Double bottomValue, Double topValue) {
		StringBuilder sb = new StringBuilder();
				
		sb.append(" ");
		sb.append(fieldName);
		sb.append(" @ ");
		sb.append("(");
		
		if (SIMILARITY.equalsIgnoreCase(searchFunctionName)) {
			sb.append(bottomValue);
			sb.append(", ");
			sb.append(topValue);
			sb.append(", ");
		}

		sb.append("?, ");
		
		if (SMARTS.equalsIgnoreCase(searchFunctionName)) {
			sb.append("''");
		} else {
			sb.append("?");
		}
		
		sb.append(")");
		
		sb.append("::");
		sb.append(bingoSchema);
		sb.append(".");
		sb.append(searchFunctionName);
		sb.append(" ");
		
		return sb.toString();
	}
	
	@Override
	public String getBingoFlushQuery() {
		return null; // Bingo for PostgreSQL doesn't have FlushInserts function
	}
	
	@Override
	public String getNextJobIdQuery() {
		return "select nextval('" + dbSchema + ".jobs_id_seq')";
	}
	
	@Override
	public String getNextCompoundIdQuery() {
		return "select to_char(nextval('" + dbSchema + ".compounds_cn_seq'), 'fm09999999')";
	}

	@Override
	public PreparedStatement setStringToPreparedStatement(PreparedStatement ps, int index, Connection conn, String s) throws SQLException {
		ps.setString(index, s);
        return ps;
	}
}
