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
package com.epam.indigoeln.web.rest.errors;

final class ErrorConstants {

    static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    static final String ERR_ACCESS_DENIED = "error.accessDenied";
    static final String ERR_VALIDATION = "error.validation";
    static final String ERR_METHOD_NOT_SUPPORTED = "error.methodNotSupported";
    static final String ERR_URI_SYNTAX = "error.uriSyntax";
    static final String ERR_IO = "error.io";
    static final String ERR_DATABASE_ACCESS = "error.dataAccess";
    static final String ERR_FILE_SIZE_LIMIT = "error.fileSizeLimit";

    private ErrorConstants() {
    }
}
