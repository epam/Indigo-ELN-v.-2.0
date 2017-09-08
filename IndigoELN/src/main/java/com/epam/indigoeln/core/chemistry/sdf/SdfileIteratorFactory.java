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
