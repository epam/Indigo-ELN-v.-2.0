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
