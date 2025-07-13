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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.Binary;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Preview Servlet for SURGE AEM LLM Connector
 * 
 * This servlet handles serving of generated HTML preview files.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/aem-llm/preview",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.auth.requirements=-/bin/aem-llm/preview",
        "service.description=SURGE AEM LLM Connector - Preview Servlet",
        "service.vendor=SURGE Software Solutions Private Limited"
    })
public class PreviewServlet extends SlingAllMethodsServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(PreviewServlet.class);
    private static final long serialVersionUID = 1L;
    
    private static final String BASE_PATH = "/var/aem-llm";
    private static final String SUBSERVICE_NAME = "surge-aem-llm-service";
    
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        
        String filePath = request.getParameter("file");
        if (filePath == null || filePath.isEmpty()) {
            response.setStatus(400);
            response.setContentType("text/html");
            response.getWriter().write(createErrorPage("Missing file parameter", 400));
            return;
        }
        
        // Security check: ensure file path is within allowed directories
        if (!isValidFilePath(filePath)) {
            response.setStatus(403);
            response.setContentType("text/html");
            response.getWriter().write(createErrorPage("Access denied", 403));
            return;
        }
        
        try (ResourceResolver resolver = getServiceResourceResolver()) {
            Session session = resolver.adaptTo(Session.class);
            String fullPath = BASE_PATH + "/" + filePath;
            
            if (!session.nodeExists(fullPath)) {
                response.setStatus(404);
                response.setContentType("text/html");
                response.getWriter().write(createErrorPage("Preview file not found", 404));
                return;
            }
            
            Node fileNode = session.getNode(fullPath);
            if (!fileNode.isNodeType("nt:file")) {
                response.setStatus(404);
                response.setContentType("text/html");
                response.getWriter().write(createErrorPage("Invalid preview file", 404));
                return;
            }
            
            Node contentNode = fileNode.getNode("jcr:content");
            Binary binary = contentNode.getProperty("jcr:data").getBinary();
            
            // Set response headers for HTML content
            response.setContentType("text/html; charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // Stream the HTML content
            try (InputStream inputStream = binary.getStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    response.getWriter().println(line);
                }
            }
            
            response.getWriter().flush();
            LOG.info("Successfully served preview: {}", filePath);
            
        } catch (Exception e) {
            LOG.error("Error serving preview for file {}: {}", filePath, e.getMessage(), e);
            response.setStatus(500);
            response.setContentType("text/html");
            response.getWriter().write(createErrorPage("Internal server error", 500));
        }
    }
    
    private boolean isValidFilePath(String filePath) {
        // Ensure file path is safe and within allowed directories
        if (filePath.contains("..") || filePath.startsWith("/") || filePath.contains("\\")) {
            return false;
        }
        
        // Only allow previews from the previews directory and must be HTML
        return filePath.startsWith("previews/") && filePath.endsWith(".html");
    }
    
    private String createErrorPage(String errorMessage, int statusCode) {
        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Preview Error - " + statusCode + "</title>\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; margin: 0; padding: 40px; background: #f5f5f5; }\n" +
            "        .error-container { max-width: 600px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }\n" +
            "        .error-code { font-size: 72px; font-weight: bold; color: #dc3545; margin: 0; }\n" +
            "        .error-message { font-size: 24px; color: #6c757d; margin: 20px 0; }\n" +
            "        .error-description { color: #6c757d; margin: 20px 0; }\n" +
            "        .back-button { display: inline-block; margin-top: 30px; padding: 12px 24px; background: #0066cc; color: white; text-decoration: none; border-radius: 5px; }\n" +
            "        .back-button:hover { background: #0056b3; }\n" +
            "        .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; color: #6c757d; font-size: 14px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"error-container\">\n" +
            "        <div class=\"error-code\">" + statusCode + "</div>\n" +
            "        <div class=\"error-message\">" + errorMessage + "</div>\n" +
            "        <div class=\"error-description\">\n" +
            "            The requested preview file could not be displayed.\n" +
            "        </div>\n" +
            "        <a href=\"javascript:history.back()\" class=\"back-button\">Go Back</a>\n" +
            "        <div class=\"footer\">\n" +
            "            SURGE AEM LLM Connector &copy; 2024 SURGE Software Solutions Private Limited\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
    
    private ResourceResolver getServiceResourceResolver() throws Exception {
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);
        return resourceResolverFactory.getServiceResourceResolver(authInfo);
    }
} 