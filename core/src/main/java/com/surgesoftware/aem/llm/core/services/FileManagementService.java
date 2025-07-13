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
package com.surgesoftware.aem.llm.core.services;

import java.util.Map;

/**
 * File Management Service for SURGE AEM LLM Connector
 * 
 * Service interface for managing file operations like saving generated
 * component files, creating ZIP archives, and managing file storage.
 * 
 * @author SURGE Software Solutions Private Limited
 */
public interface FileManagementService {
    
    /**
     * Save generated component files to the AEM repository
     * 
     * @param componentFiles Map of file names to file content
     * @param timestamp Timestamp for folder naming
     * @return Path to the saved files directory
     */
    String saveComponentFiles(Map<String, String> componentFiles, String timestamp);
    
    /**
     * Create a ZIP file from generated component files
     * 
     * @param componentFiles Map of file names to file content
     * @param timestamp Timestamp for file naming
     * @return Path to the created ZIP file
     */
    String createZipFile(Map<String, String> componentFiles, String timestamp);
    
    /**
     * Generate and save a standalone preview HTML file
     * 
     * @param htmlContent The HTL/HTML content to preview
     * @param timestamp Timestamp for file naming
     * @return Path to the preview HTML file
     */
    String savePreviewFile(String htmlContent, String timestamp);
    
    /**
     * Get the download URL for a ZIP file
     * 
     * @param zipPath Path to the ZIP file
     * @return Download URL
     */
    String getDownloadUrl(String zipPath);
    
    /**
     * Get the preview URL for an HTML file
     * 
     * @param previewPath Path to the preview HTML file
     * @return Preview URL
     */
    String getPreviewUrl(String previewPath);
    
    /**
     * Clean up old generated files (older than specified days)
     * 
     * @param daysOld Number of days to keep files
     */
    void cleanupOldFiles(int daysOld);
} 