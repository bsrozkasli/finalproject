# Airline Project - ACR Push Script (Final Version)
# Bu betik sağladığınız şifre ile ACR'ye otomatik login yapar ve tüm servisleri yükler.

$ACR_NAME = "airlineproject.azurecr.io"
$ACR_USER = "airlineproject"
$ACR_PASS = "your_acr_password_here"
$SERVICES = @("api-gateway", "flight-service", "notification-service", "scheduler-service", "ml-service", "frontend")

Write-Host "=== Dağıtım Listesi Hazırlanıyor ===" -ForegroundColor Green

# 1. ACR Login (Otomatik)
Write-Host "`n1. Azure Container Registry'ye Giriş Yapılıyor..." -ForegroundColor Yellow
echo $ACR_PASS | docker login $ACR_NAME -u $ACR_USER --password-stdin

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ ACR girişi başarısız oldu. Lütfen şifreyi kontrol edin." -ForegroundColor Red
    exit
}
Write-Host "✅ Başarıyla giriş yapıldı!" -ForegroundColor Green

# 2. Servisleri Derle (Maven)
Write-Host "`n2. Java Servisleri Paketleniyor (Lütfen bekleyin)..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven build başarısız!" -ForegroundColor Red
    exit
}

# 3. Build ve Push
Write-Host "`n3. Imajlar Hazırlanıyor ve Yükleniyor..." -ForegroundColor Yellow

foreach ($service in $SERVICES) {
    $imageTag = "${ACR_NAME}/${service}:demo"
    Write-Host "`n>> İşlenen Servis: $service" -ForegroundColor Cyan
    
    docker build -t $imageTag "./$service"
    if ($LASTEXITCODE -eq 0) {
        docker push $imageTag
        Write-Host "✅ $service tamamlandı." -ForegroundColor Green
    } else {
        Write-Host "❌ $service build hatası!" -ForegroundColor Red
    }
}

Write-Host "`n=== İŞLEM TAMAMLANDI! Tüm servisler bulutta. ===" -ForegroundColor Green
