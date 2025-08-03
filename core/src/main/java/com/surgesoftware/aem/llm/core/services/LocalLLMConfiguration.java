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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi Configuration for SURGE AEM LLM Connector - Local LLM Service
 * 
 * This configuration interface defines the configurable properties
 * for the local LLM service integration (Ollama/LocalAI).
 * 
 * @author SURGE Software Solutions Private Limited
 */
@ObjectClassDefinition(
    name = "SURGE AEM LLM Connector - Local LLM Configuration",
    description = "Configuration for Local LLM service integration (Ollama/LocalAI)"
)
public @interface LocalLLMConfiguration {
    
    @AttributeDefinition(
        name = "LLM Provider",
        description = "Local LLM provider type (ollama, localai, huggingface)",
        type = AttributeType.STRING
    )
    String provider() default "ollama";
    
    @AttributeDefinition(
        name = "LLM API URL",
        description = "Local LLM API endpoint (Ollama: http://localhost:11434/api/generate, LocalAI: http://localhost:8080/v1/chat/completions)",
        type = AttributeType.STRING
    )
    String apiUrl() default "http://localhost:11434/api/generate";
    
    @AttributeDefinition(
        name = "LLM Model",
        description = "Model name (Ollama: llama3.1, codellama, mistral, etc.)",
        type = AttributeType.STRING
    )
    String model() default "llama3.1";
    
    @AttributeDefinition(
        name = "Max Tokens",
        description = "Maximum tokens per request",
        type = AttributeType.INTEGER
    )
    int maxTokens() default 4000;
    
    @AttributeDefinition(
        name = "Temperature",
        description = "Creativity temperature (0.0 to 1.0)",
        type = AttributeType.DOUBLE
    )
    double temperature() default 0.7;
    
    @AttributeDefinition(
        name = "Service Enabled",
        description = "Enable/disable the Local LLM service",
        type = AttributeType.BOOLEAN
    )
    boolean enabled() default true;
    
    @AttributeDefinition(
        name = "Timeout (seconds)",
        description = "Request timeout in seconds",
        type = AttributeType.INTEGER
    )
    int timeout() default 60;
    
    @AttributeDefinition(
        name = "Retry Attempts",
        description = "Number of retry attempts for failed requests",
        type = AttributeType.INTEGER
    )
    int retryAttempts() default 3;
    
    @AttributeDefinition(
        name = "System Prompt",
        description = "System prompt for LLM to understand AEM component generation",
        type = AttributeType.STRING
    )
    String systemPrompt() default "You are an expert AEM developer working for SURGE Software Solutions. Generate clean, production-ready AEM component files following Adobe best practices. Always respond with valid JSON containing component structure, fields, HTML, dialog XML, JavaScript, Java model, and sample data.";
    
    @AttributeDefinition(
        name = "Strip Markdown",
        description = "Enable automatic removal of Markdown code fences from LLM responses (recommended: true)",
        type = AttributeType.BOOLEAN
    )
    boolean stripMarkdown() default true;
} 