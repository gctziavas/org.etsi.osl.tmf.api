package org.etsi.osl.tmf.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristicValue;

import java.util.*;

public class CharacteristicParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public void updateServiceSpecCharacteristicValues(
            Set<ServiceSpecCharacteristicValue> serviceSpecCharacteristicValues,
            Characteristic characteristic
    ) {
        Any input = characteristic.getValue();
        if (input == null) {
            return;
        }
        List<Any> values = new ArrayList<>();
        try {
            JsonNode node = mapper.readTree(input.getValue());
            if (node.isArray()) {
                values = mapper.readValue(input.getValue(), new TypeReference<>() {});
            } else if (node.isObject()) {
                values.add(mapper.treeToValue(node, Any.class));
            } else {
                values.add(input);
            }
        } catch (Exception e) {
            values.add(input);
        }
        Iterator<ServiceSpecCharacteristicValue> serviceSpecCharacteristicValueIterator = serviceSpecCharacteristicValues.iterator();
        Iterator<Any> valueIterator = values.iterator();
        while (serviceSpecCharacteristicValueIterator.hasNext() && valueIterator.hasNext()) {
            ServiceSpecCharacteristicValue serviceSpecCharacteristicValue = serviceSpecCharacteristicValueIterator.next();
            Any value = valueIterator.next();
            serviceSpecCharacteristicValue.setValue(value);
        }
    }
}
