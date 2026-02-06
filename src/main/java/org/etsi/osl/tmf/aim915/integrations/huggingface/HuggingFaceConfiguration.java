package org.etsi.osl.tmf.aim915.integrations.huggingface;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for Hugging Face integration.
 * 
 * Provides Spring beans for Hugging Face Hub API access.
 * This configuration is only activated when huggingface.enabled=true.
 */
@Configuration
@ConditionalOnProperty(name = "huggingface.enabled", havingValue = "true", matchIfMissing = true)
public class HuggingFaceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceConfiguration.class);

    public static final String HF_API_BASE_URL = "https://huggingface.co/api";
    public static final String HF_HUB_URL = "https://huggingface.co";

    @Value("${huggingface.api.token:}")
    private String apiToken;

    @Value("${huggingface.download.timeout:300000}")
    private int downloadTimeout;

    @Value("${huggingface.connection-timeout:30000}")
    private int connectionTimeout;

    @Value("${huggingface.read-timeout:60000}")
    private int readTimeout;

    /**
     * Provides the Hugging Face API token.
     * 
     * @return The API token (may be empty for public models)
     */
    @Bean
    public String huggingFaceApiToken() {
        if (apiToken != null && !apiToken.isEmpty()) {
            log.info("Hugging Face API token configured");
        } else {
            log.info("No Hugging Face API token configured - only public models will be accessible");
        }
        return apiToken;
    }

    /**
     * Provides the Hugging Face API base URL.
     * 
     * @return The API base URL
     */
    @Bean
    public String huggingFaceApiBaseUrl() {
        return HF_API_BASE_URL;
    }

    /**
     * Provides the Hugging Face Hub URL.
     * 
     * @return The Hub URL
     */
    @Bean
    public String huggingFaceHubUrl() {
        return HF_HUB_URL;
    }

    /**
     * Creates a RestTemplate for Hugging Face API calls with configured timeouts.
     * 
     * @return RestTemplate instance with connection and read timeouts
     */
    @Bean
    public RestTemplate huggingFaceRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        log.info("Hugging Face RestTemplate configured with connection timeout: {}ms, read timeout: {}ms", 
                connectionTimeout, readTimeout);
        
        return new RestTemplate(factory);
    }

    /**
     * Creates a RestTemplate configured for streaming large files.
     * Uses extended timeout for large model downloads.
     * 
     * @return RestTemplate instance configured for streaming
     */
    @Bean
    public RestTemplate huggingFaceStreamingRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(downloadTimeout);
        factory.setReadTimeout(downloadTimeout);
        factory.setBufferRequestBody(false); // Don't buffer the entire response in memory
        
        log.info("Hugging Face streaming RestTemplate configured with timeout: {}ms", downloadTimeout);
        
        return new RestTemplate(factory);
    }

    /**
     * Provides the download timeout value.
     * 
     * @return Download timeout in milliseconds
     */
    @Bean
    public Integer huggingFaceDownloadTimeout() {
        return downloadTimeout;
    }
}
