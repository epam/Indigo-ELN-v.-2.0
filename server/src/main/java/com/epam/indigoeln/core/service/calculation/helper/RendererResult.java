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
package com.epam.indigoeln.core.service.calculation.helper;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;

/**
 * Describes RenderResult object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RendererResult implements Serializable {

    private static final long serialVersionUID = -6575893956964671094L;

    private byte[] image;

    public RendererResult(byte[] image) {
        this.image = Arrays.copyOf(image, image.length);
    }

    public byte[] getImage() {
        return Arrays.copyOf(image, image.length);
    }

    public String getImageBase64() {
        return Base64.getEncoder().encodeToString(image);
    }

    public void setImage(byte[] image) {
        this.image = Arrays.copyOf(image, image.length);
    }
}
