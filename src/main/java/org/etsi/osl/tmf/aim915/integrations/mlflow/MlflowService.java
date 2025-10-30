package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.mlflow.tracking.MlflowClient;
import org.mlflow.api.proto.ModelRegistry.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.mlflow.api.proto.Service.Run;
import org.mlflow.api.proto.Service.Experiment;
import org.mlflow.api.proto.Service.RunInfo;
import org.mlflow.api.proto.Service.RunData;
import org.mlflow.api.proto.Service.Param;
import org.mlflow.api.proto.Service.Metric;
import org.mlflow.api.proto.Service.RunTag;
import java.util.List;

@Service
public class MlflowService {

    private final MlflowClient client;

    public MlflowService(@Value("${mlflow.host:127.0.0.1}") String host,
                         @Value("${mlflow.port:5000}") int port) {
        String url = String.format("http://%s:%d", host, port);
        this.client = new MlflowClient(url);
    }

    public Run getMlFlowRun(String runId) {
        return client.getRun(runId);
    }

    public Experiment getMlFlowExperiment(String experimentId) {
        return client.getExperiment(experimentId);
    }

    public AiModelSpecification convertMlflowRunToAiModel(Run run) {
        AiModelSpecification aiModelSpecification = new AiModelSpecification();


        RunInfo runInfo = run.getInfo();
        RunData runData = run.getData();
        List<Param> paramsList = runData.getParamsList();
        List<Metric> metricList = runData.getMetricsList();
        List<RunTag> runTags = runData.getTagsList();

        aiModelSpecification.setAtType("AiModelSpecification");

        if(runInfo.getRunName() != null) {
            aiModelSpecification.setName(runInfo.getRunName());
        } else if (!runTags.isEmpty()){
            for (RunTag tag : runTags) {
                if (tag.getKey().equals("mlflow.runName")) {
                    aiModelSpecification.setName(tag.getValue());
                    break;
                }
            }
        } else {
            aiModelSpecification.setName("Mlflow Run " + runInfo.getRunId());
        }

        if(!runTags.isEmpty()){
            for (RunTag tag : runTags) {
                if (tag.getKey().equals("mlflow.source.name")) {
                    aiModelSpecification.setDescription("Source: " + tag.getValue());
                    break;
                }
            }
        }

        return aiModelSpecification;
    }


}