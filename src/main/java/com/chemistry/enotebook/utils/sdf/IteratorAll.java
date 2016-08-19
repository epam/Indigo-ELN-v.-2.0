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
package com.chemistry.enotebook.utils.sdf;

import java.io.*;

class IteratorAll implements SdfileIterator {

    private static final String M_END = "M  END";

    BufferedReader buffer;
    int index;
    boolean toUpper;

    IteratorAll(File f, boolean allKeysToUpperCase)
            throws FileNotFoundException {
        buffer = null;
        index = 0;
        toUpper = false;
        toUpper = allKeysToUpperCase;
        FileInputStream fis = new FileInputStream(f);
        init(fis);
    }

    IteratorAll(String filename, boolean allKeysToUpperCase)
            throws FileNotFoundException {
        buffer = null;
        index = 0;
        toUpper = false;
        toUpper = allKeysToUpperCase;
        FileInputStream fis = new FileInputStream(filename);
        init(fis);
    }

    IteratorAll(InputStream is, boolean allKeysToUpperCase) {
        buffer = null;
        index = 0;
        toUpper = false;
        toUpper = allKeysToUpperCase;
        init(is);
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

    private void init(InputStream is) {
        InputStreamReader isr = new InputStreamReader(is);
        buffer = new BufferedReader(isr);
    }

    private void init(Reader r) {
        buffer = new BufferedReader(r);
    }

    @Override
    public SdUnit getNext() throws IOException {
        String line;
        String out = "";
        boolean hasMEND = false;
        int lineCount = 0;
        while ((line = buffer.readLine()) != null) {
            lineCount++;
            out += line + "\n";
            if (M_END.equals(line.trim()))
                hasMEND = true;
            if (lineCount > 4000 && !hasMEND)
                throw new IOException(
                        "\"M  END\" was never encountered and the maximum length of a mol file was reached");
            if ("$$$$".equals(line.trim())) {
                index++;
                return new SdUnit(out, toUpper, false);
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
