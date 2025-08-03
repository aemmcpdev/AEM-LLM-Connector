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
package com.surgesoftware.aem.llm.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Component Generation Request Model
 * 
 * Represents a request to generate AEM component files using local LLM
 * 
 * @author SURGE Software Solutions Private Limited
 */
public class ComponentGenerationRequest {
    
    @JsonProperty("prompt")
    private String prompt;
    
    @JsonProperty("componentType")
    private String componentType;
    
    @JsonProperty("requirements")
    private String requirements;
    
    @JsonProperty("format")
    private String format;
    
    // NEW: Image data support for visual prompts
    @JsonProperty("imageData")
    private String imageData;
    
    public ComponentGenerationRequest() {
        // Default constructor for Jackson
    }
    
    public ComponentGenerationRequest(String prompt, String componentType, String requirements) {
        this.prompt = prompt;
        this.componentType = componentType != null ? componentType : "component";
        this.requirements = requirements;
        this.format = "json";
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public String getComponentType() {
        return componentType;
    }
    
    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }
    
    public String getRequirements() {
        return requirements;
    }
    
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    // NEW: Image data getter and setter
    public String getImageData() {
        return imageData;
    }
    
    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
    
    public boolean hasImage() {
        return imageData != null && !imageData.isEmpty();
    }
    
    @Override
    public String toString() {
        return "ComponentGenerationRequest{" +
                "prompt='" + prompt + '\'' +
                ", componentType='" + componentType + '\'' +
                ", requirements='" + requirements + '\'' +
                ", format='" + format + '\'' +
                ", hasImage=" + hasImage() +
                '}';
    }
} 