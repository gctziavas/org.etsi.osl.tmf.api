package org.etsi.osl.services.api.rcm634;

import org.etsi.osl.tmf.rcm634.model.ConnectionPointSpecificationRef;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;

public class ConnectionPointSpecificationRefTest {

    @Test
    void testConnectionPointSpecificationRef() {
        ConnectionPointSpecificationRef ref = new ConnectionPointSpecificationRef();
        String id = "testId";
        String href = "testHref";
        String name = "testName";
        String version = "testVersion";
        String referredType = "testReferredType";

        ref.id(id);
        ref.setUuid(id);
        ref.href(href);
        ref.name(name);
        ref.version(version);
        ref._atReferredType(referredType);

        assertEquals(id, ref.getId());
        assertEquals(href, ref.getHref());
        assertEquals(name, ref.getName());
        assertEquals(version, ref.getVersion());
        assertEquals(referredType, ref.getAtReferredType());

        ref = new ConnectionPointSpecificationRef();
        ref.setUuid(id);
        ref.setHref(href);
        ref.setName(name);
        ref.setVersion(version);
        ref.setAtReferredType(referredType);

        assertEquals(id, ref.getId());
        assertEquals(href, ref.getHref());
        assertEquals(name, ref.getName());
        assertEquals(version, ref.getVersion());
        assertEquals(referredType, ref.getAtReferredType());
    }

    @Test
    void testEquals() {
        String id = "testId";
        String href = "testHref";
        String name = "testName";
        String version = "testVersion";
        String referredType = "testReferredType";

        ConnectionPointSpecificationRef ref1 = new ConnectionPointSpecificationRef();
        ref1.setUuid(id);
        ref1.setHref(href);
        ref1.setName(name);
        ref1.setVersion(version);
        ref1.setAtReferredType(referredType);

        ConnectionPointSpecificationRef ref2 = new ConnectionPointSpecificationRef();
        ref2.setUuid(id);
        ref2.setHref(href);
        ref2.setName(name);
        ref2.setVersion(version);
        ref2.setAtReferredType(referredType);


        assertTrue(ref1.equals(ref2));
        assertEquals(ref1.hashCode(), ref2.hashCode());

        ref1.id("differentId");

        assertFalse(ref1.equals(ref2));
        assertNotEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    void testToString() {
        ConnectionPointSpecificationRef ref = new ConnectionPointSpecificationRef();

        String id = "testId";
        String href = "testHref";
        String name = "testName";
        String version = "testVersion";
        String baseType = "testBaseType";
        String schemaLocation = "testSchemaLocation";
        String type = "testType";
        String referredType = "testReferredType";

        ref.id(id);
        ref.setUuid(id);
        ref.setHref(href);
        ref.setName(name);
        ref.setVersion(version);
        ref.setBaseType(baseType);
        ref.setSchemaLocation(schemaLocation);
        ref.setType(type);
        ref.setAtReferredType(referredType);

        String expectedString = "class ConnectionPointSpecificationRef {\n"
                + "    id: " + id + "\n"
                + "    href: " + href + "\n"
                + "    name: " + name + "\n"
                + "    version: " + version + "\n"
                + "    _atBaseType: " + baseType + "\n"
                + "    _atSchemaLocation: " + schemaLocation + "\n"
                + "    _atType: " + type + "\n"
                + "    _atReferredType: " + referredType + "\n"
                + "}";

        assertEquals(expectedString, ref.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        ConnectionPointSpecificationRef ref = new ConnectionPointSpecificationRef();

        Method method = ConnectionPointSpecificationRef.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String input = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(ref, input);

        assertEquals(expectedOutput, output);
    }
}
