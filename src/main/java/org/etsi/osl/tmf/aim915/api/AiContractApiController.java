package org.etsi.osl.tmf.aim915.api;

import org.etsi.osl.tmf.aim915.model.AiContract;
import org.etsi.osl.tmf.aim915.model.AiContractCreate;
import org.etsi.osl.tmf.aim915.model.AiContractUpdate;
import org.etsi.osl.tmf.aim915.model.Error;


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
public class AiContractApiController implements AiContractApi {

    private final NativeWebRequest request;

    @Autowired
    public AiContractApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

}
