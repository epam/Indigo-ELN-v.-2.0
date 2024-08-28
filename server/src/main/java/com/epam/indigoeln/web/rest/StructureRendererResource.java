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
package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.core.service.calculation.helper.RendererResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api
@RestController
@RequestMapping("/api/renderer")
public class StructureRendererResource {

    @Autowired
    private CalculationService calculationService;

    /**
     * POST /molecule/image -> get molecule's graphical representation.
     *
     * @param type      Representation type
     * @param structure Molecule's structure
     * @return Renderer result
     */
    @ApiOperation(value = "Returns molecule's graphical representation.")
    @RequestMapping(value = "/{type}/image", method = RequestMethod.POST)
    public ResponseEntity<RendererResult> getMoleculeImagePOST(
            @ApiParam("Representation type.") @PathVariable String type,
            @ApiParam("Molecule structure.") @RequestBody String structure
    ) {
        RendererResult result = calculationService.getStructureWithImage(structure);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
