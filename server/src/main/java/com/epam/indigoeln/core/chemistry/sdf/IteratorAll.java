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
