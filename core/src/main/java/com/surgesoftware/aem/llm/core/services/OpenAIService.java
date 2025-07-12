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

import java.util.Map;

/**
 * OpenAI Service for SURGE AEM LLM Connector
 * 
 * Service interface for interacting with OpenAI ChatGPT-4 API to generate
 * AEM component files based on user requirements.
 * 
 * @author SURGE Software Solutions Private Limited
 */
public interface OpenAIService {
    
    /**
     * Generate AEM component files for a given component type
     * 
     * @param componentType The type of component to generate (e.g., "text", "title", "header")
     * @param requirements Additional requirements or specifications
     * @return Map containing file names as keys and file content as values
     */
    Map<String, String> generateComponentFiles(String componentType, String requirements);
    
    /**
     * Generate a specific AEM component file
     * 
     * @param componentType The type of component
     * @param fileType The type of file to generate (e.g., "dialog", "html", "js")
     * @param requirements Additional requirements
     * @return The generated file content
     */
    String generateComponentFile(String componentType, String fileType, String requirements);
    
    /**
     * Test the OpenAI API connection
     * 
     * @return true if connection is successful, false otherwise
     */
    boolean testConnection();
} 