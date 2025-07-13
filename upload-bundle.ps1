# Upload bundle to AEM using PowerShell
$bundlePath = "target/surge-aem-llm-connector.core-1.0.0-SNAPSHOT.jar"
$aemUrl = "http://localhost:4502/system/console/bundles"

# Create credentials
$username = "admin"
$password = "admin"
$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(("{0}:{1}" -f $username, $password)))

Write-Host "Uploading bundle: $bundlePath" -ForegroundColor Green

try {
    # Read the bundle file
    $bundleBytes = [System.IO.File]::ReadAllBytes($bundlePath)
    
    # Create multipart form data
    $boundary = [System.Guid]::NewGuid().ToString()
    $LF = "`r`n"
    
    $bodyLines = @()
    $bodyLines += "--$boundary"
    $bodyLines += "Content-Disposition: form-data; name=`"action`""
    $bodyLines += ""
    $bodyLines += "install"
    $bodyLines += "--$boundary"
    $bodyLines += "Content-Disposition: form-data; name=`"bundlestartlevel`""
    $bodyLines += ""
    $bodyLines += "20"
    $bodyLines += "--$boundary"
    $bodyLines += "Content-Disposition: form-data; name=`"bundlefile`"; filename=`"surge-aem-llm-connector.core-1.0.0-SNAPSHOT.jar`""
    $bodyLines += "Content-Type: application/java-archive"
    $bodyLines += ""
    
    $bodyText = $bodyLines -join $LF
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($bodyText)
    
    # Combine body with file content
    $endBoundary = [System.Text.Encoding]::UTF8.GetBytes("$LF--$boundary--$LF")
    $fullBody = $bodyBytes + $bundleBytes + $endBoundary
    
    # Create headers
    $headers = @{
        'Authorization' = "Basic $base64AuthInfo"
        'Content-Type' = "multipart/form-data; boundary=$boundary"
    }
    
    # Upload bundle
    $response = Invoke-WebRequest -Uri $aemUrl -Method Post -Headers $headers -Body $fullBody
    
    if ($response.StatusCode -eq 200) {
        Write-Host "Bundle uploaded successfully!" -ForegroundColor Green
        Write-Host "Response: $($response.StatusCode)" -ForegroundColor Green
    } else {
        Write-Host "Upload failed with status: $($response.StatusCode)" -ForegroundColor Red
    }
} catch {
    Write-Host "Error uploading bundle: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Please upload manually via AEM Web Console:" -ForegroundColor Yellow
    Write-Host "1. Go to http://localhost:4502/system/console/bundles" -ForegroundColor Yellow
    Write-Host "2. Click 'Install/Update' button" -ForegroundColor Yellow
    Write-Host "3. Choose file: $bundlePath" -ForegroundColor Yellow
    Write-Host "4. Click 'Install or Update'" -ForegroundColor Yellow
} 