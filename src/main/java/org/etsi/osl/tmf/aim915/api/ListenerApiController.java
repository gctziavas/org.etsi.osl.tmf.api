package org.openapitools.api;

import org.openapitools.model.AiContractAttributeValueChangeEvent;
import org.openapitools.model.AiContractCreateEvent;
import org.openapitools.model.AiContractDeleteEvent;
import org.openapitools.model.AiContractSpecificationAttributeValueChangeEvent;
import org.openapitools.model.AiContractSpecificationCreateEvent;
import org.openapitools.model.AiContractSpecificationDeleteEvent;
import org.openapitools.model.AiContractStateChangeEvent;
import org.openapitools.model.AiContractViolationAttributeValueChangeEvent;
import org.openapitools.model.AiContractViolationCreateEvent;
import org.openapitools.model.AiContractViolationDeleteEvent;
import org.openapitools.model.AiModelAttributeValueChangeEvent;
import org.openapitools.model.AiModelCreateEvent;
import org.openapitools.model.AiModelDeleteEvent;
import org.openapitools.model.AiModelSpecificationAttributeValueChangeEvent;
import org.openapitools.model.AiModelSpecificationCreateEvent;
import org.openapitools.model.AiModelSpecificationDeleteEvent;
import org.openapitools.model.AiModelStateChangeEvent;
import org.openapitools.model.AlarmAttributeValueChangeEvent;
import org.openapitools.model.AlarmCreateEvent;
import org.openapitools.model.AlarmDeleteEvent;
import org.openapitools.model.AlarmStateChangeEvent;
import org.openapitools.model.Error;
import org.openapitools.model.EventCreateEvent;
import org.openapitools.model.EventSubscription;
import org.openapitools.model.MonitorAttributeValueChangeEvent;
import org.openapitools.model.MonitorCreateEvent;
import org.openapitools.model.MonitorDeleteEvent;
import org.openapitools.model.MonitorStateChangeEvent;
import org.openapitools.model.RuleAttributeValueChangeEvent;
import org.openapitools.model.RuleCreateEvent;
import org.openapitools.model.RuleDeleteEvent;
import org.openapitools.model.RuleStateChangeEvent;
import org.openapitools.model.TopicChangeEvent;
import org.openapitools.model.TopicCreateEvent;
import org.openapitools.model.TopicDeleteEvent;


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
