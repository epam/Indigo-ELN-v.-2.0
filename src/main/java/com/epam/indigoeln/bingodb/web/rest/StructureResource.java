package com.epam.indigoeln.bingodb.web.rest;

import com.epam.indigoeln.bingodb.service.BingoService;
import com.epam.indigoeln.bingodb.web.rest.dto.ErrorDTO;
import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@RestController
@RequestMapping("api/structures")
public class StructureResource {

    private final BingoService bingoService;

    @Autowired
    public StructureResource(BingoService bingoService) {
        this.bingoService = bingoService;
    }

    @ApiOperation("Insert a new structure")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Structure inserted"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> insert(@ApiParam("Molecule or Reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.insert(structure))));
    }

    @ApiOperation("Update structure with given ID")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Structure updated"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PutMapping(value = "{id}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> update(@ApiParam("Structure ID") @PathVariable String id,
                                              @ApiParam("Molecule or Reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.update(id, structure))));
    }

    @ApiOperation("Delete structure with given ID")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Structure deleted"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> delete(@ApiParam("Structure ID") @PathVariable String id) {
        bingoService.delete(id);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Retrieve structure with given ID")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found structure"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> getById(@ApiParam("Structure ID") @PathVariable String id) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.getById(id))));
    }
}
