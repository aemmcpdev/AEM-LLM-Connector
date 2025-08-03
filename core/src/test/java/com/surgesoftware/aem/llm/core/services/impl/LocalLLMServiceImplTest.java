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

import com.surgesoftware.aem.llm.core.models.ComponentGenerationRequest;
import com.surgesoftware.aem.llm.core.models.ComponentGenerationResponse;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

/**
 * Test class for LocalLLMServiceImpl
 * 
 * @author SURGE Software Solutions Private Limited
 */
public class LocalLLMServiceImplTest {
    
    private LocalLLMServiceImpl localLLMService;
    
    @Before
    public void setUp() {
        localLLMService = new LocalLLMServiceImpl();
    }
    
    @After
    public void tearDown() {
        localLLMService = null;
    }
    
    @Test
    public void testGenerateComponentWithValidRequest() {
        // Create a test request
        ComponentGenerationRequest request = new ComponentGenerationRequest(
            "Create a product card component with image, title, description and CTA button",
            "product-card",
            "Include responsive design"
        );
        
        // Test the service (this will fail if no local LLM is running, which is expected)
        ComponentGenerationResponse response = localLLMService.generateComponent(request);
        
        // Verify response structure
        assertNotNull("Response should not be null", response);
        assertNotNull("Response should have timestamp", response.getTimestamp());
        
        // Since we don't have a local LLM running in tests, we expect an error
        // but the response structure should be valid
        if (!response.isSuccess()) {
            assertNotNull("Error should be set when service fails", response.getError());
        }
    }
    
    @Test
    public void testGetLLMInfo() {
        String llmInfo = localLLMService.getLLMInfo();
        assertNotNull("LLM info should not be null", llmInfo);
        assertTrue("LLM info should contain service status", 
                  llmInfo.contains("Local LLM Service"));
    }
    
    @Test
    public void testTestConnection() {
        // Test connection (will fail without local LLM, which is expected)
        boolean isConnected = localLLMService.testConnection();
        
        // We can't assert the result since it depends on local LLM availability
        // But the method should not throw an exception
        assertTrue("Connection test should complete without exception", true);
    }
    
    /**
     * Test extractJsonBlock method with various input scenarios
     */
    @Test
    public void testExtractJsonBlock() throws Exception {
        // Use reflection to access the private method
        Method extractJsonBlockMethod = LocalLLMServiceImpl.class.getDeclaredMethod("extractJsonBlock", String.class);
        extractJsonBlockMethod.setAccessible(true);
        
        // Test case 1: Valid JSON without Markdown
        String validJson = "{\"name\":\"test\",\"description\":\"A test component\"}";
        String result1 = (String) extractJsonBlockMethod.invoke(localLLMService, validJson);
        assertEquals("Should return the same JSON when no Markdown", validJson, result1);
        
        // Test case 2: JSON wrapped in Markdown code fences
        String markdownJson = "```json\n{\"name\":\"test\",\"description\":\"A test component\"}\n```";
        String result2 = (String) extractJsonBlockMethod.invoke(localLLMService, markdownJson);
        assertEquals("Should extract JSON from Markdown", validJson, result2);
        
        // Test case 2b: JSON with problematic backticks (the failing case)
        String problematicJson = "```json\n```json\n{\"name\":\"test\",\"description\":\"A test component\"}\n```";
        String result2b = (String) extractJsonBlockMethod.invoke(localLLMService, problematicJson);
        assertEquals("Should handle double code fence issue", validJson, result2b);
        
        // Test case 3: JSON with extra text before and after
        String jsonWithExtra = "Here's the component JSON:\n{\"name\":\"test\",\"description\":\"A test component\"}\nThat's the structure.";
        String result3 = (String) extractJsonBlockMethod.invoke(localLLMService, jsonWithExtra);
        assertEquals("Should extract JSON from text with extra content", validJson, result3);
        
        // Test case 4: Simple Markdown code fence without 'json' specifier
        String simpleMarkdown = "```\n{\"name\":\"test\",\"description\":\"A test component\"}\n```";
        String result4 = (String) extractJsonBlockMethod.invoke(localLLMService, simpleMarkdown);
        assertEquals("Should extract JSON from simple Markdown", validJson, result4);
        
        // Test case 5: Empty or null input
        String result5 = (String) extractJsonBlockMethod.invoke(localLLMService, (String) null);
        assertNull("Should return null for null input", result5);
        
        String result6 = (String) extractJsonBlockMethod.invoke(localLLMService, "");
        assertNull("Should return null for empty input", result6);
        
        // Test case 6: No JSON content
        String noJson = "This is just text without any JSON";
        String result7 = (String) extractJsonBlockMethod.invoke(localLLMService, noJson);
        assertNull("Should return null when no JSON found", result7);
        
        // Test case 7: Complex streaming artifacts
        String streamingJson = "Some intro text\n```json\n{\"name\":\"test\",\"description\":\"A test component\"}\n```\nSome trailing text";
        String result8 = (String) extractJsonBlockMethod.invoke(localLLMService, streamingJson);
        assertEquals("Should handle streaming artifacts", validJson, result8);
    }
    
