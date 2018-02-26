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
 * Custom exception, which displays, that permissions are incorrect.
 */
public final class PermissionIncorrectException extends CustomParametrizedException {

    /**
     * Creates instance of PermissionIncorrectException.
     *
     * @param message Exception message
     * @param params  Exception parameters
     */
    private PermissionIncorrectException(String message, String params) {
        super(message, params);
    }

    /**
     * Creates instance of PermissionIncorrectException if user permission is incorrect.
     *
     * @param userId     User's id
     * @param permission User's permission
     * @return Instance of PermissionIncorrectException
     */
    public static PermissionIncorrectException createWithUserIdAndPermission(
            String userId, String permission) {
        return new PermissionIncorrectException("The user permission = " + permission
                + " for user with id = " + userId + " isn't correct", userId);
    }

    /**
     * Creates instance of PermissionIncorrectException if user try to change his own permissions for some entity.
     *
     * @param userId User's id
     * @return Instance of PermissionIncorrectException
     */
    public static PermissionIncorrectException createWithUserIdOnSelfPermissionChanges(String userId) {
        return new PermissionIncorrectException("The user permission for user with id = " + userId +
                " can't be changed by user himself", userId);
    }

    /**
     * Creates instance of PermissionIncorrectException if user permission can't be with unspecified user id.
     *
     * @return Instance of PermissionIncorrectException
     */
    public static PermissionIncorrectException createWithoutUserId() {
        return new PermissionIncorrectException("The user permission can't be with unspecified user id", null);
    }
}
