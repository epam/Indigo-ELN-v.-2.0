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
import java.util.ArrayList;

class IteratorAll implements SdfileIterator {//, StreamBasedIterator {

    BufferedReader buffer;
    int index;
    boolean toUpper;

    IteratorAll(File f) throws FileNotFoundException {
        this(f, false);
    }

    IteratorAll(File f, boolean allKeysToUpperCase)
            throws FileNotFoundException {
        buffer = null;
        index = 0;
        toUpper = false;
        toUpper = allKeysToUpperCase;
        FileInputStream fis = new FileInputStream(f);
        init(fis);
    }

    IteratorAll(String filename) throws FileNotFoundException {
        this(filename, false);
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

    IteratorAll(InputStream is) {
        this(is, false);
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

    public void close() throws IOException {
        buffer.close();
    }

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

    public SdUnit getNext() throws IOException {
        String line = "";
        StringBuffer out = new StringBuffer();
        boolean hasMEND = false;
        int lineCount = 0;
        while ((line = buffer.readLine()) != null) {
            lineCount++;
            out.append(line);
            out.append("\n");
            if (line.trim().equals("M  END"))
                hasMEND = true;
            if (lineCount > 4000 && !hasMEND)
                throw new IOException(
                        "\"M  END\" was never encountered and the maximum length of a mol file was reached");
            if (line.trim().equals("$$$$")) {
                index++;
                return new SdUnit(out.toString(), toUpper, false);
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

    public SdUnit getNthNext(int n) throws IOException {
        if (n <= 0)
            n = 1;
        SdUnit current = null;
        for (; n > 0; n--)
            current = getNext();

        return current;
    }

    public SdUnit[] getNNext(int n) throws IOException {
        if (n < 0)
            n = 0;
        ArrayList al = new ArrayList();
        for (; n > 0; n--) {
            SdUnit current = getNext();
            if (current != null)
                al.add(current);
        }

        SdUnit result[] = new SdUnit[al.size()];
        for (int x = 0; x <= result.length - 1; x++)
            result[x] = (SdUnit) al.get(x);

        return result;
    }
}
