package com.epam.indigoeln.bingodb.web;

import com.epam.indigoeln.bingodb.service.BingoService;
import com.epam.indigoeln.bingodb.web.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/structures")
public class StructureResource {

    private static final Logger log = LoggerFactory.getLogger(StructureResource.class);

    @Autowired
    private BingoService bingoService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ResponseDTO> insert(@RequestBody String s) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.insert(s))));
        } catch (Exception e) {
            log.warn("Cannot insert structure: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot insert structure"));
    }

    @RequestMapping(method = RequestMethod.PUT, path = "{id}")
    public ResponseEntity<ResponseDTO> update(@PathVariable String id, @RequestBody String s) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.update(id, s))));
        } catch (Exception e) {
            log.warn("Cannot update structure with id=" + id + ": " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot update structure with id=" + id));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "{id}")
    public ResponseEntity<ResponseDTO> delete(@PathVariable String id) {
        try {
            bingoService.delete(id);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.warn("Cannot delete structure with id=" + id + ": " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot delete structure with id=" + id));
    }

    @RequestMapping(method = RequestMethod.GET, path = "{id}")
    public ResponseEntity<ResponseDTO> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(Collections.singletonList(bingoService.getById(id))));
        } catch (Exception e) {
            log.warn("Cannot load structure with id=" + id + ": " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot load structure with id=" + id));
    }
}
