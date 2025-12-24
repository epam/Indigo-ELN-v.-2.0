package com.epam.indigoeln.eln.config.hibernate;

import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.function.AbstractSqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SelfRenderingSqmFunction;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.produce.function.FunctionParameterType;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.List;

public class PostgresWebSearchToTSQueryFunction extends AbstractSqmFunctionDescriptor {

    public static final String NAME = "websearch_to_tsquery";

    PostgresWebSearchToTSQueryFunction() {
        super(NAME);
    }

    @Override
    protected <T> SelfRenderingSqmFunction<T> generateSqmFunctionExpression(List<? extends SqmTypedNode<?>> arguments, ReturnableType<T> impliedResultType, QueryEngine queryEngine) {
        SqmFunctionRegistry registry = queryEngine.getSqmFunctionRegistry();
        TypeConfiguration types = queryEngine.getTypeConfiguration();
        return registry.patternDescriptorBuilder(NAME, "websearch_to_tsquery(?1, ?2)")
                .setExactArgumentCount(2)
                .setParameterTypes(FunctionParameterType.STRING, FunctionParameterType.STRING)
                .setInvariantType(types.standardBasicTypeForJavaType(String.class))
                .descriptor()
                .generateSqmExpression(arguments, impliedResultType, queryEngine);
    }
}
