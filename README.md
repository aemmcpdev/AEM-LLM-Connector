# SURGE AEM LLM Connector

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](#)
[![Version](https://img.shields.io/badge/Version-1.0.0--SNAPSHOT-orange.svg)](#)

## Overview

The **SURGE AEM LLM Connector** is an innovative solution developed by **SURGE Software Solutions Private Limited** that bridges the gap between Adobe Experience Manager (AEM) developers and AI-powered development tools. This connector provides a seamless interface to generate AEM component files using OpenAI's ChatGPT-4 model.

## Features

- 🤖 **AI-Powered Component Generation**: Generate AEM components using OpenAI ChatGPT-4
- 🔧 **Text Component Support**: Initial phase focuses on text component generation
- 📦 **Maven Integration**: Standard Maven project structure for easy deployment
- 🌐 **RESTful API**: Simple URL-based interface for developers
- 📁 **Multiple Output Formats**: Support for JSON and ZIP file downloads
- 🏢 **SURGE Branding**: Professional branding throughout the solution

## Architecture

The connector consists of:

- **Core OSGi Bundle**: Contains servlets and OpenAI integration services
- **UI Apps Package**: AEM application content
- **UI Content Package**: AEM content structure
- **All Package**: Complete deployment package

## Getting Started

### Prerequisites

- Java 11 or higher
- Apache Maven 3.6+
- Adobe Experience Manager 6.5+
- OpenAI API Key

### Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd surge-aem-llm-connector
   ```

2. **Configure OpenAI API Key**:
   - Open `core/src/main/java/com/surgesoftware/aem/llm/core/services/impl/OpenAIServiceImpl.java`
   - Replace `your-openai-api-key-here` with your actual OpenAI API key

3. **Build the project**:
   ```bash
   mvn clean install
   ```

4. **Deploy to AEM**:
   ```bash
   mvn clean install -PautoInstallPackage
   ```

### Usage

Once deployed, the connector provides a REST endpoint at:

```
http://localhost:4502/content/aem-llm/generate
```

#### Parameters

- `type`: Component type (default: "text")
- `requirements`: Additional requirements or specifications
- `format`: Output format ("json" or "zip", default: "zip")

#### Examples

1. **Generate a text component (ZIP format)**:
   ```
   http://localhost:4502/content/aem-llm/generate?type=text
   ```

2. **Generate with requirements (JSON format)**:
   ```
   http://localhost:4502/content/aem-llm/generate?type=text&requirements=Add%20rich%20text%20support&format=json
   ```

3. **Generate with custom specifications**:
   ```
   http://localhost:4502/content/aem-llm/generate?type=text&requirements=Include%20character%20count%20and%20validation
   ```

## Generated Files

The connector generates the following files for each component:

- **dialog.xml**: Component dialog configuration
- **[component].html**: HTL template file
- **[component].js**: JavaScript/Use-API logic
- **.content.xml**: Component metadata

## Project Structure

```
surge-aem-llm-connector/
├── core/                           # OSGi bundle with servlets and services
│   ├── src/main/java/
│   │   └── com/surgesoftware/aem/llm/core/
│   │       ├── services/           # OpenAI service interfaces
│   │       │   └── impl/          # Service implementations
│   │       └── servlets/          # HTTP servlets
│   └── pom.xml
├── ui.apps/                        # AEM application content
│   ├── src/main/content/
│   │   └── META-INF/vault/
│   └── pom.xml
├── ui.content/                     # AEM content structure
│   ├── src/main/content/
│   │   └── META-INF/vault/
│   └── pom.xml
├── all/                           # Complete deployment package
│   ├── src/main/content/
│   │   └── META-INF/vault/
│   └── pom.xml
└── pom.xml                        # Root Maven configuration
```

## Development

### Building

```bash
# Clean and build all modules
mvn clean install

# Build and deploy to local AEM instance
mvn clean install -PautoInstallPackage

# Build without tests
mvn clean install -DskipTests
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=OpenAIServiceImplTest
```

## Configuration

The connector supports the following configurations:

- **OpenAI API Key**: Set in the service implementation
- **AEM Server URL**: Configured in Maven profiles
- **Component Generation Parameters**: Customizable via service parameters

## Roadmap

### Phase 1 (Current)
- ✅ Text component generation
- ✅ Basic OpenAI integration
- ✅ ZIP/JSON output formats

### Phase 2 (Upcoming)
- 🔄 Header component generation
- 🔄 Title component generation
- 🔄 Banner component generation

### Phase 3 (Future)
- 🔄 Advanced component types
- 🔄 Custom styling options
- 🔄 Template variations

## Contributing

We welcome contributions to the SURGE AEM LLM Connector! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Support

For support and more information about SURGE Software Solutions:

- **Website**: [https://surgesoftware.com](https://surgesoftware.com)
- **Email**: support@surgesoftware.com
- **Documentation**: [Project Wiki](wiki-link)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## About SURGE Software Solutions

SURGE Software Solutions Private Limited is a leading technology company specializing in innovative software solutions for enterprise clients. Our expertise spans across various domains including:

- Enterprise Content Management
- AI-Powered Development Tools
- Cloud Solutions
- Digital Transformation

---

**© 2024 SURGE Software Solutions Private Limited. All rights reserved.**

*Generated by SURGE AEM LLM Connector - Bridging AEM Development with AI Innovation* 