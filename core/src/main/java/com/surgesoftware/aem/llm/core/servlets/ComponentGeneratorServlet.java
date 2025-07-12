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
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
@Component(
    service = Servlet.class,
    immediate = true,
    property = {
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.paths=/bin/surge/llm/generate"
    }
)
public class ComponentGeneratorServlet extends SlingSafeMethodsServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(ComponentGeneratorServlet.class);
    private static final long serialVersionUID = 1L;
    
    @Reference
    private OpenAIService openAIService;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        
        LOG.info("SURGE AEM LLM Connector: Processing component generation request");
        
        try {
            // Get request parameters
            String componentType = request.getParameter("type");
            String requirements = request.getParameter("requirements");
            String format = request.getParameter("format");
            
            // Validate component type
            if (componentType == null || componentType.isEmpty()) {
                componentType = "text"; // Default to text component
            }
            
            // Validate format
            if (format == null || format.isEmpty()) {
                format = "zip"; // Default to zip format
            }
            
            LOG.info("Generating {} component with format: {}", componentType, format);
            
            // Generate component files using OpenAI service
            Map<String, String> componentFiles = openAIService.generateComponentFiles(componentType, requirements);
            
            if (componentFiles == null || componentFiles.isEmpty()) {
                LOG.error("Failed to generate component files for type: {}", componentType);
                response.setStatus(500);
                response.getWriter().write("{\"error\": \"Failed to generate component files\"}");
                return;
            }
            
            // Return files based on requested format
            if ("json".equalsIgnoreCase(format)) {
                sendJsonResponse(response, componentFiles, componentType);
            } else {
                sendZipResponse(response, componentFiles, componentType);
            }
            
            LOG.info("Successfully generated and sent {} component files", componentType);
            
        } catch (Exception e) {
            LOG.error("Error in ComponentGeneratorServlet: {}", e.getMessage(), e);
            response.setStatus(500);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }
    
    private void sendJsonResponse(SlingHttpServletResponse response, Map<String, String> files, String componentType) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter writer = response.getWriter();
        writer.write("{\n");
        writer.write("  \"componentType\": \"" + componentType + "\",\n");
        writer.write("  \"generatedBy\": \"SURGE AEM LLM Connector\",\n");
        writer.write("  \"timestamp\": \"" + System.currentTimeMillis() + "\",\n");
        writer.write("  \"files\": {\n");
        
        int count = 0;
        for (Map.Entry<String, String> entry : files.entrySet()) {
            if (count > 0) {
                writer.write(",\n");
            }
            writer.write("    \"" + entry.getKey() + "\": " + escapeJsonString(entry.getValue()));
            count++;
        }
        
        writer.write("\n  }\n");
        writer.write("}");
        writer.flush();
    }
    
    private void sendZipResponse(SlingHttpServletResponse response, Map<String, String> files, String componentType) 
            throws IOException {
        
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", 
            "attachment; filename=\"" + componentType + "-component-" + System.currentTimeMillis() + ".zip\"");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        
        // Add a README file
        ZipEntry readmeEntry = new ZipEntry("README.md");
        zos.putNextEntry(readmeEntry);
        String readme = createReadmeContent(componentType);
        zos.write(readme.getBytes("UTF-8"));
        zos.closeEntry();
        
        // Add component files
        for (Map.Entry<String, String> entry : files.entrySet()) {
            ZipEntry zipEntry = new ZipEntry(componentType + "/" + entry.getKey());
            zos.putNextEntry(zipEntry);
            zos.write(entry.getValue().getBytes("UTF-8"));
            zos.closeEntry();
        }
        
        zos.close();
        
        byte[] zipData = baos.toByteArray();
        response.setContentLength(zipData.length);
        response.getOutputStream().write(zipData);
        response.getOutputStream().flush();
    }
    
    private String createReadmeContent(String componentType) {
        return String.format(
            "# %s Component\n\n" +
            "Generated by **SURGE AEM LLM Connector**\n\n" +
            "## About SURGE Software Solutions Private Limited\n\n" +
            "This component was generated using SURGE's AEM LLM Connector, an innovative solution that bridges the gap between AEM developers and AI-powered development tools.\n\n" +
            "## Installation Instructions\n\n" +
            "1. Copy the `%s` folder to your AEM project's components directory\n" +
            "2. Update your project's component group if needed\n" +
            "3. Install the component files in your AEM instance\n" +
            "4. The component will be available in the component browser\n\n" +
            "## Files Included\n\n" +
            "- `dialog.xml` - Component dialog configuration\n" +
            "- `%s.html` - Component HTML template\n" +
            "- `%s.js` - Component JavaScript logic\n" +
            "- `.content.xml` - Component metadata\n\n" +
            "## Generated Information\n\n" +
            "- **Component Type**: %s\n" +
            "- **Generated On**: %s\n" +
            "- **Generated By**: SURGE AEM LLM Connector v1.0.0\n" +
            "- **Powered By**: OpenAI ChatGPT-4\n\n" +
            "## Support\n\n" +
            "For support and more information about SURGE Software Solutions:\n" +
            "- Website: https://surgesoftware.com\n" +
            "- Email: support@surgesoftware.com\n\n" +
            "---\n\n" +
            "© 2024 SURGE Software Solutions Private Limited. All rights reserved.",
            componentType.substring(0, 1).toUpperCase() + componentType.substring(1),
            componentType,
            componentType,
            componentType,
            componentType,
            new java.util.Date()
        );
    }
    
    private String escapeJsonString(String str) {
        if (str == null) {
            return "null";
        }
        
        return "\"" + str.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t") + "\"";
    }
} 