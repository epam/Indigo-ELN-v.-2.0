package com.epam.indigoeln.eln.config.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class HibernateContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry().register(PostgresFullTextSearchFunction.NAME, new PostgresFullTextSearchFunction());
        functionContributions.getFunctionRegistry().register(PostgresWebSearchToTSQueryFunction.NAME, new PostgresWebSearchToTSQueryFunction());
        functionContributions.getFunctionRegistry().register(PostgresILikeFunction.NAME, new PostgresILikeFunction());
        functionContributions.getFunctionRegistry().register(BingoFunctions.Substructure.NAME, new BingoFunctions.Substructure());
        functionContributions.getFunctionRegistry().register(BingoFunctions.Exact.NAME, new BingoFunctions.Exact());
        functionContributions.getFunctionRegistry().register(BingoFunctions.Similarity.NAME, new BingoFunctions.Similarity());
    }
}
