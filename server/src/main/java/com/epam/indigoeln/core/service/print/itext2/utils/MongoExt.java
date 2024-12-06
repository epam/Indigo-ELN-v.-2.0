/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.print.itext2.utils;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.mongodb.Function;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Helper wrapper for mongo 'BasicDBObject' class.
 * Used to eliminate boilerplate code while mapping mongo jsons to pdf section models.
 */
public final class MongoExt {
    private Document origin;

    private MongoExt(Document origin) {
        this.origin = origin;
    }

    /**
     * Used for getting MongoExt from input object.
     *
     * @param origin BasicDBObject's instance
     * @return Returns instance of MongoExt for origin
     * @see com.mongodb.BasicDBObject
     */
    public static MongoExt of(Document origin) {
        if (origin != null) {
            return new MongoExt(origin);
        } else {
            return new MongoExt(new Document());
        }
    }

    /**
     * Used for getting MongoExt from input object.
     *
     * @param component Component's instance
     * @return Returns instance of MongoExt for origin for component
     * @see com.epam.indigoeln.core.model.Component
     */
    public static MongoExt of(Component component) {
        if (component.getContent() != null) {
            return new MongoExt(component.getContent());
        } else {
            return new MongoExt(new Document());
        }
    }

    public List<AbstractPdfSection> map(Function<MongoExt, List<AbstractPdfSection>> function) {
        return function.apply(this);
    }

    public StreamEx<MongoExt> streamObjects(String field) {
        List<Document> array = (List<Document>) origin.get(field);
        return Objects.nonNull(array)
                ? StreamEx.of(array).select(Document.class).map(MongoExt::of)
                : StreamEx.empty();
    }

    public StreamEx<String> streamStrings(String field) {
        List<String> array = (List<String>) origin.get(field);
        return Objects.nonNull(array)
                ? StreamEx.of(array).select(String.class)
                : StreamEx.empty();
    }

    /**
     * example:
     * json: {array: [{field: "1"}, {field: "2"}, {field: "3"}]}.
     * arrayFields("array", "field") -> List(1, 2, 3).
     *
     * @param arrayFieldName Array's name
     * @param entryFieldName Field's name
     * @return Stream of fields.
     */
    private StreamEx<String> arrayFields(String arrayFieldName, String entryFieldName) {
        return streamObjects(arrayFieldName).map(o -> o.getString(entryFieldName));
    }

    private String joinArray(String arrayFieldName, String entryFieldName, String delimeter) {
        return arrayFields(arrayFieldName, entryFieldName).joining(delimeter);
    }

    public String joinArray(String arrayFieldName, String entryFieldName) {
        return joinArray(arrayFieldName, entryFieldName, ", ");
    }

    public Object get(String field) {
        return origin.get(field);
    }

    public MongoExt getObject(String field) {
        Document object = (Document) get(field);
        return MongoExt.of(object);
    }

    public Date getDate(String field) {
        return origin.getDate(field);
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
        return findDeepestObject(path).map(o -> o.origin.get(lastField)).map(Object::toString).orElse(StringUtils.EMPTY);
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
