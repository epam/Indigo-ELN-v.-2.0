/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.codetable;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides methods for getting information from csv files.
 */
@Service
public class CodeTableService implements InitializingBean {

    public static final String TABLE_SALT_CODE = "GCM_SALT_CDT";
    public static final String SALT_CODE = "SALT_CODE";
    public static final String SALT_DESC = "SALT_DESC";
    public static final String SALT_WEIGHT = "SALT_WEIGHT";
    public static final String SALT_FORMULA = "SALT_FORMULA";

    private Map<String, List<Map<String, String>>> codeTablesMap;

    /**
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails
     * @see org.springframework.beans.factory.InitializingBean
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        codeTablesMap = new HashMap<>();
        codeTablesMap.put(TABLE_SALT_CODE, parseTableValues(TABLE_SALT_CODE));
    }

    /**
     * Returns data from table by table's name.
     *
     * @param tableName Name of table
     * @return Returns data from table
     */
    public List<Map<String, String>> getCodeTable(String tableName) {
        if (codeTablesMap == null || !codeTablesMap.containsKey(tableName)) {
            throw new CustomParametrizedException("Table with name='" + tableName + "' does not exist");
        }
        return codeTablesMap.get(tableName);
    }

    private List<Map<String, String>> parseTableValues(String tableName) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        URL resource = getClass().getResource(String.format("/data/%s.csv", tableName));
        try (CSVParser csvRecords = CSVParser.parse(resource, Charset.defaultCharset(),
                CSVFormat.DEFAULT.withHeader())) {
            csvRecords.forEach(
                    csvRecord -> result.add(csvRecord.toMap())
            );
        }
        return result;
    }
}
