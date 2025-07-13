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
 * OSGi Configuration for SURGE AEM LLM Connector - OpenAI Service
 * 
 * This configuration interface defines the configurable properties
 * for the OpenAI service integration.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@ObjectClassDefinition(
    name = "SURGE AEM LLM Connector - OpenAI Configuration",
    description = "Configuration for OpenAI service integration"
)
public @interface OpenAIConfiguration {
    
    @AttributeDefinition(
        name = "OpenAI API Key",
        description = "Your OpenAI API key for ChatGPT-4 integration",
        type = AttributeType.PASSWORD
    )
    String apiKey() default "";
    
    @AttributeDefinition(
        name = "OpenAI API URL",
        description = "OpenAI API endpoint URL",
        type = AttributeType.STRING
    )
    String apiUrl() default "https://api.openai.com/v1/chat/completions";
    
    @AttributeDefinition(
        name = "OpenAI Model",
        description = "OpenAI model to use for component generation",
        type = AttributeType.STRING
    )
    String model() default "gpt-4";
    
    @AttributeDefinition(
        name = "Max Tokens",
        description = "Maximum tokens per request",
        type = AttributeType.INTEGER
    )
    int maxTokens() default 2000;
    
    @AttributeDefinition(
        name = "Temperature",
        description = "Creativity temperature (0.0 to 1.0)",
        type = AttributeType.DOUBLE
    )
    double temperature() default 0.7;
    
    @AttributeDefinition(
        name = "Service Enabled",
        description = "Enable/disable the OpenAI service",
        type = AttributeType.BOOLEAN
    )
    boolean enabled() default true;
} 