package com.epam.indigoeln.eln.config.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.spi.TypeConfiguration;

public class HibernateContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        TypeConfiguration types = functionContributions.getTypeConfiguration();

        // full text search
        functionContributions.getFunctionRegistry().registerPattern(
                "full_text_search",
                "(?1 @@ websearch_to_tsquery(?2, ?3))",
                types.standardBasicTypeForJavaType(Boolean.class)
        );
        functionContributions.getFunctionRegistry().registerPattern(
                "ts_rank",
                "(ts_rank(?1, websearch_to_tsquery(?2, ?3)))",
                types.standardBasicTypeForJavaType(Double.class)
        );
        functionContributions.getFunctionRegistry().registerPattern(
                "ts_headline",
                "(ts_headline(?1, ?2, websearch_to_tsquery(?3, ?4), ?5))",
                types.standardBasicTypeForJavaType(String.class)
        );

        // molecule search
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
        functionContributions.getFunctionRegistry().registerPattern(
                "bingo_getsimilarity",
                "(bingo.getsimilarity(?1, ?2, ?3))",
                types.standardBasicTypeForJavaType(Double.class)
        );

        // reaction search
        functionContributions.getFunctionRegistry().registerPattern(
                "bingo_rexact_match",
                "(?1 @ (?2, ?3)::bingo.rexact)",
                types.standardBasicTypeForJavaType(Boolean.class)
        );
        functionContributions.getFunctionRegistry().registerPattern(
                "bingo_rsubstructure_match",
                "(?1 @ (?2, ?3)::bingo.rsub)",
                types.standardBasicTypeForJavaType(Boolean.class)
        );
    }
}
