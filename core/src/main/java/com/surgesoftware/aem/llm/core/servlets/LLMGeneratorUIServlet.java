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
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet to serve the LLM Generator UI
 * 
 * This servlet serves the HTML interface for the Local LLM component generator.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/aem-llm/ui",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.auth.requirements=-/bin/aem-llm/ui",
        "service.description=SURGE AEM LLM Connector - UI Servlet",
        "service.vendor=SURGE Software Solutions Private Limited"
    })
public class LLMGeneratorUIServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(LLMGeneratorUIServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        
        LOG.debug("Serving Local LLM Generator UI");
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter writer = response.getWriter();
        writer.write(getHtmlContent());
        writer.flush();
    }
    
    private String getHtmlContent() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>SURGE AEM LLM Component Generator (Local LLM)</title>\n" +
               "    <style>\n" +
               "        * {\n" +
               "            margin: 0;\n" +
               "            padding: 0;\n" +
               "            box-sizing: border-box;\n" +
               "        }\n" +
               "        \n" +
               "        body {\n" +
               "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;\n" +
               "            background: #f5f5f5;\n" +
               "            color: #333;\n" +
               "            line-height: 1.6;\n" +
               "        }\n" +
               "        \n" +
               "        .container {\n" +
               "            max-width: 1200px;\n" +
               "            margin: 0 auto;\n" +
               "            padding: 20px;\n" +
               "        }\n" +
               "        \n" +
               "        .header {\n" +
               "            text-align: center;\n" +
               "            margin-bottom: 40px;\n" +
               "            padding: 30px 0;\n" +
               "            background: white;\n" +
               "            border-radius: 10px;\n" +
               "            box-shadow: 0 2px 15px rgba(0,0,0,0.1);\n" +
               "        }\n" +
               "        \n" +
               "        .header h1 {\n" +
               "            color: #2c3e50;\n" +
               "            font-size: 28px;\n" +
               "            margin-bottom: 10px;\n" +
               "        }\n" +
               "        \n" +
               "        .header p {\n" +
               "            color: #7f8c8d;\n" +
               "            font-size: 16px;\n" +
               "        }\n" +
               "        \n" +
               "        .llm-info {\n" +
               "            background: #e8f4f8;\n" +
               "            border-left: 4px solid #3498db;\n" +
               "            padding: 15px;\n" +
               "            margin: 20px 0;\n" +
               "            border-radius: 5px;\n" +
               "        }\n" +
               "        \n" +
               "        .main-content {\n" +
               "            display: grid;\n" +
               "            grid-template-columns: 1fr 1fr;\n" +
               "            gap: 30px;\n" +
               "            min-height: 600px;\n" +
               "        }\n" +
               "        \n" +
               "        .input-panel {\n" +
               "            background: white;\n" +
               "            padding: 30px;\n" +
               "            border-radius: 10px;\n" +
               "            box-shadow: 0 2px 15px rgba(0,0,0,0.1);\n" +
               "            height: fit-content;\n" +
               "        }\n" +
               "        \n" +
               "        .form-group {\n" +
               "            margin-bottom: 25px;\n" +
               "        }\n" +
               "        \n" +
               "        .form-group label {\n" +
               "            display: block;\n" +
               "            margin-bottom: 8px;\n" +
               "            font-weight: 600;\n" +
               "            color: #2c3e50;\n" +
               "            font-size: 14px;\n" +
               "        }\n" +
               "        \n" +
               "        .prompt-textarea {\n" +
               "            width: 100%;\n" +
               "            padding: 15px;\n" +
               "            border: 2px solid #e1e8ed;\n" +
               "            border-radius: 8px;\n" +
               "            font-size: 14px;\n" +
               "            resize: vertical;\n" +
               "            min-height: 120px;\n" +
               "            transition: all 0.3s ease;\n" +
               "            font-family: inherit;\n" +
               "        }\n" +
               "        \n" +
               "        .prompt-textarea:focus {\n" +
               "            outline: none;\n" +
               "            border-color: #3498db;\n" +
               "            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);\n" +
               "        }\n" +
               "        \n" +
               "        .generate-button {\n" +
               "            background: linear-gradient(135deg, #3498db, #2980b9);\n" +
               "            color: white;\n" +
               "            border: none;\n" +
               "            padding: 15px 30px;\n" +
               "            border-radius: 8px;\n" +
               "            font-size: 16px;\n" +
               "            font-weight: 600;\n" +
               "            cursor: pointer;\n" +
               "            transition: all 0.3s ease;\n" +
               "            width: 100%;\n" +
               "            position: relative;\n" +
               "            overflow: hidden;\n" +
               "        }\n" +
               "        \n" +
               "        .generate-button:hover {\n" +
               "            transform: translateY(-2px);\n" +
               "            box-shadow: 0 5px 20px rgba(52, 152, 219, 0.4);\n" +
               "        }\n" +
               "        \n" +
               "        .generate-button:active {\n" +
               "            transform: translateY(0);\n" +
               "        }\n" +
               "        \n" +
               "        .generate-button:disabled {\n" +
               "            background: #bdc3c7;\n" +
               "            cursor: not-allowed;\n" +
               "            transform: none;\n" +
               "            box-shadow: none;\n" +
               "        }\n" +
               "        \n" +
               "        .status-message {\n" +
               "            padding: 15px;\n" +
               "            border-radius: 8px;\n" +
               "            margin-top: 15px;\n" +
               "            display: none;\n" +
               "            font-weight: 500;\n" +
               "        }\n" +
               "        \n" +
               "        .status-message.success {\n" +
               "            background: #d5f4e6;\n" +
               "            color: #27ae60;\n" +
               "            border-left: 4px solid #27ae60;\n" +
               "        }\n" +
               "        \n" +
               "        .status-message.error {\n" +
               "            background: #fdf2f2;\n" +
               "            color: #e74c3c;\n" +
               "            border-left: 4px solid #e74c3c;\n" +
               "        }\n" +
               "        \n" +
               "        .status-message.info {\n" +
               "            background: #e8f4f8;\n" +
               "            color: #3498db;\n" +
               "            border-left: 4px solid #3498db;\n" +
               "        }\n" +
               "        \n" +
               "        .preview-panel {\n" +
               "            background: white;\n" +
               "            border-radius: 10px;\n" +
               "            box-shadow: 0 2px 15px rgba(0,0,0,0.1);\n" +
               "            overflow: hidden;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "        }\n" +
               "        \n" +
               "        .preview-header {\n" +
               "            background: #34495e;\n" +
               "            color: white;\n" +
               "            padding: 20px;\n" +
               "            display: flex;\n" +
               "            justify-content: space-between;\n" +
               "            align-items: center;\n" +
               "        }\n" +
               "        \n" +
               "        .preview-header h3 {\n" +
               "            margin: 0;\n" +
               "            font-size: 18px;\n" +
               "            font-weight: 600;\n" +
               "        }\n" +
               "        \n" +
               "        .download-button {\n" +
               "            background: #27ae60;\n" +
               "            color: white;\n" +
               "            border: none;\n" +
               "            padding: 10px 20px;\n" +
               "            border-radius: 6px;\n" +
               "            font-size: 14px;\n" +
               "            font-weight: 500;\n" +
               "            cursor: pointer;\n" +
               "            transition: background-color 0.3s ease;\n" +
               "            display: none;\n" +
               "        }\n" +
               "        \n" +
               "        .download-button:hover {\n" +
               "            background: #219a52;\n" +
               "        }\n" +
               "        \n" +
               "        .preview-container {\n" +
               "            flex: 1;\n" +
               "            min-height: 500px;\n" +
               "            position: relative;\n" +
               "            overflow: hidden;\n" +
               "        }\n" +
               "        \n" +
               "        .preview-placeholder {\n" +
               "            display: flex;\n" +
               "            align-items: center;\n" +
               "            justify-content: center;\n" +
               "            height: 100%;\n" +
               "            color: #95a5a6;\n" +
               "            font-size: 16px;\n" +
               "            text-align: center;\n" +
               "            padding: 40px;\n" +
               "        }\n" +
               "        \n" +
               "        .preview-content {\n" +
               "            width: 100%;\n" +
               "            height: 100%;\n" +
               "            border: none;\n" +
               "            padding: 20px;\n" +
               "            overflow-y: auto;\n" +
               "        }\n" +
               "        \n" +
               "        .loading-spinner {\n" +
               "            display: none;\n" +
               "            margin-left: 10px;\n" +
               "        }\n" +
               "        \n" +
               "        @keyframes spin {\n" +
               "            0% { transform: rotate(0deg); }\n" +
               "            100% { transform: rotate(360deg); }\n" +
               "        }\n" +
               "        \n" +
               "        .spinning {\n" +
               "            animation: spin 1s linear infinite;\n" +
               "        }\n" +
               "        \n" +
               "        @media (max-width: 768px) {\n" +
               "            .main-content {\n" +
               "                grid-template-columns: 1fr;\n" +
               "            }\n" +
               "            \n" +
               "            .container {\n" +
               "                padding: 10px;\n" +
               "            }\n" +
               "            \n" +
               "            .header h1 {\n" +
               "                font-size: 24px;\n" +
               "            }\n" +
               "            \n" +
               "            .input-panel, .preview-panel {\n" +
               "                margin-bottom: 20px;\n" +
               "            }\n" +
               "        }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <div class=\"header\">\n" +
               "            <h1>🚀 SURGE AEM LLM Component Generator</h1>\n" +
               "            <p>Generate AEM component files with Local LLM assistance - Enter a prompt and get instant results</p>\n" +
               "        </div>\n" +
               "        \n" +
               "        <div class=\"llm-info\">\n" +
               "            <strong>🔧 Local LLM Integration:</strong> This tool uses your local LLM (Ollama/LocalAI) to generate AEM components. No external API calls or rate limits!\n" +
               "        </div>\n" +
               "        \n" +
               "        <div class=\"main-content\">\n" +
               "            <div class=\"input-panel\">\n" +
               "                <div class=\"form-group\">\n" +
               "                    <label for=\"prompt-input\">Component Prompt</label>\n" +
               "                    <textarea id=\"prompt-input\" \n" +
               "                             class=\"prompt-textarea\"\n" +
               "                             placeholder=\"Enter your prompt here...&#10;&#10;Examples:&#10;• Create a product card component with image, title, description and CTA button&#10;• Build a hero banner with image and CTA button&#10;• Make a card component with image, title, and text\"\n" +
               "                             rows=\"6\"></textarea>\n" +
               "                </div>\n" +
               "                \n" +
               "                <div class=\"form-group\">\n" +
               "                    <button id=\"generate-btn\" class=\"generate-button\">\n" +
               "                        <span class=\"button-text\">Generate Component</span>\n" +
               "                        <span class=\"loading-spinner\">🔄</span>\n" +
               "                    </button>\n" +
               "                </div>\n" +
               "                \n" +
               "                <div id=\"status-message\" class=\"status-message\"></div>\n" +
               "            </div>\n" +
               "            \n" +
               "            <div class=\"preview-panel\">\n" +
               "                <div class=\"preview-header\">\n" +
               "                    <h3>Live Preview</h3>\n" +
               "                    <button id=\"download-btn\" class=\"download-button\">\n" +
               "                        📥 Download ZIP\n" +
               "                    </button>\n" +
               "                </div>\n" +
               "                \n" +
               "                <div id=\"preview-container\" class=\"preview-container\">\n" +
               "                    <div class=\"preview-placeholder\">\n" +
               "                        <div>\n" +
               "                            <p>💡 Enter a prompt and click \\\"Generate Component\\\" to see the live preview here</p>\n" +
               "                            <p style=\"margin-top: 10px; font-size: 14px; color: #bdc3c7;\">\n" +
               "                                Your generated component will appear as an interactive preview with sample data\n" +
               "                            </p>\n" +
               "                        </div>\n" +
               "                    </div>\n" +
               "                </div>\n" +
               "            </div>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "\n" +
               "    <script>\n" +
               "        document.addEventListener('DOMContentLoaded', function() {\n" +
               "            const promptInput = document.getElementById('prompt-input');\n" +
               "            const generateBtn = document.getElementById('generate-btn');\n" +
               "            const downloadBtn = document.getElementById('download-btn');\n" +
               "            const statusMessage = document.getElementById('status-message');\n" +
               "            const previewContainer = document.getElementById('preview-container');\n" +
               "            const buttonText = generateBtn.querySelector('.button-text');\n" +
               "            const loadingSpinner = generateBtn.querySelector('.loading-spinner');\n" +
               "            \n" +
               "            let currentDownloadUrl = null;\n" +
               "            \n" +
               "            generateBtn.addEventListener('click', function() {\n" +
               "                const prompt = promptInput.value.trim();\n" +
               "                \n" +
               "                if (!prompt) {\n" +
               "                    showStatusMessage('Please enter a prompt before generating.', 'error');\n" +
               "                    promptInput.focus();\n" +
               "                    return;\n" +
               "                }\n" +
               "                \n" +
               "                // Show loading state\n" +
               "                setLoadingState(true);\n" +
               "                showStatusMessage('🔄 Generating component files using Local LLM... This may take a few moments.', 'info');\n" +
               "                hideDownloadButton();\n" +
               "                \n" +
               "                // First get CSRF token, then make the request\n" +
               "                fetch('/libs/granite/csrf/token.json', {\n" +
               "                    method: 'GET',\n" +
               "                    credentials: 'include'\n" +
               "                })\n" +
               "                .then(response => response.json())\n" +
               "                .then(tokenData => {\n" +
               "                    // Make POST request to the servlet with CSRF token\n" +
               "                    return fetch('/bin/aem-llm/generate', {\n" +
               "                        method: 'POST',\n" +
               "                        headers: {\n" +
               "                            'Content-Type': 'application/x-www-form-urlencoded',\n" +
               "                            'CSRF-Token': tokenData.token\n" +
               "                        },\n" +
               "                        credentials: 'include',\n" +
               "                        body: 'prompt=' + encodeURIComponent(prompt)\n" +
               "                    });\n" +
               "                })\n" +
               "                .then(response => {\n" +
               "                    if (!response.ok) {\n" +
               "                        throw new Error('HTTP error! status: ' + response.status);\n" +
               "                    }\n" +
               "                    return response.json();\n" +
               "                })\n" +
               "                .then(data => {\n" +
               "                    if (data.status === 'success') {\n" +
               "                        showStatusMessage('✅ Component generated successfully! ' + data.filesGenerated + ' files created.', 'success');\n" +
               "                        \n" +
               "                        // Store download URL\n" +
               "                        currentDownloadUrl = data.downloadUrl;\n" +
               "                        \n" +
               "                        // Show download button\n" +
               "                        showDownloadButton();\n" +
               "                        \n" +
               "                        // Load preview HTML from LLM response\n" +
               "                        if (data.previewHtml) {\n" +
               "                            loadPreviewHtml(data.previewHtml);\n" +
               "                        } else if (data.previewUrl) {\n" +
               "                            loadPreview(data.previewUrl);\n" +
               "                        } else {\n" +
               "                            showPreviewPlaceholder('Preview not available for this component.');\n" +
               "                        }\n" +
               "                    } else {\n" +
               "                        showStatusMessage('❌ Error: ' + (data.message || 'Unknown error occurred'), 'error');\n" +
               "                        showPreviewPlaceholder('Generation failed. Please try again.');\n" +
               "                    }\n" +
               "                })\n" +
               "                .catch(error => {\n" +
               "                    console.error('Error:', error);\n" +
               "                    if (error.message && error.message.includes('CSRF')) {\n" +
               "                        showStatusMessage('❌ Error: CSRF token issue. Please refresh the page and try again.', 'error');\n" +
               "                    } else {\n" +
               "                        showStatusMessage('❌ Error: Failed to generate component. Please check your Local LLM connection and try again.', 'error');\n" +
               "                    }\n" +
               "                    showPreviewPlaceholder('Generation failed. Please try again.');\n" +
               "                })\n" +
               "                .finally(() => {\n" +
               "                    setLoadingState(false);\n" +
               "                });\n" +
               "            });\n" +
               "            \n" +
               "            downloadBtn.addEventListener('click', function() {\n" +
               "                if (currentDownloadUrl) {\n" +
               "                    window.open(currentDownloadUrl, '_blank');\n" +
               "                }\n" +
               "            });\n" +
               "            \n" +
               "            function setLoadingState(isLoading) {\n" +
               "                generateBtn.disabled = isLoading;\n" +
               "                buttonText.style.display = isLoading ? 'none' : 'inline';\n" +
               "                loadingSpinner.style.display = isLoading ? 'inline' : 'none';\n" +
               "                \n" +
               "                if (isLoading) {\n" +
               "                    loadingSpinner.classList.add('spinning');\n" +
               "                } else {\n" +
               "                    loadingSpinner.classList.remove('spinning');\n" +
               "                }\n" +
               "            }\n" +
               "            \n" +
               "            function showStatusMessage(message, type) {\n" +
               "                statusMessage.textContent = message;\n" +
               "                statusMessage.className = 'status-message ' + type;\n" +
               "                statusMessage.style.display = 'block';\n" +
               "                \n" +
               "                // Auto-hide success messages after 5 seconds\n" +
               "                if (type === 'success') {\n" +
               "                    setTimeout(() => {\n" +
               "                        statusMessage.style.display = 'none';\n" +
               "                    }, 5000);\n" +
               "                }\n" +
               "            }\n" +
               "            \n" +
               "            function showDownloadButton() {\n" +
               "                downloadBtn.style.display = 'inline-block';\n" +
               "            }\n" +
               "            \n" +
               "            function hideDownloadButton() {\n" +
               "                downloadBtn.style.display = 'none';\n" +
               "            }\n" +
               "            \n" +
               "            function loadPreview(previewUrl) {\n" +
               "                previewContainer.innerHTML = '<iframe src=\"' + previewUrl + '\" class=\"preview-content\" sandbox=\"allow-same-origin allow-scripts\"></iframe>';\n" +
               "            }\n" +
               "            \n" +
               "            function loadPreviewHtml(htmlContent) {\n" +
               "                previewContainer.innerHTML = '<div class=\"preview-content\">' + htmlContent + '</div>';\n" +
               "            }\n" +
               "            \n" +
               "            function showPreviewPlaceholder(message) {\n" +
               "                previewContainer.innerHTML = '<div class=\"preview-placeholder\"><div><p>' + message + '</p></div></div>';\n" +
               "            }\n" +
               "            \n" +
               "            // Allow Enter key to submit (Ctrl+Enter for new line)\n" +
               "            promptInput.addEventListener('keydown', function(e) {\n" +
               "                if (e.key === 'Enter' && !e.ctrlKey && !e.shiftKey) {\n" +
               "                    e.preventDefault();\n" +
               "                    generateBtn.click();\n" +
               "                }\n" +
               "            });\n" +
               "        });\n" +
               "    </script>\n" +
               "</body>\n" +
               "</html>";
    }
} 