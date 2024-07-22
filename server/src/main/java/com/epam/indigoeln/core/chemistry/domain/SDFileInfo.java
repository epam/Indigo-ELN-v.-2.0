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
package com.epam.indigoeln.core.chemistry.domain;

public class SDFileInfo implements java.io.Serializable {


    public static final long serialVersionUID = 7526472295645376147L;
    //String representation of SDFile ( one or more SDUnits)
    private String sdfileStr;
    //Represents the number of sdunits in the SDFileStr and their offset in the file
    //starting from 0. i.e first compound in the sdunit will have offset 0 and second compound is 1 etc
    private int[] sdunitOffsets;

    public String getSdfileStr() {
        return sdfileStr;
    }

    public void setSdfileStr(String sdfileStr) {
        this.sdfileStr = sdfileStr;
    }

    public int[] getSdunitOffsets() {
        return sdunitOffsets;
    }

    public void setSdunitOffsets(int[] sdunitOffsets) {
        this.sdunitOffsets = sdunitOffsets;
    }


}
