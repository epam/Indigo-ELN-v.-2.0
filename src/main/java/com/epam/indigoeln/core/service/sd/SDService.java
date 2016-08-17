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

import com.chemistry.enotebook.utils.sdf.SdUnit;
import com.chemistry.enotebook.utils.sdf.SdfileIterator;
import com.chemistry.enotebook.utils.sdf.SdfileIteratorFactory;
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

    public Collection<SdUnit> getProductBatchModelsFromSDFile(InputStream is) throws Exception {
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

}