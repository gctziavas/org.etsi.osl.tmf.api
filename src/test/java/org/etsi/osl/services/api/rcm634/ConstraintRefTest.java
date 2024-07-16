package org.etsi.osl.services.api.rcm634;

import org.etsi.osl.tmf.rcm634.model.ConstraintRef;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConstraintRefTest {

    @Test
    void testConstraintRef() {
        ConstraintRef ref = new ConstraintRef();
        String id = "testId";
        String href = "testHref";
        String name = "testName";
        String version = "testVersion";
        String referredType = "testReferredType";

        String baseType = "BaseEntity";
        String schemaLocation = "null";
        String type = "null";

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

        String expectedString = "class ConstraintRef {\n"
                + "    id: " + id + "\n"
                + "    href: " + href + "\n"
                + "    name: " + name + "\n"
                + "    version: " + version + "\n"
                + "    baseType: " + baseType + "\n"
                + "    schemaLocation: " + schemaLocation + "\n"
                + "    type: " + type + "\n"
                + "    _atReferredType: " + referredType + "\n"
                + "}";

        assertEquals(expectedString, ref.toString());

        ConstraintRef ref2 = new ConstraintRef();
        ref2.id(id);
        ref2.setUuid(id);
        ref2.href(href);
        ref2.name(name);
        ref2.version(version);
        ref2._atReferredType(referredType);

        assertTrue(ref.equals(ref2));
        assertEquals(ref.hashCode(), ref2.hashCode());

        ref.id("differentId");
        ref.setUuid("differentId");

        assertFalse(ref.equals(ref2));
        assertNotEquals(ref.hashCode(), ref2.hashCode());

        ref = new ConstraintRef();

        ref.setId(id);
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
}
