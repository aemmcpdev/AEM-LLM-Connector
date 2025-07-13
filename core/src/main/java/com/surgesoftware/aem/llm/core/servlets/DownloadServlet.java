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
import java.util.HashMap;
import java.util.Map;

/**
 * Download Servlet for SURGE AEM LLM Connector
 * 
 * This servlet handles downloading of generated ZIP files.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/aem-llm/download",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.auth.requirements=-/bin/aem-llm/download",
        "service.description=SURGE AEM LLM Connector - Download Servlet",
        "service.vendor=SURGE Software Solutions Private Limited"
    })
public class DownloadServlet extends SlingAllMethodsServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(DownloadServlet.class);
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
            response.getWriter().write("Missing file parameter");
            return;
        }
        
        // Security check: ensure file path is within allowed directories
        if (!isValidFilePath(filePath)) {
            response.setStatus(403);
            response.getWriter().write("Access denied");
            return;
        }
        
        try (ResourceResolver resolver = getServiceResourceResolver()) {
            Session session = resolver.adaptTo(Session.class);
            String fullPath = BASE_PATH + "/" + filePath;
            
            if (!session.nodeExists(fullPath)) {
                response.setStatus(404);
                response.getWriter().write("File not found");
                return;
            }
            
            Node fileNode = session.getNode(fullPath);
            if (!fileNode.isNodeType("nt:file")) {
                response.setStatus(404);
                response.getWriter().write("Invalid file");
                return;
            }
            
            Node contentNode = fileNode.getNode("jcr:content");
            Binary binary = contentNode.getProperty("jcr:data").getBinary();
            String mimeType = contentNode.getProperty("jcr:mimeType").getString();
            
            // Set response headers for download
            String fileName = fileNode.getName();
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLengthLong(binary.getSize());
            
            // Stream the file content
            try (InputStream inputStream = binary.getStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
            }
            
            response.getOutputStream().flush();
            LOG.info("Successfully served download: {}", fileName);
            
        } catch (Exception e) {
            LOG.error("Error serving download for file {}: {}", filePath, e.getMessage(), e);
            response.setStatus(500);
            response.getWriter().write("Internal server error");
        }
    }
    
    private boolean isValidFilePath(String filePath) {
        // Ensure file path is safe and within allowed directories
        if (filePath.contains("..") || filePath.startsWith("/") || filePath.contains("\\")) {
            return false;
        }
        
        // Only allow downloads from specific subdirectories
        return filePath.startsWith("downloads/") || filePath.startsWith("generated/");
    }
    
    private ResourceResolver getServiceResourceResolver() throws Exception {
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);
        return resourceResolverFactory.getServiceResourceResolver(authInfo);
    }
} 