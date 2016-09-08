package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.search.AbstractSearchAggregationBuilder;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class NotebookSearchAggregationBuilder extends AbstractSearchAggregationBuilder {

    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_ACCESS_LIST = "accessList";
    public static final String FIELD_CREATION_DATE = "creationDate";
    public static final String FIELD_KIND = "kind";

    public static final String FIELD_AUTHOR_ID = FIELD_AUTHOR + "._id";

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList(FIELD_DESCRIPTION, FIELD_NAME);
    private static final List<String> AVAILABLE_FIELDS = Arrays.asList(FIELD_DESCRIPTION, FIELD_NAME, FIELD_AUTHOR_ID, FIELD_KIND);

    private NotebookSearchAggregationBuilder() {
        aggregationOperations = new ArrayList<>();
        setSearchQueryFields(SEARCH_QUERY_FIELDS);
        setAvailableFields(AVAILABLE_FIELDS);
        final int packageLength = Notebook.class.getPackage().getName().length() + 1;
        aggregationOperations.add(
                Aggregation.project(FIELD_DESCRIPTION, FIELD_NAME, FIELD_AUTHOR, FIELD_ACCESS_LIST, FIELD_CREATION_DATE)
                        .andExpression("substr(_class, " + packageLength + ", -1)").as(FIELD_KIND)
        );
    }

    public static NotebookSearchAggregationBuilder getInstance() {
        return new NotebookSearchAggregationBuilder();
    }

    public Optional<Aggregation> build() {
        return Optional.ofNullable(aggregationOperations.isEmpty() ? null : Aggregation.newAggregation(aggregationOperations));
    }

}
