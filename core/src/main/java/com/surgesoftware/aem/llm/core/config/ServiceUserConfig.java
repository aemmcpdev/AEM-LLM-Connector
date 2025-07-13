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
package com.surgesoftware.aem.llm.core.config;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.util.HashMap;
import java.util.Map;

/**
 * Service User Configuration for SURGE AEM LLM Connector
 * 
 * This component creates and manages a service user for file operations.
 * 
 * @author SURGE Software Solutions Private Limited
 */
@Component(service = ServiceUserConfig.class, immediate = true,
    property = {
        "service.description=SURGE AEM LLM Connector - Service User Configuration",
        "service.vendor=SURGE Software Solutions Private Limited"
    })
public class ServiceUserConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceUserConfig.class);
    
    private static final String SERVICE_USER_NAME = "surge-aem-llm-service";
    private static final String SUBSERVICE_NAME = "surge-aem-llm-service";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Activate
    protected void activate() {
        LOG.info("SURGE AEM LLM Connector: Service User Configuration activated");
        LOG.info("Service user configuration ready. Ensure service user mapping is set for subservice '{}'.", SUBSERVICE_NAME);
    }

    /**
     * Get a resource resolver for the service user (using subservice mapping)
     */
    public ResourceResolver getServiceResourceResolver() throws Exception {
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);
        try {
            return resourceResolverFactory.getServiceResourceResolver(authInfo);
        } catch (Exception e) {
            LOG.error("Failed to get service resource resolver for subservice: {}", SUBSERVICE_NAME, e);
            throw new Exception("Unable to obtain resource resolver via subservice mapping", e);
        }
    }
} 