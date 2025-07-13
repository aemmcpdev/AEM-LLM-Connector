# Deploy SURGE AEM LLM Connector Bundle to AEM
$bundlePath = "core/target/surge-aem-llm-connector.core-1.0.0-SNAPSHOT.jar"
$aemUrl = "http://localhost:4502/system/console/bundles"
$username = "admin"
$password = "admin"

# Create credential object
$securePassword = ConvertTo-SecureString $password -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential ($username, $securePassword)

# Check if bundle exists
if (-not (Test-Path $bundlePath)) {
    Write-Host "Bundle not found at: $bundlePath" -ForegroundColor Red
    exit 1
}

Write-Host "Deploying bundle to AEM..." -ForegroundColor Green

try {
    # Upload bundle to AEM
    $response = Invoke-WebRequest -Uri $aemUrl -Method Post -Credential $credential -InFile $bundlePath -ContentType "multipart/form-data" -Body @{
        action = "install"
        bundlestartlevel = "20"
        bundlefile = Get-Item $bundlePath
    }
    
    if ($response.StatusCode -eq 200) {
        Write-Host "Bundle deployed successfully!" -ForegroundColor Green
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    } else {
        Write-Host "Deployment failed with status: $($response.StatusCode)" -ForegroundColor Red
        Write-Host $response.Content
    }
} catch {
    Write-Host "Error deploying bundle: $($_.Exception.Message)" -ForegroundColor Red
} 