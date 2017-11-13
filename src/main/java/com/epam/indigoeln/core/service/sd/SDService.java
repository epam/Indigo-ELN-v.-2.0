/****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************************/
package com.epam.indigoeln.core.service.sd;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.chemistry.domain.SDFileInfo;
import com.epam.indigoeln.core.chemistry.sdf.SdUnit;
import com.epam.indigoeln.core.chemistry.sdf.SdfileIterator;
import com.epam.indigoeln.core.chemistry.sdf.SdfileIteratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service class for exporting and importing SDFiles
 */
@Service
public class SDService {

    /**
     * Logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SDService.class);

    /**
     * Read SDFile to collection of item representation
     *
     * @param reader reader for SDFile
     * @return collection of items (structures with properties) from SDFile
     * @throws IndigoRuntimeException in case of any errors during SDFile reading
     */
    public Collection<SdUnit> parse(Reader reader) throws IndigoRuntimeException {
        List<SdUnit> result = new ArrayList<>();
        try {
            SdUnit sdu;
            SdfileIterator it = SdfileIteratorFactory.getIterator(reader);
            int offset = 0;
            while ((sdu = it.getNext()) != null) {
                offset++;

                if (!sdu.isValidMol()) {
                    LOGGER.warn("Invalid SD file\noffset:" + offset + "\n" + sdu.getInvalidDescription());
                    continue;
                }

                // add to list
                result.add(sdu);
            }
        } catch (Exception e) {
            LOGGER.error("Error while extracting batch from SD file: " + e);
            throw new IndigoRuntimeException(e);
        }
        return result;
    }

    /**
     * Create SDFile representation from given collection of SDFile items
     *
     * @param items collection of SDFile items
     * @return SDFile representation
     */
    public SDFileInfo create(Collection<SDExportItem> items) {
        SDFileInfo info = new SDFileInfo();
        if (items != null && !items.isEmpty()) {
            StringBuilder str = new StringBuilder();
            int size = items.size();
            int[] fileOffsets = new int[size];
            int i = 0;
            for (SDExportItem item : items) {
                String sdStr = create(item);
                fileOffsets[i] = (i + 1);
                i++;
                str.append(sdStr);
            }
            info.setSdfileStr(str.toString());
            info.setSdunitOffsets(fileOffsets);
            LOGGER.debug("SDFile prepared is:\n" + info.getSdfileStr());
            return info;
        } else {
            return info;
        }
    }

    /**
     * Create string representation of given SDFile item
     *
     * @param item SDFile item
     * @return string representation of given SDFile item
     * @throws IndigoRuntimeException in case if compound structure is empty or any other error
     */
    public String create(SDExportItem item) throws IndigoRuntimeException {
        SdUnit sDunit;

        try {
            final String molfile = item.getMolfile();
            if (molfile != null) {
                // Check if it is not mol format already
                if (molfile.indexOf("M END") <= 0) {
                    String molformat = Decoder.decodeString(molfile);
                    sDunit = new SdUnit(molformat, true);
                } else {
                    sDunit = new SdUnit(molfile, true);
                }
            } else {
                throw new IndigoRuntimeException("Compound structure is emtpy. Cannot prepare SD.");
            }

            item.getProperties().forEach(sDunit::setValue);

            return sDunit.toString();
        } catch (Exception e) {
            LOGGER.error("Error occurred while creating SD unit for item.", e);
            throw new IndigoRuntimeException(e);
        }

    }
}
