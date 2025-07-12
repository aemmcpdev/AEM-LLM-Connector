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

import com.surgesoftware.aem.llm.core.services.OpenAIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenAI Service Implementation for SURGE AEM LLM Connector
 * 
 * Implementation of OpenAI service for generating AEM component files
 * using ChatGPT-4 API integration.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(
    service = OpenAIService.class,
    configurationPolicy = ConfigurationPolicy.OPTIONAL,
    immediate = true
)
public class OpenAIServiceImpl implements OpenAIService {
    
    private static final Logger LOG = LoggerFactory.getLogger(OpenAIServiceImpl.class);
    
    // TODO: Move to OSGi configuration
    private static final String OPENAI_API_KEY = "your-openai-api-key-here";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Map<String, String> generateComponentFiles(String componentType, String requirements) {
        LOG.info("Generating {} component files for SURGE AEM LLM Connector", componentType);
        
        Map<String, String> files = new HashMap<>();
        
        try {
            // Generate different types of files for the component
            files.put("dialog.xml", generateComponentFile(componentType, "dialog", requirements));
            files.put(componentType + ".html", generateComponentFile(componentType, "html", requirements));
            files.put(componentType + ".js", generateComponentFile(componentType, "js", requirements));
            files.put(".content.xml", generateComponentFile(componentType, "content", requirements));
            
            LOG.info("Successfully generated {} files for {} component", files.size(), componentType);
            return files;
            
        } catch (Exception e) {
            LOG.error("Error generating component files for {}: {}", componentType, e.getMessage(), e);
            return getDefaultComponentFiles(componentType);
        }
    }
    
    @Override
    public String generateComponentFile(String componentType, String fileType, String requirements) {
        LOG.debug("Generating {} file for {} component", fileType, componentType);
        
        try {
            String prompt = buildPrompt(componentType, fileType, requirements);
            String response = callOpenAI(prompt);
            
            if (response != null && !response.isEmpty()) {
                LOG.debug("Successfully generated {} file for {} component", fileType, componentType);
                return response;
            } else {
                LOG.warn("Empty response from OpenAI for {} {} file", componentType, fileType);
                return getDefaultFileContent(componentType, fileType);
            }
            
        } catch (Exception e) {
            LOG.error("Error generating {} file for {} component: {}", fileType, componentType, e.getMessage(), e);
            return getDefaultFileContent(componentType, fileType);
        }
    }
    
