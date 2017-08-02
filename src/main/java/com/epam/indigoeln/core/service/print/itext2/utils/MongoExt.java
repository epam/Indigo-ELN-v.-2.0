package com.epam.indigoeln.core.service.print.itext2.utils;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Function;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Helper wrapper for mongo 'BasicDBObject' class.
 * Used to eliminate boilerplate code while mapping mongo jsons to pdf section models.
 */
public class MongoExt {
    private BasicDBObject origin;

    private MongoExt(BasicDBObject origin) {
        this.origin = origin;
    }

    public static MongoExt of(BasicDBObject origin) {
        if (origin != null) {
            return new MongoExt(origin);
        } else {
            return new MongoExt(new BasicDBObject());
        }
    }

    public static MongoExt of(Component component) {
        if (component.getContent() != null) {
            return new MongoExt(component.getContent());
        } else {
            return new MongoExt(new BasicDBObject());
        }
    }

    public List<AbstractPdfSection> map(Function<MongoExt, List<AbstractPdfSection>> function) {
        return function.apply(this);
    }

    public StreamEx<MongoExt> streamObjects(String field) {
        BasicDBList array = Optional
                .ofNullable(origin.get(field))
                .map(BasicDBList.class::cast)
                .orElse(null);
        return Objects.nonNull(array)
                ? StreamEx.of(array).select(BasicDBObject.class).map(MongoExt::of)
                : StreamEx.empty();
    }

    /**
     * example:
     * json: {array: [{field: "1"}, {field: "2"}, {field: "3"}]}
     * arrayFields("array", "field") -> List(1, 2, 3)
     */
    private StreamEx<String> arrayFields(String arrayFieldName, String entryFieldName) {
        return streamObjects(arrayFieldName).map(o -> o.getString(entryFieldName));
    }

    public String joinArray(String arrayFieldName, String entryFieldName, String delimeter) {
        return arrayFields(arrayFieldName, entryFieldName).joining(delimeter);
    }

    public String joinArray(String arrayFieldName, String entryFieldName) {
        return joinArray(arrayFieldName, entryFieldName, ", ");
    }

    public Object get(String field) {
        return origin.get(field);
    }

    public MongoExt getObject(String field) {
        BasicDBObject object = (BasicDBObject) get(field);
        return MongoExt.of(object);
    }

    /**
     * The method gets a string value by its path in json.
     * It is safe to use if some object in the path does not exist.
     *
     * @param path path in json object
     *             example json: {a : {b {c: "hello" }}}
     *             example usage: getString("a", "b", "c")
     * @return {@code null} if some of intermidiate object does not exist
     */
    public String getString(String... path) {
        validatePath(path);

        String lastField = path[path.length - 1];
        return findDeepestObject(path).map(o -> o.origin.getString(lastField)).orElse(StringUtils.EMPTY);
    }

    private Optional<MongoExt> findDeepestObject(String[] path) {
        Optional<MongoExt> currentObject = Optional.of(this);

        for (int fieldId = 0; fieldId < path.length - 1; fieldId++) {
            String field = path[fieldId];
            currentObject = currentObject.map(o -> o.getObject(field));
        }

        return currentObject;
    }

    private void validatePath(String[] path) {
        if (path.length == 0) {
            throw new MongoExtException("Path should not be empty");
        }
    }

    private static class MongoExtException extends RuntimeException {
        MongoExtException(String message) {
            super(message);
        }
    }
}
