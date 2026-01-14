# PowerShell Script for Testing Authentication

Write-Host "=== Authentication Test Script ===" -ForegroundColor Green

# Test 1: Public Endpoint (No Auth Required)
Write-Host "`n1. Testing Public Endpoint (Search Flights)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/flights/search?from=IST&to=JFK&date=2026-01-15" -UseBasicParsing
    Write-Host "✅ Public endpoint works: $($response.StatusCode)" -ForegroundColor Green
}
catch {
    Write-Host "❌ Public endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: API Key Authentication (Partner API)
Write-Host "`n2. Testing API Key Authentication (Partner API)..." -ForegroundColor Yellow
$apiKey = "sap_secret_key_99"
$body = @{
    userId      = "test-user-001"
    amount      = 1000
    description = "Test miles from partner"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/partners/miles" `
        -Method POST `
        -Headers @{"X-API-KEY" = $apiKey; "Content-Type" = "application/json" } `
        -Body $body `
        -UseBasicParsing
    Write-Host "✅ API Key auth works: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
}
catch {
    Write-Host "❌ API Key auth failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error Response: $responseBody" -ForegroundColor Red
    }
}

# Test 3: Admin Endpoint (Should require ADMIN role)
Write-Host "`n3. Testing Admin Endpoint (Requires ADMIN role)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/admin/flights" -UseBasicParsing
    Write-Host "⚠️ Admin endpoint accessible without auth (Development mode)" -ForegroundColor Yellow
}
catch {
    Write-Host "✅ Admin endpoint protected: $($_.Exception.Message)" -ForegroundColor Green
}

# Test 4: Auth0 Token Test (if configured)
Write-Host "`n4. Testing Auth0 Token..." -ForegroundColor Yellow
Write-Host "⚠️ Auth0 token test requires actual token" -ForegroundColor Yellow
Write-Host "To test with Auth0:" -ForegroundColor Cyan
Write-Host "  1. Get token from Auth0" -ForegroundColor Cyan
Write-Host "  2. Use: Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/bookings/my-bookings' -Headers @{'Authorization'='Bearer YOUR_TOKEN'}" -ForegroundColor Cyan

Write-Host "`n=== Test Complete ===" -ForegroundColor Green

