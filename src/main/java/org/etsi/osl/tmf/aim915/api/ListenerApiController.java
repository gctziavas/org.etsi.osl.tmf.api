package org.etsi.osl.tmf.aim915.api;

import org.etsi.osl.tmf.aim915.model.AiContractAttributeValueChangeEvent;
import org.etsi.osl.tmf.aim915.model.AiContractCreateEvent;
import org.etsi.osl.tmf.aim915.model.AiContractDeleteEvent;
import org.etsi.osl.tmf.aim915.model.AiContractSpecificationAttributeValueChangeEvent;
import org.etsi.osl.tmf.aim915.model.AiContractSpecificationCreateEvent;
import org.etsi.osl.tmf.aim915.model.AiContractSpecificationDeleteEvent;
import org.etsi.osl.tmf.aim915.model.AiContractStateChangeEvent;
import org.etsi.osl.tmf.aim915.model.AiContractViolationAttributeValueChangeEvent;
import org.etsi.osl.tmf.aim915.model.AiContractViolationCreateEvent;
import org.etsi.osl.tmf.aim915.model.AiContractViolationDeleteEvent;
import org.etsi.osl.tmf.aim915.model.AiModelAttributeValueChangeEvent;
import org.etsi.osl.tmf.aim915.model.AiModelCreateEvent;
import org.etsi.osl.tmf.aim915.model.AiModelDeleteEvent;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationAttributeValueChangeEvent;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationCreateEvent;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationDeleteEvent;
import org.etsi.osl.tmf.aim915.model.AiModelStateChangeEvent;
import org.etsi.osl.tmf.aim915.model.AlarmAttributeValueChangeEvent;
import org.etsi.osl.tmf.aim915.model.AlarmCreateEvent;
import org.etsi.osl.tmf.aim915.model.AlarmDeleteEvent;
import org.etsi.osl.tmf.aim915.model.AlarmStateChangeEvent;
import org.etsi.osl.tmf.aim915.model.Error;
import org.etsi.osl.tmf.aim915.model.EventCreateEvent;
import org.etsi.osl.tmf.aim915.model.EventSubscription;
import org.etsi.osl.tmf.aim915.model.MonitorAttributeValueChangeEvent;
import org.etsi.osl.tmf.aim915.model.MonitorCreateEvent;
import org.etsi.osl.tmf.aim915.model.MonitorDeleteEvent;
import org.etsi.osl.tmf.aim915.model.MonitorStateChangeEvent;
import org.etsi.osl.tmf.aim915.model.RuleAttributeValueChangeEvent;
import org.etsi.osl.tmf.aim915.model.RuleCreateEvent;
import org.etsi.osl.tmf.aim915.model.RuleDeleteEvent;
import org.etsi.osl.tmf.aim915.model.RuleStateChangeEvent;
import org.etsi.osl.tmf.aim915.model.TopicChangeEvent;
import org.etsi.osl.tmf.aim915.model.TopicCreateEvent;
import org.etsi.osl.tmf.aim915.model.TopicDeleteEvent;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.constraints.*;
import javax.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-08-20T10:28:49.370045968Z[Etc/UTC]", comments = "Generator version: 7.14.0")
@Controller
@RequestMapping("${openapi.aiManagement.base-path:/tmf-api/AiM/v4}")
public class ListenerApiController implements ListenerApi {

    private final NativeWebRequest request;

    @Autowired
    public ListenerApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

}
