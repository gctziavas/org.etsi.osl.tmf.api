package org.etsi.osl.services.api.rcm634;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import org.etsi.osl.tmf.rcm634.model.EntityRef;
import org.junit.jupiter.api.Test;

public class EntityRefTest {

    @Test
    void testEntityRef() {
        EntityRef ref = new EntityRef();
        String id = "testId";
        String href = "testHref";
        String name = "testName";
        String referredType = "testReferredType";

        ref.id(id);
        ref.setUuid(id);
        ref.href(href);
        ref.name(name);
        ref._atReferredType(referredType);

        assertEquals(id, ref.getId());
        assertEquals(href, ref.getHref());
        assertEquals(name, ref.getName());
        assertEquals(referredType, ref.getAtReferredType());

        ref = new EntityRef();
        ref.setId(id);
        ref.setUuid(id);
        ref.setHref(href);
        ref.setName(name);
        ref.setAtReferredType(referredType);

        assertEquals(id, ref.getId());
        assertEquals(href, ref.getHref());
        assertEquals(name, ref.getName());
        assertEquals(referredType, ref.getAtReferredType());
    }

    @Test
    void testEquals() {
        String id = "testId";
        String href = "testHref";
        String name = "testName";
        String referredType = "testReferredType";

        EntityRef ref1 = new EntityRef();
        ref1.setUuid(id);
        ref1.setHref(href);
        ref1.setName(name);
        ref1.setAtReferredType(referredType);

        EntityRef ref2 = new EntityRef();
        ref2.setUuid(id);
        ref2.setHref(href);
        ref2.setName(name);
        ref2.setAtReferredType(referredType);

        assertTrue(ref1.equals(ref2));

        ref1.id("differentId");

        assertFalse(ref1.equals(ref2));
    }

    @Test
    void testHashCode() {
        String id = "testId";
        String href = "testHref";
        String name = "testName";
        String referredType = "testReferredType";

        EntityRef ref1 = new EntityRef();
        ref1.setUuid(id);
        ref1.setHref(href);
        ref1.setName(name);
        ref1.setAtReferredType(referredType);

        EntityRef ref2 = new EntityRef();
        ref2.setUuid(id);
        ref2.setHref(href);
        ref2.setName(name);
        ref2.setAtReferredType(referredType);

        assertEquals(ref1.hashCode(), ref2.hashCode());

        ref1.id("differentId");
        ref1.setUuid("differentId");

        assertNotEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    void testToString() {
        EntityRef ref = new EntityRef();

        String id = "testId";
        String href = "testHref";
        String name = "testName";
        String baseType = "testBaseType";
        String schemaLocation = "testSchemaLocation";
        String type = "testType";
        String referredType = "testReferredType";

        ref.id(id);
        ref.setUuid(id);
        ref.setHref(href);
        ref.name(name);
        ref.setBaseType(baseType);
        ref.setSchemaLocation(schemaLocation);
        ref.setType(type);
        ref.setAtReferredType(referredType);

        String expectedString = "class EntityRef {\n" +
                "    id: testId\n" +
                "    href: testHref\n" +
                "    name: testName\n" +
                "    baseType: testBaseType\n" +
                "    schemaLocation: testSchemaLocation\n" +
                "    type: testType\n" +
                "    _atReferredType: testReferredType\n" +
                "}";

        assertEquals(expectedString, ref.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        EntityRef ref = new EntityRef();

        Method method = EntityRef.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String input = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(ref, input);

        assertEquals(expectedOutput, output);
    }
}