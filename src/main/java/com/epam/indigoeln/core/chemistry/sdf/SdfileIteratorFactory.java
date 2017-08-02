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
package com.epam.indigoeln.core.chemistry.sdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class SdfileIteratorFactory {
    private SdfileIteratorFactory() {
    }

    public static SdfileIterator getIterator(String fileName) throws IOException {
        return new IteratorAll(fileName, true);
    }

    public static SdfileIterator getIterator(File f) throws IOException {
        return new IteratorAll(f, true);
    }

    public static SdfileIterator getIterator(InputStream is) throws IOException {
        return new IteratorAll(is, true);
    }

    public static SdfileIterator getIterator(Reader r) throws IOException {
        return new IteratorAll(r);
    }

    public static SdfileIterator getIterator(String fileName, boolean allKeysToUpperCase) throws IOException {
        return new IteratorAll(fileName, allKeysToUpperCase);
    }

    public static SdfileIterator getIterator(File f, boolean allKeysToUpperCase) throws IOException {
        return new IteratorAll(f, allKeysToUpperCase);
    }

    public static SdfileIterator getIterator(InputStream is, boolean allKeysToUpperCase) throws IOException {
        return new IteratorAll(is, allKeysToUpperCase);
    }

}
