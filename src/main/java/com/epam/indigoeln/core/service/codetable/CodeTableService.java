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