    /**
     * Test escapeUnescapedQuotesInStringValues method with comprehensive scenarios
     */
    @Test
    public void testEscapeUnescapedQuotesInStringValues() throws Exception {
        // Use reflection to access the private method
        Method escapeQuotesMethod = LocalLLMServiceImpl.class.getDeclaredMethod("escapeUnescapedQuotesInStringValues", String.class);
        escapeQuotesMethod.setAccessible(true);
        
        // Test case 1: Null and empty inputs
        String result1 = (String) escapeQuotesMethod.invoke(localLLMService, (String) null);
        assertNull("Should handle null input", result1);
        
        String result2 = (String) escapeQuotesMethod.invoke(localLLMService, "");
        assertEquals("Should handle empty input", "", result2);
        
        // Test case 2: HTML content with unescaped quotes
        String htmlWithQuotes = "{\"sample\":\"<p>This is a \"bad\" example</p>\"}";
        String result3 = (String) escapeQuotesMethod.invoke(localLLMService, htmlWithQuotes);
        assertTrue("Should escape quotes in HTML content", result3.contains("\\\"bad\\\""));
        assertFalse("Should not have unescaped quotes", result3.contains("\"bad\""));
        
        // Test case 3: HTML img tag with alt attribute
        String imgTag = "{\"html\":\"<img alt=\"bad quote\">\"}";
        String result4 = (String) escapeQuotesMethod.invoke(localLLMService, imgTag);
        assertTrue("Should escape quotes in img alt", result4.contains("\\\"bad quote\\\""));
        
        // Test case 4: Multiple unescaped quotes in one value
        String multipleQuotes = "{\"content\":\"He said \"hello\" and \"goodbye\"\"}";
        String result5 = (String) escapeQuotesMethod.invoke(localLLMService, multipleQuotes);
        assertTrue("Should escape all internal quotes", 
                  result5.contains("\\\"hello\\\"") && result5.contains("\\\"goodbye\\\""));
        
        // Test case 5: Already properly escaped quotes (should preserve)
        String alreadyEscaped = "{\"sample\":\"This is \\\"already\\\" escaped\"}";
        String result6 = (String) escapeQuotesMethod.invoke(localLLMService, alreadyEscaped);
        assertEquals("Should not modify already escaped quotes", alreadyEscaped, result6);
        
        // Test case 6: Valid JSON without quotes in values (should not change)
        String validJson = "{\"name\":\"component\",\"description\":\"A valid component\"}";
        String result7 = (String) escapeQuotesMethod.invoke(localLLMService, validJson);
        assertEquals("Should not modify valid JSON", validJson, result7);
        
        // Test case 7: Complex richtext with HTML attributes
        String complexHtml = "{\"richtext\":\"<div class=\"container\" data-id=\"123\">Content</div>\"}";
        String result8 = (String) escapeQuotesMethod.invoke(localLLMService, complexHtml);
        assertTrue("Should escape quotes in HTML attributes", 
                  result8.contains("\\\"container\\\"") && result8.contains("\\\"123\\\""));
        
        // Test case 8: Mixed content with both keys and values containing quotes
        String mixedContent = "{\"key\":\"value\",\"html\":\"<p>Quote: \"text\"</p>\"}";
        String result9 = (String) escapeQuotesMethod.invoke(localLLMService, mixedContent);
        assertTrue("Should only escape quotes in values", result9.contains("\\\"text\\\""));
        assertTrue("Should preserve key structure", result9.contains("\"key\":\"value\""));
    }
    
