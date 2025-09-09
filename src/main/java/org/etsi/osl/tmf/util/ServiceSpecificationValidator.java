package org.etsi.osl.tmf.util;

import org.etsi.osl.tmf.common.model.ERangeInterval;
import org.etsi.osl.tmf.common.model.EValueType;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristicValue;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationUpdate;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Component
public class ServiceSpecificationValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ServiceSpecificationUpdate.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ServiceSpecificationUpdate update = (ServiceSpecificationUpdate) target;
        boolean invalid = update.getServiceSpecCharacteristic().stream()
                .flatMap(serviceSpecCharacteristic ->
                        serviceSpecCharacteristic.getServiceSpecCharacteristicValue().stream())
                .anyMatch(serviceSpecCharacteristicValue ->
                        !validateType(serviceSpecCharacteristicValue) || !isWithinRangeInterval(serviceSpecCharacteristicValue));
        if (invalid) {
            errors.reject("invalid.request");
        }
    }

    private boolean validateType(ServiceSpecCharacteristicValue serviceSpecCharacteristicValue) {
        final String INTEGER_REGEX = "[-+]?\\d+";
        final String FLOAT_REGEX = "[-+]?\\d*([.,]\\d+)?([eE][-+]?\\d+)?";
        final String BOOLEAN_REGEX = "(?i)true|false";
        final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (serviceSpecCharacteristicValue.getValueType() == null) {
            return true;
        }
        String value = serviceSpecCharacteristicValue.getValue().getValue();
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            return switch (EValueType.getEnum(serviceSpecCharacteristicValue.getValueType())) {
                case INTEGER, SMALLINT, lONGINT -> value.matches(INTEGER_REGEX);
                case FLOAT -> value.matches(FLOAT_REGEX);
                case BOOLEAN -> value.matches(BOOLEAN_REGEX) || value.matches(INTEGER_REGEX);
                case TIMESTAMP -> {
                    try {
                        LocalDateTime.parse(value, TIMESTAMP_FORMATTER);
                        yield true;
                    } catch (DateTimeParseException e) {
                        yield false;
                    }
                }
                default -> true;
            };
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isWithinRangeInterval(ServiceSpecCharacteristicValue serviceSpecCharacteristicValue) {
        if (serviceSpecCharacteristicValue.getRangeInterval() == null) {
            return true;
        }
        if (!Objects.equals(serviceSpecCharacteristicValue.getValueType(), EValueType.INTEGER.getValue()) &&
                !Objects.equals(serviceSpecCharacteristicValue.getValueType(), EValueType.SMALLINT.getValue()) &&
                !Objects.equals(serviceSpecCharacteristicValue.getValueType(), EValueType.lONGINT.getValue())) {
            return true;
        }
        String stringValue = serviceSpecCharacteristicValue.getValue().getValue();
        if (stringValue == null || stringValue.isBlank()) {
            return true;
        }
        int valueFrom = serviceSpecCharacteristicValue.getValueFrom() != null ? serviceSpecCharacteristicValue.getValueFrom() : Integer.MIN_VALUE;
        int valueTo = serviceSpecCharacteristicValue.getValueTo() != null ? serviceSpecCharacteristicValue.getValueTo() : Integer.MAX_VALUE;
        try {
            int value = Integer.parseInt(stringValue);
            return switch (ERangeInterval.getEnum(serviceSpecCharacteristicValue.getRangeInterval())) {
                case OPEN -> value > valueFrom && value < valueTo;
                case CLOSED -> value >= valueFrom && value <= valueTo;
                case CLOSED_BOTTOM -> value >= valueFrom && value < valueTo;
                case CLOSED_TOP -> value > valueFrom && value <= valueTo;
            };
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
