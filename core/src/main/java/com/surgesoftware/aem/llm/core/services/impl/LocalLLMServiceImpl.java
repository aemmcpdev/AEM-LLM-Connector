/*
 * Copyright 2024 SURGE Software Solutions Private Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.surgesoftware.aem.llm.core.services.impl;

import com.surgesoftware.aem.llm.core.services.LocalLLMService;
import com.surgesoftware.aem.llm.core.services.LocalLLMConfiguration;
import com.surgesoftware.aem.llm.core.models.*;
import com.surgesoftware.aem.llm.core.exceptions.LLMUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.util.EntityUtils;
import org.apache.http.conn.ConnectTimeoutException;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;


/**
 * Local LLM Service Implementation for SURGE AEM LLM Connector
 * 
 * Implementation of Local LLM service for generating AEM component files
 * using Ollama/LocalAI integration.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = LocalLLMService.class,
    property = {
        "service.description=SURGE AEM LLM Connector - Local LLM Service",
        "service.vendor=SURGE Software Solutions Private Limited"
    })
@Designate(ocd = LocalLLMConfiguration.class)
public class LocalLLMServiceImpl implements LocalLLMService {
    
    private static final Logger LOG = LoggerFactory.getLogger(LocalLLMServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Production-hardened defaults - no OSGi config required for startup
    private static final String DEFAULT_PROVIDER = "ollama";
    private static final String DEFAULT_API_URL = "http://localhost:11434/api/generate";
    private static final String DEFAULT_MODEL = "llama3.2";
    private static final String[] FALLBACK_MODELS = {"llama3", "llama2", "codellama", "llama3.2:latest"};
    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_MAX_TOKENS = 4000;
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int DEFAULT_TIMEOUT = 180;
    private static final int DEFAULT_RETRY_ATTEMPTS = 3;
    private static final String DEFAULT_SYSTEM_PROMPT = "You are an expert AEM developer working for SURGE Software Solutions. Generate clean, production-ready AEM component files following Adobe best practices.";
    private static final boolean DEFAULT_STRIP_MARKDOWN = true;
    
    // Retry and backoff configuration for timeout resilience
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 2000; // 2 seconds
    private static final long WARMUP_TIMEOUT_MS = 10000; // 10 seconds for warm-up
    private static final String WARMUP_PROMPT = "hi"; // Simple prompt for model warm-up
    
    // Instance variables - configured from defaults or OSGi
    private String provider;
    private String apiUrl;
    private String model;
    private boolean enabled;
    private int maxTokens;
    private double temperature;
    private int timeout;
    private int retryAttempts;
    private String systemPrompt;
    private boolean stripMarkdown;
    
    private CloseableHttpClient httpClient;
    
    @Activate
    @Modified
    protected void activate(LocalLLMConfiguration configuration) {
        LOG.info("SURGE AEM LLM Connector: Local LLM Service activating...");
        
        // Use defaults if OSGi config is missing or incomplete
        if (configuration == null) {
            LOG.info("No OSGi configuration found - using embedded defaults");
            initializeWithDefaults();
        } else {
            // Apply configuration with fallback to defaults for missing values
            this.enabled = configuration.enabled();
            this.apiUrl = Optional.ofNullable(configuration.apiUrl())
                .filter(s -> !s.trim().isEmpty())
                .orElse(DEFAULT_API_URL);
            this.model = Optional.ofNullable(configuration.model())
                .filter(s -> !s.trim().isEmpty())
                .orElse(DEFAULT_MODEL);
            this.provider = Optional.ofNullable(configuration.provider())
                .filter(s -> !s.trim().isEmpty())
                .orElse(DEFAULT_PROVIDER);
            this.maxTokens = configuration.maxTokens() > 0 ? configuration.maxTokens() : DEFAULT_MAX_TOKENS;
            this.temperature = configuration.temperature() > 0 ? configuration.temperature() : DEFAULT_TEMPERATURE;
            this.timeout = configuration.timeout() > 0 ? configuration.timeout() : DEFAULT_TIMEOUT;
            this.retryAttempts = configuration.retryAttempts() > 0 ? configuration.retryAttempts() : DEFAULT_RETRY_ATTEMPTS;
            this.systemPrompt = Optional.ofNullable(configuration.systemPrompt())
                .filter(s -> !s.trim().isEmpty())
                .orElse(DEFAULT_SYSTEM_PROMPT);
            this.stripMarkdown = configuration.stripMarkdown();
        }
        
        if (this.enabled) {
            // Create HTTP client with proper timeouts
            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(this.timeout * 1000) // Convert to milliseconds
                .setSocketTimeout(this.timeout * 1000)
                .setConnectionRequestTimeout(this.timeout * 1000)
                .build();
            
            this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
            
            LOG.info("✅ Local LLM Provider: {}", this.provider);
            LOG.info("✅ Local LLM API URL: {}", this.apiUrl);
            LOG.info("✅ Local LLM Model: {}", this.model);
            LOG.info("✅ Max Tokens: {}", this.maxTokens);
            LOG.info("✅ Temperature: {}", this.temperature);
            LOG.info("🎉 SURGE AEM LLM Connector: Service activated successfully with {} configuration", 
                    configuration != null ? "OSGi" : "embedded defaults");
        } else {
            LOG.info("❌ Local LLM Service is disabled in configuration");
        }
    }
    
    /**
     * Initialize service with hardcoded production defaults
     */
    private void initializeWithDefaults() {
        this.enabled = DEFAULT_ENABLED;
        this.provider = DEFAULT_PROVIDER;
        this.apiUrl = DEFAULT_API_URL;
        this.model = DEFAULT_MODEL;
        this.maxTokens = DEFAULT_MAX_TOKENS;
        this.temperature = DEFAULT_TEMPERATURE;
        this.timeout = DEFAULT_TIMEOUT;
        this.retryAttempts = DEFAULT_RETRY_ATTEMPTS;
        this.systemPrompt = DEFAULT_SYSTEM_PROMPT;
        this.stripMarkdown = DEFAULT_STRIP_MARKDOWN;
        LOG.info("🔧 Initialized with production defaults: {} - {} - {}", 
                this.provider, this.model, this.apiUrl);
    }
    
    @Override
    public ComponentGenerationResponse generateComponent(ComponentGenerationRequest request) {
        LOG.info("Generating component using Local LLM for prompt: {}", request.getPrompt());
        
        ComponentGenerationResponse response = new ComponentGenerationResponse();
        response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        if (!this.enabled) {
            response.setStatus("error");
            response.setError("Local LLM service is not enabled");
            return response;
        }
        
        try {
            // Build the prompt for the LLM
            String prompt = buildComponentPrompt(request);
            LOG.debug("Sending prompt to Local LLM: {}", prompt);
            
            // Call the local LLM with image support and retry logic
            String llmResponse = callLocalLLMWithRetry(prompt, request.getImageData());
            
            if (llmResponse == null || llmResponse.trim().isEmpty()) {
                response.setStatus("error");
                response.setError("Empty response from Local LLM after all retry attempts");
                return response;
            }
            
            // Parse the LLM response
            LLMResponse parsedResponse = parseLLMResponse(llmResponse);
            
            if (parsedResponse == null) {
                response.setStatus("error");
                response.setError("Failed to parse Local LLM response");
                return response;
            }
            
            // Build the component generation response
            response.setStatus("success");
            response.setMessage("Component generated successfully using Local LLM");
            response.setComponentName(parsedResponse.getName());
            response.setComponentDescription(parsedResponse.getDescription());
            
            // Add generated files
            if (parsedResponse.getDialog() != null) {
                response.addGeneratedFile("dialog.xml", parsedResponse.getDialog());
            }
            if (parsedResponse.getHtml() != null) {
                response.addGeneratedFile(parsedResponse.getName() + ".html", parsedResponse.getHtml());
            }
            if (parsedResponse.getJs() != null) {
                response.addGeneratedFile(parsedResponse.getName() + ".js", parsedResponse.getJs());
            }
            if (parsedResponse.getJava() != null) {
                response.addGeneratedFile(capitalize(parsedResponse.getName()) + "Model.java", parsedResponse.getJava());
            }
            if (parsedResponse.getContent() != null) {
                response.addGeneratedFile(".content.xml", parsedResponse.getContent());
            }
            
            // Set preview HTML and sample data
            if (parsedResponse.getPreviewHtml() != null) {
                response.setPreviewHtml(parsedResponse.getPreviewHtml());
            } else {
                // Generate preview HTML from sample data
                response.setPreviewHtml(generatePreviewHtml(parsedResponse));
            }
            
            if (parsedResponse.getSampleData() != null) {
                response.setSampleData(parsedResponse.getSampleData());
            }
            
            LOG.info("Successfully generated component: {} with {} files", 
                    parsedResponse.getName(), response.getGeneratedFiles().size());
            
        } catch (LLMUnavailableException e) {
            LOG.error("❌ LLM service unavailable: {}", e.getMessage(), e);
            response.setStatus("error");
            
            // Provide user-friendly error messages from custom exception
            response.setError(e.getUserFriendlyMessage());
            if (e.getTechnicalDetails() != null) {
                response.setModelError(e.getTechnicalDetails());
            }
            if (e.getSuggestion() != null) {
                String currentError = response.getError();
                response.setError(currentError + ". " + e.getSuggestion());
            }
            
        } catch (Exception e) {
            LOG.error("❌ Unexpected error generating component using Local LLM: {}", e.getMessage(), e);
            response.setStatus("error");
            
            // Provide specific error messages based on exception type
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Connection timeout") || errorMessage.contains("Connection refused")) {
                response.setError("Cannot connect to Local LLM. Please ensure Ollama is running on " + this.apiUrl);
            } else if (errorMessage.contains("not found") || errorMessage.contains("Available models:")) {
                // This is a model availability error
                response.setModelError(errorMessage);
                response.setError("Model not available. " + errorMessage);
            } else if (errorMessage.contains("timeout")) {
                response.setError("LLM request timeout. " + errorMessage);
            } else {
                response.setError("Failed to generate component: " + errorMessage);
            }
        }
        
        return response;
    }
    
    @Override
    public boolean testConnection() {
        LOG.info("Testing Local LLM API connection");
        
        if (!this.enabled) {
            LOG.warn("Local LLM service is not enabled");
            return false;
        }
        
        try {
            String testPrompt = "Generate a simple test response for AEM component generation.";
            String response = callLocalLLM(testPrompt);
            boolean isConnected = response != null && !response.trim().isEmpty();
            
            LOG.info("Local LLM API connection test result: {}", isConnected ? "SUCCESS" : "FAILED");
            return isConnected;
            
        } catch (Exception e) {
            LOG.error("Local LLM API connection test failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getLLMInfo() {
        if (!this.enabled) {
            return "Local LLM Service: Disabled";
        }
        return String.format("Local LLM Service: %s - %s - %s", 
                this.provider, this.model, this.apiUrl);
    }
    
    /**
     * Check what models are available on the Ollama server
     */
    private List<String> getAvailableModels() throws IOException {
        if (!"ollama".equals(this.provider)) {
            return new ArrayList<>();
        }
        
        String tagsUrl = this.apiUrl.replace("/api/generate", "/api/tags");
        LOG.info("🔍 Checking available models at: {}", tagsUrl);
        
        HttpGet httpGet = new HttpGet(tagsUrl);
        
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            if (statusCode == 200) {
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                List<String> availableModels = new ArrayList<>();
                
                if (responseMap.containsKey("models")) {
                    List<Map<String, Object>> models = (List<Map<String, Object>>) responseMap.get("models");
                    for (Map<String, Object> model : models) {
                        if (model.containsKey("name")) {
                            availableModels.add((String) model.get("name"));
                        }
                    }
                }
                
                LOG.info("📋 Available models: {}", availableModels);
                return availableModels;
            } else {
                LOG.warn("Failed to get available models. Status: {}, Response: {}", statusCode, responseBody);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOG.warn("Error checking available models: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Check if a specific model is available and find best alternative
     */
    private String findBestAvailableModel(String requestedModel) throws IOException {
        List<String> availableModels = getAvailableModels();
        
        if (availableModels.isEmpty()) {
            LOG.warn("No models found on Ollama server");
            return requestedModel; // Fall back to original request
        }
        
        // Check if requested model is available (exact match or with :latest)
        String normalizedRequest = requestedModel.contains(":") ? requestedModel : requestedModel + ":latest";
        for (String available : availableModels) {
            if (available.equals(requestedModel) || available.equals(normalizedRequest)) {
                LOG.info("✅ Requested model '{}' is available", requestedModel);
                return requestedModel;
            }
        }
        
        // Model not found - try to find a suitable alternative
        LOG.warn("❌ Model '{}' not found. Available models: {}", requestedModel, availableModels);
        
        // Try to find a similar model (llama family first)
        String bestMatch = findBestModelMatch(requestedModel, availableModels);
        if (bestMatch != null) {
            LOG.info("🔄 Auto-selecting alternative model: {}", bestMatch);
            return bestMatch;
        }
        
        // If no good match, just use the first available model
        if (!availableModels.isEmpty()) {
            String firstModel = availableModels.get(0);
            LOG.info("🔄 Using first available model: {}", firstModel);
            return firstModel;
        }
        
        // No models available at all
        throw new IOException("No models available on Ollama server. Available models: " + availableModels + 
                             ". Please run 'ollama pull " + requestedModel + "' to install the requested model.");
    }
    
    /**
     * Find the best matching model from available models
     */
    private String findBestModelMatch(String requestedModel, List<String> availableModels) {
        String baseRequest = requestedModel.split(":")[0].toLowerCase();
        
        // Priority order for model families
        String[] preferredPrefixes = {"llama3", "llama", "codellama", "mistral", "phi"};
        
        for (String prefix : preferredPrefixes) {
            if (baseRequest.startsWith(prefix)) {
                // Look for models with the same prefix
                for (String available : availableModels) {
                    String baseAvailable = available.split(":")[0].toLowerCase();
                    if (baseAvailable.startsWith(prefix)) {
                        return available;
                    }
                }
            }
        }
        
        // If no family match, look for any llama model
        for (String available : availableModels) {
            if (available.toLowerCase().contains("llama")) {
                return available;
            }
        }
        
        return null;
    }
    
    /**
     * Enhanced Local LLM call with retry logic, exponential backoff, and model warm-up
     */
    private String callLocalLLMWithRetry(String prompt, String imageData) throws LLMUnavailableException {
        LLMUnavailableException lastException = null;
        boolean attemptedWarmup = false;
        
        // Enhanced retry loop with exponential backoff
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                LOG.info("⏳ Sending prompt to Ollama... (timeout: {}s, model: {}, attempt: {}/{})", 
                         this.timeout, this.model, attempt, MAX_RETRY_ATTEMPTS);
                         
                return callLocalLLM(prompt, imageData);
                
            } catch (IOException e) {
                LOG.warn("🔄 Retrying LLM call... attempt {} of {} failed: {}", attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                // Check if this is a timeout that might benefit from warm-up
                boolean isTimeout = e instanceof SocketTimeoutException || 
                                  e.getMessage().contains("timeout") || 
                                  e.getMessage().contains("Timeout");
                
                if (isTimeout && !attemptedWarmup && "ollama".equals(this.provider)) {
                    LOG.info("🔥 Model warm-up triggered. Waiting for LLM to load...");
                    try {
                        warmUpModel(this.model);
                        attemptedWarmup = true;
                        LOG.info("✅ Model warm-up completed, retrying original request");
                        // Don't count warm-up as an attempt, continue with same attempt number
                        attempt--;
                        continue;
                    } catch (Exception warmupException) {
                        LOG.warn("⚠️ Model warm-up failed: {}", warmupException.getMessage());
                    }
                }
                
                // Convert IOException to LLMUnavailableException for better error handling
                if (e instanceof SocketTimeoutException) {
                    lastException = LLMUnavailableException.forTimeout(this.model, this.timeout);
                } else if (e instanceof ConnectException || e.getMessage().contains("Connection refused")) {
                    lastException = LLMUnavailableException.forConnectivity(this.apiUrl);
                } else {
                    lastException = new LLMUnavailableException(e.getMessage(), e);
                }
                
                // If this is the last attempt, try fallback models
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    if (e.getMessage().contains("not found") && "ollama".equals(this.provider)) {
                        return tryFallbackModels(prompt, imageData, lastException);
                    }
                } else {
                    // Apply exponential backoff before next attempt
                    long backoffTime = BASE_BACKOFF_MS * (long) Math.pow(2, attempt - 1);
                    LOG.info("⏱️ Waiting {}ms before retry {} of {}", backoffTime, attempt + 1, MAX_RETRY_ATTEMPTS);
                    try {
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LLMUnavailableException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        // All attempts exhausted
        throw lastException != null ? lastException : new LLMUnavailableException("All retry attempts failed");
    }
    
    /**
     * Attempt to warm up the model with a simple request
     */
    private void warmUpModel(String modelToWarmUp) throws IOException {
        LOG.info("🔥 Warming up model: {}", modelToWarmUp);
        
        Map<String, Object> warmupRequest = new HashMap<>();
        warmupRequest.put("model", modelToWarmUp);
        warmupRequest.put("prompt", WARMUP_PROMPT);
        warmupRequest.put("stream", false); // Don't stream for warm-up
        warmupRequest.put("options", Map.of("num_predict", 1)); // Minimal response
        
        String jsonBody = objectMapper.writeValueAsString(warmupRequest);
        HttpPost httpPost = new HttpPost(this.apiUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
        
        // Use shorter timeout for warm-up
        RequestConfig warmupConfig = RequestConfig.custom()
            .setConnectTimeout((int) WARMUP_TIMEOUT_MS)
            .setSocketTimeout((int) WARMUP_TIMEOUT_MS)
            .build();
        httpPost.setConfig(warmupConfig);
        
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                LOG.info("✅ Model warm-up successful for: {}", modelToWarmUp);
            } else {
                LOG.warn("⚠️ Model warm-up returned status {}", statusCode);
            }
        }
    }
    
    /**
     * Try fallback models when primary model fails
     */
    private String tryFallbackModels(String prompt, String imageData, LLMUnavailableException originalException) throws LLMUnavailableException {
        String originalModel = this.model;
        LOG.info("🔄 Trying fallback models after primary model '{}' failed", originalModel);
        
        for (String fallbackModel : FALLBACK_MODELS) {
            if (fallbackModel.equals(originalModel)) {
                continue; // Skip the model that already failed
            }
            
            try {
                LOG.info("🔄 Trying fallback model: {}", fallbackModel);
                this.model = fallbackModel; // Temporarily switch model
                String result = callLocalLLM(prompt, imageData);
                LOG.info("✅ Fallback model '{}' succeeded", fallbackModel);
                this.model = originalModel; // Restore original model
                return result;
            } catch (IOException fallbackException) {
                LOG.warn("Fallback model '{}' failed: {}", fallbackModel, fallbackException.getMessage());
            }
        }
        
        this.model = originalModel; // Restore original model
        LOG.error("❌ All fallback models failed, throwing original exception");
        throw originalException;
    }
    
    /**
     * Check if Ollama service is ready to accept requests
     */
    private boolean isOllamaReady() {
        if (!"ollama".equals(this.provider)) {
            return true; // Skip check for non-Ollama providers
        }
        
        try {
            String tagsUrl = this.apiUrl.replace("/api/generate", "/api/tags");
            HttpGet httpGet = new HttpGet(tagsUrl);
            
            // Short timeout for readiness check
            RequestConfig quickCheck = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();
            httpGet.setConfig(quickCheck);
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    LOG.debug("✅ Ollama readiness check passed");
                    return true;
                } else {
                    LOG.warn("⚠️ Ollama readiness check failed with status: {}", statusCode);
                    return false;
                }
            }
        } catch (Exception e) {
            LOG.warn("⚠️ Ollama readiness check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private String callLocalLLM(String prompt) throws IOException {
        return callLocalLLM(prompt, null);
    }
    
    /**
     * Enhanced method that can handle both text and image prompts
     */
    private String callLocalLLM(String prompt, String imageData) throws IOException {
        if (!this.enabled) {
            LOG.warn("Local LLM service is not enabled");
            return null;
        }
        
        String providerName = this.provider.toLowerCase();
        
        switch (providerName) {
            case "ollama":
                return callOllamaAPI(prompt, imageData);
            case "localai":
                return callLocalAIAPI(prompt, imageData);
            default:
                LOG.warn("Unsupported LLM provider: {}", providerName);
                return null;
        }
    }
    
    private String callOllamaAPI(String prompt) throws IOException {
        return callOllamaAPI(prompt, null);
    }
    
    /**
     * Enhanced Ollama API call with readiness check and improved timeout handling
     */
    private String callOllamaAPI(String prompt, String imageData) throws IOException {
        String requestedModel = (imageData != null) ? "llava:7b" : this.model;
        
        // Perform readiness check before making the actual request
        if (!isOllamaReady()) {
            LOG.warn("⚠️ Ollama service not ready, proceeding anyway");
        }
        
        // Check model availability and find best alternative if needed
        String modelToUse;
        try {
            modelToUse = findBestAvailableModel(requestedModel);
        } catch (IOException e) {
            // If we can't check models, try the requested model anyway
            LOG.warn("Cannot check model availability, proceeding with requested model: {}", requestedModel);
            modelToUse = requestedModel;
        }
        
        LOG.info("🚀 Calling Ollama API: {} with model: {} (requested: {}) - Start time: {}", 
                 this.apiUrl, modelToUse, requestedModel, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        LOG.debug("Request details - Prompt length: {} chars, Image provided: {}, Timeout: {}s", 
                  prompt.length(), imageData != null, this.timeout);
        LOG.debug("Prompt preview: {}", prompt.length() > 100 ? prompt.substring(0, 100) + "..." : prompt);
        
        if (!modelToUse.equals(requestedModel)) {
            LOG.info("🔄 Using alternative model '{}' instead of '{}'", modelToUse, requestedModel);
        }
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelToUse);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", true);
        requestBody.put("options", Map.of(
            "temperature", this.temperature,
            "num_predict", this.maxTokens
        ));
        
        // Add image data if provided
        if (imageData != null) {
            String base64Data = imageData.substring(imageData.indexOf(",") + 1);
            requestBody.put("images", List.of(base64Data));
            LOG.info("📷 Added image data to Ollama request, using vision model: {}", modelToUse);
        }
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        LOG.info("📤 Sending request to Ollama - JSON size: {} bytes", jsonBody.length());
        LOG.debug("Request JSON preview: {}", jsonBody.length() > 200 ? jsonBody.substring(0, 200) + "..." : jsonBody);
        
        HttpPost httpPost = new HttpPost(this.apiUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
        
        long startTime = System.currentTimeMillis();
        LOG.info("⏱️ Starting request at: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            LOG.info("📬 Ollama API Response: HTTP {} - Processing streaming response...", statusCode);
            
            if (statusCode == 200) {
                try {
                    // Handle streaming response (application/x-ndjson)
                    String fullResponse = processStreamingResponse(responseBody, modelToUse, startTime);
                    
                    long duration = System.currentTimeMillis() - startTime;
                    LOG.info("✅ Successfully received LLM response ({} chars) in {}ms", 
                             fullResponse.length(), duration);
                    
                    return fullResponse;
                } catch (JsonProcessingException e) {
                    LOG.error("❌ Failed to parse Ollama JSON response: {}", e.getMessage());
                    throw new IOException("Invalid JSON response from Ollama: " + e.getMessage());
                }
            } else if (statusCode == 404) {
                // Get available models for better error message
                List<String> availableModels;
                try {
                    availableModels = getAvailableModels();
                } catch (Exception ex) {
                    availableModels = new ArrayList<>();
                }
                
                String errorMsg = "Model '" + modelToUse + "' not found on Ollama server.";
                if (!availableModels.isEmpty()) {
                    errorMsg += " Available models: " + availableModels + ".";
                }
                errorMsg += " Please run 'ollama pull " + modelToUse + "' to install this model.";
                
                LOG.error("❌ {}", errorMsg);
                throw new IOException(errorMsg);
            } else if (statusCode == 500) {
                LOG.error("❌ Ollama server error (500): {}", responseBody);
                throw new IOException("Ollama server error: " + responseBody);
            } else {
                LOG.error("❌ Ollama API failed with status {}: {}", statusCode, responseBody);
                throw new IOException("Ollama API failed with status " + statusCode + ": " + responseBody);
            }
            
        } catch (ConnectTimeoutException e) {
            LOG.error("❌ Connection timeout to Ollama API at {}", this.apiUrl);
            throw new IOException("Connection timeout to Ollama. Is Ollama running on " + this.apiUrl + "?");
        } catch (SocketTimeoutException e) {
            LOG.error("❌ Socket timeout waiting for Ollama response ({}s) - Model: {} - Time: {}", 
                     this.timeout, modelToUse, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
            LOG.info("💡 Suggestion: Try running 'ollama run {}' manually to warm up the model", modelToUse);
            throw new IOException("Timeout waiting for Ollama response. Try increasing timeout or using a smaller model.");
        } catch (ConnectException e) {
            LOG.error("❌ Connection refused to Ollama API at {}", this.apiUrl);
            throw new IOException("Cannot connect to Ollama. Is Ollama running on " + this.apiUrl + "?");
        } catch (IOException e) {
            LOG.error("❌ IO error calling Ollama API: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Process streaming response from Ollama (NDJSON format)
     */
    private String processStreamingResponse(String responseBody, String modelToUse, long startTime) throws IOException {
        StringBuilder fullResponse = new StringBuilder();
        String[] lines = responseBody.split("\n");
        
        LOG.info("🔄 Processing {} stream chunks for model: {}", lines.length, modelToUse);
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            try {
                Map<String, Object> chunk = objectMapper.readValue(line, Map.class);
                
                if (chunk.containsKey("response")) {
                    String partialResponse = (String) chunk.get("response");
                    fullResponse.append(partialResponse);
                    
                    // Log progress every 10 chunks or if it's the last chunk
                    if (chunk.containsKey("done") && (Boolean) chunk.get("done")) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        LOG.info("🏁 Stream completed - Total response: {} chars in {}ms", 
                                fullResponse.length(), elapsed);
                        break;
                    }
                } else if (chunk.containsKey("error")) {
                    String error = chunk.get("error").toString();
                    LOG.error("❌ Ollama streaming error: {}", error);
                    throw new IOException("Ollama streaming error: " + error);
                }
            } catch (JsonProcessingException e) {
                LOG.warn("⚠️ Skipping malformed JSON chunk: {}", line);
                continue;
            }
        }
        
        if (fullResponse.length() == 0) {
            LOG.error("❌ No response content received from streaming");
            throw new IOException("No response content received from Ollama streaming API");
        }
        
        return fullResponse.toString();
    }
    
    private String callLocalAIAPI(String prompt) throws IOException {
        return callLocalAIAPI(prompt, null);
    }
    
    /**
     * NEW: Enhanced LocalAI API call that supports image prompts (basic implementation)
     * Note: LocalAI image support may vary based on model configuration
     */
    private String callLocalAIAPI(String prompt, String imageData) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", this.model);
        requestBody.put("max_tokens", this.maxTokens);
        requestBody.put("temperature", this.temperature);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", this.systemPrompt));
        
        // NEW: Handle image content for LocalAI
        if (imageData != null) {
            // For LocalAI, we include image information in the prompt
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt + "\n\n[Note: Image data provided for analysis]");
            messages.add(userMessage);
            LOG.info("Added image context to LocalAI request");
        } else {
        messages.add(Map.of("role", "user", "content", prompt));
        }
        
        requestBody.put("messages", messages);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpPost httpPost = new HttpPost(this.apiUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
        
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                if (responseMap.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        if (choice.containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) choice.get("message");
                            if (message.containsKey("content")) {
                                return (String) message.get("content");
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    private String buildComponentPrompt(ComponentGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Generate an AEM component based on the following requirements:\n\n");
        prompt.append("User Prompt: ").append(request.getPrompt()).append("\n\n");
        
        // NEW: Handle image prompts
        if (request.hasImage()) {
            prompt.append("IMPORTANT: An image has been provided with this request. ")
                  .append("Analyze the visual content and incorporate relevant design elements, ")
                  .append("colors, layout, and content structure from the image into the AEM component. ")
                  .append("If the image shows UI elements, recreate them as appropriate AEM fields and styling.\n\n");
        }
        
        if (request.getRequirements() != null && !request.getRequirements().trim().isEmpty()) {
            prompt.append("Additional Requirements: ").append(request.getRequirements()).append("\n\n");
        }
        
        prompt.append("Please respond with a valid JSON object containing the following structure:\n");
        prompt.append("{\n");
        prompt.append("  \"name\": \"component-name\",\n");
        prompt.append("  \"description\": \"Component description\",\n");
        prompt.append("  \"fields\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"fieldName\",\n");
        prompt.append("      \"type\": \"text|richtext|image|link|select\",\n");
        prompt.append("      \"label\": \"Field Label\",\n");
        prompt.append("      \"description\": \"Field description\",\n");
        prompt.append("      \"required\": false,\n");
        prompt.append("      \"sample\": \"Sample value for preview\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"html\": \"HTL template code\",\n");
        prompt.append("  \"dialog\": \"Dialog XML code\",\n");
        prompt.append("  \"js\": \"JavaScript code\",\n");
        prompt.append("  \"java\": \"Sling Model Java code\",\n");
        prompt.append("  \"content\": \".content.xml code\",\n");
        prompt.append("  \"previewHtml\": \"HTML for preview with sample data\",\n");
        prompt.append("  \"sampleData\": {\n");
        prompt.append("    \"fieldName\": \"sample value\"\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");
        prompt.append("Ensure all code follows AEM best practices and is production-ready.");
        
        return prompt.toString();
    }
    
    private LLMResponse parseLLMResponse(String rawResponse) {
        LOG.info("🔄 Starting JSON parsing for LLM response ({} chars)", rawResponse.length());
        
        try {
            // Enhanced JSON extraction with Markdown sanitization
            String cleaned = extractJsonBlock(rawResponse);
            
            if (cleaned == null) {
                LOG.error("❌ No JSON content found in LLM response. Raw response preview:\n{}", 
                         rawResponse.substring(0, Math.min(500, rawResponse.length())));
                return null;
            }
            
            // Validate the cleaned JSON structure
            if (!cleaned.trim().startsWith("{")) {
                LOG.error("❌ Sanitized content does not start with '{{'. Content: {}", 
                        cleaned.substring(0, Math.min(200, cleaned.length())));
                return null;
            }
            
            if (!cleaned.trim().endsWith("}")) {
                LOG.error("❌ Sanitized content does not end with '}}'. Content: {}", 
                        cleaned.substring(Math.max(0, cleaned.length() - 200)));
                return null;
            }
            
            // Log the cleaned JSON before parsing
            LOG.info("🧹 Cleaned JSON ready for parsing (first 500 chars):\n{}", 
                     cleaned.substring(0, Math.min(500, cleaned.length())));
            
            // Attempt to parse with Jackson ObjectMapper
            LLMResponse response = objectMapper.readValue(cleaned, LLMResponse.class);
            
            // Validate the parsed object
            if (response == null) {
                LOG.error("❌ ObjectMapper returned null response");
                return null;
            }
            
            if (response.getName() == null || response.getName().trim().isEmpty()) {
                LOG.warn("⚠️ Parsed response has empty/null component name");
            }
            
            LOG.info("✅ Successfully parsed LLM response: component='{}', description='{}'", 
                     response.getName(), 
                     response.getDescription() != null ? response.getDescription().substring(0, Math.min(50, response.getDescription().length())) + "..." : "null");
            
            return response;
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            LOG.error("❌ JSON Parsing Failed - JsonProcessingException:");
            LOG.error("   Error: {}", e.getMessage());
            LOG.error("   Location: Line {}, Column {}", e.getLocation().getLineNr(), e.getLocation().getColumnNr());
            LOG.error("   Raw response (first 500 chars):\n{}", 
                     rawResponse.substring(0, Math.min(500, rawResponse.length())));
            
            // Try to show the problematic area
            String cleaned = extractJsonBlock(rawResponse);
            if (cleaned != null) {
                LOG.error("   Cleaned JSON (first 500 chars):\n{}", 
                         cleaned.substring(0, Math.min(500, cleaned.length())));
            }
            
            throw new RuntimeException("Failed to parse JSON from LLM response due to malformed formatting: " + e.getMessage(), e);
            
        } catch (Exception e) {
            LOG.error("❌ Unexpected error during JSON parsing: {}", e.getMessage(), e);
            LOG.error("   Raw response (first 300 chars): {}", 
                     rawResponse.substring(0, Math.min(300, rawResponse.length())));
            throw new RuntimeException("Unexpected error parsing Local LLM response", e);
        }
    }
    
    /**
     * Robust JSON extraction method that handles Markdown code fences and streaming artifacts
     */
    private String extractJsonBlock(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        
        LOG.debug("🔍 Raw response before sanitization (first 300 chars): {}", 
                 raw.substring(0, Math.min(300, raw.length())));
        
        // Only apply sanitization if stripMarkdown is enabled
        if (this.stripMarkdown) {
            // Step 1: Remove various Markdown code fence patterns
            raw = raw.replaceAll("(?s)```\\s*json\\s*\\n?", "")     // ```json or ```json\n
                     .replaceAll("(?s)```\\s*\\n?", "")             // ``` or ```\n
                     .replaceAll("(?s)```\\s*$", "")                // trailing ```
                     .trim();
            
            // Step 2: Remove any remaining backticks at start/end
            while (raw.startsWith("`")) {
                raw = raw.substring(1);
            }
            while (raw.endsWith("`")) {
                raw = raw.substring(0, raw.length() - 1);
            }
            
            // Step 3: Remove common streaming artifacts
            raw = raw.replaceAll("(?s)^[^{]*", "")  // Remove everything before first {
                     .replaceAll("(?s)[^}]*$", "")  // Remove everything after last }
                     .trim();
        }
        
        // Step 4: Extract JSON block from first '{' to last '}'
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        
        if (start != -1 && end != -1 && end > start) {
            String extracted = raw.substring(start, end + 1);
            
            // Step 5: Additional validation - ensure it looks like JSON
            if (extracted.trim().startsWith("{") && extracted.trim().endsWith("}")) {
                // Step 6: Try to validate JSON, if it fails, fix quotes
                String finalJson = extracted;
                if (!isValidJson(extracted)) {
                    LOG.debug("🔧 Invalid JSON detected, attempting quote fixing");
                    finalJson = escapeUnescapedQuotesInStringValues(extracted);
                }
                
                LOG.debug("✅ Successfully extracted and sanitized JSON block ({} chars)", finalJson.length());
                return finalJson;
            }
        }
        
        // Step 7: Last resort - try to find any JSON-like structure
        if (raw.contains("{") && raw.contains("}")) {
            LOG.warn("⚠️ Fallback JSON extraction - attempting quote fixes on raw content");
            String fallback = escapeUnescapedQuotesInStringValues(raw);
            return isValidJson(fallback) ? fallback : raw;
        }
        
        LOG.error("❌ No valid JSON structure found in response");
        return null;
    }
    
    /**
     * Escape unescaped quotes within JSON string values only
     * This handles cases like: "sample": "<p>This is a "bad" example</p>"
     */
    private String escapeUnescapedQuotesInStringValues(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }
        
        LOG.debug("🔧 Escaping unescaped quotes in JSON string values");
        LOG.debug("🔍 Before sanitization (first 300 chars): {}", 
                 json.substring(0, Math.min(300, json.length())));
        
        StringBuilder result = new StringBuilder();
        boolean inStringValue = false;
        boolean escaped = false;
        
        for (int i = 0; i < json.length(); i++) {
            char current = json.charAt(i);
            char prev = (i > 0) ? json.charAt(i - 1) : '\0';
            
            if (escaped) {
                // Previous character was backslash, so this character is escaped
                result.append(current);
                escaped = false;
                continue;
            }
            
            if (current == '\\') {
                // This is an escape character
                result.append(current);
                escaped = true;
                continue;
            }
            
            if (current == '"') {
                if (!inStringValue) {
                    // Check if this starts a string value (must be preceded by : and optional whitespace)
                    if (isStartOfStringValue(json, i)) {
                        inStringValue = true;
                        result.append(current);
                    } else {
                        // This is a key or structural quote
                        result.append(current);
                    }
                } else {
                    // We're inside a string value, check if this ends it
                    if (isEndOfStringValue(json, i)) {
                        inStringValue = false;
                        result.append(current);
                    } else {
                        // This is an unescaped quote inside a string value - escape it
                        result.append("\\\"");
                        LOG.debug("🔧 Escaped quote at position {}", i);
                    }
                }
            } else {
                result.append(current);
            }
        }
        
        String sanitized = result.toString();
        if (!sanitized.equals(json)) {
            LOG.debug("✅ Applied quote escaping to JSON string values");
            LOG.debug("🔍 After sanitization (first 300 chars): {}", 
                     sanitized.substring(0, Math.min(300, sanitized.length())));
        }
        
        return sanitized;
    }
    
    /**
     * Check if a quote at position i starts a string value (preceded by : and optional whitespace)
     */
    private boolean isStartOfStringValue(String json, int quotePos) {
        // Look backwards for ':' skipping whitespace
        for (int i = quotePos - 1; i >= 0; i--) {
            char c = json.charAt(i);
            if (c == ':') {
                return true;
            } else if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Check if a quote at position i ends a string value (followed by structural characters)
     */
    private boolean isEndOfStringValue(String json, int quotePos) {
        // Look forwards for structural characters (comma, closing brace/bracket) skipping whitespace
        for (int i = quotePos + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ',' || c == '}' || c == ']') {
                return true;
            } else if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        // If we reach end of string, this could be the final closing quote
        return quotePos == json.length() - 1 || 
               json.substring(quotePos + 1).trim().isEmpty() ||
               json.substring(quotePos + 1).trim().matches("^[}\\]]*$");
    }
    
    /**
     * Basic JSON validation check
     */
    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String generatePreviewHtml(LLMResponse llmResponse) {
        if (llmResponse.getHtml() == null) {
            return "<div>No HTML template available for preview</div>";
        }
        
        // Replace HTL variables with sample data
        String previewHtml = llmResponse.getHtml();
        
        if (llmResponse.getSampleData() != null) {
            for (Map.Entry<String, Object> entry : llmResponse.getSampleData().entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String replacement = entry.getValue() != null ? entry.getValue().toString() : "";
                previewHtml = previewHtml.replace(placeholder, replacement);
            }
        }
        
        // Also replace common HTL patterns
        previewHtml = previewHtml.replaceAll("\\$\\{properties\\.([^}]+)\\}?", "Sample $1");
        previewHtml = previewHtml.replaceAll("\\$\\{wcmmode\\.edit\\}", "");
        previewHtml = previewHtml.replaceAll("\\$\\{wcmmode\\.preview\\}", "");
        
        return previewHtml;
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
} 