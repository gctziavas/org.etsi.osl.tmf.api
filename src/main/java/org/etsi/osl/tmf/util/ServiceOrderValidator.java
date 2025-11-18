package org.etsi.osl.tmf.util;

import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristic;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristicValue;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.etsi.osl.tmf.so641.model.ServiceOrderCreate;
import org.etsi.osl.tmf.so641.model.ServiceOrderItem;
import org.etsi.osl.tmf.so641.model.ServiceOrderUpdate;
import org.etsi.osl.tmf.so641.model.ServiceRestriction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

@Component
public class ServiceOrderValidator implements Validator {
    @Autowired
    private ServiceSpecificationRepoService serviceSpecificationRepoService;

    @Override
    public boolean supports(Class<?> clazz) {
        return ServiceOrderCreate.class.isAssignableFrom(clazz) || ServiceOrderUpdate.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof ServiceOrderCreate)) {
            return;
        }
        ServiceOrderCreate create = (ServiceOrderCreate) target;
        for (ServiceOrderItem orderItem: create.getOrderItem()) {
            if (orderItem.getService() == null) {
                return;
            }
            ServiceRestriction service = orderItem.getService();
            if (service.getServiceCharacteristic() == null || service.getServiceSpecification() == null) {
                return;
            }
            String serviceSpecificationId = service.getServiceSpecification().getId();
            ServiceSpecification serviceSpecification = serviceSpecificationRepoService.findByUuid(serviceSpecificationId);
            CharacteristicParser characteristicParser = new CharacteristicParser();
            for (Characteristic characteristic: service.getServiceCharacteristic()) {
                ServiceSpecCharacteristic serviceSpecCharacteristic = serviceSpecification.findSpecCharacteristicByName(characteristic.getName());
                if (serviceSpecCharacteristic != null) {
                    Set<ServiceSpecCharacteristicValue> serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
                    characteristicParser.updateServiceSpecCharacteristicValues(serviceSpecCharacteristicValues, characteristic);
                    if (serviceSpecCharacteristicValues.stream().anyMatch(
                            value -> {
                        ServiceSpecCharacteristicValueValidator serviceSpecCharacteristicValueValidator = new ServiceSpecCharacteristicValueValidator(value);
                        return !serviceSpecCharacteristicValueValidator.validateType() || !serviceSpecCharacteristicValueValidator.isWithinRangeInterval();
                    })) {
                        errors.reject("invalid.request");
                        return;
                    }
                }
            }
        }
    }
}
