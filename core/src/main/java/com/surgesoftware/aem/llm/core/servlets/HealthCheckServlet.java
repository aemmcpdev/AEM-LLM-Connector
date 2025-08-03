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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surgesoftware.aem.llm.core.services.LocalLLMService;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Servlet for LLM Service
 * 
 * Provides a quick health check endpoint to verify LLM connectivity
 * and return readiness status.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/aem-llm/health",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.extensions=json"
})
public class HealthCheckServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServlet.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Reference
    private LocalLLMService localLLMService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        long startTime = System.currentTimeMillis();
        
        try {
            LOG.info("🏥 Health check requested for LLM service");
            
            // Quick connectivity test
            boolean connected = localLLMService.testConnection();
            String llmInfo = localLLMService.getLLMInfo();
            
            long duration = System.currentTimeMillis() - startTime;
            
            result.put("status", connected ? "LLM READY" : "LLM NOT READY");
            result.put("connected", connected);
            result.put("llmInfo", llmInfo);
            result.put("responseTime", duration + "ms");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            if (connected) {
                result.put("message", "✅ Local LLM is reachable and ready for requests");
                response.setStatus(SlingHttpServletResponse.SC_OK); // 200
                LOG.info("✅ Health check PASSED - LLM ready ({}ms)", duration);
            } else {
                result.put("message", "❌ Local LLM is not responding or service is disabled");
                response.setStatus(SlingHttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
                LOG.warn("⚠️ Health check FAILED - LLM not ready ({}ms)", duration);
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOG.error("❌ Health check ERROR: {} ({}ms)", e.getMessage(), duration, e);
            
            result.put("status", "ERROR");
            result.put("connected", false);
            result.put("message", "Error checking LLM health: " + e.getMessage());
            result.put("responseTime", duration + "ms");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
        } finally {
            objectMapper.writeValue(out, result);
        }
    }
}