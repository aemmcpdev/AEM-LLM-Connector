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
package com.surgesoftware.aem.llm.core.exceptions;

import java.io.IOException;

/**
 * Custom exception for LLM unavailability scenarios
 * Provides user-friendly error messages for timeout, cold start, and connectivity issues
 * 
 * @author SURGE Software Solutions Private Limited
 */
public class LLMUnavailableException extends IOException {
    
    private final String userFriendlyMessage;
    private final String technicalDetails;
    private final String suggestion;
    
    public LLMUnavailableException(String message) {
        super(message);
        this.userFriendlyMessage = message;
        this.technicalDetails = null;
        this.suggestion = null;
    }
    
    public LLMUnavailableException(String userFriendlyMessage, String technicalDetails, String suggestion) {
        super(userFriendlyMessage + " " + technicalDetails);
        this.userFriendlyMessage = userFriendlyMessage;
        this.technicalDetails = technicalDetails;
        this.suggestion = suggestion;
    }
    
    public LLMUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.userFriendlyMessage = message;
        this.technicalDetails = cause != null ? cause.getMessage() : null;
        this.suggestion = null;
    }
    
    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }
    
    public String getTechnicalDetails() {
        return technicalDetails;
    }
    
    public String getSuggestion() {
        return suggestion;
    }
    
    /**
     * Create a timeout-specific exception with appropriate suggestions
     */
    public static LLMUnavailableException forTimeout(String model, int timeoutSeconds) {
        return new LLMUnavailableException(
            "LLM request timed out",
            "Model '" + model + "' did not respond within " + timeoutSeconds + " seconds",
            "Try starting model manually using 'ollama run " + model + "' or increase timeout in config"
        );
    }
    
    /**
     * Create a cold start exception with warm-up suggestions
     */
    public static LLMUnavailableException forColdStart(String model) {
        return new LLMUnavailableException(
            "Model is starting up (cold start)",
            "Model '" + model + "' needs to be loaded into memory",
            "The system will attempt to warm up the model automatically. This may take 30-60 seconds on first use"
        );
    }
    
    /**
     * Create a connectivity exception
     */
    public static LLMUnavailableException forConnectivity(String apiUrl) {
        return new LLMUnavailableException(
            "Cannot connect to LLM service",
            "Failed to connect to " + apiUrl,
            "Please ensure Ollama is running. Try: 'ollama serve' or check if the service is accessible"
        );
    }
}