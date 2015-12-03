package com.epam.indigo.bingoservice.repository.impl;

import com.epam.indigo.Bingo;
import com.epam.indigo.bingoservice.repository.BingoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ReactionRepository extends BingoRepository {

    @Autowired
    public void setReactionDatabase(Bingo reactionDatabase) {
        this.database = reactionDatabase;
    }
}
