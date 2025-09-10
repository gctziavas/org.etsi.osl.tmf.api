package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.mlflow.tracking.MlflowClient;
import org.mlflow.api.proto.ModelRegistry.*;
import org.springframework.stereotype.Service;

@Service
public class MlflowService {

    private final MlflowClient client;

    public MlflowService() {
        this.client = new MlflowClient("http://your-mlflow-server:5000");
    }

    public ListRegisteredModels.Response listRegisteredModels() {
        return client.listRegisteredModels();
    }

    // Add more methods as needed
}