# SURGE AEM LLM Connector

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](#)
[![Version](https://img.shields.io/badge/Version-1.0.0--SNAPSHOT-orange.svg)](#)

---

## 🚀 Direct Download & Installation (Adobe Software Distribution)

**AEM administrators can download the latest SURGE AEM LLM Connector package directly from Adobe Software Distribution.**

- No build or deployment steps required.
- Simply download the `.zip` package and install it via AEM Package Manager.
- All dependencies and configuration are included for immediate use.

> **Get the package:** [Adobe Software Distribution Portal](https://experience.adobe.com/#/downloads/content/software-distribution/en/aem.html)

---

## Overview

The **SURGE AEM LLM Connector** is an innovative solution developed by **SURGE Software Solutions Pvt Ltd** that bridges the gap between Adobe Experience Manager (AEM) developers and AI-powered development tools. This connector provides a seamless interface to generate AEM component files using OpenAI's ChatGPT-4 model.

---

**Author:** Muvva Venu Gopal Reddy  
**Company:** SURGE Software Solutions Pvt Ltd  
**Website:** [https://surgesoftware.co.in](https://surgesoftware.co.in)

---

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

## 🚧 Roadmap & Vision

This project is just the beginning. The goal is to empower AEM developers to shift from traditional coding to prompt-driven development, covering up to 80% of project development — all through natural language instructions.

### ✅ Current Features (MVP)

- 📦 **Local AEM package installation**
- 🧠 **Prompt-based component generation using LLM**  
  _e.g., "Create a banner component"_
- 🖥️ **Live preview of generated components in the same UI**
- 📁 **Auto-generated backend files + HTL structure**

### 🛣️ Coming Soon

1. **Conversational Prompt Refinement**
   - Iteratively refine the UI or behavior of the generated component by continuing the conversation
   - _Example:_
     - "Make the banner full width"
     - "Add CTA button with link"
     - "Change background to gradient blue"

2. **Multi-Model Support**
   - Choose between OpenAI, Claude, Gemini, or custom LLMs
   - Easily switch between models to compare performance and responses

3. **Sign-in Based LLM Access**
   - Users can sign in with Google/GitHub/email
   - API key is auto-configured — no manual setup required
   - Personalized model configuration and usage stats per user

4. **Expand Beyond Components**
   - Prompt-based creation for:
     - 🧱 Templates
     - 📄 Content Fragments
     - 🎞️ Experience Fragments
     - 🧩 Page policies and editable templates
   - The aim is to cover most AEM authoring and development use cases via LLMs

5. **Component Library + Save/Reuse**
   - Save components as reusable blueprints
   - Re-invoke previous components and apply changes via new prompts

6. **Real-Time Project Sync (Optional)**
   - Push generated components directly to GitHub via PR
   - Preview sandbox instances for QA testing

---

### 🎯 Vision: Prompt-First AEM Development

Shift from manual AEM development to prompt-first development — allowing architects, designers, and developers to build entire page structures, content models, and components with conversational instructions.

This tool aims to be your developer copilot inside AEM — not just generating code, but transforming how AEM projects are built, reviewed, and delivered.

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

SURGE Software Solutions Pvt Ltd is a leading technology company specializing in innovative software solutions for enterprise clients. Our expertise spans across various domains including:

- Enterprise Content Management
- AI-Powered Development Tools
- Cloud Solutions
- Digital Transformation

---

**© 2025 SURGE Software Solutions Pvt Ltd. All rights reserved.**

*Generated by SURGE AEM LLM Connector - Bridging AEM Development with AI Innovation* 