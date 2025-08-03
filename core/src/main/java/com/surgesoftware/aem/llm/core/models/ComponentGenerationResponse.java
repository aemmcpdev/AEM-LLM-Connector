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
import java.util.Map;
import java.util.HashMap;

/**
 * Component Generation Response Model
 * 
 * Represents the response from local LLM with generated AEM component files
 * 
 * @author SURGE Software Solutions Private Limited
 */
public class ComponentGenerationResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("componentName")
    private String componentName;
    
    @JsonProperty("componentDescription")
    private String componentDescription;
    
    @JsonProperty("generatedFiles")
    private Map<String, String> generatedFiles;
    
    @JsonProperty("previewHtml")
    private String previewHtml;
    
    @JsonProperty("sampleData")
    private Map<String, Object> sampleData;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("modelError")
    private String modelError;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    public ComponentGenerationResponse() {
        this.generatedFiles = new HashMap<>();
        this.sampleData = new HashMap<>();
    }
    
    public ComponentGenerationResponse(String status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getComponentName() {
        return componentName;
    }
    
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    public String getComponentDescription() {
        return componentDescription;
    }
    
    public void setComponentDescription(String componentDescription) {
        this.componentDescription = componentDescription;
    }
    
    public Map<String, String> getGeneratedFiles() {
        return generatedFiles;
    }
    
    public void setGeneratedFiles(Map<String, String> generatedFiles) {
        this.generatedFiles = generatedFiles;
    }
    
    public void addGeneratedFile(String fileName, String content) {
        this.generatedFiles.put(fileName, content);
    }
    
    public String getPreviewHtml() {
        return previewHtml;
    }
    
    public void setPreviewHtml(String previewHtml) {
        this.previewHtml = previewHtml;
    }
    
    public Map<String, Object> getSampleData() {
        return sampleData;
    }
    
    public void setSampleData(Map<String, Object> sampleData) {
        this.sampleData = sampleData;
    }
    
    public void addSampleData(String key, Object value) {
        this.sampleData.put(key, value);
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getModelError() {
        return modelError;
    }
    
    public void setModelError(String modelError) {
        this.modelError = modelError;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isSuccess() {
        return "success".equals(status);
    }
    
    @Override
    public String toString() {
        return "ComponentGenerationResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", componentName='" + componentName + '\'' +
                ", generatedFiles=" + generatedFiles.keySet() +
                ", previewHtml=" + (previewHtml != null ? "available" : "null") +
                ", error='" + error + '\'' +
                '}';
    }
} 