package org.etsi.osl.services.api.rcm634;

import org.etsi.osl.tmf.rcm634.model.ConnectionPointSpecificationRef;
import org.etsi.osl.tmf.rcm634.model.EndpointSpecificationRef;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;

public class EndpointSpecificationRefTest {
    @Test
    void testName() {
        EndpointSpecificationRef ref = new EndpointSpecificationRef();
        String name = "testName";
        ref.setName(name);
        assertEquals(name, ref.getName());

        EndpointSpecificationRef newRef = ref.name("newTestName");
        assertEquals(ref, newRef);
    }

    @Test
    void testConnectionPointSpecification() {
        EndpointSpecificationRef ref = new EndpointSpecificationRef();
        ConnectionPointSpecificationRef connectionPointSpecificationRef = new ConnectionPointSpecificationRef();
        ref.setConnectionPointSpecification(connectionPointSpecificationRef);
        assertEquals(connectionPointSpecificationRef, ref.getConnectionPointSpecification());

        EndpointSpecificationRef newRef = ref.connectionPointSpecification(connectionPointSpecificationRef.name("newRef"));
        assertEquals(ref, newRef);
    }

    @Test
    void testEndpointSpecificationRef() {
        EndpointSpecificationRef ref = new EndpointSpecificationRef();
        String id = "testId";
        String href = "testHref";
        Boolean isRoot = true;
        String role = "testRole";
        String referredType = "testReferredType";

        ref.id(id);
        ref.setUuid(id);
        ref.href(href);
        ref.isRoot(isRoot);
        ref.role(role);
        ref._atReferredType(referredType);

        assertEquals(id, ref.getId());
        assertEquals(href, ref.getHref());
        assertEquals(isRoot, ref.isIsRoot());
        assertEquals(role, ref.getRole());
        assertEquals(referredType, ref.getAtReferredType());

        ref = new EndpointSpecificationRef();
        ref.setUuid(id);
        ref.setHref(href);
        ref.setIsRoot(isRoot);
        ref.setRole(role);
        ref.setAtReferredType(referredType);

        assertEquals(id, ref.getId());
        assertEquals(href, ref.getHref());
        assertEquals(isRoot, ref.isIsRoot());
        assertEquals(role, ref.getRole());
        assertEquals(referredType, ref.getAtReferredType());
    }

    @Test
    void testEquals() {
        String id = "testId";
        String href = "testHref";
        Boolean isRoot = true;
        String role = "testRole";
        String referredType = "testReferredType";

        EndpointSpecificationRef ref1 = new EndpointSpecificationRef();
        ref1.setUuid(id);
        ref1.setHref(href);
        ref1.setIsRoot(isRoot);
        ref1.setRole(role);
        ref1.setAtReferredType(referredType);

        EndpointSpecificationRef ref2 = new EndpointSpecificationRef();
        ref2.setUuid(id);
        ref2.setHref(href);
        ref2.setIsRoot(isRoot);
        ref2.setRole(role);
        ref2.setAtReferredType(referredType);

        assertTrue(ref1.equals(ref2));

        ref1.id("differentId");

        assertFalse(ref1.equals(ref2));
    }

    @Test
    void testHashCode() {
        String id = "testId";
        String href = "testHref";
        Boolean isRoot = true;
        String role = "testRole";
        String referredType = "testReferredType";

        EndpointSpecificationRef ref1 = new EndpointSpecificationRef();
        ref1.setUuid(id);
        ref1.setHref(href);
        ref1.setIsRoot(isRoot);
        ref1.setRole(role);
        ref1.setAtReferredType(referredType);

        EndpointSpecificationRef ref2 = new EndpointSpecificationRef();
        ref2.setUuid(id);
        ref2.setHref(href);
        ref2.setIsRoot(isRoot);
        ref2.setRole(role);
        ref2.setAtReferredType(referredType);

        assertEquals(ref1.hashCode(), ref2.hashCode());

        ref1.id("differentId");

        assertNotEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    void testToString() {
        EndpointSpecificationRef ref = new EndpointSpecificationRef();

        String id = "testId";
        String href = "testHref";
        Boolean isRoot = true;
        String role = "testRole";
        String baseType = "testBaseType";
        String schemaLocation = "testSchemaLocation";
        String type = "testType";
        String referredType = "testReferredType";

        ref.id(id);
        ref.setUuid(id);
        ref.setHref(href);
        ref.isRoot(isRoot);
        ref.role(role);
        ref.setBaseType(baseType);
        ref.setSchemaLocation(schemaLocation);
        ref.setType(type);
        ref.setAtReferredType(referredType);


        String expectedString = "class EndpointSpecificationRef {\n" +
                "    id: testId\n" +
                "    href: testHref\n" +
                "    isRoot: true\n" +
                "    name: null\n" +
                "    role: testRole\n" +
                "    connectionPointSpecification: null\n" +
                "    _atBaseType: testBaseType\n" +
                "    _atSchemaLocation: testSchemaLocation\n" +
                "    _atType: testType\n" +
                "    _atReferredType: testReferredType\n" +
                "}";

        assertEquals(expectedString, ref.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        EndpointSpecificationRef ref = new EndpointSpecificationRef();

        Method method = EndpointSpecificationRef.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String input = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(ref, input);

        assertEquals(expectedOutput, output);
    }
}