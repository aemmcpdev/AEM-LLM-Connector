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
 * Component Field Model
 * 
 * Represents a single field in an AEM component dialog
 * 
 * @author SURGE Software Solutions Private Limited
 */
public class ComponentField {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("label")
    private String label;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("required")
    private boolean required;
    
    @JsonProperty("defaultValue")
    private String defaultValue;
    
    @JsonProperty("sample")
    private String sample;
    
    @JsonProperty("options")
    private String[] options;
    
    public ComponentField() {
        // Default constructor for Jackson
    }
    
    public ComponentField(String name, String type, String label) {
        this.name = name;
        this.type = type;
        this.label = label;
        this.required = false;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getSample() {
        return sample;
    }
    
    public void setSample(String sample) {
        this.sample = sample;
    }
    
    public String[] getOptions() {
        return options;
    }
    
    public void setOptions(String[] options) {
        this.options = options;
    }
    
    @Override
    public String toString() {
        return "ComponentField{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", label='" + label + '\'' +
                ", required=" + required +
                ", sample='" + sample + '\'' +
                '}';
    }
} 