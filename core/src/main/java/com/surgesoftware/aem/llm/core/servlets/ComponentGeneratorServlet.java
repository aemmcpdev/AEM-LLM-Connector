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

import com.surgesoftware.aem.llm.core.services.LocalLLMService;
import com.surgesoftware.aem.llm.core.services.FileManagementService;
import com.surgesoftware.aem.llm.core.models.ComponentGenerationRequest;
import com.surgesoftware.aem.llm.core.models.ComponentGenerationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.apache.sling.api.request.RequestParameter;
import org.apache.commons.io.IOUtils;

/**
 * Component Generator Servlet for SURGE AEM LLM Connector
 * 
 * This servlet handles HTTP requests to generate AEM component files
 * using Local LLM (Ollama/LocalAI) integration. It provides a REST endpoint
 * that AEM developers can use to generate component files.
 * 
 * URL Pattern: /bin/aem-llm/generate
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/aem-llm/generate",
        "sling.servlet.methods=POST,GET,OPTIONS",
        "sling.auth.requirements=-/bin/aem-llm/generate",
        "service.description=SURGE AEM LLM Connector - Component Generator Servlet",
        "service.vendor=SURGE Software Solutions Private Limited"
    })
public class ComponentGeneratorServlet extends SlingAllMethodsServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(ComponentGeneratorServlet.class);
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Reference
    private LocalLLMService localLLMService;
    
    @Reference
    private FileManagementService fileManagementService;
    
    @Activate
    protected void activate() {
        LOG.info("🚀 SURGE AEM LLM Connector: ComponentGeneratorServlet activated successfully");
        LOG.info("✅ Servlet registered at path: /bin/aem-llm/generate");
        LOG.info("✅ Supported HTTP methods: GET, POST, OPTIONS");
        LOG.info("✅ POST requests will be intercepted by this servlet (not Sling default POST servlet)");
        LOG.info("✅ Authentication bypassed for /bin/aem-llm/* paths");
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
        // Log POST request interception to ensure servlet is handling it correctly
        LOG.info("✅ POST request intercepted by ComponentGeneratorServlet at /bin/aem-llm/generate");
        LOG.debug("Request details - Method: {}, Path: {}, Query: {}", 
                 request.getMethod(), request.getPathInfo(), request.getQueryString());
        
        addCORSHeaders(response);
        processRequest(request, response);
    }
    
    @Override
    protected void doOptions(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        addCORSHeaders(response);
        response.setStatus(200);
    }
    
    /**
     * Handle unsupported HTTP methods with proper 405 Method Not Allowed response
     */
    @Override
    protected void service(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        String method = request.getMethod();
        String allowedMethods = "GET, POST, OPTIONS";
        
        // Log all requests for debugging
        LOG.debug("Servlet handling {} request to {}", method, request.getRequestURI());
        
        // Check if method is supported
        if ("GET".equals(method) || "POST".equals(method) || "OPTIONS".equals(method)) {
            // Call parent service method to handle supported methods
            super.service(request, response);
        } else {
            // Return 405 for unsupported methods
            LOG.warn("⚠️ Unsupported HTTP method {} attempted on /bin/aem-llm/generate. Allowed: {}", 
                     method, allowedMethods);
            response.setStatus(405); // Method Not Allowed
            response.setHeader("Allow", allowedMethods);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Method " + method + " not allowed\", " +
                                     "\"allowed_methods\": \"" + allowedMethods + "\", " +
                                     "\"status\": \"method_not_allowed\"}");
        }
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
            if (localLLMService == null || fileManagementService == null) {
                LOG.error("Required services are not available");
                response.setStatus(503);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Required services not available\", \"status\": \"service_unavailable\"}");
                return;
            }
            
            // Get request parameters - support both regular form and multipart
            String prompt = extractPrompt(request);
            if (prompt == null || prompt.isEmpty()) {
                response.setStatus(400);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Missing prompt parameter\", \"status\": \"bad_request\"}");
                return;
            }
            
            // NEW: Handle image upload if present
            String imageData = null;
            try {
                imageData = extractImageData(request);
                if (imageData != null) {
                    LOG.info("Image data extracted successfully, length: {} characters", imageData.length());
                }
            } catch (Exception e) {
                LOG.error("Error processing uploaded image: {}", e.getMessage(), e);
                response.setStatus(400);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Failed to process uploaded image: " + escapeJsonString(e.getMessage()) + "\", \"status\": \"image_processing_error\"}");
                return;
            }
            
            // Generate timestamp for this request
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            
            LOG.info("Processing prompt: '{}' with timestamp: {}, hasImage: {}", prompt, timestamp, imageData != null);
            
            // Create component generation request
            ComponentGenerationRequest generationRequest = new ComponentGenerationRequest(
                prompt, 
                "component", 
                extractRequirements(request)
            );
            
            // NEW: Add image data to request if available
            if (imageData != null) {
                generationRequest.setImageData(imageData);
            }
            
            // Generate component using Local LLM service
            ComponentGenerationResponse generationResponse = localLLMService.generateComponent(generationRequest);
            
            if (!generationResponse.isSuccess()) {
                LOG.error("Failed to generate component for prompt: {} - {}", prompt, generationResponse.getError());
                
                // Determine appropriate HTTP status based on error type
                int httpStatus = 500; // Default to server error
                String errorStatus = "generation_failed";
                
                String errorMessage = generationResponse.getError();
                if (errorMessage != null) {
                    if (errorMessage.contains("timeout") || errorMessage.contains("Timeout")) {
                        httpStatus = 504; // Gateway timeout
                        errorStatus = "llm_timeout";
                    } else if (errorMessage.contains("Cannot connect") || errorMessage.contains("Connection refused")) {
                        httpStatus = 503; // Service unavailable
                        errorStatus = "llm_unavailable";
                    } else if (errorMessage.contains("Model not available") || errorMessage.contains("not found")) {
                        httpStatus = 422; // Unprocessable entity
                        errorStatus = "model_unavailable";
                    }
                }
                
                response.setStatus(httpStatus);
                response.setContentType("application/json");
                
                // Enhanced error response with suggestions and model error details
                PrintWriter writer = response.getWriter();
                writer.write("{\n");
                writer.write("  \"error\": \"" + escapeJsonString(generationResponse.getError()) + "\",\n");
                writer.write("  \"status\": \"" + errorStatus + "\",\n");
                if (generationResponse.getModelError() != null) {
                    writer.write("  \"modelError\": \"" + escapeJsonString(generationResponse.getModelError()) + "\",\n");
                }
                writer.write("  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\n");
                writer.write("  \"suggestion\": \"" + getSuggestionForError(errorStatus) + "\"\n");
                writer.write("}");
                writer.flush();
                return;
            }
            
            LOG.info("Generated component: {} with {} files", 
                    generationResponse.getComponentName(), 
                    generationResponse.getGeneratedFiles().size());
            
            // Save component files to repository
            String savedPath = fileManagementService.saveComponentFiles(generationResponse.getGeneratedFiles(), timestamp);
            LOG.info("savedPath: {}", savedPath);
            
            // Create ZIP file for download
            String zipPath = fileManagementService.createZipFile(generationResponse.getGeneratedFiles(), timestamp);
            LOG.info("zipPath: {}", zipPath);
            
            // Save preview HTML if available
            String previewPath = null;
            if (generationResponse.getPreviewHtml() != null) {
                previewPath = fileManagementService.savePreviewFile(generationResponse.getPreviewHtml(), timestamp);
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
            writer.write("  \"message\": \"" + escapeJsonString(generationResponse.getMessage()) + "\",\n");
            writer.write("  \"timestamp\": \"" + formatTimestamp(timestamp) + "\",\n");
            writer.write("  \"prompt\": \"" + escapeJsonString(prompt) + "\",\n");
            writer.write("  \"componentName\": \"" + escapeJsonString(generationResponse.getComponentName()) + "\",\n");
            writer.write("  \"componentDescription\": \"" + escapeJsonString(generationResponse.getComponentDescription()) + "\",\n");
            writer.write("  \"filesGenerated\": " + generationResponse.getGeneratedFiles().size() + ",\n");
            writer.write("  \"downloadUrl\": \"" + (downloadUrl != null ? downloadUrl : "") + "\",\n");
            writer.write("  \"previewUrl\": \"" + (previewUrl != null ? previewUrl : "") + "\",\n");
            writer.write("  \"previewHtml\": \"" + escapeJsonString(generationResponse.getPreviewHtml()) + "\",\n");
            writer.write("  \"savedPath\": \"" + (savedPath != null ? savedPath : "") + "\",\n");
            writer.write("  \"generatedBy\": \"SURGE AEM LLM Connector (Local LLM)\"\n");
            writer.write("}");
            writer.flush();
            
            LOG.info("Successfully generated component for prompt: '{}', timestamp: {}", prompt, timestamp);
            
        } catch (Exception e) {
            LOG.error("Error in ComponentGeneratorServlet: {}", e.getMessage(), e);
            response.setStatus(500);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Internal server error: " + escapeJsonString(e.getMessage()) + "\", \"status\": \"server_error\"}");
        }
    }
    
    private String formatTimestamp(String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH));
        } catch (Exception e) {
            return timestamp;
        }
    }
    
    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    /**
     * NEW: Extract prompt parameter from both regular and multipart requests
     */
    private String extractPrompt(SlingHttpServletRequest request) {
        // Try regular parameter first
        String prompt = request.getParameter("prompt");
        if (prompt != null && !prompt.isEmpty()) {
            return prompt;
        }
        
        // Try multipart parameter
        RequestParameter promptParam = request.getRequestParameter("prompt");
        if (promptParam != null) {
            return promptParam.getString();
        }
        
        return null;
    }
    
    /**
     * NEW: Extract requirements parameter from both regular and multipart requests
     */
    private String extractRequirements(SlingHttpServletRequest request) {
        // Try regular parameter first
        String requirements = request.getParameter("requirements");
        if (requirements != null && !requirements.isEmpty()) {
            return requirements;
        }
        
        // Try multipart parameter
        RequestParameter requirementsParam = request.getRequestParameter("requirements");
        if (requirementsParam != null) {
            return requirementsParam.getString();
        }
        
        return null;
    }
    
    /**
     * NEW: Extract and encode image data from multipart request
     */
    private String extractImageData(SlingHttpServletRequest request) throws IOException {
        RequestParameter imageParam = request.getRequestParameter("image");
        if (imageParam == null || imageParam.getSize() == 0) {
            return null;
        }
        
        // Validate file type
        String contentType = imageParam.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Uploaded file is not an image. Content type: " + contentType);
        }
        
        // Validate file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (imageParam.getSize() > maxSize) {
            throw new IllegalArgumentException("Image file too large. Maximum size is 10MB, uploaded: " + 
                                               (imageParam.getSize() / 1024 / 1024) + "MB");
        }
        
        // Convert to base64
        try (InputStream inputStream = imageParam.getInputStream()) {
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            LOG.info("Image processed: {} bytes, content type: {}", imageBytes.length, contentType);
            
            // Return with data URL format for LLM processing
            return "data:" + contentType + ";base64," + base64Image;
        }
    }
    
    /**
     * Provide helpful suggestions based on error type
     */
    private String getSuggestionForError(String errorStatus) {
        switch (errorStatus) {
            case "llm_timeout":
                return "The model may be cold-starting. Try waiting 30-60 seconds and retry, or run 'ollama run " + 
                       (localLLMService != null ? "llama3.2" : "your-model") + "' manually to warm up the model.";
            case "llm_unavailable":
                return "Please ensure Ollama is running with 'ollama serve' and accessible at the configured URL.";
            case "model_unavailable":
                return "Install the model with 'ollama pull llama3.2' or configure a different model in OSGi settings.";
            default:
                return "Check the logs for more details and ensure your LLM service is properly configured.";
        }
    }
} 