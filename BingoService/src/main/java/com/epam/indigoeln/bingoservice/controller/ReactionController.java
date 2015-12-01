package com.epam.indigoeln.bingoservice.controller;

import com.epam.indigoeln.bingoservice.service.BingoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReactionController {

    @Autowired
    private BingoService bingoService;
}
