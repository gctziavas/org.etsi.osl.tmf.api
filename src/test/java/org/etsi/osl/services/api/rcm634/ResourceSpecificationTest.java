package org.etsi.osl.services.api.rcm634;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Set;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.common.model.AttachmentRefOrValue;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.rcm634.model.FeatureSpecification;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecCharRelationship;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationCharacteristic;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationRelationship;
import org.etsi.osl.tmf.rcm634.model.TargetResourceSchema;
import org.junit.jupiter.api.Test;

public class ResourceSpecificationTest   extends BaseIT{

    static class TestResourceSpecification extends ResourceSpecification {
        // You can add additional methods or override existing ones if needed
    }

    @Test
    void testResourceSpecification() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();

        String id = "testId";
        Boolean isBundle = true;
        String category = "testCategory";

        resourceSpec.setUuid(id);
        resourceSpec.setIsBundle(isBundle);
        resourceSpec.setCategory(category);

        assertEquals(id, resourceSpec.getId());
        assertEquals(isBundle, resourceSpec.isIsBundle());
        assertEquals(category, resourceSpec.getCategory());
    }

    @Test
    void testEquals() {
        ResourceSpecification resourceSpec1 = new TestResourceSpecification();
        resourceSpec1.setUuid("testId");
        resourceSpec1.getId();
        resourceSpec1.setIsBundle(true);
        resourceSpec1.setCategory("testCategory");

        ResourceSpecification resourceSpec2 = new TestResourceSpecification();
        resourceSpec2.setUuid("testId");
        resourceSpec2.getId();
        resourceSpec2.setIsBundle(true);
        resourceSpec2.setCategory("testCategory");

        assertTrue(resourceSpec1.equals(resourceSpec2));

        resourceSpec1.setUuid("differentId");
        resourceSpec1.getId();

        assertFalse(resourceSpec1.equals(resourceSpec2));
    }

    @Test
    void testHashCode() {
        ResourceSpecification resourceSpec1 = new TestResourceSpecification();
        resourceSpec1.setUuid("testId");
        resourceSpec1.getId();
        resourceSpec1.setIsBundle(true);
        resourceSpec1.setCategory("testCategory");

        ResourceSpecification resourceSpec2 = new TestResourceSpecification();
        resourceSpec2.setUuid("testId");
        resourceSpec2.getId();
        resourceSpec2.setIsBundle(true);
        resourceSpec2.setCategory("testCategory");

        assertEquals(resourceSpec1.hashCode(), resourceSpec2.hashCode());

        resourceSpec1.setUuid("differentId");
        resourceSpec1.getId();

        assertNotEquals(resourceSpec1.hashCode(), resourceSpec2.hashCode());
    }

    @Test
    void testHref() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String href = "testHref";
        resourceSpec.setHref(href);
        assertEquals(href, resourceSpec.getHref());

        ResourceSpecification newResourceSpec = resourceSpec.href("newTestHref");
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testCategory() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String category = "testCategory";
        resourceSpec.setCategory(category);
        assertEquals(category, resourceSpec.getCategory());

        ResourceSpecification newResourceSpec = resourceSpec.category("newTestCategory");
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testDescription() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String description = "testDescription";
        resourceSpec.setDescription(description);
        assertEquals(description, resourceSpec.getDescription());

        ResourceSpecification newResourceSpec = resourceSpec.description("newTestDescription");
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testIsBundle() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        Boolean isBundle = true;
        resourceSpec.setIsBundle(isBundle);
        assertEquals(isBundle, resourceSpec.isIsBundle());

        ResourceSpecification newResourceSpec = resourceSpec.isBundle(false);
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testLifecycleStatus() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String lifecycleStatus = "In study";
        resourceSpec.setLifecycleStatus(lifecycleStatus);
        assertEquals(lifecycleStatus, resourceSpec.getLifecycleStatus());

        ResourceSpecification newResourceSpec = resourceSpec.lifecycleStatus("In use");
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testName() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String name = "testName";
        resourceSpec.setName(name);
        assertEquals(name, resourceSpec.getName());

        ResourceSpecification newResourceSpec = resourceSpec.name("newTestName");
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testSettersAndGetters() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();

        // Test id
        String id = "testId";
        resourceSpec.setUuid(id);
        resourceSpec.getId();
        assertEquals(id, resourceSpec.getId());

        // Test href
        String href = "testHref";
        resourceSpec.setHref(href);
        assertEquals(href, resourceSpec.getHref());

        // Test category
        String category = "testCategory";
        resourceSpec.setCategory(category);
        assertEquals(category, resourceSpec.getCategory());

        // Test description
        String description = "testDescription";
        resourceSpec.setDescription(description);
        assertEquals(description, resourceSpec.getDescription());

        // Test isBundle
        Boolean isBundle = true;
        resourceSpec.setIsBundle(isBundle);
        assertEquals(isBundle, resourceSpec.isIsBundle());

        // Test lifecycleStatus
        String lifecycleStatus = "In study";
        resourceSpec.setLifecycleStatus(lifecycleStatus);
        assertEquals(lifecycleStatus, resourceSpec.getLifecycleStatus());

        // Test name
        String name = "testName";
        resourceSpec.setName(name);
        assertEquals(name, resourceSpec.getName());

        // Test version
        String version = "testVersion";
        resourceSpec.setVersion(version);
        assertEquals(version, resourceSpec.getVersion());

        // Test baseType
        String baseType = "BaseRootEntity";
        resourceSpec.setBaseType(baseType);
        assertEquals(baseType, resourceSpec.getBaseType());

        // Test schemaLocation
        String schemaLocation = "testSchemaLocation";
        resourceSpec.setAtSchemaLocation(schemaLocation);
        assertEquals(schemaLocation, resourceSpec.getAtSchemaLocation());

        // Test type
        String type = "BaseEntity";
        resourceSpec.setType(type);
        assertEquals(type, resourceSpec.getType());
    }
    @Test
    void testToString() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        resourceSpec.setUuid("testId");
        resourceSpec.getId();
        resourceSpec.setIsBundle(true);
        resourceSpec.setCategory("testCategory");
        resourceSpec.setHref("testHref");
        resourceSpec.setLifecycleStatus("In study");
        resourceSpec.setName("testName");
        resourceSpec.setVersion("testVersion");
        resourceSpec.setBaseType("BaseRootEntity");
        resourceSpec.setAtSchemaLocation("testSchemaLocation");
        resourceSpec.setType("BaseEntity");

        String expectedString = "class ResourceSpecification {\n" +
                "    id: testId\n" +
                "    href: testHref\n" +
                "    category: testCategory\n" +
                "    description: null\n" +
                "    isBundle: true\n" +
                "    lastUpdate: null\n" +
                "    lifecycleStatus: In study\n" +
                "    name: testName\n" +
                "    version: testVersion\n" +
                "    attachment: []\n" +
                "    featureSpecification: []\n" +
                "    relatedParty: []\n" +
                "    resourceSpecCharacteristic: []\n" +
                "    resourceSpecRelationship: []\n" +
                "    targetResourceSchema: null\n" +
                "    validFor: null\n" +
                "    baseType: BaseRootEntity\n" +
                "    schemaLocation: testSchemaLocation\n" +
                "    type: BaseEntity\n" +
                "}";

        assertEquals(expectedString, resourceSpec.toString());
    }

    @Test
    void testFixResourceCharRelationhsipIDs() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        resourceSpec.setName("testResourceSpec");

        ResourceSpecificationCharacteristic schar1 = new ResourceSpecificationCharacteristic();
        schar1.setName("schar1");
        schar1.setUuid("uuid1");

        ResourceSpecificationCharacteristic schar2 = new ResourceSpecificationCharacteristic();
        schar2.setName("schar2");

        ResourceSpecCharRelationship charRel1 = new ResourceSpecCharRelationship();
        charRel1.setName("schar1"); // same name as schar1

        ResourceSpecCharRelationship charRel2 = new ResourceSpecCharRelationship();
        charRel2.setName("schar2"); // same name as schar2

        schar1.addResourceSpecCharRelationshipItem(charRel1);
        schar2.addResourceSpecCharRelationshipItem(charRel2);

        resourceSpec.addResourceSpecCharacteristicItem(schar1);
        resourceSpec.addResourceSpecCharacteristicItem(schar2);

        resourceSpec.fixResourceCharRelationhsipIDs();

        assertEquals("uuid1", charRel1.getId()); // id should be set to the uuid of schar1
        assertEquals("testResourceSpec-schar2", charRel2.getId()); // id should be set to "resourceSpecName-scharName"
    }

    @Test
    void testRelatedParty() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        Set<RelatedParty> relatedPartySet = new HashSet<>();
        relatedPartySet.add(new RelatedParty());
        resourceSpec.setRelatedParty(relatedPartySet);
        assertEquals(relatedPartySet, resourceSpec.getRelatedParty());

        relatedPartySet.add(new RelatedParty());
        resourceSpec.setRelatedParty(relatedPartySet);
        assertEquals(relatedPartySet, resourceSpec.getRelatedParty());
    }

    @Test
    void testFindSpecCharacteristicByName() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        ResourceSpecificationCharacteristic characteristic1 = new ResourceSpecificationCharacteristic();
        characteristic1.setName("testName1");
        resourceSpec.getResourceSpecCharacteristic().add(characteristic1);

        ResourceSpecificationCharacteristic characteristic2 = new ResourceSpecificationCharacteristic();
        characteristic2.setName("testName2");
        resourceSpec.getResourceSpecCharacteristic().add(characteristic2);

        assertEquals(characteristic1, resourceSpec.findSpecCharacteristicByName("testName1"));
        assertEquals(characteristic2, resourceSpec.findSpecCharacteristicByName("testName2"));
        assertNull(resourceSpec.findSpecCharacteristicByName("nonexistentName"));
    }



    @Test
    void testResourceSpecRelationship() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        Set<ResourceSpecificationRelationship> relationships = new HashSet<>();
        relationships.add(new ResourceSpecificationRelationship());
        resourceSpec.setResourceSpecRelationship(relationships);
        assertEquals(relationships, resourceSpec.getResourceSpecRelationship());

        ResourceSpecificationRelationship newRelationship = new ResourceSpecificationRelationship();
        resourceSpec.addResourceSpecRelationshipItem(newRelationship);
        assertTrue(resourceSpec.getResourceSpecRelationship().contains(newRelationship));
    }

    @Test
    void testResourceCandidateObjId() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String resourceCandidateObjId = "testResourceCandidateObjId";
        resourceSpec.setResourceCandidateObjId(resourceCandidateObjId);
        assertEquals(resourceCandidateObjId, resourceSpec.getResourceCandidateObjId());
    }


    @Test
    void testBaseType() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String baseType = "testBaseType";
        resourceSpec.setBaseType(baseType);
        assertEquals(baseType, resourceSpec.getBaseType());

        ResourceSpecification newResourceSpec = resourceSpec.baseType("newTestBaseType");
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testSchemaLocation() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String schemaLocation = "testSchemaLocation";
        resourceSpec.setAtSchemaLocation(schemaLocation);
        assertEquals(schemaLocation, resourceSpec.getAtSchemaLocation());

        ResourceSpecification newResourceSpec = resourceSpec.schemaLocation("newTestSchemaLocation");
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testVersion() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String version = "testVersion";
        resourceSpec.setVersion(version);
        assertEquals(version, resourceSpec.getVersion());

        ResourceSpecification newResourceSpec = resourceSpec.version("newTestVersion");
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testAttachment() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        Set<AttachmentRefOrValue> attachments = new HashSet<>();
        attachments.add(new AttachmentRefOrValue());
        resourceSpec.setAttachment(attachments);
        assertEquals(attachments, resourceSpec.getAttachment());

        AttachmentRefOrValue newAttachment = new AttachmentRefOrValue();
        resourceSpec.addAttachmentItem(newAttachment);
        assertTrue(resourceSpec.getAttachment().contains(newAttachment));
    }

    @Test
    void testFeatureSpecification() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        Set<FeatureSpecification> featureSpecifications = new HashSet<>();
        featureSpecifications.add(new FeatureSpecification());
        resourceSpec.setFeatureSpecification(featureSpecifications);
        assertEquals(featureSpecifications, resourceSpec.getFeatureSpecification());

        FeatureSpecification newFeatureSpecification = new FeatureSpecification();
        resourceSpec.addFeatureSpecificationItem(newFeatureSpecification);
        assertTrue(resourceSpec.getFeatureSpecification().contains(newFeatureSpecification));
    }

    @Test
    void testResourceSpecCharacteristic() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        Set<ResourceSpecificationCharacteristic> characteristics = new HashSet<>();
        characteristics.add(new ResourceSpecificationCharacteristic());
        resourceSpec.setResourceSpecCharacteristic(characteristics);
        assertEquals(characteristics, resourceSpec.getResourceSpecCharacteristic());

        ResourceSpecification newResourceSpec = resourceSpec.resourceSpecCharacteristic(characteristics);
        assertEquals(resourceSpec, newResourceSpec);
    }


    @Test
    void testTargetResourceSchema() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        TargetResourceSchema targetResourceSchema = new TargetResourceSchema();
        resourceSpec.setTargetResourceSchema(targetResourceSchema);
        assertEquals(targetResourceSchema, resourceSpec.getTargetResourceSchema());

        ResourceSpecification newResourceSpec = resourceSpec.targetResourceSchema(targetResourceSchema);
        assertEquals(resourceSpec, newResourceSpec);
    }

    @Test
    void testType() {
        ResourceSpecification resourceSpec = new TestResourceSpecification();
        String type = "testType";
        resourceSpec.setType(type);
        assertEquals(type, resourceSpec.getType());

        ResourceSpecification newResourceSpec = resourceSpec.type("newTestType");
        assertEquals(resourceSpec, newResourceSpec);
    }
}