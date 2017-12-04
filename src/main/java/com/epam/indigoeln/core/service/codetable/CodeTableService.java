package com.epam.indigoeln.core.service.codetable;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
import java.util.stream.Collectors;

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

    /**
     * Convert data map from csv file to data transfer object.
     *
     * @param rowData map of data from csv file
     * @return Salt information entity from {@code CodeTableService.TABLE_SALT_CODE} table
     */
    public List<SaltDTO> convert(List<Map<String, String>> rowData) {

        return rowData.stream()
                .map(rowDataEntity -> new SaltDTO(
                        rowDataEntity.get(SALT_CODE),
                        rowDataEntity.get(SALT_DESC),
                        parseDouble(rowDataEntity.get(SALT_WEIGHT)),
                        rowDataEntity.get(SALT_FORMULA)))
                .collect(Collectors.toList());
    }

    private Double parseDouble(String rowDoubleData) {
        Double aDouble;
        try {
            aDouble = Double.valueOf(rowDoubleData);
        } catch (NumberFormatException e) {
            aDouble = Double.NaN;
        }
        return aDouble;
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

    /**
     * Data transfer object to send salt information to FE.
     */
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    @AllArgsConstructor
    public static class SaltDTO {
        String saltCode;
        String saltDescription;
        @Getter
        @JsonProperty("weight")
        Double saltWeight;
        String saltFormula;

        public String getValue() {
            return String.format("%s - %s", saltCode, saltDescription);
        }
    }
}