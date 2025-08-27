package org.etsi.osl.tmf.aim915.reposervices;


import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelMapper;
import org.etsi.osl.tmf.aim915.model.AiModelUpdate;
import org.etsi.osl.tmf.aim915.repo.AiModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AiModelRepositoryService {

    private static final Logger log = LoggerFactory.getLogger(AiModelRepositoryService.class);

    private final AiModelRepository aiModelRepository;

    @Autowired
    public AiModelRepositoryService(AiModelRepository aiModelRepository) {
        this.aiModelRepository = aiModelRepository;
    }

    public List<AiModel> findAllAiModels() {
        log.info("AiModels LIST");
        return (List<AiModel>)  aiModelRepository.findAll();
    }

    public AiModel findAiModelByUuid(String uuid) {
        log.info("AiModel FIND BY UUID");
        return aiModelRepository.findByUuid(uuid).orElse(null);
    }

    public AiModel createAiModel(AiModelCreate aiModelCreate) {
        log.info("AiModel CREATE: {}", aiModelCreate);
        AiModelMapper mapper = AiModelMapper.INSTANCE;
        AiModel aiModel = mapper.toAiModel(aiModelCreate);
        return aiModelRepository.save(aiModel);
    }

    public AiModel updateAiModel(String uuid, AiModelUpdate aiModelUpdate) {
        log.info("AiModel UPDATE with UUID: {}", uuid);
        aiModelRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No AI Model with UUID: " + uuid));
        AiModelMapper mapper = AiModelMapper.INSTANCE;
        AiModel aiModel = mapper.toAiModel(aiModelUpdate);
        return aiModelRepository.save(aiModel);
    }

    public void deleteAiModel(String uuid) {
        log.info("AiModel DELETE with UUID: {}", uuid);
        AiModel aiModel = aiModelRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No AI Model with UUID: " + uuid));
        aiModelRepository.delete(aiModel);
    }
}
