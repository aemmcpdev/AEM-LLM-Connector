@echo off
echo SURGE AEM LLM Connector - Bundle Deployment
echo =============================================

echo Deploying bundle to AEM at localhost:4502...

curl -u admin:admin -F "action=install" -F "bundlestartlevel=20" -F "bundlefile=@core/target/surge-aem-llm-connector.core-1.0.0-SNAPSHOT.jar" http://localhost:4502/system/console/bundles

echo.
echo Deployment completed!
echo.
echo To verify deployment, check:
echo http://localhost:4502/system/console/bundles
echo.
echo To test the servlet, visit:
echo http://localhost:4502/bin/surge/llm/generate?type=text^&format=json
echo.
pause 