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
package com.epam.indigoeln.core.service.sd;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Util class to decode zipped base64 encoded strings.
 */
final class Decoder {

    /**
     * Logger instance.
     */
    private static final Log LOGGER = LogFactory.getLog(Decoder.class);

    /**
     * Hidden default constructor.
     */
    private Decoder() {
        // Hide the default constructor
    }

    /**
     * Decode base64 and unzip string.
     *
     * @param molRecord string to decode and unzip
     * @return base64 decoded and unzipped string
     */
    static String decodeString(String molRecord) {
        String result = molRecord;

        if (StringUtils.isNotBlank(molRecord)) {
            Inflater decompressor = new Inflater(true);

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[1024];
                byte[] decoded = Base64.decodeBase64(molRecord);

                decompressor.setInput(decoded);

                int count;
                while (!decompressor.finished()) {
                    count = decompressor.inflate(buf);

                    if (count == 0) {
                        break;
                    }

                    bos.write(buf, 0, count);
                }

                result = bos.toString(StandardCharsets.UTF_8.name());
            } catch (IOException | DataFormatException e) {
                LOGGER.debug("Unable to decode Mol String - may be it is already decoded: " + e);
            } finally {
                decompressor.reset();
            }
        }

        return result;
    }
}
