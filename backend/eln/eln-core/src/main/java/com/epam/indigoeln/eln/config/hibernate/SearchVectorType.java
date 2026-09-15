package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.util.SearchVector;
import org.hibernate.type.descriptor.WrapperOptions;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchVectorType extends AbstractJsonUserType<SearchVector> {

    SearchVectorType() {
        super(SearchVector.class);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    @Nullable
    public SearchVector deepCopy(@Nullable SearchVector value) {
        // TODO deep copy
        return value != null ? new SearchVector(value.getA(), value.getB(), value.getC(), value.getD()) : null;
    }

    @Override
    @Nullable
    public SearchVector nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        throw new UnsupportedOperationException("Shouldn't be read");
    }
}
