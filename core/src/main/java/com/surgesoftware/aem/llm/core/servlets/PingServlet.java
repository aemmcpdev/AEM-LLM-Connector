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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Ping Servlet for testing LLM connectivity
 * 
 * Provides a health check endpoint for the Local LLM service
 * to verify if Ollama/LocalAI is running and reachable.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = Servlet.class,
    property = {
        "sling.servlet.methods=GET",
        "sling.servlet.paths=/bin/aem-llm/ping"
    })
public class PingServlet extends SlingAllMethodsServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(PingServlet.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Reference
    private LocalLLMService localLLMService;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Enable CORS for frontend calls
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        Map<String, Object> pingResponse = new HashMap<>();
        
        try {
            LOG.info("🏓 Ping: Testing LLM connectivity...");
            
            // Get LLM service info
            String llmInfo = localLLMService.getLLMInfo();
            
            // Test actual connection
            boolean isConnected = localLLMService.testConnection();
            
            if (isConnected) {
                LOG.info("✅ Ping: LLM is reachable");
                pingResponse.put("status", "OK");
                pingResponse.put("message", "Local LLM is reachable");
                pingResponse.put("llmInfo", llmInfo);
                pingResponse.put("connected", true);
                response.setStatus(200);
            } else {
                LOG.warn("❌ Ping: LLM is not reachable");
                pingResponse.put("status", "ERROR");
                pingResponse.put("message", "Local LLM not responding");
                pingResponse.put("llmInfo", llmInfo);
                pingResponse.put("connected", false);
                response.setStatus(503); // Service Unavailable
            }
            
        } catch (Exception e) {
            LOG.error("❌ Ping: Error testing LLM connectivity: {}", e.getMessage(), e);
            pingResponse.put("status", "ERROR");
            pingResponse.put("message", "Error testing LLM: " + e.getMessage());
            pingResponse.put("connected", false);
            response.setStatus(500);
        }
        
        // Add timestamp
        pingResponse.put("timestamp", System.currentTimeMillis());
        
        // Write JSON response
        String jsonResponse = objectMapper.writeValueAsString(pingResponse);
        response.getWriter().write(jsonResponse);
        
        LOG.debug("Ping response: {}", jsonResponse);
    }
}