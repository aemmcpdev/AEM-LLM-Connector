# 🚀 Local LLM Setup Guide for SURGE AEM LLM Connector

This guide will help you set up a local LLM (Ollama or LocalAI) to work with the SURGE AEM LLM Connector.

## 📋 Prerequisites

- **Java 17+** installed
- **AEM 6.5+** running locally
- **Docker** (optional, for easier setup)
- **Git** for cloning the repository

## 🔧 Option 1: Ollama Setup (Recommended)

### Step 1: Install Ollama

#### On macOS:
```bash
curl -fsSL https://ollama.ai/install.sh | sh
```

#### On Linux:
```bash
curl -fsSL https://ollama.ai/install.sh | sh
```

#### On Windows:
Download from [https://ollama.ai/download](https://ollama.ai/download)

### Step 2: Start Ollama Service
```bash
ollama serve
```

### Step 3: Pull a Model
```bash
# For general purpose (recommended)
ollama pull llama3.1

# For code generation
ollama pull codellama

# For smaller, faster model
ollama pull mistral
```

### Step 4: Test Ollama
```bash
ollama run llama3.1 "Hello, how are you?"
```

## 🔧 Option 2: LocalAI Setup

### Step 1: Install LocalAI

#### Using Docker (Recommended):
```bash
docker run -d --name local-ai \
  -p 8080:8080 \
  -v local-ai:/app/backend/models \
  localai/localai:latest
```

#### Manual Installation:
Follow instructions at [https://github.com/go-skynet/LocalAI](https://github.com/go-skynet/LocalAI)

### Step 2: Download Models
```bash
# Download a model
wget https://huggingface.co/TheBloke/Llama-2-7B-Chat-GGML/resolve/main/llama-2-7b-chat.ggmlv3.q4_0.bin
```

### Step 3: Configure LocalAI
Create `config.yaml`:
```yaml
models:
  - name: llama3.1
    backend: llama
    parameters:
      model: llama-2-7b-chat.ggmlv3.q4_0.bin
```

## 🔧 AEM Configuration

### Step 1: Deploy the Connector
```bash
# Build and deploy
mvn clean install -PautoInstallPackage
```

### Step 2: Configure OSGi Settings

1. Open AEM OSGi Console: `http://localhost:4502/system/console/configMgr`
2. Search for "SURGE AEM LLM Connector - Local LLM Configuration"
3. Configure the following settings:

#### For Ollama:
- **LLM Provider**: `ollama`
- **LLM API URL**: `http://localhost:11434/api/generate`
- **LLM Model**: `llama3.1` (or your chosen model)
- **Service Enabled**: `true`
- **Timeout (seconds)**: `60`
- **Retry Attempts**: `3`

#### For LocalAI:
- **LLM Provider**: `localai`
- **LLM API URL**: `http://localhost:8080/v1/chat/completions`
- **LLM Model**: `llama3.1` (or your model name)
- **Service Enabled**: `true`
- **Timeout (seconds)**: `60`
- **Retry Attempts**: `3`

### Step 3: Test the Connection

1. Go to: `http://localhost:4502/bin/aem-llm/generate?mode=test`
2. You should see a success response

## 🧪 Testing the Setup

### Test 1: Basic Component Generation
```bash
curl -X POST "http://localhost:4502/bin/aem-llm/generate" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "prompt=Create a simple text component with title and description"
```

### Test 2: UI Interface
1. Open: `http://localhost:4502/bin/aem-llm/ui`
2. Enter a prompt like: "Create a product card component with image, title, description and CTA button"
3. Click "Generate Component"
4. You should see a live preview and download option

## 🔍 Troubleshooting

### Common Issues:

#### 1. "Connection refused" error
- **Solution**: Make sure Ollama/LocalAI is running
- **Check**: `curl http://localhost:11434/api/generate` (Ollama) or `curl http://localhost:8080/v1/models` (LocalAI)

#### 2. "Model not found" error
- **Solution**: Pull the model first
- **For Ollama**: `ollama pull llama3.1`
- **For LocalAI**: Download the model file and update config

#### 3. "Timeout" error
- **Solution**: Increase timeout in OSGi configuration
- **Check**: Model size and system resources

#### 4. "Empty response" error
- **Solution**: Check model compatibility
- **Try**: Different model or adjust system prompt

### Performance Tips:

1. **Use smaller models** for faster responses:
   - `mistral` (7B parameters)
   - `llama3.1:1b` (1B parameters)

2. **Adjust timeout** based on your model:
   - Small models: 30 seconds
   - Large models: 120 seconds

3. **Monitor system resources**:
   - CPU usage during generation
   - Memory usage (models can be large)

## 📊 Model Recommendations

### For AEM Component Generation:

#### Best Overall:
- **llama3.1** - Good balance of speed and quality
- **codellama** - Excellent for code generation

#### For Speed:
- **mistral** - Fast and efficient
- **llama3.1:1b** - Very fast, smaller model

#### For Quality:
- **llama3.1:70b** - Highest quality (requires more resources)
- **codellama:34b** - Best for complex code generation

## 🔐 Security Considerations

1. **Local Only**: All processing happens on your machine
2. **No External Calls**: No data sent to external APIs
3. **Model Safety**: Use trusted models from official sources
4. **Network Isolation**: Keep LLM service on internal network

## 📈 Monitoring

### Check Service Status:
```bash
# Ollama
curl http://localhost:11434/api/tags

# LocalAI
curl http://localhost:8080/v1/models
```

### Monitor Logs:
- AEM logs: `http://localhost:4502/system/console/slinglog`
- Ollama logs: Check terminal where `ollama serve` is running
- LocalAI logs: Check Docker logs or service logs

## 🎯 Next Steps

Once your local LLM is working:

1. **Test different prompts** to see what works best
2. **Experiment with different models** for various use cases
3. **Customize the system prompt** in OSGi configuration
4. **Set up monitoring** for production use
5. **Consider model fine-tuning** for specific AEM patterns

---

**Need Help?** Contact support@surgesoftware.com or check the project documentation. 