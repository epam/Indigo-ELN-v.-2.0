package com.epam.indigoeln.core.service.codetable;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodeTableService {

    public static final  String TABLE_SALT_CODE = "GCM_SALT_CDT";

    private Map<String, List<Map>> codeTablesMap;

    @PostConstruct
    private void init() throws IOException {
        codeTablesMap = new HashMap<>();
        codeTablesMap.put(TABLE_SALT_CODE, parseTableValues(TABLE_SALT_CODE));
    }

    public List<Map> getCodeTable(String tableName) {
        if(!codeTablesMap.containsKey(tableName)) {
            throw new CustomParametrizedException("Table with name='" + tableName + "' does not exist");
        }
        return codeTablesMap.get(tableName);
    }

    private List<Map> parseTableValues(String tableName) throws IOException {
        List<Map> result = new ArrayList<>();
        URL resource = getClass().getResource("/data/" + tableName + ".csv");
        try (CSVParser csvRecords = CSVParser.parse(resource, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader())) {
            csvRecords.forEach(
                    csvRecord -> result.add(csvRecord.toMap())
            );
        }
        return result;
    }
}
