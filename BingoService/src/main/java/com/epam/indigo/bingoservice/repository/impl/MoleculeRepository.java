package com.epam.indigo.bingoservice.repository.impl;

import com.epam.indigo.Bingo;
import com.epam.indigo.bingoservice.repository.BingoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MoleculeRepository extends BingoRepository {

    @Autowired
    public void setMoleculeDatabase(Bingo moleculeDatabase) {
        this.database = moleculeDatabase;
    }
}
