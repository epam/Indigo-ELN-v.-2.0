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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public final class SDFileIteratorFactory {
    private SDFileIteratorFactory() {
    }

    public static SDFileIterator getIterator(String fileName) throws IOException {
        return new IteratorAll(fileName, true);
    }

    public static SDFileIterator getIterator(File f) throws IOException {
        return new IteratorAll(f, true);
    }

    public static SDFileIterator getIterator(InputStream is) {
        return new IteratorAll(is, true);
    }

    public static SDFileIterator getIterator(Reader r) {
        return new IteratorAll(r);
    }

    public static SDFileIterator getIterator(String fileName, boolean allKeysToUpperCase) throws IOException {
        return new IteratorAll(fileName, allKeysToUpperCase);
    }

    public static SDFileIterator getIterator(File f, boolean allKeysToUpperCase) throws IOException {
        return new IteratorAll(f, allKeysToUpperCase);
    }

    public static SDFileIterator getIterator(InputStream is, boolean allKeysToUpperCase) throws IOException {
        return new IteratorAll(is, allKeysToUpperCase);
    }

}
