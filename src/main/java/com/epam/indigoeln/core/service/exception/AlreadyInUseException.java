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
 * Custom exception, which displays, that links to Entity already exist.
 *
 * @author Timofey Zubkov
 */
public final class AlreadyInUseException extends CustomParametrizedException {

    private static final long serialVersionUID = 5641713615602592557L;

    private AlreadyInUseException(String message, String params) {
        super(message, params);
    }

    /**
     * Creates instance of AlreadyInUseException class with role's id.
     *
     * @param roleId Role's identifier
     * @return Instance of AlreadyInUseException class with role's id
     */
    public static AlreadyInUseException createWithRoleId(String roleId) {
        return new AlreadyInUseException("The role with id = " + roleId + " already in use", roleId);
    }
}
