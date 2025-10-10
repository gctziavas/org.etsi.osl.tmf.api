package org.etsi.osl.services.api.rcm634;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Set;
import org.etsi.osl.tmf.rcm634.model.ConnectionSpecification;
import org.etsi.osl.tmf.rcm634.model.EndpointSpecificationRef;
import org.junit.jupiter.api.Test;

public class ConnectionSpecificationTest {

    @Test
    void testConnectionSpecification() {
        ConnectionSpecification spec = new ConnectionSpecification();
        String id = "testId";
        String href = "testHref";
        String associationType = "testAssociationType";
        String name = "testName";
        Set<EndpointSpecificationRef> endpointSpecification = new HashSet<>();
        String baseType = "testBaseType";
        String schemaLocation = "testSchemaLocation";
        String type = "testType";

        spec.id(id);
        spec.setUuid(id);
        spec.href(href);
        spec.associationType(associationType);
        spec.name(name);
        spec.endpointSpecification(endpointSpecification);
        spec._atBaseType(baseType);
        spec._atSchemaLocation(schemaLocation);
        spec._atType(type);

        assertEquals(id, spec.getId());
        assertEquals(href, spec.getHref());
        assertEquals(associationType, spec.getAssociationType());
        assertEquals(name, spec.getName());
        assertEquals(endpointSpecification, spec.getEndpointSpecification());
        assertEquals(baseType, spec.getAtBaseType());
        assertEquals(schemaLocation, spec.getAtSchemaLocation());
        assertEquals(type, spec.getAtType());

        String expectedString = "class ConnectionSpecification {\n"
                + "    id: " + id + "\n"
                + "    href: " + href + "\n"
                + "    associationType: " + associationType + "\n"
                + "    name: " + name + "\n"
                + "    endpointSpecification: " + endpointSpecification.toString().replace("\n", "\n    ") + "\n"
                + "    _atBaseType: " + baseType + "\n"
                + "    _atSchemaLocation: " + schemaLocation + "\n"
                + "    _atType: " + type + "\n"
                + "}";

        assertEquals(expectedString, spec.toString());

        ConnectionSpecification spec2 = new ConnectionSpecification();
        spec2.id(id);
        spec2.href(href);
        spec2.associationType(associationType);
        spec2.name(name);
        spec2.endpointSpecification(endpointSpecification);
        spec2._atBaseType(baseType);
        spec2._atSchemaLocation(schemaLocation);
        spec2._atType(type);

        assertTrue(spec.equals(spec2));
        assertEquals(spec.hashCode(), spec2.hashCode());

        spec.id("differentId");

        assertFalse(spec.equals(spec2));
        assertNotEquals(spec.hashCode(), spec2.hashCode());
    }

    @Test
    void testSetters() {
        ConnectionSpecification spec = new ConnectionSpecification();
        String id = "testId";
        String href = "testHref";
        String associationType = "testAssociationType";
        String name = "testName";
        Set<EndpointSpecificationRef> endpointSpecification = new HashSet<>();
        String baseType = "testBaseType";
        String schemaLocation = "testSchemaLocation";
        String type = "testType";

        spec.setUuid(id);
        spec.id(id);
        spec.setHref(href);
        spec.setAssociationType(associationType);
        spec.setName(name);
        spec.setEndpointSpecification(endpointSpecification);
        spec.setAtBaseType(baseType);
        spec.setAtSchemaLocation(schemaLocation);
        spec.setAtType(type);

        assertEquals(id, spec.getId());
        assertEquals(href, spec.getHref());
        assertEquals(associationType, spec.getAssociationType());
        assertEquals(name, spec.getName());
        assertEquals(endpointSpecification, spec.getEndpointSpecification());
        assertEquals(baseType, spec.getAtBaseType());
        assertEquals(schemaLocation, spec.getAtSchemaLocation());
        assertEquals(type, spec.getAtType());
    }

    @Test
    void testAddEndpointSpecificationItem() {
        ConnectionSpecification spec = new ConnectionSpecification();
        EndpointSpecificationRef endpointSpecRef = new EndpointSpecificationRef();
        endpointSpecRef.id("testId");

        spec.addEndpointSpecificationItem(endpointSpecRef);

        assertTrue(spec.getEndpointSpecification().contains(endpointSpecRef));
    }
}