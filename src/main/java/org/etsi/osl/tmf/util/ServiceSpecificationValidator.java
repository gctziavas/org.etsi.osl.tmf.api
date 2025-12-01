package org.etsi.osl.tmf.util;

import org.etsi.osl.tmf.scm633.model.ServiceSpecificationUpdate;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ServiceSpecificationValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ServiceSpecificationUpdate.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ServiceSpecificationUpdate update = (ServiceSpecificationUpdate) target;
        if (update.getServiceSpecCharacteristic() == null) {
            return;
        }
        boolean invalid = update.getServiceSpecCharacteristic().stream()
                .flatMap(serviceSpecCharacteristic ->
                        serviceSpecCharacteristic.getServiceSpecCharacteristicValue().stream())
                .anyMatch(serviceSpecCharacteristicValue -> {
                    ServiceSpecCharacteristicValueValidator serviceSpecCharacteristicValueValidator =
                            new ServiceSpecCharacteristicValueValidator(serviceSpecCharacteristicValue);
                    return !serviceSpecCharacteristicValueValidator.validateType() || !serviceSpecCharacteristicValueValidator.isWithinRangeInterval();
                });
        if (invalid) {
            errors.reject("invalid.request");
        }
    }
}
