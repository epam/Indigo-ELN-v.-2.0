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

public class BingoFunctions {

    public static class Substructure extends AbstractSqmFunctionDescriptor {

        public static final String NAME = "bingo_substructure_match";

        Substructure() {
            super(NAME);
        }

        @Override
        protected <T> SelfRenderingSqmFunction<T> generateSqmFunctionExpression(List<? extends SqmTypedNode<?>> arguments, ReturnableType<T> impliedResultType, QueryEngine queryEngine) {
            SqmFunctionRegistry registry = queryEngine.getSqmFunctionRegistry();
            TypeConfiguration types = queryEngine.getTypeConfiguration();
            return registry.patternDescriptorBuilder(NAME, "?1 @ (?2, ?3)::bingo.sub")
                    .setExactArgumentCount(3)
                    .setParameterTypes(FunctionParameterType.ANY, FunctionParameterType.ANY, FunctionParameterType.ANY)
                    .setInvariantType(types.standardBasicTypeForJavaType(Boolean.class))
                    .descriptor()
                    .generateSqmExpression(arguments, impliedResultType, queryEngine);
        }
    }

    public static class Exact extends AbstractSqmFunctionDescriptor {

        public static final String NAME = "bingo_exact_match";

        Exact() {
            super(NAME);
        }

        @Override
        protected <T> SelfRenderingSqmFunction<T> generateSqmFunctionExpression(List<? extends SqmTypedNode<?>> arguments, ReturnableType<T> impliedResultType, QueryEngine queryEngine) {
            SqmFunctionRegistry registry = queryEngine.getSqmFunctionRegistry();
            TypeConfiguration types = queryEngine.getTypeConfiguration();
            return registry.patternDescriptorBuilder(NAME, "?1 @ (?2, ?3)::bingo.exact")
                    .setExactArgumentCount(3)
                    .setParameterTypes(FunctionParameterType.ANY, FunctionParameterType.ANY, FunctionParameterType.ANY)
                    .setInvariantType(types.standardBasicTypeForJavaType(Boolean.class))
                    .descriptor()
                    .generateSqmExpression(arguments, impliedResultType, queryEngine);
        }
    }

    public static class Similarity extends AbstractSqmFunctionDescriptor {

        public static final String NAME = "bingo_similarity_search";

        Similarity() {
            super(NAME);
        }

        @Override
        protected <T> SelfRenderingSqmFunction<T> generateSqmFunctionExpression(List<? extends SqmTypedNode<?>> arguments, ReturnableType<T> impliedResultType, QueryEngine queryEngine) {
            SqmFunctionRegistry registry = queryEngine.getSqmFunctionRegistry();
            TypeConfiguration types = queryEngine.getTypeConfiguration();
            return registry.patternDescriptorBuilder(NAME, "?1 @ (?2, ?3, ?4, ?5)::bingo.sim")
                    .setExactArgumentCount(3)
                    .setParameterTypes(FunctionParameterType.ANY, FunctionParameterType.ANY, FunctionParameterType.ANY, FunctionParameterType.ANY, FunctionParameterType.ANY)
                    .setInvariantType(types.standardBasicTypeForJavaType(Boolean.class))
                    .descriptor()
                    .generateSqmExpression(arguments, impliedResultType, queryEngine);
        }
    }
}
