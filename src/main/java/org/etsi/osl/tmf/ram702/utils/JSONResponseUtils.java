package org.etsi.osl.tmf.ram702.utils;

public class JSONResponseUtils {
    public static final String RESOURCE_JSON_RESPONSE = "{\\r\\n" + //
                "  \\\"note\\\" : [ {\\r\\n" + //
                "    \\\"date\\\" : \\\"2000-01-23T04:56:07.000+00:00\\\",\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"author\\\" : \\\"author\\\",\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "    \\\"text\\\" : \\\"text\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  }, {\\r\\n" + //
                "    \\\"date\\\" : \\\"2000-01-23T04:56:07.000+00:00\\\",\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"author\\\" : \\\"author\\\",\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "    \\\"text\\\" : \\\"text\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  } ],\\r\\n" + //
                "  \\\"endOperatingDate\\\" : \\\"2000-01-23T04:56:07.000+00:00\\\",\\r\\n" + //
                "  \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "  \\\"resourceVersion\\\" : \\\"resourceVersion\\\",\\r\\n" + //
                "  \\\"activationFeature\\\" : [ {\\r\\n" + //
                "    \\\"isBundle\\\" : true,\\r\\n" + //
                "    \\\"isEnabled\\\" : true,\\r\\n" + //
                "    \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "    \\\"featureCharacteristic\\\" : [ {\\r\\n" + //
                "      \\\"characteristicRelationship\\\" : [ {\\r\\n" + //
                "        \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "      }, {\\r\\n" + //
                "        \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "      } ],\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"valueType\\\" : \\\"valueType\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"value\\\" : { }\\r\\n" + //
                "    }, {\\r\\n" + //
                "      \\\"characteristicRelationship\\\" : [ {\\r\\n" + //
                "        \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "      }, {\\r\\n" + //
                "        \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "      } ],\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"valueType\\\" : \\\"valueType\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"value\\\" : { }\\r\\n" + //
                "    } ],\\r\\n" + //
                "    \\\"constraint\\\" : [ {\\r\\n" + //
                "      \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"version\\\" : \\\"version\\\"\\r\\n" + //
                "    }, {\\r\\n" + //
                "      \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"version\\\" : \\\"version\\\"\\r\\n" + //
                "    } ],\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"featureRelationship\\\" : [ {\\r\\n" + //
                "      \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "      \\\"validFor\\\" : {\\r\\n" + //
                "        \\\"startDateTime\\\" : \\\"1985-04-12T23:20:50.52Z\\\",\\r\\n" + //
                "        \\\"endDateTime\\\" : \\\"1985-04-12T23:20:50.52Z\\\"\\r\\n" + //
                "      },\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\"\\r\\n" + //
                "    }, {\\r\\n" + //
                "      \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "      \\\"validFor\\\" : {\\r\\n" + //
                "        \\\"startDateTime\\\" : \\\"1985-04-12T23:20:50.52Z\\\",\\r\\n" + //
                "        \\\"endDateTime\\\" : \\\"1985-04-12T23:20:50.52Z\\\"\\r\\n" + //
                "      },\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\"\\r\\n" + //
                "    } ]\\r\\n" + //
                "  }, {\\r\\n" + //
                "    \\\"isBundle\\\" : true,\\r\\n" + //
                "    \\\"isEnabled\\\" : true,\\r\\n" + //
                "    \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "    \\\"featureCharacteristic\\\" : [ {\\r\\n" + //
                "      \\\"characteristicRelationship\\\" : [ {\\r\\n" + //
                "        \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "      }, {\\r\\n" + //
                "        \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "      } ],\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"valueType\\\" : \\\"valueType\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"value\\\" : { }\\r\\n" + //
                "    }, {\\r\\n" + //
                "      \\\"characteristicRelationship\\\" : [ {\\r\\n" + //
                "        \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "      }, {\\r\\n" + //
                "        \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "      } ],\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"valueType\\\" : \\\"valueType\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"value\\\" : { }\\r\\n" + //
                "    } ],\\r\\n" + //
                "    \\\"constraint\\\" : [ {\\r\\n" + //
                "      \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"version\\\" : \\\"version\\\"\\r\\n" + //
                "    }, {\\r\\n" + //
                "      \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "      \\\"version\\\" : \\\"version\\\"\\r\\n" + //
                "    } ],\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"featureRelationship\\\" : [ {\\r\\n" + //
                "      \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "      \\\"validFor\\\" : {\\r\\n" + //
                "        \\\"startDateTime\\\" : \\\"1985-04-12T23:20:50.52Z\\\",\\r\\n" + //
                "        \\\"endDateTime\\\" : \\\"1985-04-12T23:20:50.52Z\\\"\\r\\n" + //
                "      },\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\"\\r\\n" + //
                "    }, {\\r\\n" + //
                "      \\\"relationshipType\\\" : \\\"relationshipType\\\",\\r\\n" + //
                "      \\\"validFor\\\" : {\\r\\n" + //
                "        \\\"startDateTime\\\" : \\\"1985-04-12T23:20:50.52Z\\\",\\r\\n" + //
                "        \\\"endDateTime\\\" : \\\"1985-04-12T23:20:50.52Z\\\"\\r\\n" + //
                "      },\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\"\\r\\n" + //
                "    } ]\\r\\n" + //
                "  } ],\\r\\n" + //
                "  \\\"description\\\" : \\\"description\\\",\\r\\n" + //
                "  \\\"resourceCharacteristic\\\" : [ null, null ],\\r\\n" + //
                "  \\\"relatedParty\\\" : [ {\\r\\n" + //
                "    \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "    \\\"role\\\" : \\\"role\\\",\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  }, {\\r\\n" + //
                "    \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "    \\\"role\\\" : \\\"role\\\",\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  } ],\\r\\n" + //
                "  \\\"attachment\\\" : [ {\\r\\n" + //
                "    \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "    \\\"attachmentType\\\" : \\\"video\\\",\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"description\\\" : \\\"Photograph of the Product\\\",\\r\\n" + //
                "    \\\"mimeType\\\" : \\\"mimeType\\\",\\r\\n" + //
                "    \\\"content\\\" : \\\"content\\\",\\r\\n" + //
                "    \\\"url\\\" : \\\"http://host/Content/4aafacbd-11ff-4dc8-b445-305f2215715f\\\",\\r\\n" + //
                "    \\\"size\\\" : {\\r\\n" + //
                "      \\\"amount\\\" : 0.8008282,\\r\\n" + //
                "      \\\"units\\\" : \\\"units\\\"\\r\\n" + //
                "    },\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"4aafacbd-11ff-4dc8-b445-305f2215715f\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"http://host/Attachment/4aafacbd-11ff-4dc8-b445-305f2215715f\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  }, {\\r\\n" + //
                "    \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "    \\\"attachmentType\\\" : \\\"video\\\",\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"description\\\" : \\\"Photograph of the Product\\\",\\r\\n" + //
                "    \\\"mimeType\\\" : \\\"mimeType\\\",\\r\\n" + //
                "    \\\"content\\\" : \\\"content\\\",\\r\\n" + //
                "    \\\"url\\\" : \\\"http://host/Content/4aafacbd-11ff-4dc8-b445-305f2215715f\\\",\\r\\n" + //
                "    \\\"size\\\" : {\\r\\n" + //
                "      \\\"amount\\\" : 0.8008282,\\r\\n" + //
                "      \\\"units\\\" : \\\"units\\\"\\r\\n" + //
                "    },\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"4aafacbd-11ff-4dc8-b445-305f2215715f\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"http://host/Attachment/4aafacbd-11ff-4dc8-b445-305f2215715f\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  } ],\\r\\n" + //
                "  \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "  \\\"administrativeState\\\" : \\\"locked\\\",\\r\\n" + //
                "  \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "  \\\"resourceRelationship\\\" : [ {\\r\\n" + //
                "    \\\"relationshipType\\\" : \\\"bundled\\\",\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"resource\\\" : {\\r\\n" + //
                "      \\\"note\\\" : [ null, null ],\\r\\n" + //
                "      \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "      \\\"endOperatingDate\\\" : \\\"2000-01-23T04:56:07.000+00:00\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"resourceVersion\\\" : \\\"resourceVersion\\\",\\r\\n" + //
                "      \\\"activationFeature\\\" : [ null, null ],\\r\\n" + //
                "      \\\"resourceSpecification\\\" : {\\r\\n" + //
                "        \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"version\\\" : \\\"version\\\"\\r\\n" + //
                "      },\\r\\n" + //
                "      \\\"description\\\" : \\\"description\\\",\\r\\n" + //
                "      \\\"resourceCharacteristic\\\" : [ null, null ],\\r\\n" + //
                "      \\\"relatedParty\\\" : [ null, null ],\\r\\n" + //
                "      \\\"resourceStatus\\\" : \\\"standby\\\",\\r\\n" + //
                "      \\\"usageState\\\" : \\\"idle\\\",\\r\\n" + //
                "      \\\"attachment\\\" : [ null, null ],\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"resourceRelationship\\\" : [ null, null ],\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"href\\\" : \\\"href\\\",\\r\\n" + //
                "      \\\"startOperatingDate\\\" : \\\"2000-01-23T04:56:07.000+00:00\\\",\\r\\n" + //
                "      \\\"category\\\" : \\\"category\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "    },\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  }, {\\r\\n" + //
                "    \\\"relationshipType\\\" : \\\"bundled\\\",\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"resource\\\" : {\\r\\n" + //
                "      \\\"note\\\" : [ null, null ],\\r\\n" + //
                "      \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "      \\\"endOperatingDate\\\" : \\\"2000-01-23T04:56:07.000+00:00\\\",\\r\\n" + //
                "      \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "      \\\"resourceVersion\\\" : \\\"resourceVersion\\\",\\r\\n" + //
                "      \\\"activationFeature\\\" : [ null, null ],\\r\\n" + //
                "      \\\"resourceSpecification\\\" : {\\r\\n" + //
                "        \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "        \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "        \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "        \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "        \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "        \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "        \\\"version\\\" : \\\"version\\\"\\r\\n" + //
                "      },\\r\\n" + //
                "      \\\"description\\\" : \\\"description\\\",\\r\\n" + //
                "      \\\"resourceCharacteristic\\\" : [ null, null ],\\r\\n" + //
                "      \\\"relatedParty\\\" : [ null, null ],\\r\\n" + //
                "      \\\"resourceStatus\\\" : \\\"standby\\\",\\r\\n" + //
                "      \\\"usageState\\\" : \\\"idle\\\",\\r\\n" + //
                "      \\\"attachment\\\" : [ null, null ],\\r\\n" + //
                "      \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "      \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "      \\\"resourceRelationship\\\" : [ null, null ],\\r\\n" + //
                "      \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "      \\\"href\\\" : \\\"href\\\",\\r\\n" + //
                "      \\\"startOperatingDate\\\" : \\\"2000-01-23T04:56:07.000+00:00\\\",\\r\\n" + //
                "      \\\"category\\\" : \\\"category\\\",\\r\\n" + //
                "      \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "    },\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"http://example.com/aeiou\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  } ],\\r\\n" + //
                "  \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "  \\\"href\\\" : \\\"href\\\",\\r\\n" + //
                "  \\\"startOperatingDate\\\" : \\\"2000-01-23T04:56:07.000+00:00\\\",\\r\\n" + //
                "  \\\"operationalState\\\" : \\\"enable\\\",\\r\\n" + //
                "  \\\"place\\\" : {\\r\\n" + //
                "    \\\"@referredType\\\" : \\\"@referredType\\\",\\r\\n" + //
                "    \\\"role\\\" : \\\"role\\\",\\r\\n" + //
                "    \\\"@baseType\\\" : \\\"@baseType\\\",\\r\\n" + //
                "    \\\"@type\\\" : \\\"@type\\\",\\r\\n" + //
                "    \\\"name\\\" : \\\"name\\\",\\r\\n" + //
                "    \\\"id\\\" : \\\"id\\\",\\r\\n" + //
                "    \\\"href\\\" : \\\"href\\\",\\r\\n" + //
                "    \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "  },\\r\\n" + //
                "  \\\"category\\\" : \\\"category\\\",\\r\\n" + //
                "  \\\"@schemaLocation\\\" : \\\"http://example.com/aeiou\\\"\\r\\n" + //
                "}";
                
    public static final String RESOURCE_LIST_JSON_RESPONSE = "[" + RESOURCE_JSON_RESPONSE + "]";
}
