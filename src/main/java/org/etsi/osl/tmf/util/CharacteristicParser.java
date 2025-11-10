package org.etsi.osl.tmf.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristicValue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CharacteristicParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public Set<ServiceSpecCharacteristicValue> toSetOfCharacteristicValues(Characteristic characteristic) {
        Set<ServiceSpecCharacteristicValue> result = new HashSet<>();
        Any input = characteristic.getValue();
        if (input == null) {
            return result;
        }
        try {
            JsonNode node = mapper.readTree(input.getValue());
            if (node.isArray()) {
                Set<Any> values = mapper.readValue(input.getValue(), new TypeReference<Set<Any>>() {});
                result = values.stream().map(value -> {
                    ServiceSpecCharacteristicValue serviceSpecCharacteristicValue = new ServiceSpecCharacteristicValue();
                    serviceSpecCharacteristicValue.setValue(value);
                    return serviceSpecCharacteristicValue;
                }).collect(Collectors.toSet());
            } else if (node.isObject()) {
                ServiceSpecCharacteristicValue serviceSpecCharacteristicValue = new ServiceSpecCharacteristicValue();
                serviceSpecCharacteristicValue.setValue(mapper.treeToValue(node, Any.class));
                result.add(serviceSpecCharacteristicValue);
            } else {
                ServiceSpecCharacteristicValue serviceSpecCharacteristicValue = new ServiceSpecCharacteristicValue();
                serviceSpecCharacteristicValue.setValue(input);
                result.add(serviceSpecCharacteristicValue);
            }
        } catch (Exception e) {
            ServiceSpecCharacteristicValue serviceSpecCharacteristicValue = new ServiceSpecCharacteristicValue();
            serviceSpecCharacteristicValue.setValue(input);
            result.add(serviceSpecCharacteristicValue);
        }
        return result;
    }
}
