package org.etsi.osl.tmf.aim915.reposervices;


import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelMapper;
import org.etsi.osl.tmf.aim915.model.AiModelUpdate;
import org.etsi.osl.tmf.aim915.repo.AiModelRepository;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
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

    private AiModelMapper aiModelMapper;

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

        AiModel aiModel = aiModelMapper.fromCreate(aiModelCreate);
        return aiModelRepository.save(aiModel);
    }

    public AiModel updateAiModel(String uuid, AiModelUpdate aiModelUpdate) {
        log.info("AiModel UPDATE with UUID: {}", uuid);
        aiModelRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No AI Model with UUID: " + uuid));
        AiModel aiModel = aiModelMapper.fromUpdate(aiModelUpdate);
        return aiModelRepository.save(aiModel);
    }

    public void deleteAiModel(String uuid) {
        log.info("AiModel DELETE with UUID: {}", uuid);
        AiModel aiModel = aiModelRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No AI Model with UUID: " + uuid));
        aiModelRepository.delete(aiModel);
    }

    /**
     * Updates only the state of an AiModel.
     * 
     * @param uuid The UUID of the AiModel to update
     * @param state The new state
     * @return The updated AiModel
     * @throws IllegalArgumentException if no AiModel with the given UUID exists
     */
    public AiModel updateAiModelState(String uuid, ServiceStateType state) {
        log.info("AiModel UPDATE STATE with UUID: {} to {}", uuid, state);
        AiModel aiModel = aiModelRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No AI Model with UUID: " + uuid));
        aiModel.setState(state);
        return aiModelRepository.save(aiModel);
    }

    public List<AiModel> findByNameStartingWith(String namePrefix) {
        return aiModelRepository.findByNameStartingWith(namePrefix);
    }

    /**
     * Finds all AiModels with a given state.
     * 
     * @param state The state to filter by
     * @return List of AiModels with the given state
     */
    public List<AiModel> findByState(ServiceStateType state) {
        log.info("AiModel FIND BY STATE: {}", state);
        return aiModelRepository.findByState(state);
    }
}
