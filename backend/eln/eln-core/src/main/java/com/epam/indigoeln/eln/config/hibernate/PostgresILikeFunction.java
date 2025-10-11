package com.epam.indigoeln.eln.config.hibernate;

import org.hibernate.metamodel.mapping.BasicValuedMapping;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.function.AbstractSqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SelfRenderingSqmFunction;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.produce.function.FunctionParameterType;
import org.hibernate.query.sqm.produce.function.FunctionReturnTypeResolver;
import org.hibernate.query.sqm.sql.SqmToSqlAstConverter;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.spi.TypeConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class PostgresILikeFunction extends AbstractSqmFunctionDescriptor {

    public static final String NAME = "ilike";

    PostgresILikeFunction() {
        super(NAME);
    }

    @Override
    protected <T> SelfRenderingSqmFunction<T> generateSqmFunctionExpression(List<? extends SqmTypedNode<?>> arguments, ReturnableType<T> impliedResultType, QueryEngine queryEngine) {
        SqmFunctionRegistry registry = queryEngine.getSqmFunctionRegistry();
        TypeConfiguration types = queryEngine.getTypeConfiguration();
        return registry.patternDescriptorBuilder(NAME, "(?1 ilike ?2)")
                .setExactArgumentCount(2)
                .setParameterTypes(FunctionParameterType.STRING, FunctionParameterType.STRING)
                .setInvariantType(types.standardBasicTypeForJavaType(String.class))
                .setReturnTypeResolver(new FunctionReturnTypeResolver() {
                    @Override
                    @Nullable
                    public ReturnableType<?> resolveFunctionReturnType(ReturnableType<?> impliedType, @Nullable SqmToSqlAstConverter converter, List<? extends SqmTypedNode<?>> arguments, TypeConfiguration typeConfiguration) {
                        return typeConfiguration.standardBasicTypeForJavaType(Boolean.TYPE);
                    }
                    @Override
                    @Nullable
                    public BasicValuedMapping resolveFunctionReturnType(@Nullable Supplier<BasicValuedMapping> impliedTypeAccess, List<? extends SqlAstNode> arguments) {
                        return impliedTypeAccess != null ? impliedTypeAccess.get() : null;
                    }
                })
                .descriptor()
                .generateSqmExpression(arguments, impliedResultType, queryEngine);
    }
}
