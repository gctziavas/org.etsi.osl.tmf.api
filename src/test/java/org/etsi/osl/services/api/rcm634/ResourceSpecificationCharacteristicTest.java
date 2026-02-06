package org.etsi.osl.services.api.rcm634;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.common.model.TimePeriod;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecCharRelationship;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationCharacteristic;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationCharacteristicValue;
import org.junit.jupiter.api.Test;

public class ResourceSpecificationCharacteristicTest  extends BaseIT{
    @Test
    void testResourceSpecificationCharacteristicConstructorAndAllProperties() {
        // Create a new ResourceSpecificationCharacteristic object and set its properties
        ResourceSpecificationCharacteristic characteristic1 = new ResourceSpecificationCharacteristic();
        characteristic1.setName("testName1");
        characteristic1.setDescription("testDescription1");
        characteristic1.setValueType("testValueType1");
        characteristic1.setConfigurable(true);
        characteristic1.setValidFor(new TimePeriod());
        characteristic1.setSchemaLocation("testSchemaLocation1");
        characteristic1.setValueSchemaLocation("testValueSchemaLocation1");
        characteristic1.setMinCardinality(1);
        characteristic1.setMaxCardinality(1);
        characteristic1.setIsUnique(true);
        characteristic1.setRegex("testRegex1");
        characteristic1.setExtensible(true);
        characteristic1.setResourceSpecCharRelationship(new HashSet<>());
        characteristic1.setResourceSpecificationCharacteristicValue(new HashSet<>());

        // Create another ResourceSpecificationCharacteristic object using the constructor that takes another ResourceSpecificationCharacteristic object as a parameter
        ResourceSpecificationCharacteristic characteristic2 = new ResourceSpecificationCharacteristic(characteristic1);
        characteristic2.setSchemaLocation("testSchemaLocation1");
        characteristic2.setValueSchemaLocation("testValueSchemaLocation1");

        // Assert that the properties of the two objects are equal
        assertEquals(characteristic1.getName(), characteristic2.getName());
        assertEquals(characteristic1.getDescription(), characteristic2.getDescription());
        assertEquals(characteristic1.getValueType(), characteristic2.getValueType());
        assertEquals(characteristic1.isConfigurable(), characteristic2.isConfigurable());
        assertEquals(characteristic1.getValidFor(), characteristic2.getValidFor());
        assertEquals(characteristic1.getSchemaLocation(), characteristic2.getSchemaLocation());
        assertEquals(characteristic1.getValueSchemaLocation(), characteristic2.getValueSchemaLocation());
        assertEquals(characteristic1.getMinCardinality(), characteristic2.getMinCardinality());
        assertEquals(characteristic1.getMaxCardinality(), characteristic2.getMaxCardinality());
        assertEquals(characteristic1.isIsUnique(), characteristic2.isIsUnique());
        assertEquals(characteristic1.getRegex(), characteristic2.getRegex());
        assertEquals(characteristic1.isExtensible(), characteristic2.isExtensible());
        assertEquals(characteristic1.getResourceSpecCharRelationship(), characteristic2.getResourceSpecCharRelationship());
        assertEquals(characteristic1.getResourceSpecCharacteristicValue(), characteristic2.getResourceSpecCharacteristicValue());
    }

