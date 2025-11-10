package org.etsi.osl.tmf.util;

import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristic;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.etsi.osl.tmf.sim638.model.ServiceUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ServiceInventoryValidator implements Validator {
    @Autowired
    private ServiceSpecificationRepoService serviceSpecificationRepoService;

    @Override
    public boolean supports(Class<?> clazz) {
        return ServiceUpdate.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ServiceUpdate update = (ServiceUpdate) target;
        if (update.getServiceCharacteristic() == null || update.getServiceSpecificationRef() == null) {
            return;
        }
        String serviceSpecificationId = update.getServiceSpecificationRef().getId();
        ServiceSpecification serviceSpecification = serviceSpecificationRepoService.findByUuid(serviceSpecificationId);
        CharacteristicParser characteristicParser = new CharacteristicParser();
        for (Characteristic characteristic: update.getServiceCharacteristic()) {
            ServiceSpecCharacteristic serviceSpecCharacteristic = serviceSpecification.findSpecCharacteristicByName(characteristic.getName());
            serviceSpecCharacteristic.setServiceSpecCharacteristicValue(characteristicParser.toSetOfCharacteristicValues(characteristic));
            if (serviceSpecCharacteristic.getServiceSpecCharacteristicValue().stream()
                    .anyMatch(value -> {
                        ServiceSpecCharacteristicValueValidator serviceSpecCharacteristicValueValidator =
                                new ServiceSpecCharacteristicValueValidator(value);
                        return !serviceSpecCharacteristicValueValidator.validateType() || !serviceSpecCharacteristicValueValidator.isWithinRangeInterval();
                    })) {
                errors.reject("invalid.request");
                return;
            }
        }
    }
}