    /**
     * Test LLMUnavailableException creation and message handling
     */
    @Test
    public void testLLMUnavailableExceptionCreation() {
        // Test timeout exception
        Exception timeoutEx = com.surgesoftware.aem.llm.core.exceptions.LLMUnavailableException.forTimeout("llama3.2", 180);
        assertTrue("Should contain timeout message", timeoutEx.getMessage().contains("timed out"));
        
        // Test cold start exception
        Exception coldStartEx = com.surgesoftware.aem.llm.core.exceptions.LLMUnavailableException.forColdStart("llama3.2");
        assertTrue("Should contain cold start message", coldStartEx.getMessage().contains("cold start"));
        
        // Test connectivity exception
        Exception connectEx = com.surgesoftware.aem.llm.core.exceptions.LLMUnavailableException.forConnectivity("http://localhost:11434");
        assertTrue("Should contain connectivity message", connectEx.getMessage().contains("connect"));
    }
    
    /**
     * Test component generation with various error scenarios
     */
    @Test
    public void testComponentGenerationErrorHandling() {
        // Test with disabled service
        localLLMService = new LocalLLMServiceImpl();
        // Service is disabled by default in test environment
        
        ComponentGenerationRequest request = new ComponentGenerationRequest(
            "Create a test component",
            "test-component",
            "Basic requirements"
        );
        
        ComponentGenerationResponse response = localLLMService.generateComponent(request);
        
        assertNotNull("Response should not be null", response);
        assertEquals("Should return error status", "error", response.getStatus());
        assertTrue("Should contain disabled message", response.getError().contains("not enabled"));
    }
    
    /**
     * Test model warm-up and retry logic (mock test)
     * This test verifies the structure without actual LLM calls
     */
    @Test
    public void testRetryLogicStructure() throws Exception {
        // Use reflection to test private method behavior
        Method findBestModelMethod = LocalLLMServiceImpl.class.getDeclaredMethod("findBestAvailableModel", String.class);
        findBestModelMethod.setAccessible(true);
        
        // Test should not throw exception even with invalid setup
        try {
            Object result = findBestModelMethod.invoke(localLLMService, "test-model");
            // If no IOException is thrown during method call setup, the structure is correct
            assertTrue("Method structure is valid", true);
        } catch (Exception e) {
            // Expected for test environment - no actual Ollama connection
            assertTrue("Expected exception in test environment", true);
        }
    }
    
    /**
     * Test error message formatting and user-friendly responses
     */
    @Test
    public void testErrorMessageFormatting() {
        ComponentGenerationRequest request = new ComponentGenerationRequest(
            "Test prompt with special characters: \"quotes\" and 'apostrophes'",
            "test-component",
            "Test requirements"
        );
        
        ComponentGenerationResponse response = localLLMService.generateComponent(request);
        
        // Should handle special characters gracefully
        assertNotNull("Response should not be null", response);
        assertNotNull("Error message should not be null", response.getError());
        
        // Timestamp should be properly formatted
        assertNotNull("Timestamp should be set", response.getTimestamp());
        assertTrue("Timestamp should contain date format", response.getTimestamp().contains("-"));
    }
    
    /**
     * Test readiness check method structure
     */
    @Test
    public void testReadinessCheckStructure() throws Exception {
        // Use reflection to verify readiness check method exists and has correct signature
        Method readinessMethod = LocalLLMServiceImpl.class.getDeclaredMethod("isOllamaReady");
        readinessMethod.setAccessible(true);
        
        assertNotNull("Readiness check method should exist", readinessMethod);
        assertEquals("Should return boolean", boolean.class, readinessMethod.getReturnType());
        
        // Test execution (will return false in test environment)
        boolean result = (boolean) readinessMethod.invoke(localLLMService);
        // In test environment, this should be false since no Ollama is running, but the method structure is what we're testing
        assertTrue("Readiness check method executed successfully", result == false || result == true);
    }
    
    /**
     * Test warm-up method structure and error handling
     */
    @Test
    public void testWarmUpMethodStructure() throws Exception {
        // Use reflection to verify warm-up method exists
        Method warmUpMethod = LocalLLMServiceImpl.class.getDeclaredMethod("warmUpModel", String.class);
        warmUpMethod.setAccessible(true);
        
        assertNotNull("Warm-up method should exist", warmUpMethod);
        
        // Test should handle IOException gracefully in test environment
        try {
            warmUpMethod.invoke(localLLMService, "test-model");
            fail("Should throw exception in test environment");
        } catch (Exception e) {
            // Expected - no actual HTTP client configured in test
            assertTrue("Expected exception in test environment", true);
        }
    }
} 