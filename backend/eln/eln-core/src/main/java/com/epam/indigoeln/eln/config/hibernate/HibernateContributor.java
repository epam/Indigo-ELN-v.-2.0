package com.epam.indigoeln.eln.config.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class HibernateContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry().register("full_text_search", new PostgresFullTextSearchFunction());
        functionContributions.getFunctionRegistry().register("to_tsquery", new PostgresToTSQueryFunction());
        functionContributions.getFunctionRegistry().register("bingo_substructure_match", new BingoSubstructureMatchFunction());
    }
}
