# PowerShell Script for Testing Email Functionality

Write-Host "=== Email Test Script ===" -ForegroundColor Green

# Check if services are running
Write-Host "`n1. Checking Service Status..." -ForegroundColor Yellow
$services = @("airline-rabbitmq", "airline-notification-service", "airline-scheduler-service")
foreach ($service in $services) {
    $status = docker ps --filter "name=$service" --format "{{.Status}}"
    if ($status) {
        Write-Host "✅ $service : $status" -ForegroundColor Green
    } else {
        Write-Host "❌ $service : Not running" -ForegroundColor Red
    }
}

# Test 1: Create Booking (triggers email via RabbitMQ)
Write-Host "`n2. Testing Booking Creation (triggers email)..." -ForegroundColor Yellow
$bookingBody = @{
    flightId = 1
    email = "test@example.com"
    paymentMethod = "CREDIT_CARD"
    passengers = @(
        @{
            firstName = "Test"
            lastName = "User"
            passportNo = "TEST123456"
            dateOfBirth = "1990-01-01"
            nationality = "TR"
        }
    )
} | ConvertTo-Json -Depth 10

try {
    Write-Host "Creating booking..." -ForegroundColor Cyan
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/bookings" `
        -Method POST `
        -Headers @{"Content-Type" = "application/json"} `
        -Body $bookingBody `
        -UseBasicParsing
    Write-Host "✅ Booking created: $($response.StatusCode)" -ForegroundColor Green
    $bookingData = $response.Content | ConvertFrom-Json
    Write-Host "Booking Ref: $($bookingData.ref)" -ForegroundColor Cyan
    
    Write-Host "`nWaiting 5 seconds for email processing..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    
    # Check RabbitMQ
    Write-Host "`n3. Checking RabbitMQ Queue..." -ForegroundColor Yellow
    Write-Host "Open: http://localhost:15672 (guest/guest)" -ForegroundColor Cyan
    Write-Host "Check queue: email.queue" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Booking creation failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error: $responseBody" -ForegroundColor Red
    }
}

# Test 2: Check Notification Service Logs
Write-Host "`n4. Checking Notification Service Logs..." -ForegroundColor Yellow
Write-Host "Run: docker logs airline-notification-service --tail 50" -ForegroundColor Cyan

# Test 3: Manual Email Test (if scheduler has test endpoint)
Write-Host "`n5. Testing Scheduler Service Email..." -ForegroundColor Yellow
Write-Host "Note: Email will be sent when:" -ForegroundColor Cyan
Write-Host "  - Flight completes (nightly job)" -ForegroundColor Cyan
Write-Host "  - New MilesSmiles member created" -ForegroundColor Cyan

# Check Gmail Configuration
Write-Host "`n6. Gmail SMTP Configuration Check..." -ForegroundColor Yellow
Write-Host "Required Environment Variables:" -ForegroundColor Cyan
Write-Host "  MAIL_USERNAME=your-email@gmail.com" -ForegroundColor Cyan
Write-Host "  MAIL_PASSWORD=your-16-char-app-password" -ForegroundColor Cyan
Write-Host "`nTo get Gmail App Password:" -ForegroundColor Cyan
Write-Host "  1. Enable 2-Step Verification" -ForegroundColor Cyan
Write-Host "  2. Go to App Passwords" -ForegroundColor Cyan
Write-Host "  3. Generate 16-character password" -ForegroundColor Cyan

# Check scheduler service logs for email errors
Write-Host "`n7. Checking Scheduler Service for Email Errors..." -ForegroundColor Yellow
$schedulerLogs = docker logs airline-scheduler-service --tail 20 2>&1
if ($schedulerLogs -match "email|smtp|mail") {
    Write-Host "Email-related logs found:" -ForegroundColor Cyan
    $schedulerLogs | Select-String -Pattern "email|smtp|mail" | ForEach-Object { Write-Host $_ -ForegroundColor Yellow }
} else {
    Write-Host "No email-related logs in last 20 lines" -ForegroundColor Yellow
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
Write-Host "`nNext Steps:" -ForegroundColor Cyan
Write-Host "  1. Check RabbitMQ Management UI: http://localhost:15672" -ForegroundColor Cyan
Write-Host "  2. Check notification service logs: docker logs airline-notification-service -f" -ForegroundColor Cyan
Write-Host "  3. Check scheduler service logs: docker logs airline-scheduler-service -f" -ForegroundColor Cyan

