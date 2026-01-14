# Test Email Sending - Direct Test (No Frontend Required)
# This script tests the email endpoint directly

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Email Sending" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$emailTo = "user1@gmail.com"  # Change this to your email
$schedulerUrl = "http://localhost:8083/api/v1/scheduler/test/email"

Write-Host "Sending test email to: $emailTo" -ForegroundColor Yellow
Write-Host "Endpoint: $schedulerUrl" -ForegroundColor Yellow
Write-Host ""

try {
    $params = @{
        to = $emailTo
    }
    
    $queryString = ($params.GetEnumerator() | ForEach-Object { "$($_.Key)=$([System.Web.HttpUtility]::UrlEncode($_.Value))" }) -join "&"
    $fullUrl = "$schedulerUrl" + "?" + $queryString
    
    $response = Invoke-RestMethod -Uri $fullUrl -Method POST -ErrorAction Stop
    
    Write-Host "SUCCESS!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 5
    Write-Host ""
    Write-Host "Check your inbox: $emailTo" -ForegroundColor Yellow
    Write-Host "Also check spam folder!" -ForegroundColor Yellow
    
} catch {
    Write-Host "ERROR!" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "1. Make sure scheduler-service is running on port 8083" -ForegroundColor Yellow
    Write-Host "2. Check application.yml for mail configuration" -ForegroundColor Yellow
    Write-Host "3. Verify Gmail app password is correct" -ForegroundColor Yellow
    Write-Host "4. Check logs for detailed error messages" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
