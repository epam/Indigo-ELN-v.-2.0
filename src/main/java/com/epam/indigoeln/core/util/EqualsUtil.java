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
package com.epam.indigoeln.core.util;

/**
 * Utility class for equals check.
 */
public final class EqualsUtil {

    private static final double PRECISION = 0.00000001;

    private EqualsUtil() {
    }

    /**
     * Returns true if number equals zero with preset accuracy.
     *
     * @param number Number for check
     * @return True if number equals zero with preset accuracy, otherwise returns false
     */
    public static boolean doubleEqZero(double number) {
        return Math.abs(number - 0.0) < PRECISION;
    }

}
