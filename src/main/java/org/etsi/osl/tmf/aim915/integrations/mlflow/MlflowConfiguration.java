package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.mlflow.tracking.MlflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for MLflow integration.
 * 
 * Provides Spring beans for MLflow client and related utilities.
 * This configuration is only activated when mlflow.enabled=true.
 * 
 * The MlflowClient from the official mlflow-client library handles
 * all communication with the MLflow tracking server.
 */
@Configuration
@ConditionalOnProperty(name = "mlflow.enabled", havingValue = "true", matchIfMissing = true)
public class MlflowConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MlflowConfiguration.class);

    @Value("${mlflow.host:127.0.0.1}")
    private String mlflowHost;

    @Value("${mlflow.port:5000}")
    private int mlflowPort;

    /**
     * Creates the MLflow tracking URI from host and port configuration.
     * 
     * @return The MLflow tracking URI (e.g., "http://localhost:5000")
     */
    @Bean
    public String mlflowTrackingUri() {
        String uri = String.format("http://%s:%d", mlflowHost, mlflowPort);
        log.info("MLflow tracking URI: {}", uri);
        return uri;
    }

    /**
     * Creates the MLflow client for interacting with the MLflow tracking server.
     * 
     * The MlflowClient handles connection management, serialization,
     * and all REST API calls internally.
     * 
     * @return Configured MlflowClient instance
     */
    @Bean
    public MlflowClient mlflowClient() {
        String uri = mlflowTrackingUri();
        log.info("Initializing MlflowClient with URI: {}", uri);
        return new MlflowClient(uri);
    }
}
