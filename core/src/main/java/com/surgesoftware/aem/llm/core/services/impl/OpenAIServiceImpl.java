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
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.JSONArray;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    private static final String OPENAI_API_KEY = "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";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    // Using Sling JSON instead of Jackson
    
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
        try {
            URL url = new URL(OPENAI_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set request method and headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Build request body using Sling JSON
            JSONObject requestJson = new JSONObject();
            requestJson.put("model", "gpt-4");
            requestJson.put("max_tokens", 2000);
            requestJson.put("temperature", 0.7);
            
            JSONArray messages = new JSONArray();
            
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are an expert AEM developer working for SURGE Software Solutions. Generate clean, production-ready AEM component files following Adobe best practices.");
            messages.put(systemMessage);
            
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);
            
            requestJson.put("messages", messages);
            
            // Send request
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            
            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parse response using Sling JSON
                JSONObject responseJson = new JSONObject(response.toString());
                if (responseJson.has("choices")) {
                    JSONArray choices = responseJson.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        if (choice.has("message")) {
                            JSONObject message = choice.getJSONObject("message");
                            if (message.has("content")) {
                                return message.getString("content");
                            }
                        }
                    }
                }
            }
            
            connection.disconnect();
        } catch (Exception e) {
            LOG.error("Error calling OpenAI API: {}", e.getMessage(), e);
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