    @Test
    void testName() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        String name = "Test Name";
        characteristic.name(name);
        assertEquals(name, characteristic.getName());
    }

    @Test
    void testDescription() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        String description = "Test Description";
        characteristic.description(description);
        assertEquals(description, characteristic.getDescription());
    }

    @Test
    void testValueType() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        String valueType = "Test ValueType";
        characteristic.valueType(valueType);
        assertEquals(valueType, characteristic.getValueType());
    }

    @Test
    void testConfigurable() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        Boolean configurable = true;
        characteristic.configurable(configurable);
        assertEquals(configurable, characteristic.isConfigurable());
    }

    @Test
    void testValidFor() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        TimePeriod validFor = new TimePeriod();
        characteristic.validFor(validFor);
        assertEquals(validFor, characteristic.getValidFor());
    }

    @Test
    void testType() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        String type = "Test Type";
        characteristic.type(type);
        assertEquals(type, characteristic.getType());
    }

    @Test
    void testSchemaLocation() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        String schemaLocation = "Test SchemaLocation";
        characteristic.schemaLocation(schemaLocation);
        assertEquals(schemaLocation, characteristic.getSchemaLocation());
    }

    @Test
    void testValueSchemaLocation() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        String valueSchemaLocation = "Test ValueSchemaLocation";
        characteristic.valueSchemaLocation(valueSchemaLocation);
        assertEquals(valueSchemaLocation, characteristic.getValueSchemaLocation());
    }

    @Test
    void testMinCardinality() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        Integer minCardinality = 1;
        characteristic.minCardinality(minCardinality);
        assertEquals(minCardinality, characteristic.getMinCardinality());
    }

    @Test
    void testMaxCardinality() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        Integer maxCardinality = 10;
        characteristic.maxCardinality(maxCardinality);
        assertEquals(maxCardinality, characteristic.getMaxCardinality());
    }

    @Test
    void testIsUnique() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        Boolean isUnique = true;
        characteristic.isUnique(isUnique);
        assertEquals(isUnique, characteristic.isIsUnique());
    }

    @Test
    void testRegex() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        String regex = "Test Regex";
        characteristic.regex(regex);
        assertEquals(regex, characteristic.getRegex());
    }

    @Test
    void testExtensible() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        Boolean extensible = true;
        characteristic.extensible(extensible);
        assertEquals(extensible, characteristic.isExtensible());
    }

    @Test
    void testResourceSpecCharRelationship() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        Set<ResourceSpecCharRelationship> resourceSpecCharRelationship = new HashSet<>();
        resourceSpecCharRelationship.add(new ResourceSpecCharRelationship());
        characteristic.resourceSpecCharRelationship(resourceSpecCharRelationship);
        assertEquals(resourceSpecCharRelationship, characteristic.getResourceSpecCharRelationship());
    }

    @Test
    void testResourceSpecCharacteristicValue() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        Set<ResourceSpecificationCharacteristicValue> resourceSpecCharacteristicValue = new HashSet<>();
        resourceSpecCharacteristicValue.add(new ResourceSpecificationCharacteristicValue());
        characteristic.ResourceSpecCharacteristicValue(resourceSpecCharacteristicValue);
        assertEquals(resourceSpecCharacteristicValue, characteristic.getResourceSpecCharacteristicValue());
    }

    @Test
    void testAddResourceSpecCharacteristicValueItem() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        ResourceSpecificationCharacteristicValue resourceSpecCharacteristicValueItem = new ResourceSpecificationCharacteristicValue();
        characteristic.addResourceSpecCharacteristicValueItem(resourceSpecCharacteristicValueItem);
        assertTrue(characteristic.getResourceSpecCharacteristicValue().contains(resourceSpecCharacteristicValueItem));
    }

    @Test
    void testEquals() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        characteristic.setName("Test Name");
        characteristic.setDescription("Test Description");
        characteristic.setValueType("Test ValueType");
        characteristic.setConfigurable(true);
        characteristic.setType("Test Type");
        characteristic.setSchemaLocation("Test SchemaLocation");
        characteristic.setValueSchemaLocation("Test ValueSchemaLocation");
        characteristic.setMinCardinality(1);
        characteristic.setMaxCardinality(10);
        characteristic.setIsUnique(true);
        characteristic.setRegex("Test Regex");
        characteristic.setExtensible(true);

        ResourceSpecificationCharacteristic characteristic2 = new ResourceSpecificationCharacteristic();
        characteristic2.setName("Test Name");
        characteristic2.setDescription("Test Description");
        characteristic2.setValueType("Test ValueType");
        characteristic2.setConfigurable(true);
        characteristic2.setType("Test Type");
        characteristic2.setSchemaLocation("Test SchemaLocation");
        characteristic2.setValueSchemaLocation("Test ValueSchemaLocation");
        characteristic2.setMinCardinality(1);
        characteristic2.setMaxCardinality(10);
        characteristic2.setIsUnique(true);
        characteristic2.setRegex("Test Regex");
        characteristic2.setExtensible(true);

        assertTrue(characteristic.equals(characteristic2));

        characteristic2.name("Test Name 2");
        assertFalse(characteristic.equals(characteristic2));
    }

    @Test
    void testToString() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        characteristic.setName("Test Name");
        characteristic.setDescription("Test Description");
        characteristic.setValueType("Test ValueType");
        characteristic.setConfigurable(true);
        characteristic.setValidFor(new TimePeriod());
        characteristic.setType("Test Type");
        characteristic.setSchemaLocation("Test SchemaLocation");
        characteristic.setValueSchemaLocation("Test ValueSchemaLocation");
        characteristic.setMinCardinality(1);
        characteristic.setMaxCardinality(10);
        characteristic.setIsUnique(true);
        characteristic.setRegex("Test Regex");
        characteristic.setExtensible(true);
        characteristic.setResourceSpecCharRelationship(new HashSet<>());
        characteristic.setResourceSpecificationCharacteristicValue(new HashSet<>());

        String expectedString = "class ResourceSpecificationCharacteristic {\n" +
                "    name: Test Name\n" +
                "    description: Test Description\n" +
                "    valueType: Test ValueType\n" +
                "    configurable: true\n" +
                "    validFor: " + characteristic.getValidFor().toString().replace("\n", "\n    ") + "\n" +
                "    type: Test Type\n" +
                "    schemaLocation: Test SchemaLocation\n" +
                "    valueSchemaLocation: Test ValueSchemaLocation\n" +
                "    minCardinality: 1\n" +
                "    maxCardinality: 10\n" +
                "    isUnique: true\n" +
                "    regex: Test Regex\n" +
                "    extensible: true\n" +
                "    resourceSpecCharRelationship: []\n" +
                "    ResourceSpecificationCharacteristicValue: []\n" +
                "}";

        assertEquals(expectedString, characteristic.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        ResourceSpecificationCharacteristic resourceSpecificationCharacteristic = new ResourceSpecificationCharacteristic();

        Method method = ResourceSpecificationCharacteristic.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String input = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(resourceSpecificationCharacteristic, input);

        assertEquals(expectedOutput, output);
    }

    @Test
    void testUpdateWith() {
        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
        characteristic.setName("Test Name");
        characteristic.setDescription("Test Description");
        characteristic.setConfigurable(true);
        characteristic.setType("org.etsi.osl.tmf.rcm634.model.ResourceSpecificationCharacteristic");
        characteristic.setMinCardinality(1);
        characteristic.setMaxCardinality(10);
        characteristic.setIsUnique(true);
        characteristic.setRegex("Test Regex");
        characteristic.setExtensible(true);

        ResourceSpecificationCharacteristic characteristic2 = new ResourceSpecificationCharacteristic();
        characteristic2.updateWith(characteristic);

        assertEquals(characteristic, characteristic2);
    }

