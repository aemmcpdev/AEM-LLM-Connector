# SURGE AEM LLM Connector - Installation Guide

## Quick Installation (Adobe Software Distribution)

This package is designed for direct installation from Adobe Software Distribution. No build or deployment steps required.

### Prerequisites

- Adobe Experience Manager 6.5 or higher
- Admin access to AEM instance
- OpenAI API Key

### Installation Steps

1. **Download the Package**
   - Download the latest version from Adobe Software Distribution
   - Extract the ZIP file to your local machine

2. **Install the Core Bundle**
   - Navigate to AEM Package Manager: `http://localhost:4502/crx/packmgr/`
   - Upload and install: `surge-aem-llm-connector.core-1.0.0-SNAPSHOT.jar`
   - The bundle will be automatically installed and started

3. **Deploy Content Structure**
   - Copy the `ui.apps` and `ui.content` folders to your AEM instance
   - Or use the provided content package if available

4. **Configure OpenAI API Key**
   - Navigate to AEM OSGi Configuration: `http://localhost:4502/system/console/configMgr`
   - Find "SURGE AEM LLM Connector - OpenAI Service"
   - Enter your OpenAI API Key
   - Save the configuration

5. **Access the Connector**
   - Navigate to: `http://localhost:4502/content/aem-llm/generate`
   - The connector is now ready to use!

### Verification

1. **Check Bundle Status**
   - Go to: `http://localhost:4502/system/console/bundles`
   - Search for "surge-aem-llm-connector"
   - Ensure the bundle status is "Active"

2. **Test the Connector**
   - Visit: `http://localhost:4502/content/aem-llm/generate?type=text`
   - You should see the component generation interface

### Troubleshooting

**Bundle Not Starting:**
- Check AEM logs for errors
- Ensure all dependencies are available
- Verify Java version compatibility

**API Key Issues:**
- Verify your OpenAI API key is valid
- Check network connectivity to OpenAI
- Review OSGi configuration settings

**Content Not Loading:**
- Ensure content structure is properly deployed
- Check JCR permissions
- Verify service user mapping

### Support

For technical support:
- **Company:** SURGE Software Solutions Pvt Ltd
- **Website:** https://surgesoftware.co.in
- **Author:** Muvva Venu Gopal Reddy

---

**© 2025 SURGE Software Solutions Pvt Ltd. All rights reserved.** 