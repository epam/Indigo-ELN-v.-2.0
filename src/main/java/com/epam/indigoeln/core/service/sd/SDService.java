/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * <p>
 * This file is part of Indigo ELN.
 * <p>
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * <p>
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.epam.indigoeln.core.service.sd;

import com.chemistry.enotebook.domain.SDFileInfo;
import com.chemistry.enotebook.utils.sdf.SdUnit;
import com.chemistry.enotebook.utils.sdf.SdfileIterator;
import com.chemistry.enotebook.utils.sdf.SdfileIteratorFactory;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class SDService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SDService.class);

    public Collection<SdUnit> parse(InputStream is) throws Exception {
        List<SdUnit> result = new ArrayList<>();
        try {
            SdUnit sdu;
            SdfileIterator it = SdfileIteratorFactory.getIterator(is);
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
            throw e;
        }
        return result;
    }

    public SDFileInfo create(Collection<BasicDBObject> batches) throws Exception {
        SDFileInfo info = new SDFileInfo();
        if (batches != null && !batches.isEmpty()) {
            StringBuilder str = new StringBuilder();
            int size = batches.size();
            int[] fileOffsets = new int[size];
            int i = 0;
            for (BasicDBObject batch : batches) {
                String sdStr = create(batch);
                fileOffsets[i] = (i + 1);
                i++;
                str.append(sdStr);
            }
            info.setSdfileStr(str.toString());
            info.setSdunitOffsets(fileOffsets);
            LOGGER.debug("SDFile prepared is:\n" + info.getSdfileStr());
            return info;
        } else
            return info;
    }

    public String create(BasicDBObject batch) throws Exception {
        SdUnit sDunit;

        try {
            final BasicDBObject structure = (BasicDBObject) batch.get("structure");
            final String molfile = structure.getString("molfile");
            if (molfile != null) {
                // Check if it is not mol format already
                if (molfile.indexOf("M END") <= 0) {
                    String molformat = Decoder.decodeString(molfile);
                    sDunit = new SdUnit(molformat, true);
                } else {
                    sDunit = new SdUnit(molfile, true);
                }
            } else {
                throw new Exception("Compound structure is emtpy. Cannot prepare SD.");
            }

            //TODO:
//            sDunit.setValue("STEREOISOMER_CODE", "");

            return sDunit.toString();
        } catch (Exception e) {
            LOGGER.error("Error occurred while creating SD unit for batch.", e);
            throw e;
        }

    }

}