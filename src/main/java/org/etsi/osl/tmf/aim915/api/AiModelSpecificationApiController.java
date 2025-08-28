package org.etsi.osl.tmf.aim915.api;

import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationCreate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationUpdate;
import org.etsi.osl.tmf.aim915.reposervices.AiModelSpecificationRepositoryService;
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

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Controller("AiModelSpecificationApiController915")
@RequestMapping("/AiM/v4/")
public class AiModelSpecificationApiController implements AiModelSpecificationApi {

    private static final Logger log = LoggerFactory.getLogger(AiModelSpecificationApiController.class);

    private final NativeWebRequest request;
    private final AiModelSpecificationRepositoryService aiModelSpecificationRepositoryService;

    @Autowired
    public AiModelSpecificationApiController(
            NativeWebRequest request,
            AiModelSpecificationRepositoryService aiModelSpecificationRepositoryService) {
        this.request = request;
        this.aiModelSpecificationRepositoryService = aiModelSpecificationRepositoryService;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public ResponseEntity<AiModelSpecification> createAiModelSpecification(
            Principal principal,
            @Valid @RequestBody AiModelSpecificationCreate aiModelSpecification) {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                aiModelSpecification.setRelatedParty(
                        org.etsi.osl.tmf.util.AddUserAsOwnerToRelatedParties.addUser(
                                principal.getName(),
                                principal.getName(),
                                UserPartRoleType.REQUESTER,
                                "",
                                aiModelSpecification.getRelatedParty()
                        )
                );
                AiModelSpecification created = aiModelSpecificationRepositoryService.createAiModelSpecification(aiModelSpecification);
                return new ResponseEntity<>(created, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public ResponseEntity<Void> deleteAiModelSpecification(@PathVariable("id") String id) {
        try {
            aiModelSpecificationRepositoryService.deleteAiModelSpecification(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public ResponseEntity<List<AiModelSpecification>> listAiModelSpecification(
            Principal principal,
            @RequestParam(value = "fields", required = false) String fields,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                List<AiModelSpecification> specs = aiModelSpecificationRepositoryService.findAllAiModelSpecifications();

                if (offset == null || offset < 0) {
                    offset = 0;
                }
                if (limit != null && limit > 0) {
                    int toIndex = Math.min(offset + limit, specs.size());
                    if (offset > specs.size()) {
                        specs = List.of();
                    } else {
                        specs = specs.subList(offset, toIndex);
                    }
                } else if (offset > 0 && offset < specs.size()) {
                    specs = specs.subList(offset, specs.size());
                }
                return new ResponseEntity<>(specs, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public ResponseEntity<AiModelSpecification> patchAiModelSpecification(
            Principal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody AiModelSpecificationUpdate aiModelSpecification) {
        try {
            AiModelSpecification updated = aiModelSpecificationRepositoryService.updateAiModelSpecification(id, aiModelSpecification);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public ResponseEntity<AiModelSpecification> retrieveAiModelSpecification(
            Principal principal,
            @PathVariable("id") String id,
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            AiModelSpecification spec = aiModelSpecificationRepositoryService.findAiModelSpecificationByUuid(id);
            if (spec == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(spec, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}