//    @Test
//    void testUpdateResourceSpecificationCharacteristicValues() throws NoSuchMethodException {
//        // Create a ResourceSpecificationCharacteristic instance
//        ResourceSpecificationCharacteristic characteristic = new ResourceSpecificationCharacteristic();
//
//        // Create a set of ResourceSpecificationCharacteristicValue instances
//        Set<ResourceSpecificationCharacteristicValue> values = new HashSet<>();
//        ResourceSpecificationCharacteristicValue value1 = new ResourceSpecificationCharacteristicValue();
//        value1.setValue(new Any("Test value 1"));
//
//        ResourceSpecificationCharacteristicValue value2 = new ResourceSpecificationCharacteristicValue();
//        value2.setValue(new Any("Test value 2"));
//        values.add(value2);
//
//        Method method = ResourceSpecificationCharacteristic.class.getDeclaredMethod("updateResourceSpecificationCharacteristicValues", Object.class);
//        method.setAccessible(true);
//        characteristic.updateResourceSpecificationCharacteristicValues(values);
//
//        // Assert that the resourceSpecCharacteristicValue set has been updated correctly
//        Set<ResourceSpecificationCharacteristicValue> updatedValues = characteristic.getResourceSpecCharacteristicValue();
//        assertEquals(2, updatedValues.size());
//        assertTrue(updatedValues.contains(value1));
//        assertTrue(updatedValues.contains(value2));
//
//        // Create a new set of ResourceSpecificationCharacteristicValue instances with one new value
//        Set<ResourceSpecificationCharacteristicValue> newValues = new HashSet<>(values);
//        ResourceSpecificationCharacteristicValue value3 = new ResourceSpecificationCharacteristicValue();
//        value3.setValue(new Any("Test Value 3"));
//        newValues.add(value3);
//
//        // Call the updateResourceSpecificationCharacteristicValues method with the new set
//        characteristic.updateResourceSpecificationCharacteristicValues(newValues);
//
//        // Assert that the resourceSpecCharacteristicValue set has been updated correctly
//        updatedValues = characteristic.getResourceSpecCharacteristicValue();
//        assertEquals(3, updatedValues.size());
//        assertTrue(updatedValues.contains(value1));
//        assertTrue(updatedValues.contains(value2));
//        assertTrue(updatedValues.contains(value3));
//
//        // Create a new set of ResourceSpecificationCharacteristicValue instances without the first value
//        newValues = new HashSet<>(values);
//        newValues.remove(value1);
//
//        // Call the updateResourceSpecificationCharacteristicValues method with the new set
//        characteristic.updateResourceSpecificationCharacteristicValues(newValues);
//
//        // Assert that the resourceSpecCharacteristicValue set has been updated correctly
//        updatedValues = characteristic.getResourceSpecCharacteristicValue();
//        assertEquals(2, updatedValues.size());
//        assertFalse(updatedValues.contains(value1));
//        assertTrue(updatedValues.contains(value2));
//        assertTrue(updatedValues.contains(value3));
//    }
}