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
