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
package com.surgesoftware.aem.llm.core.services;

import com.surgesoftware.aem.llm.core.models.ComponentGenerationRequest;
import com.surgesoftware.aem.llm.core.models.ComponentGenerationResponse;

/**
 * Local LLM Service for SURGE AEM LLM Connector
 * 
 * Service interface for interacting with local LLM (Ollama/LocalAI) to generate
 * AEM component files based on user requirements.
 * 
 * @author SURGE Software Solutions Private Limited
 */
public interface LocalLLMService {
    
    /**
     * Generate AEM component files using local LLM
     * 
     * @param request The component generation request containing prompt and requirements
     * @return ComponentGenerationResponse with generated files and metadata
     */
    ComponentGenerationResponse generateComponent(ComponentGenerationRequest request);
    
    /**
     * Test the local LLM connection
     * 
     * @return true if connection is successful, false otherwise
     */
    boolean testConnection();
    
    /**
     * Get the current LLM provider and model information
     * 
     * @return String containing provider and model details
     */
    String getLLMInfo();
} 