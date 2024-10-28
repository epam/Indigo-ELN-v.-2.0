/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of BingoDB.
 *
 *  BingoDB is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BingoDB is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with BingoDB.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.bingodb.web.rest;

import com.epam.indigoeln.bingodb.service.BingoService;
import com.epam.indigoeln.bingodb.web.rest.dto.ErrorDTO;
import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * REST API for structures operations.
 */
@RestController
@RequestMapping("api/structures")
public class StructureResource {

    /**
     * Service instance for work with structure databases.
     */
    private final BingoService bingoService;

    /**
     * Create a new StructureResource instance.
     *
     * @param bingoService service instance for work with structure databases
     */
    @Autowired
    public StructureResource(BingoService bingoService) {
        this.bingoService = bingoService;
    }

    /**
     * Insert a new structure.
     *
     * @param structure molecule or reaction in Molfile/Rxnfile/Smiles format
     * @return REST response with OK status and inserted structure with its ID
     */
    @ApiOperation("Insert a new structure")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Structure inserted"),
            @ApiResponse(
                    code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    message = "Server error occurred",
                    response = ErrorDTO.class)
    })
    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> insert(
            @ApiParam("Molecule or reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.insert(structure))));
    }

    /**
     * Update structure with given ID.
     *
     * @param id        structure ID
     * @param structure molecule or reaction in Molfile/Rxnfile/Smiles format
     * @return REST response with OK status and updated structure with its ID
     */
    @ApiOperation("Update structure with given ID")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Structure updated"),
            @ApiResponse(
                    code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    message = "Server error occurred",
                    response = ErrorDTO.class)
    })
    @PutMapping(value = "{id}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> update(
            @ApiParam("Structure ID") @PathVariable String id,
            @ApiParam("Molecule or reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.update(id, structure))));
    }

    /**
     * Delete structure with given ID.
     *
     * @param id structure ID
     * @return REST response with OK status
     */
    @ApiOperation("Delete structure with given ID")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Structure deleted"),
            @ApiResponse(
                    code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    message = "Server error occurred",
                    response = ErrorDTO.class)
    })
    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> delete(@ApiParam("Structure ID") @PathVariable String id) {
        bingoService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieve structure with given ID.
     *
     * @param id structure ID
     * @return REST response with OK status and found structure with its ID
     */
    @ApiOperation("Retrieve structure with given ID")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found structure"),
            @ApiResponse(
                    code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    message = "Server error occurred",
                    response = ErrorDTO.class)
    })
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> getById(@ApiParam("Structure ID") @PathVariable String id) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.getById(id))));
    }
}
