# Airline Project - ACR Push Script (Yerel Dağıtım)
# Bu betik, yerel makinenizden ACR'ye imajları manuel olarak yüklemek içindir.

# === AYARLAR ===
$ACR_NAME = "airlineproject.azurecr.io"
$ACR_USER = "airlineproject"
# Kullanım: Buraya ACR şifrenizi girin veya boş bırakıp script çalışırken login komutunu kendiniz çalıştırın.
$ACR_PASS = "your_acr_password_here" 

$SERVICES = @("api-gateway", "flight-service", "notification-service", "scheduler-service", "ml-service", "frontend")

Write-Host "=== Dağıtım Hazırlanıyor ===" -ForegroundColor Green
Write-Host "Not: GitHub Actions (CI/CD) otomatik dağıtım için önerilen yöntemdir." -ForegroundColor Cyan

# 1. ACR Login
if ($ACR_PASS -eq "your_acr_password_here") {
    Write-Host "`n1. Lütfen Azure CLI üzerinden giriş yapın: az acr login --name airlineproject" -ForegroundColor Yellow
} else {
    Write-Host "`n1. Azure Container Registry'ye Giriş Yapılıyor..." -ForegroundColor Yellow
    echo $ACR_PASS | docker login $ACR_NAME -u $ACR_USER --password-stdin
}

# 2. Servisleri Derle (Maven)
Write-Host "`n2. Java Servisleri Paketleniyor..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven build başarısız!" -ForegroundColor Red
    exit
}

# 3. Frontend Build (Vite)
Write-Host "`n3. Frontend Derleniyor..." -ForegroundColor Yellow
cd frontend
npm install
npm run build
cd ..

# 4. Build ve Push
Write-Host "`n4. Imajlar Hazırlanıyor ve Yükleniyor..." -ForegroundColor Yellow

foreach ($service in $SERVICES) {
    $imageTag = "${ACR_NAME}/${service}:latest"
    Write-Host "`n>> İşlenen Servis: $service" -ForegroundColor Cyan
    
    docker build -t $imageTag "./$service"
    if ($LASTEXITCODE -eq 0) {
        docker push $imageTag
        Write-Host "✅ $service başarıyla yüklendi." -ForegroundColor Green
    } else {
        Write-Host "❌ $service build hatası!" -ForegroundColor Red
    }
}

Write-Host "`n=== İŞLEM TAMAMLANDI! Tüm servisler güncellendi. ===" -ForegroundColor Green

