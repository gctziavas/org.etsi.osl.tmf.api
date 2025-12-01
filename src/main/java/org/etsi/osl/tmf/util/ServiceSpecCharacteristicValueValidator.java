package org.etsi.osl.tmf.util;

import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.ERangeInterval;
import org.etsi.osl.tmf.common.model.EValueType;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristicValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class ServiceSpecCharacteristicValueValidator {
    private final ServiceSpecCharacteristicValue serviceSpecCharacteristicValue;

    public ServiceSpecCharacteristicValueValidator(ServiceSpecCharacteristicValue serviceSpecCharacteristicValue) {
        this.serviceSpecCharacteristicValue = serviceSpecCharacteristicValue;
    }

    public boolean validateType() {
        final String INTEGER_REGEX = "[-+]?\\d+";
        final String FLOAT_REGEX = "[-+]?\\d*([.,]\\d+)?([eE][-+]?\\d+)?";
        final String BOOLEAN_REGEX = "(?i)true|false";
        final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        if (serviceSpecCharacteristicValue.getValueType() == null) {
            return true;
        }
        Any value = serviceSpecCharacteristicValue.getValue();
        if (value == null) {
            return true;
        }
        String stringValue = value.getValue();
        if (stringValue == null || stringValue.isBlank()) {
            return true;
        }
        try {
            return switch (EValueType.getEnum(serviceSpecCharacteristicValue.getValueType())) {
                case INTEGER, SMALLINT, lONGINT -> stringValue.matches(INTEGER_REGEX);
                case FLOAT -> stringValue.matches(FLOAT_REGEX);
                case BOOLEAN -> stringValue.matches(BOOLEAN_REGEX) || stringValue.matches(INTEGER_REGEX);
                case TIMESTAMP -> {
                    try {
                        LocalDateTime.parse(stringValue, ISO_DATE_TIME);
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

    public boolean isWithinRangeInterval() {
        if (serviceSpecCharacteristicValue.getRangeInterval() == null) {
            return true;
        }
        if (!Objects.equals(serviceSpecCharacteristicValue.getValueType(), EValueType.INTEGER.getValue()) &&
                !Objects.equals(serviceSpecCharacteristicValue.getValueType(), EValueType.SMALLINT.getValue()) &&
                !Objects.equals(serviceSpecCharacteristicValue.getValueType(), EValueType.lONGINT.getValue())) {
            return true;
        }
        Any value = serviceSpecCharacteristicValue.getValue();
        if (value == null) {
            return true;
        }
        String stringValue = value.getValue();
        if (stringValue == null || stringValue.isBlank()) {
            return true;
        }
        int valueFrom = serviceSpecCharacteristicValue.getValueFrom() != null ? serviceSpecCharacteristicValue.getValueFrom() : Integer.MIN_VALUE;
        int valueTo = serviceSpecCharacteristicValue.getValueTo() != null ? serviceSpecCharacteristicValue.getValueTo() : Integer.MAX_VALUE;
        try {
            int intValue = Integer.parseInt(stringValue);
            return switch (ERangeInterval.getEnum(serviceSpecCharacteristicValue.getRangeInterval())) {
                case OPEN -> intValue > valueFrom && intValue < valueTo;
                case CLOSED -> intValue >= valueFrom && intValue <= valueTo;
                case CLOSED_BOTTOM -> intValue >= valueFrom && intValue < valueTo;
                case CLOSED_TOP -> intValue > valueFrom && intValue <= valueTo;
            };
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
