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
package com.epam.indigo.crs.db.connectors.oracle;

import com.epam.indigo.crs.db.connectors.DbConnector;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OracleConnector extends DbConnector {

	public OracleConnector() throws SQLException {
		super();
    }

	@Override
	public String getBingoQuery(String searchFunctionName, String fieldName, Double bottomValue, Double topValue) {
		StringBuilder sb = new StringBuilder();
				
		sb.append(" ");
		sb.append(bingoSchema);
		sb.append(".");
		sb.append(searchFunctionName);
		sb.append("(");
		sb.append(fieldName);
		sb.append(", ");
		
		if (SMARTS.equalsIgnoreCase(searchFunctionName)) {
			sb.append("?");
		} else {
			sb.append("?, ?");
		}
		
		sb.append(") ");
		
		if (SIMILARITY.equalsIgnoreCase(searchFunctionName)) {
			if (bottomValue != null && topValue != null) {
                sb.append("between ").append(bottomValue).append(" and ").append(topValue);
            } else if (bottomValue != null) {
                sb.append("> ").append(bottomValue);
            } else if (topValue != null) {
                sb.append("< ").append(topValue);
            }
		} else {
			sb.append("= 1");
		}
		
		sb.append(" ");
		
		return sb.toString();
	}
	
	@Override
	public String getBingoFlushQuery() {
		return "begin " + bingoSchema + ".flushInserts(); end;";
	}
	
	@Override
	public String getNextJobIdQuery() {
		return "select " + dbSchema + ".jobs_id_seq.nextval from dual";
	}
	
	@Override
	public String getNextCompoundIdQuery() {
		return "select to_char(" + dbSchema + ".compounds_cn_seq.nextval, 'fm09999999') from dual";
	}

	@Override
	public PreparedStatement setStringToPreparedStatement(PreparedStatement ps, int index, Connection conn, String s) throws SQLException {
		if (s.length() >= 4000) {
            Clob clob = conn.createClob();
            clob.setString(1, s);
            ps.setClob(index, clob);
        } else {
            ps.setString(index, s);
        }
        return ps;
	}
}
