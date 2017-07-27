package com.epam.indigoeln.core.service.print.itext2;

import com.epam.indigoeln.core.service.print.itext2.utils.MongoExt;
import com.mongodb.BasicDBObject;
import one.util.streamex.StreamEx;
import org.junit.Test;

import static org.junit.Assert.*;

public class MongoExtTest {
    @Test
    public void getObject() throws Exception {
        BasicDBObject object = BasicDBObject.parse("{obj: {a : \"1\"}}");
        assertEquals("1", MongoExt.of(object).getObject("obj").getString("a"));
        assertEquals(null, MongoExt.of(object).getObject("XXX"));
    }

    @Test
    public void testGetStringByPath() throws Exception {
        BasicDBObject simpleObject = BasicDBObject.parse("{a: \"value\"}");
        assertEquals("value", MongoExt.of(simpleObject).getString("a"));
        assertEquals(null, MongoExt.of(simpleObject).getString("X"));

        BasicDBObject shallowObject = BasicDBObject.parse("{a: {b: \"value\"}}");
        assertEquals("value", MongoExt.of(shallowObject).getString("a", "b"));
        assertEquals(null, MongoExt.of(shallowObject).getString("X", "b"));
        assertEquals(null, MongoExt.of(shallowObject).getString("a", "X"));
        assertEquals(null, MongoExt.of(shallowObject).getString("X", "X"));

        BasicDBObject deepObject = BasicDBObject.parse("{a: {b: {c: \"value\"}}}");
        assertEquals("value", MongoExt.of(deepObject).getString("a", "b", "c"));
        assertEquals(null, MongoExt.of(deepObject).getString("X", "b", "c"));
        assertEquals(null, MongoExt.of(deepObject).getString("a", "X", "c"));
        assertEquals(null, MongoExt.of(deepObject).getString("a", "b", "X"));
    }

    @Test(expected = Exception.class)
    public void testGetStringByPathWithWrongTailPath() throws Exception {
        BasicDBObject deepObject = BasicDBObject.parse("{a: {b: {c: \"value\"}}}");
        MongoExt.of(deepObject).getString("a", "b", "c", "d", "e");
    }

    @Test
    public void testStreamObjects() throws Exception {
        BasicDBObject json = BasicDBObject.parse("{array: [{a: \"1\"}, {a: \"2\"}, {a: \"3\"}]}");
        StreamEx<MongoExt> objectsStream = MongoExt.of(json).streamObjects("array");
        String joining = objectsStream.map(it -> it.getString("a")).joining();
        assertEquals("123", joining);
    }

    @Test
    public void testStreamObjectsForNonexistingArray() throws Exception {
        BasicDBObject json = BasicDBObject.parse("{array: [{a: \"1\"}, {a: \"2\"}, {a: \"3\"}]}");
        assertTrue(MongoExt.of(json).streamObjects("XXX").toList().isEmpty());
    }

    @Test
    public void testStreamObjectsForEmptyArray() throws Exception {
        BasicDBObject json = BasicDBObject.parse("{array: []}");
        assertTrue(MongoExt.of(json).streamObjects("array").toList().isEmpty());
    }
}
