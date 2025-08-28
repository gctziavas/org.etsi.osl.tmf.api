package org.etsi.osl.tmf.aim915.api;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelUpdate;

import org.etsi.osl.tmf.aim915.reposervices.AiModelRepositoryService;


import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import javax.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-08-20T10:28:49.370045968Z[Etc/UTC]", comments = "Generator version: 7.14.0")
@Controller("AiManagementApiController915")
@RequestMapping("/AiM/v4/")
public class AiModelApiController implements AiModelApi {

    private static final Logger log = LoggerFactory.getLogger(AiModelApiController.class);

    private final NativeWebRequest request;

    private final AiModelRepositoryService aiModelRepositoryService;

    @Autowired
    public AiModelApiController(NativeWebRequest request, AiModelRepositoryService aiModelRepositoryService) {
        this.request = request;
        this.aiModelRepositoryService = aiModelRepositoryService;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public ResponseEntity<AiModel> createAiModel(Principal principal, @Valid @RequestBody AiModelCreate aiModel) {
        try {
            if(SecurityContextHolder.getContext().getAuthentication()!= null){
                aiModel.setRelatedParty(
                        org.etsi.osl.tmf.util.AddUserAsOwnerToRelatedParties.addUser(
                                principal.getName(),
                                principal.getName(),
                                UserPartRoleType.REQUESTER,
                                "",
                                aiModel.getRelatedParty()
                        )
                );
                AiModel createdAiModel = aiModelRepositoryService.createAiModel(aiModel);
                return new ResponseEntity<AiModel>(createdAiModel, HttpStatus.CREATED);
            } else{
                return new ResponseEntity<AiModel>(HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER')" )
    @Override
    public ResponseEntity<Void> deleteAiModel(@PathVariable("id") String id) {
        try {
            aiModelRepositoryService.deleteAiModel(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER')" )
    public ResponseEntity<List<AiModel>> listAiModel(
            Principal principal,
            @RequestParam(value = "fields", required = false) String fields,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {

                try {
                    List<AiModel> models = aiModelRepositoryService.findAllAiModels();

                    if (offset == null || offset < 0) {
                        offset = 0;
                    }
                    if (limit != null && limit > 0) {
                        int toIndex = Math.min(offset + limit, models.size());
                        if (offset > models.size()) {
                            models = List.of();
                        } else {
                            models = models.subList(offset, toIndex);
                        }
                    } else if (offset > 0 && offset < models.size()) {
                        models = models.subList(offset, models.size());
                    }
                    return new ResponseEntity<>(models, HttpStatus.OK);
                } catch (Exception e) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER')" )
    @Override
    public ResponseEntity<AiModel> patchAiModel(
            Principal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody AiModelUpdate aiModel) {
        try {
            AiModel updated = aiModelRepositoryService.updateAiModel(id, aiModel);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER')" )
    @Override
    public ResponseEntity<AiModel> retrieveAiModel(
            Principal principal,
            @PathVariable("id") String id,
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            AiModel model = aiModelRepositoryService.findAiModelByUuid(id);
            if (model == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(model, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
