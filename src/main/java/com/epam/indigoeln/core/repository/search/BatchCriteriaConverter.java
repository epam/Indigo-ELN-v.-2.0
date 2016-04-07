package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchCriteria;
import org.springframework.data.mongodb.core.query.Criteria;

public final class BatchCriteriaConverter {
    private BatchCriteriaConverter() {
    }

    public static Criteria convert(BatchSearchCriteria dto) {
        Criteria criteria = Criteria.where("content." + dto.getField());

        Object value = dto.getValue();
        switch (dto.getCondition().orElse("")) {
            case "contains" :
                criteria.is(value); break; //TODO: implement
            case "starts with" :
                criteria.is(value); break;//TODO: implement
            case "ends with" :
                criteria.is(value); break;//TODO: implement
            case ">" :
                criteria.gt(value); break;
            case ">=" :
                criteria.gte(value); break;
            case "<" :
                criteria.lt(value); break;
            case "<=" :
                criteria.lte(value); break;

            default: criteria.is(value);
        }

        return criteria.is(value);
    }

}
