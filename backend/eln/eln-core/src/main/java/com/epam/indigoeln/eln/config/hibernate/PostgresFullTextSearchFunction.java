package com.epam.indigoeln.eln.config.hibernate;

import org.hibernate.query.ReturnableType;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.function.AbstractSqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SelfRenderingSqmFunction;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.produce.function.FunctionParameterType;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.List;

public class PostgresFullTextSearchFunction extends AbstractSqmFunctionDescriptor {

    PostgresFullTextSearchFunction() {
        super("full_text_search");
    }

    @Override
    protected <T> SelfRenderingSqmFunction<T> generateSqmFunctionExpression(List<? extends SqmTypedNode<?>> arguments, ReturnableType<T> impliedResultType, QueryEngine queryEngine) {
        SqmFunctionRegistry registry = queryEngine.getSqmFunctionRegistry();
        TypeConfiguration types = queryEngine.getTypeConfiguration();
        return registry.patternDescriptorBuilder("full_text_search", "?1 @@ ?2")
                .setExactArgumentCount(2)
                .setParameterTypes(FunctionParameterType.ANY, FunctionParameterType.ANY)
                .setInvariantType(types.standardBasicTypeForJavaType(Boolean.class))
                .descriptor()
                .generateSqmExpression(arguments, impliedResultType, queryEngine);
    }
}
