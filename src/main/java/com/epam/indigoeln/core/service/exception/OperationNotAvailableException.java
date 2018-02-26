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
package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays that operation not available.
 */
public class OperationNotAvailableException extends CustomParametrizedException {
    private OperationNotAvailableException(String message, String... params) {
        super(message, params);
    }

    /**
     * Creates instance of OperationNotAvailableException.
     * if user tried to update notebook if there is at least one batch or open experiment.
     *
     * @return Instance of OperationDeniedException
     */
    public static OperationNotAvailableException createNotebookUpdateNameOperation() {
        return new OperationNotAvailableException("The notebook can't be updated if there is at least one experiment "
                + "with a batch or experiments in non-Open status");
    }
}
