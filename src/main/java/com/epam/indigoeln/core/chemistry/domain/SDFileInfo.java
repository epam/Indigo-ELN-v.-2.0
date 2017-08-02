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
