# Enhanced SURGE AEM LLM Connector - Usage Guide

## Overview

The enhanced SURGE AEM LLM Connector now provides a complete end-to-end flow for generating AEM component files using AI. It saves files to the repository, creates downloadable ZIP archives, and generates HTML previews.

## New Endpoint

**URL:** `/bin/aem-llm/generate`

## Required Configuration

1. **OpenAI Configuration**: Configure your OpenAI API key in OSGi Config Manager
   - Go to: `http://localhost:4502/system/console/configMgr`
   - Find: "SURGE AEM LLM Connector - OpenAI Configuration"
   - Set your API key and other parameters

2. **Service User Mapping** (Important!): You'll need to create a service user mapping for the `aem-llm-service`:
   ```
   # Add this to your service user mapping configuration
   com.surgesoftware.aem.surge-aem-llm-connector.core:aem-llm-service=aem-llm-service
   ```
   
   Or configure it via OSGi Config Manager:
   - Go to: `http://localhost:4502/system/console/configMgr`
   - Find: "Apache Sling Service User Mapper Service"
   - Add mapping: `com.surgesoftware.aem.surge-aem-llm-connector.core:aem-llm-service=aem-llm-service`

## API Usage

### Generate Component Files

Send a GET or POST request to `/bin/aem-llm/generate` with a `prompt` parameter:

```bash
# Example 1: Simple text component
curl "http://localhost:4502/bin/aem-llm/generate?prompt=Create a text component with title and description fields"

# Example 2: Image component
curl "http://localhost:4502/bin/aem-llm/generate?prompt=Create an image component with alt text, caption, and responsive sizing"

# Example 3: Complex component
curl -X POST http://localhost:4502/bin/aem-llm/generate \
  -d "prompt=Create a hero banner component with background image, title, subtitle, and call-to-action button"
```

### Response Format

The servlet returns a JSON response with:

```json
{
  "status": "success",
  "message": "Component files generated successfully",
  "timestamp": "20241213-143022-123",
  "prompt": "Create a text component...",
  "filesGenerated": 4,
  "downloadUrl": "/bin/aem-llm/download?file=downloads/20241213-143022-123.zip",
  "previewUrl": "/bin/aem-llm/preview?file=previews/20241213-143022-123.html",
  "savedPath": "/var/aem-llm/generated/20241213-143022-123",
  "generatedBy": "SURGE AEM LLM Connector"
}
```

## File Organization

The system organizes files in the AEM repository under `/var/aem-llm/`:

```
/var/aem-llm/
├── generated/
│   └── 20241213-143022-123/
│       ├── dialog.xml
│       ├── component.html
│       ├── component.js
│       └── .content.xml
├── downloads/
│   └── 20241213-143022-123.zip
└── previews/
    └── 20241213-143022-123.html
```

## Download and Preview URLs

### Download ZIP File
- **URL**: `/bin/aem-llm/download?file=downloads/[timestamp].zip`
- **Response**: ZIP file download containing all generated component files
- **Security**: Only allows downloads from the `downloads/` and `generated/` directories

### Preview HTML
- **URL**: `/bin/aem-llm/preview?file=previews/[timestamp].html`
- **Response**: Styled HTML page showing the component preview
- **Security**: Only allows previews from the `previews/` directory

## Test Mode

For testing the servlet without generating files:

```bash
curl "http://localhost:4502/bin/aem-llm/generate?mode=test"
```

Returns:
```json
{
  "status": "success", 
  "message": "SURGE AEM LLM Connector is working!", 
  "timestamp": 1702467022123
}
```

## Error Handling

The servlet provides detailed error responses:

### Missing Prompt
```json
{
  "error": "Missing prompt parameter",
  "status": "bad_request"
}
```

### Service Unavailable
```json
{
  "error": "Required services not available",
  "status": "service_unavailable"
}
```

### Generation Failed
```json
{
  "error": "Failed to generate component files",
  "status": "generation_failed"
}
```

## File Types Generated

The system typically generates these files:
- **dialog.xml**: Component dialog configuration
- **[component].html**: HTL template file
- **[component].js**: JavaScript logic
- **.content.xml**: Component metadata
- **[Component].java**: Java model (if applicable)

## Cleanup

The system includes automatic cleanup functionality that removes old files after a specified number of days. This can be triggered programmatically through the FileManagementService.

## Security Features

1. **Path Validation**: Prevents directory traversal attacks
2. **Restricted Access**: Only allows access to designated directories
3. **Service User**: Uses dedicated service user for repository operations
4. **Input Sanitization**: Escapes HTML and JSON content appropriately

## Integration with Frontend

You can easily integrate this with a web interface:

```javascript
// Example frontend integration
async function generateComponent(prompt) {
  const response = await fetch(`/bin/aem-llm/generate?prompt=${encodeURIComponent(prompt)}`);
  const result = await response.json();
  
  if (result.status === 'success') {
    // Show download and preview links
    showDownloadLink(result.downloadUrl);
    showPreviewLink(result.previewUrl);
  } else {
    // Handle error
    showError(result.error);
  }
}
```

## Troubleshooting

1. **404 Error**: Check that the bundle is deployed and active
2. **503 Service Unavailable**: Verify OpenAI configuration and service user mapping
3. **403 Access Denied**: Check file permissions and service user configuration
4. **Generation Failed**: Verify OpenAI API key and connectivity

## Support

For issues and questions:
- **Website**: https://surgesoftware.com
- **Email**: support@surgesoftware.com

---

© 2024 SURGE Software Solutions Private Limited. All rights reserved. 