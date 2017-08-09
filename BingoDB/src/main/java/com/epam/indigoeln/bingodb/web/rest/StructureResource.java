package com.epam.indigoeln.bingodb.web.rest;

import com.epam.indigoeln.bingodb.service.BingoService;
import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping(
        value = "/api/structures",
        consumes = MediaType.TEXT_PLAIN_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class StructureResource {

    private final BingoService bingoService;

    @Autowired
    public StructureResource(BingoService bingoService) {
        this.bingoService = bingoService;
    }

    @ApiOperation("Insert a new structure")
    @PostMapping
    public ResponseEntity<ResponseDTO> insert(@ApiParam("New structure in Molfile/Rxnfile/Smiles format") @RequestBody String s) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.insert(s))));
    }

    @ApiOperation("Update structure with given id")
    @PutMapping("{id}")
    public ResponseEntity<ResponseDTO> update(@ApiParam("Structure id") @PathVariable String id,
                                              @ApiParam("New structure in Molfile/Rxnfile/Smiles format") @RequestBody String s) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.update(id, s))));
    }

    @ApiOperation("Delete structure with given id")
    @DeleteMapping(value = "{id}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Void> delete(@ApiParam("Structure id") @PathVariable String id) {
        bingoService.delete(id);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Return structure with given id")
    @GetMapping(value = "{id}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<ResponseDTO> getById(@ApiParam("Structure id") @PathVariable String id) {
        return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.getById(id))));
    }
}
