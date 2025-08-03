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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * LLM Response Model
 * 
 * Represents the structured response from local LLM for AEM component generation
 * 
 * @author SURGE Software Solutions Private Limited
 */
public class LLMResponse {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("fields")
    private List<ComponentField> fields;
    
    @JsonProperty("html")
    private String html;
    
    @JsonProperty("dialog")
    private String dialog;
    
    @JsonProperty("js")
    private String js;
    
    @JsonProperty("java")
    private String java;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("sampleData")
    private Map<String, Object> sampleData;
    
    @JsonProperty("previewHtml")
    private String previewHtml;
    
    public LLMResponse() {
        this.sampleData = new HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<ComponentField> getFields() {
        return fields;
    }
    
    public void setFields(List<ComponentField> fields) {
        this.fields = fields;
    }
    
    public String getHtml() {
        return html;
    }
    
    public void setHtml(String html) {
        this.html = html;
    }
    
    public String getDialog() {
        return dialog;
    }
    
    public void setDialog(String dialog) {
        this.dialog = dialog;
    }
    
    public String getJs() {
        return js;
    }
    
    public void setJs(String js) {
        this.js = js;
    }
    
    public String getJava() {
        return java;
    }
    
    public void setJava(String java) {
        this.java = java;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Map<String, Object> getSampleData() {
        return sampleData;
    }
    
    public void setSampleData(Map<String, Object> sampleData) {
        this.sampleData = sampleData;
    }
    
    public String getPreviewHtml() {
        return previewHtml;
    }
    
    public void setPreviewHtml(String previewHtml) {
        this.previewHtml = previewHtml;
    }
    
    @Override
    public String toString() {
        return "LLMResponse{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", fields=" + fields +
                ", html=" + (html != null ? "available" : "null") +
                ", dialog=" + (dialog != null ? "available" : "null") +
                ", js=" + (js != null ? "available" : "null") +
                ", java=" + (java != null ? "available" : "null") +
                ", content=" + (content != null ? "available" : "null") +
                ", previewHtml=" + (previewHtml != null ? "available" : "null") +
                '}';
    }
} 