    @Override
    public boolean testConnection() {
        LOG.info("Testing OpenAI API connection for SURGE AEM LLM Connector");
        
        try {
            String testPrompt = "Generate a simple test response for AEM component generation.";
            String response = callOpenAI(testPrompt);
            boolean isConnected = response != null && !response.isEmpty();
            
            LOG.info("OpenAI API connection test result: {}", isConnected ? "SUCCESS" : "FAILED");
            return isConnected;
            
        } catch (Exception e) {
            LOG.error("OpenAI API connection test failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private String callOpenAI(String prompt) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(OPENAI_API_URL);
        
        // Set headers
        request.setHeader("Authorization", "Bearer " + OPENAI_API_KEY);
        request.setHeader("Content-Type", "application/json");
        
        // Build request body
        String requestBody = String.format(
            "{\n" +
            "    \"model\": \"gpt-4\",\n" +
            "    \"messages\": [\n" +
            "        {\n" +
            "            \"role\": \"system\",\n" +
            "            \"content\": \"You are an expert AEM developer working for SURGE Software Solutions. Generate clean, production-ready AEM component files following Adobe best practices.\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"role\": \"user\",\n" +
            "            \"content\": \"%s\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"max_tokens\": 2000,\n" +
            "    \"temperature\": 0.7\n" +
            "}", prompt.replace("\"", "\\\""));
        
        request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
        
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        
        if (entity != null) {
            String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            if (jsonNode.has("choices") && jsonNode.get("choices").size() > 0) {
                JsonNode choice = jsonNode.get("choices").get(0);
                if (choice.has("message") && choice.get("message").has("content")) {
                    return choice.get("message").get("content").asText();
                }
            }
        }
        
        return null;
    }
    
    private String buildPrompt(String componentType, String fileType, String requirements) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Generate an AEM ").append(componentType).append(" component ");
        prompt.append(fileType).append(" file for SURGE Software Solutions. ");
        
        switch (fileType) {
            case "dialog":
                prompt.append("Create a dialog.xml file with appropriate fields for a ")
                      .append(componentType).append(" component. Include title, description, and any relevant fields.");
                break;
            case "html":
                prompt.append("Create an HTML template file (").append(componentType).append(".html) ")
                      .append("using HTL (Sightly) syntax with proper structure and styling.");
                break;
            case "js":
                prompt.append("Create a JavaScript file for the ").append(componentType)
                      .append(" component with proper use() function and component logic.");
                break;
            case "content":
                prompt.append("Create a .content.xml file with proper jcr:primaryType and component definitions.");
                break;
        }
        
        if (requirements != null && !requirements.isEmpty()) {
            prompt.append(" Additional requirements: ").append(requirements);
        }
        
        prompt.append(" Make sure the code follows AEM best practices and is production-ready.");
        
        return prompt.toString();
    }
    
    private Map<String, String> getDefaultComponentFiles(String componentType) {
        Map<String, String> files = new HashMap<>();
        
        files.put("dialog.xml", getDefaultFileContent(componentType, "dialog"));
        files.put(componentType + ".html", getDefaultFileContent(componentType, "html"));
        files.put(componentType + ".js", getDefaultFileContent(componentType, "js"));
        files.put(".content.xml", getDefaultFileContent(componentType, "content"));
        
        return files;
    }
    
    private String getDefaultFileContent(String componentType, String fileType) {
        switch (fileType) {
            case "dialog":
                return generateDefaultDialog(componentType);
            case "html":
                return generateDefaultHtml(componentType);
            case "js":
                return generateDefaultJs(componentType);
            case "content":
                return generateDefaultContent(componentType);
            default:
                return "<!-- Generated by SURGE AEM LLM Connector -->\n<!-- Default " + fileType + " file for " + componentType + " component -->";
        }
    }
    
    private String generateDefaultDialog(String componentType) {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- Generated by SURGE AEM LLM Connector -->\n" +
            "<jcr:root xmlns:sling=\"http://sling.apache.org/jcr/sling/1.0\" xmlns:granite=\"http://www.adobe.com/jcr/granite/1.0\" xmlns:cq=\"http://www.day.com/jcr/cq/1.0\" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"\n" +
            "    jcr:primaryType=\"nt:unstructured\"\n" +
            "    jcr:title=\"%s Component\"\n" +
            "    sling:resourceType=\"cq/gui/components/authoring/dialog\"\n" +
            "    extraClientlibs=\"[core.wcm.components.%s.v1.editor]\">\n" +
            "    <content\n" +
            "        jcr:primaryType=\"nt:unstructured\"\n" +
            "        sling:resourceType=\"granite/ui/components/coral/foundation/container\">\n" +
            "        <items jcr:primaryType=\"nt:unstructured\">\n" +
            "            <tabs\n" +
            "                jcr:primaryType=\"nt:unstructured\"\n" +
            "                sling:resourceType=\"granite/ui/components/coral/foundation/tabs\"\n" +
            "                maximized=\"{Boolean}true\">\n" +
            "                <items jcr:primaryType=\"nt:unstructured\">\n" +
            "                    <text\n" +
            "                        jcr:primaryType=\"nt:unstructured\"\n" +
            "                        jcr:title=\"Text\"\n" +
            "                        sling:resourceType=\"granite/ui/components/coral/foundation/fixedcolumns\"\n" +
            "                        margin=\"{Boolean}true\">\n" +
            "                        <items jcr:primaryType=\"nt:unstructured\">\n" +
            "                            <column\n" +
            "                                jcr:primaryType=\"nt:unstructured\"\n" +
            "                                sling:resourceType=\"granite/ui/components/coral/foundation/container\">\n" +
            "                                <items jcr:primaryType=\"nt:unstructured\">\n" +
            "                                    <text\n" +
            "                                        jcr:primaryType=\"nt:unstructured\"\n" +
            "                                        sling:resourceType=\"granite/ui/components/coral/foundation/form/textfield\"\n" +
            "                                        fieldLabel=\"Text\"\n" +
            "                                        name=\"./text\"/>\n" +
            "                                </items>\n" +
            "                            </column>\n" +
            "                        </items>\n" +
            "                    </text>\n" +
            "                </items>\n" +
            "            </tabs>\n" +
            "        </items>\n" +
            "    </content>\n" +
            "</jcr:root>",
            componentType.substring(0, 1).toUpperCase() + componentType.substring(1), componentType);
    }
    
    private String generateDefaultHtml(String componentType) {
        return String.format(
            "<!--/* Generated by SURGE AEM LLM Connector */-->\n" +
            "<!--/* %s Component HTML Template */-->\n" +
            "<div data-sly-use.component=\"com.surgesoftware.aem.llm.core.models.%sModel\"\n" +
            "     class=\"cmp-%s\">\n" +
            "    <div class=\"cmp-%s__content\">\n" +
            "        <p class=\"cmp-%s__text\">${component.text @ context='html'}</p>\n" +
            "    </div>\n" +
            "</div>",
            componentType.substring(0, 1).toUpperCase() + componentType.substring(1),
            componentType.substring(0, 1).toUpperCase() + componentType.substring(1),
            componentType, componentType, componentType
        );
    }
    
    private String generateDefaultJs(String componentType) {
        return String.format(
            "/**\n" +
            " * Generated by SURGE AEM LLM Connector\n" +
            " * %s Component JavaScript\n" +
            " */\n" +
            "use(function () {\n" +
            "    'use strict';\n" +
            "    \n" +
            "    return {\n" +
            "        text: this.text || \"Default %s text\",\n" +
            "        \n" +
            "        getFormattedText: function() {\n" +
            "            return this.text ? this.text.trim() : \"\";\n" +
            "        },\n" +
            "        \n" +
            "        isEmpty: function() {\n" +
            "            return !this.text || this.text.trim().length === 0;\n" +
            "        }\n" +
            "    };\n" +
            "});",
            componentType.substring(0, 1).toUpperCase() + componentType.substring(1),
            componentType
        );
    }
    
    private String generateDefaultContent(String componentType) {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- Generated by SURGE AEM LLM Connector -->\n" +
            "<jcr:root xmlns:sling=\"http://sling.apache.org/jcr/sling/1.0\" xmlns:cq=\"http://www.day.com/jcr/cq/1.0\" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"\n" +
            "    jcr:primaryType=\"cq:Component\"\n" +
            "    jcr:title=\"%s Component\"\n" +
            "    jcr:description=\"A %s component generated by SURGE AEM LLM Connector\"\n" +
            "    componentGroup=\"SURGE Components\"/>",
            componentType.substring(0, 1).toUpperCase() + componentType.substring(1),
            componentType
        );
    }
} 