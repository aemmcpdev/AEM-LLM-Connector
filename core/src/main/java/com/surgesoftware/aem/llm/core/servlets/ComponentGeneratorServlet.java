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
package com.surgesoftware.aem.llm.core.servlets;

import com.surgesoftware.aem.llm.core.services.OpenAIService;
import com.surgesoftware.aem.llm.core.services.FileManagementService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;

/**
 * Component Generator Servlet for SURGE AEM LLM Connector
 * 
 * This servlet handles HTTP requests to generate AEM component files
 * using OpenAI ChatGPT-4 integration. It provides a REST endpoint
 * that AEM developers can use to generate component files.
 * 
 * URL Pattern: /bin/surge/llm/generate
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/aem-llm/generate",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.methods=" + HttpConstants.METHOD_POST,
        "sling.servlet.methods=" + HttpConstants.METHOD_OPTIONS,
        "sling.auth.requirements=-/bin/aem-llm/generate",
        "sling.auth.requirements=-/bin/aem-llm/*",
        "service.description=SURGE AEM LLM Connector - Component Generator Servlet",
        "service.vendor=SURGE Software Solutions Private Limited"
    })
public class ComponentGeneratorServlet extends SlingAllMethodsServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(ComponentGeneratorServlet.class);
    private static final long serialVersionUID = 1L;
    
    @Reference
    private OpenAIService openAIService;
    
    @Reference
    private FileManagementService fileManagementService;
    
    @Activate
    protected void activate() {
        LOG.info("SURGE AEM LLM Connector: ComponentGeneratorServlet activated successfully");
        LOG.info("Servlet registered at path: /bin/aem-llm/generate");
    }
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        addCORSHeaders(response);
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        addCORSHeaders(response);
        processRequest(request, response);
    }
    
    @Override
    protected void doOptions(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        addCORSHeaders(response);
        response.setStatus(200);
    }
    
    private void addCORSHeaders(SlingHttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, CSRF-Token, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
    
    private void processRequest(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        
        LOG.info("SURGE AEM LLM Connector: Processing component generation request");
        
        // Simple test response first
        if ("test".equals(request.getParameter("mode"))) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\": \"success\", \"message\": \"SURGE AEM LLM Connector is working!\", \"timestamp\": " + System.currentTimeMillis() + "}");
            return;
        }
        
        try {
            // Check if services are available
            if (openAIService == null || fileManagementService == null) {
                LOG.error("Required services are not available");
                response.setStatus(503);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Required services not available\", \"status\": \"service_unavailable\"}");
                return;
            }
            
            // Get request parameters
            String prompt = request.getParameter("prompt");
            if (prompt == null || prompt.isEmpty()) {
                response.setStatus(400);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Missing prompt parameter\", \"status\": \"bad_request\"}");
                return;
            }
            
            // Generate timestamp for this request
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            
            LOG.info("Processing prompt: '{}' with timestamp: {}", prompt, timestamp);
            
            // Generate component files using OpenAI service
            Map<String, String> componentFiles = openAIService.generateComponentFiles("component", prompt);
            
            if (componentFiles == null || componentFiles.isEmpty()) {
                LOG.error("Failed to generate component files for prompt: {}", prompt);
                response.setStatus(500);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Failed to generate component files\", \"status\": \"generation_failed\"}");
                return;
            }
            
            LOG.info("Generated {} component files: {}", componentFiles.size(), componentFiles.keySet());
            
            // Save component files to repository
            String savedPath = fileManagementService.saveComponentFiles(componentFiles, timestamp);
            LOG.info("savedPath: {}", savedPath);
            
            // Create ZIP file for download
            String zipPath = fileManagementService.createZipFile(componentFiles, timestamp);
            LOG.info("zipPath: {}", zipPath);
            
            // Extract HTML content for preview (look for .html files)
            String htmlContent = extractHtmlContent(componentFiles);
            LOG.info("extractedHtmlContent: {}", htmlContent != null ? "Found HTML content (" + htmlContent.length() + " chars)" : "No HTML content");
            String previewPath = null;
            if (htmlContent != null) {
                previewPath = fileManagementService.savePreviewFile(htmlContent, timestamp);
                LOG.info("previewPath: {}", previewPath);
            }
            
            // Generate URLs
            String downloadUrl = fileManagementService.getDownloadUrl(zipPath);
            String previewUrl = fileManagementService.getPreviewUrl(previewPath);
            LOG.info("downloadUrl: {}, previewUrl: {}", downloadUrl, previewUrl);
            
            // Return JSON response with URLs
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            PrintWriter writer = response.getWriter();
            writer.write("{\n");
            writer.write("  \"status\": \"success\",\n");
            writer.write("  \"message\": \"Component files generated successfully\",\n");
            writer.write("  \"timestamp\": \"" + formatTimestamp(timestamp) + "\",\n");
            writer.write("  \"prompt\": \"" + escapeJsonString(prompt) + "\",\n");
            writer.write("  \"filesGenerated\": " + componentFiles.size() + ",\n");
            writer.write("  \"downloadUrl\": \"" + (downloadUrl != null ? downloadUrl : "") + "\",\n");
            writer.write("  \"previewUrl\": \"" + (previewUrl != null ? previewUrl : "") + "\",\n");
            writer.write("  \"savedPath\": \"" + (savedPath != null ? savedPath : "") + "\",\n");
            writer.write("  \"generatedBy\": \"SURGE AEM LLM Connector\"\n");
            writer.write("}");
            writer.flush();
            
            LOG.info("Successfully generated component files for prompt: '{}', timestamp: {}", prompt, timestamp);
            
        } catch (Exception e) {
            LOG.error("Error in ComponentGeneratorServlet: {}", e.getMessage(), e);
            response.setStatus(500);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Internal server error: " + e.getMessage() + "\", \"status\": \"server_error\"}");
        }
    }
    
    private String extractHtmlContent(Map<String, String> componentFiles) {
        // Look for HTML files in the generated components
        for (Map.Entry<String, String> entry : componentFiles.entrySet()) {
            String fileName = entry.getKey();
            if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                return entry.getValue();
            }
        }
        
        // If no HTML file found, create a simple preview from the first file
        if (!componentFiles.isEmpty()) {
            Map.Entry<String, String> firstEntry = componentFiles.entrySet().iterator().next();
            return "<h2>File: " + firstEntry.getKey() + "</h2>\n" +
                   "<pre><code>" + escapeHtml(firstEntry.getValue()) + "</code></pre>";
        }
        
        return null;
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    private String formatTimestamp(String timestamp) {
        try {
            LocalDateTime dt = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.MEDIUM).withLocale(Locale.ENGLISH);
            return dt.format(formatter);
        } catch (Exception e) {
            return timestamp;
        }
    }
    

    

    

    
    private String escapeJsonString(String str) {
        if (str == null) {
            return "null";
        }
        
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
} 