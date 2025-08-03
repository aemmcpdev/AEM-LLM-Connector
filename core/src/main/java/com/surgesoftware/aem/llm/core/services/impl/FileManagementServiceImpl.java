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

import com.surgesoftware.aem.llm.core.services.FileManagementService;
import com.surgesoftware.aem.llm.core.config.ServiceUserConfig;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Calendar;

/**
 * File Management Service Implementation for SURGE AEM LLM Connector
 * 
 * Implementation for managing file operations in the AEM repository.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = FileManagementService.class,
    property = {
        "service.description=SURGE AEM LLM Connector - File Management Service",
        "service.vendor=SURGE Software Solutions Private Limited"
    })
public class FileManagementServiceImpl implements FileManagementService {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileManagementServiceImpl.class);
    
    private static final String BASE_PATH = "/var/aem-llm";
    private static final String GENERATED_PATH = BASE_PATH + "/generated";
    private static final String PREVIEWS_PATH = BASE_PATH + "/previews";
    private static final String ZIP_PATH = BASE_PATH + "/downloads";
    
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    
    @Reference
    private ServiceUserConfig serviceUserConfig;


    
    @Activate
    protected void activate() {
        LOG.info("SURGE AEM LLM Connector: File Management Service activated");
        ensureBasePaths();
    }
    
    @Override
    public String saveComponentFiles(Map<String, String> componentFiles, String timestamp) {
        LOG.info("Saving component files for timestamp: {}", timestamp);
        
        try (ResourceResolver resolver = getServiceResourceResolver()) {
            LOG.info("Got resource resolver successfully");
            Session session = resolver.adaptTo(Session.class);
            String folderPath = GENERATED_PATH + "/" + timestamp;
            
            // Create the timestamped folder
            Node folderNode = createFolderStructure(session, folderPath);
            
            // Save each component file
            for (Map.Entry<String, String> entry : componentFiles.entrySet()) {
                String fileName = entry.getKey();
                String content = entry.getValue();
                
                Node fileNode = folderNode.addNode(fileName, "nt:file");
                Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
                
                Binary binary = session.getValueFactory().createBinary(
                    new ByteArrayInputStream(content.getBytes("UTF-8"))
                );
                
                contentNode.setProperty("jcr:data", binary);
                contentNode.setProperty("jcr:mimeType", getMimeType(fileName));
                contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
                
                LOG.debug("Saved file: {}", fileName);
            }
            
            session.save();
            LOG.info("Successfully saved {} files to {}", componentFiles.size(), folderPath);
            return folderPath;
            
        } catch (Exception e) {
            LOG.error("Error saving component files: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public String createZipFile(Map<String, String> componentFiles, String timestamp) {
        LOG.info("Creating ZIP file for timestamp: {}", timestamp);
        
        try (ResourceResolver resolver = getServiceResourceResolver()) {
            Session session = resolver.adaptTo(Session.class);
            String zipPath = ZIP_PATH + "/" + timestamp + ".zip";
            
            // Create ZIP content in memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                
                // Add component files to ZIP
                for (Map.Entry<String, String> entry : componentFiles.entrySet()) {
                    ZipEntry zipEntry = new ZipEntry(entry.getKey());
                    zos.putNextEntry(zipEntry);
                    zos.write(entry.getValue().getBytes("UTF-8"));
                    zos.closeEntry();
                }
                
                // Add README file
                ZipEntry readmeEntry = new ZipEntry("README.md");
                zos.putNextEntry(readmeEntry);
                String readme = createReadmeContent(timestamp);
                zos.write(readme.getBytes("UTF-8"));
                zos.closeEntry();
            }
            
            // Save ZIP to repository
            Node zipNode = createFileNode(session, zipPath, baos.toByteArray(), "application/zip");
            session.save();
            
            LOG.info("Successfully created ZIP file: {}", zipPath);
            return zipPath;
            
        } catch (Exception e) {
            LOG.error("Error creating ZIP file: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public String savePreviewFile(String htmlContent, String timestamp) {
        LOG.info("Saving preview file for timestamp: {}", timestamp);
        try (ResourceResolver resolver = getServiceResourceResolver()) {
            Session session = resolver.adaptTo(Session.class);
            String previewPath = PREVIEWS_PATH + "/" + timestamp + ".html";

            // Use the provided HTML content directly since preview is now generated by Local LLM
            String llmPreviewHtml = htmlContent;

            // Create standalone HTML with enhanced styling
            String standaloneHtml = createStandaloneHtml(llmPreviewHtml, timestamp);

            // Save preview file
            Node previewNode = createFileNode(session, previewPath, 
                standaloneHtml.getBytes("UTF-8"), "text/html");
            session.save();

            LOG.info("Successfully saved preview file: {}", previewPath);
            return previewPath;
        } catch (Exception e) {
            LOG.error("Error saving preview file: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public String getDownloadUrl(String zipPath) {
        if (zipPath == null) return null;
        return "/bin/aem-llm/download?file=" + zipPath.substring(BASE_PATH.length() + 1);
    }
    
    @Override
    public String getPreviewUrl(String previewPath) {
        if (previewPath == null) return null;
        return "/bin/aem-llm/preview?file=" + previewPath.substring(BASE_PATH.length() + 1);
    }
    
    @Override
    public void cleanupOldFiles(int daysOld) {
        LOG.info("Cleaning up files older than {} days", daysOld);
        
        try (ResourceResolver resolver = getServiceResourceResolver()) {
            Session session = resolver.adaptTo(Session.class);
            Calendar cutoffDate = Calendar.getInstance();
            cutoffDate.add(Calendar.DAY_OF_MONTH, -daysOld);
            
            // Clean up generated files, downloads, and previews
            cleanupPath(session, GENERATED_PATH, cutoffDate);
            cleanupPath(session, ZIP_PATH, cutoffDate);
            cleanupPath(session, PREVIEWS_PATH, cutoffDate);
            
            session.save();
            LOG.info("Cleanup completed");
            
        } catch (Exception e) {
            LOG.error("Error during cleanup: {}", e.getMessage(), e);
        }
    }
    
    private void ensureBasePaths() {
        try (ResourceResolver resolver = getServiceResourceResolver()) {
            Session session = resolver.adaptTo(Session.class);
            if (session != null) {
                createFolderStructure(session, GENERATED_PATH);
                createFolderStructure(session, PREVIEWS_PATH);
                createFolderStructure(session, ZIP_PATH);
                session.save();
                LOG.info("Successfully ensured base paths: {}, {}, {}", GENERATED_PATH, PREVIEWS_PATH, ZIP_PATH);
            } else {
                LOG.error("Could not adapt ResourceResolver to Session");
            }
        } catch (Exception e) {
            LOG.error("Error ensuring base paths: {}", e.getMessage(), e);
        }
    }
    
    private ResourceResolver getServiceResourceResolver() throws Exception {
        try {
            return serviceUserConfig.getServiceResourceResolver();
        } catch (Exception e) {
            LOG.error("Failed to get service resource resolver from ServiceUserConfig: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private Node createFolderStructure(Session session, String path) throws RepositoryException {
        Node current = session.getRootNode();
        String[] segments = path.substring(1).split("/");
        
        for (String segment : segments) {
            if (!current.hasNode(segment)) {
                current = current.addNode(segment, "nt:folder");
            } else {
                current = current.getNode(segment);
            }
        }
        
        return current;
    }
    
    private Node createFileNode(Session session, String path, byte[] content, String mimeType) 
            throws RepositoryException {
        
        int lastSlash = path.lastIndexOf('/');
        String parentPath = path.substring(0, lastSlash);
        String fileName = path.substring(lastSlash + 1);
        
        Node parentNode = createFolderStructure(session, parentPath);
        
        if (parentNode.hasNode(fileName)) {
            parentNode.getNode(fileName).remove();
        }
        
        Node fileNode = parentNode.addNode(fileName, "nt:file");
        Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
        
        Binary binary = session.getValueFactory().createBinary(
            new ByteArrayInputStream(content)
        );
        
        contentNode.setProperty("jcr:data", binary);
        contentNode.setProperty("jcr:mimeType", mimeType);
        contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
        
        return fileNode;
    }
    
    private String getMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "xml": return "application/xml";
            case "html": return "text/html";
            case "js": return "application/javascript";
            case "java": return "text/x-java-source";
            case "json": return "application/json";
            default: return "text/plain";
        }
    }
    
    private String createStandaloneHtml(String htmlContent, String timestamp) {
        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>AEM Component Preview - " + timestamp + "</title>\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }\n" +
            "        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
            "        .header { border-bottom: 2px solid #0066cc; padding-bottom: 20px; margin-bottom: 30px; }\n" +
            "        .header h1 { color: #0066cc; margin: 0; font-size: 28px; }\n" +
            "        .header p { color: #666; margin: 5px 0 0 0; }\n" +
            "        .preview-section { background: #f9f9f9; padding: 20px; border-radius: 5px; border-left: 4px solid #0066cc; }\n" +
            "        .preview-section h2 { margin-top: 0; color: #333; }\n" +
            "        .component-preview { background: white; padding: 20px; border-radius: 4px; margin-top: 15px; }\n" +
            "        .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; text-align: center; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>AEM Component Preview</h1>\n" +
            "            <p>Generated on: " + timestamp + " | SURGE AEM LLM Connector</p>\n" +
            "        </div>\n" +
            "        <div class=\"preview-section\">\n" +
            "            <h2>Component Output</h2>\n" +
            "            <div class=\"component-preview\">\n" +
            htmlContent +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"footer\">\n" +
            "            <p>Generated by SURGE AEM LLM Connector &copy; 2024 SURGE Software Solutions Private Limited</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
    
    private String createReadmeContent(String timestamp) {
        return "# AEM Component Files\n\n" +
            "Generated by **SURGE AEM LLM Connector**\n\n" +
            "## Generation Details\n\n" +
            "- **Timestamp**: " + timestamp + "\n" +
            "- **Generated By**: SURGE AEM LLM Connector\n" +
            "- **Powered By**: Local LLM (Ollama/LocalAI)\n\n" +
            "## Files Included\n\n" +
            "This package contains the generated AEM component files:\n" +
            "- Component dialog configuration (dialog.xml)\n" +
            "- HTL template files (*.html)\n" +
            "- JavaScript files (*.js)\n" +
            "- Component metadata (.content.xml)\n" +
            "- Java model files (*.java)\n\n" +
            "## Installation\n\n" +
            "1. Extract this ZIP file to your AEM project\n" +
            "2. Place the files in the appropriate component directory\n" +
            "3. Build and deploy your project\n" +
            "4. The component will be available in the component browser\n\n" +
            "## Support\n\n" +
            "For support and more information:\n" +
            "- Website: https://surgesoftware.com\n" +
            "- Email: support@surgesoftware.com\n\n" +
            "---\n\n" +
            "© 2024 SURGE Software Solutions Private Limited. All rights reserved.";
    }
    
    private void cleanupPath(Session session, String path, Calendar cutoffDate) {
        try {
            if (session.nodeExists(path)) {
                Node pathNode = session.getNode(path);
                javax.jcr.NodeIterator nodes = pathNode.getNodes();
                while (nodes.hasNext()) {
                    Node node = nodes.nextNode();
                    try {
                        if (node.hasProperty("jcr:created")) {
                            Calendar created = node.getProperty("jcr:created").getDate();
                            if (created.before(cutoffDate)) {
                                LOG.debug("Removing old file: {}", node.getPath());
                                node.remove();
                            }
                        }
                    } catch (RepositoryException e) {
                        LOG.warn("Error checking node date: {}", e.getMessage());
                    }
                }
            }
        } catch (RepositoryException e) {
            LOG.error("Error cleaning up path {}: {}", path, e.getMessage());
        }
    }
} 