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
package com.epam.indigoeln.core.chemistry.sdf;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
class IteratorAll implements SDFileIterator {

    private static final String M_END = "M  END";

    private BufferedReader buffer;
    private int index;
    private boolean toUpper;

    IteratorAll(File f, boolean allKeysToUpperCase)
            throws FileNotFoundException {
        buffer = null;
        index = 0;
        toUpper = allKeysToUpperCase;
        InputStreamReader isr = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
        buffer = new BufferedReader(isr);
    }

    IteratorAll(String filename, boolean allKeysToUpperCase)
            throws FileNotFoundException {
        buffer = null;
        index = 0;
        toUpper = allKeysToUpperCase;
        InputStreamReader isr = new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8);
        buffer = new BufferedReader(isr);
    }

    IteratorAll(InputStream is, boolean allKeysToUpperCase) {
        buffer = null;
        index = 0;
        toUpper = allKeysToUpperCase;
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        buffer = new BufferedReader(isr);
    }

    IteratorAll(Reader r) {
        buffer = null;
        index = 0;
        toUpper = false;
        init(r);
    }

    @Override
    public void close() throws IOException {
        buffer.close();
    }

    @Override
    public int getCurrentIndex() {
        return index;
    }

    private void init(Reader r) {
        buffer = new BufferedReader(r);
    }

    @Override
    public SdUnit getNext() throws IOException {
        String line;
        StringBuilder outStringBuilder = new StringBuilder();
        boolean hasMEND = false;
        int lineCount = 0;
        while ((line = buffer.readLine()) != null) {
            lineCount++;
            outStringBuilder
                    .append(line)
                    .append("\n");
            if (M_END.equals(line.trim())) {
                hasMEND = true;
            }
            if (lineCount > 4000 && !hasMEND) {
                throw new IOException(
                        "\"M  END\" was never encountered and the maximum length of a mol file was reached");
            }
            if ("$$$$".equals(line.trim())) {
                index++;
                return new SdUnit(outStringBuilder.toString(), toUpper, false);
            }
        }
        if (hasMEND) {
            close();
            throw new IOException("No $$$$ encountered. File or stream may be truncated!");
        } else {
            close();
            return null;
        }
    }

}
