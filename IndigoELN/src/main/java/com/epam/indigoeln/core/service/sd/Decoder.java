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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

public class Decoder {

    private static final Log log = LogFactory.getLog(Decoder.class);

    public static String decodeString(String molRecord) {
        String result = molRecord;

        if (StringUtils.isNotBlank(molRecord)) {
            BASE64Decoder decoder = new BASE64Decoder();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Inflater decompressor = new Inflater(true);

            try {
                byte[] buf = new byte[1024];
                byte[] decoded = decoder.decodeBuffer(molRecord);

                decompressor.setInput(decoded);

                int count = -1;
                while (!decompressor.finished()) {
                    count = decompressor.inflate(buf);

                    if (count == 0)
                        break;

                    bos.write(buf, 0, count);
                }

                result = bos.toString();
            } catch (Exception e) {
                log.debug("Unable to decode Mol String - may be it is already decoded: " + e);
            } finally {
                bos.reset();
                decompressor.reset();
            }
        }

        return result;
    }
}
