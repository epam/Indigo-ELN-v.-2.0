package com.epam.indigoeln.bingoservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BingoService {

    @Value("${bingo.database.folder}")
    private String databaseFolder;
}
