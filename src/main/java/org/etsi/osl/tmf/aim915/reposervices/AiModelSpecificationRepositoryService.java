package org.etsi.osl.tmf.aim915.reposervices;


import org.etsi.osl.tmf.aim915.model.*;
import org.etsi.osl.tmf.aim915.repo.AiModelSpecificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AiModelSpecificationRepositoryService {

    private static final Logger log = LoggerFactory.getLogger(AiModelSpecificationRepositoryService.class);

    private final AiModelSpecificationRepository aiModelSpecificationRepository;

    @Autowired
    public AiModelSpecificationRepositoryService(AiModelSpecificationRepository aiModelSpecificationRepository) {
        this.aiModelSpecificationRepository = aiModelSpecificationRepository;
    }

    @Autowired
    private AiModelSpecificationMapper specificationMapper;

    public List<AiModelSpecification> findAllAiModelSpecifications() {
        log.info("AiModels LIST");
        return (List<AiModelSpecification>)  aiModelSpecificationRepository.findAll();
    }

    public AiModelSpecification findAiModelSpecificationByUuid(String uuid) {
        log.info("AiModelSpecification FIND BY UUID");
        return aiModelSpecificationRepository.findByUuid(uuid).orElse(null);
    }

    public AiModelSpecification findAiModelSpecificationByNameAndVersion(String name, String version) {
        log.info("AiModelSpecification FIND BY name/version: {}/{}", name, version);
        return aiModelSpecificationRepository.findByNameAndVersion(name, version).orElse(null);
    }

    public AiModelSpecification findAiModelSpecificationByName(String name) {
        log.info("AiModelSpecification FIND BY name: {}", name);
        return aiModelSpecificationRepository.findByName(name).orElse(null);
    }

    public AiModelSpecification createAiModelSpecification(AiModelSpecificationCreate aiModelSpecCreate) {
        log.info("AiModelSpecification CREATE: {}", aiModelSpecCreate);
        AiModelSpecification aiModelSpec = specificationMapper.fromCreate(aiModelSpecCreate);
        return aiModelSpecificationRepository.save(aiModelSpec);
    }

    public AiModelSpecification updateAiModelSpecification(String uuid, AiModelSpecificationUpdate aiModelSpecUpdate) {
        log.info("AiModelSpecification UPDATE with UUID: {}", uuid);
        aiModelSpecificationRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No AI Model with UUID: " + uuid));
        AiModelSpecification aiModelSpec = specificationMapper.fromUpdate(aiModelSpecUpdate);
        return aiModelSpecificationRepository.save(aiModelSpec);
    }

    public void deleteAiModelSpecification(String uuid) {
        log.info("AiModelSpecification DELETE with UUID: {}", uuid);
        AiModelSpecification aiModelSpec = aiModelSpecificationRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No AI Model with UUID: " + uuid));
        aiModelSpecificationRepository.delete(aiModelSpec);
    }
}
