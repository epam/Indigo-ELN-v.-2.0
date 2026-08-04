package com.epam.indigoeln.eln.config.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.spi.TypeConfiguration;

public class HibernateContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        TypeConfiguration types = functionContributions.getTypeConfiguration();

        functionContributions.getFunctionRegistry().registerPattern(
                "full_text_search",
                "(?1 @@ websearch_to_tsquery(?2, ?3))",
                types.standardBasicTypeForJavaType(Boolean.class)
        );
        functionContributions.getFunctionRegistry().registerPattern(
                "bingo_exact_match",
                "(?1 @ (?2, ?3)::bingo.exact)",
                types.standardBasicTypeForJavaType(Boolean.class)
        );
        functionContributions.getFunctionRegistry().registerPattern(
                "bingo_substructure_match",
                "(?1 @ (?2, ?3)::bingo.sub)",
                types.standardBasicTypeForJavaType(Boolean.class)
        );
        functionContributions.getFunctionRegistry().registerPattern(
                "bingo_similarity_match",
                "(?1 @ (?2, ?3, ?4, ?5)::bingo.sim)",
                types.standardBasicTypeForJavaType(Boolean.class)
        );
    }
}
