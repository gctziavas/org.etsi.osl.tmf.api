package org.etsi.osl.tmf.pm628.reposervices;


import jakarta.validation.Valid;
import org.etsi.osl.tmf.pm628.model.DataAccessEndpoint;
import org.etsi.osl.tmf.pm628.model.DataAccessEndpointFVO;
import org.etsi.osl.tmf.pm628.model.DataAccessEndpointMVO;
import org.etsi.osl.tmf.pm628.model.DataAccessEndpointMapper;
import org.etsi.osl.tmf.pm628.repo.DataAccessEndpointRepository;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DataAccessEndpointService {

    private static final Logger log = LoggerFactory.getLogger(DataAccessEndpointService.class);

    private final DataAccessEndpointRepository dataAccessEndpointRepository;

    @Autowired
    public DataAccessEndpointService(DataAccessEndpointRepository dataAccessEndpointRepository) {
        this.dataAccessEndpointRepository = dataAccessEndpointRepository;
    }

    public List<DataAccessEndpoint> findAllDataAccessEndpoints(){
        log.debug("DataAccessEndpoint LIST");
        return (List<DataAccessEndpoint>) dataAccessEndpointRepository.findAll();
    }

    public DataAccessEndpoint findDataAccessEndpointByUuid(String uuid){
        log.debug("DataAccessEndpoint FIND BY UUID");
        Optional<DataAccessEndpoint> dataAccessEndpoint = dataAccessEndpointRepository.findByUuid(uuid);
        return dataAccessEndpoint.orElse(null);
    }

    public DataAccessEndpoint createDataAccessEndpoint(DataAccessEndpointFVO dataAccessEndpointFVO){
        log.debug("DataAccessEndpoint CREATE: {}",dataAccessEndpointFVO);

        DataAccessEndpointMapper mapper = Mappers.getMapper(DataAccessEndpointMapper.class);
        DataAccessEndpoint dataAccessEndpoint = mapper.createDataAccessEndpoint(dataAccessEndpointFVO);

        return dataAccessEndpointRepository.save(dataAccessEndpoint);
    }

    public DataAccessEndpoint updateDataAccessEndpoint(String uuid, @Valid DataAccessEndpointMVO dataAccessEndpointUpdate){
        log.debug("DataAccessEndpoint UPDATE with UUID: {}", uuid);
        DataAccessEndpoint dataAccessEndpoint = dataAccessEndpointRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No Data Access Endpoint with UUID: " + uuid));

        DataAccessEndpointMapper mapper = Mappers.getMapper(DataAccessEndpointMapper.class);

        dataAccessEndpoint = mapper.updateDataAccessEndpoint(dataAccessEndpointUpdate, dataAccessEndpoint);
        dataAccessEndpointRepository.save(dataAccessEndpoint);
        return dataAccessEndpoint;
    }

    public void  deleteDataAccessEndpoint(String uuid){
        log.debug("DataAccessEndpoint DELETE with UUID:{}", uuid);
        DataAccessEndpoint dataAccessEndpoint = dataAccessEndpointRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("No Data Access Endpoint with UUID: " + uuid));
        dataAccessEndpointRepository.delete(dataAccessEndpoint);
    }
}
