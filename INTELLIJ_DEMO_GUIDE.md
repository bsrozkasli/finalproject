# 🚀 IntelliJ IDEA Demo Kılavuzu

Bu dokümantasyon, Airline Ticketing System projesini IntelliJ IDEA'da nasıl çalıştıracağınızı ve demo için nasıl kullanacağınızı detaylı olarak açıklar.

---

## 📋 İçindekiler

1. [Proje Yapısı](#proje-yapısı)
2. [Gereksinimler](#gereksinimler)
3. [IntelliJ'de Proje Açma](#intellijde-proje-açma)
4. [Servisleri Çalıştırma](#servisleri-çalıştırma)
5. [Demo Senaryoları](#demo-senaryoları)
6. [Sorun Giderme](#sorun-giderme)

---

## 📁 Proje Yapısı

Proje, Maven multi-module yapısında organize edilmiştir:

```
airlineproject/
├── api-gateway/          # API Gateway servisi (Port: 8080)
├── flight-service/       # Uçuş ve rezervasyon servisi (Port: 8081)
├── scheduler-service/    # Zamanlanmış işler ve email servisi (Port: 8083)
├── notification-service/ # Bildirim servisi (Port: 8082)
├── frontend/            # React frontend uygulaması
├── ml-service/          # Machine Learning servisi (Python)
├── docker-compose.yml   # Docker Compose yapılandırması
└── pom.xml              # Parent POM dosyası
```

---

## ✅ Gereksinimler

### Yazılım Gereksinimleri

1. **IntelliJ IDEA** (Ultimate veya Community Edition)
   - Önerilen: 2023.2 veya üzeri
   - Lombok plugin yüklü olmalı

2. **Java Development Kit (JDK)**
   - Versiyon: **JDK 17** veya üzeri
   - IntelliJ'de Project SDK olarak ayarlanmalı

3. **Maven**
   - IntelliJ ile birlikte gelir veya kendi Maven'inizi kullanabilirsiniz
   - Versiyon: 3.8+

4. **Docker Desktop** (Infrastructure servisleri için)
   - PostgreSQL, RabbitMQ, Redis çalıştırmak için

5. **Node.js ve npm** (Frontend için)
   - Versiyon: Node.js 18+

### IntelliJ Plugin'leri

Aşağıdaki plugin'lerin yüklü olduğundan emin olun:

1. **Lombok** (Kritik!)
   - `File` → `Settings` → `Plugins` → "Lombok" ara ve yükle
   - `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - ✅ "Enable annotation processing" işaretli olmalı

2. **Spring Boot** (Önerilen)
   - Spring Boot desteği için

3. **Docker** (Önerilen)
   - Docker Compose dosyalarını yönetmek için

---

## 🔧 IntelliJ'de Proje Açma

### Adım 1: Projeyi Aç

1. IntelliJ IDEA'yı başlatın
2. `File` → `Open` seçin
3. Proje kök dizinini seçin: `C:\Users\basar\IdeaProjects\airlineproject`
4. "Open as Project" seçeneğini seçin

### Adım 2: Maven Projesini İçe Aktar

1. IntelliJ otomatik olarak Maven projesini tanıyacaktır
2. Sağ altta "Maven projects need to be imported" bildirimi görünürse:
   - "Import Maven Project" butonuna tıklayın
   - Veya `File` → `Reload Project from Disk`

### Adım 3: JDK'yı Ayarla

1. `File` → `Project Structure` (Ctrl+Alt+Shift+S)
2. `Project` sekmesinde:
   - **Project SDK**: JDK 17 seçin
   - **Project language level**: 17 seçin
3. `Modules` sekmesinde her modül için SDK'nın doğru olduğundan emin olun

### Adım 4: Annotation Processing'i Etkinleştir

1. `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
2. ✅ **"Enable annotation processing"** işaretleyin
3. **"Obtain processors from project classpath"** seçeneğini seçin
4. `Apply` ve `OK` tıklayın

### Adım 5: Maven Dependencies'i İndir

1. Sağ tarafta **Maven** tool window'u açın (yoksa: `View` → `Tool Windows` → `Maven`)
2. Kök projede (airline-ticketing-system) **"Reload All Maven Projects"** butonuna tıklayın (🔄 ikonu)
3. Tüm bağımlılıkların indirilmesini bekleyin

---

## 🚀 Servisleri Çalıştırma

### Ön Hazırlık: Infrastructure Servisleri

**Docker Compose ile Infrastructure'ı Başlat:**

1. Terminal açın (IntelliJ'de: `Alt+F12` veya `View` → `Tool Windows` → `Terminal`)
2. Proje kök dizininde:
   ```powershell
   docker-compose up -d postgres rabbitmq redis
   ```
3. Servislerin çalıştığını doğrulayın:
   ```powershell
   docker ps
   ```

**Beklenen Çıktı:**
- `airline-postgres` (Port: 5432)
- `airline-rabbitmq` (Port: 5672, Management: 15672)
- `airline-redis` (Port: 6379)

---

### Yöntem 1: IntelliJ Run Configuration ile (Önerilen)

#### Scheduler Service'i Çalıştırma

1. **Run Configuration Oluştur:**
   - `Run` → `Edit Configurations...`
   - Sol üstte **"+"** butonuna tıklayın
   - **"Spring Boot"** seçin

2. **Configuration Ayarları:**
   - **Name**: `Scheduler Service`
   - **Main class**: `com.airline.scheduler.SchedulerServiceApplication`
   - **Module**: `scheduler-service`
   - **Working directory**: `$MODULE_DIR$`
   - **Use classpath of module**: `scheduler-service`

3. **Environment Variables (Opsiyonel):**
   - `Environment variables` bölümüne tıklayın
   - Gerekirse ekleyin:
     ```
     SPRING_DATASOURCE_PASSWORD=your_db_password_here
     SMTP_PASSWORD=your_gmail_app_password
     RABBITMQ_HOST=localhost
     RABBITMQ_PORT=5672
     ```

4. **VM Options (Opsiyonel):**
   - `VM options` alanına:
     ```
     -Dspring.profiles.active=default
     ```

5. **Kaydet ve Çalıştır:**
   - `Apply` → `OK`
   - Run butonuna tıklayın (▶️) veya `Shift+F10`

#### Flight Service'i Çalıştırma

Aynı adımları takip ederek:

- **Name**: `Flight Service`
- **Main class**: `com.airline.flight.FlightServiceApplication` (veya ilgili main class)
- **Module**: `flight-service`
- **Port**: 8081

#### API Gateway'i Çalıştırma

- **Name**: `API Gateway`
- **Main class**: `com.airline.gateway.ApiGatewayApplication` (veya ilgili main class)
- **Module**: `api-gateway`
- **Port**: 8080

---

### Yöntem 2: Maven Goal ile Çalıştırma

1. **Maven Tool Window'u açın** (`View` → `Tool Windows` → `Maven`)
2. İlgili modülü genişletin (örn: `scheduler-service`)
3. `Lifecycle` → `spring-boot:run` çift tıklayın

**Not:** Her servis için ayrı terminal/run configuration kullanın.

---

### Yöntem 3: Terminal'den Çalıştırma

IntelliJ Terminal'inde (`Alt+F12`):

```powershell
# Terminal 1 - Scheduler Service
cd scheduler-service
mvn spring-boot:run

# Terminal 2 - Flight Service (yeni terminal açın)
cd flight-service
mvn spring-boot:run

# Terminal 3 - API Gateway (yeni terminal açın)
cd api-gateway
mvn spring-boot:run
```

---

## 🎯 Demo Senaryoları

### Senaryo 1: Scheduler Service Health Check

1. **Scheduler Service'i başlatın** (yukarıdaki yöntemlerden biriyle)
2. **Logları kontrol edin:**
   - IntelliJ'de `Run` tool window'unda logları görüntüleyin
   - Şu mesajları arayın:
     ```
     ✅ RabbitMQ connection successful! (veya ⚠️ uyarısı)
     ✅ Queue 'email.queue' created successfully
     SchedulerController initialized successfully
     ```

3. **Health Check Endpoint'ini test edin:**
   - Browser'da: `http://localhost:8083/api/v1/scheduler/health`
   - Veya IntelliJ HTTP Client ile:
     ```http
     GET http://localhost:8083/api/v1/scheduler/health
     ```

**Beklenen Yanıt:**
```json
{
  "status": "UP",
  "service": "scheduler-service"
}
```

---

### Senaryo 2: Email Test

1. **Scheduler Service çalışıyor olmalı**
2. **Test Email Endpoint'ini çağırın:**
   ```http
   GET http://localhost:8083/api/v1/scheduler/test/email?to=your-email@example.com
   ```

3. **Logları kontrol edin:**
   - Email gönderim denemesi loglanacak
   - Başarılı/başarısız durum loglarda görünecek

**Not:** Gmail App Password gerekli! `application.yml` veya environment variable'da ayarlayın.

---

### Senaryo 3: Flight Status Job'u Manuel Tetikleme

1. **Scheduler Service çalışıyor olmalı**
2. **Flight Status Job'u tetikleyin:**
   ```http
   POST http://localhost:8083/api/v1/scheduler/jobs/flight-status/trigger
   ```

3. **Logları izleyin:**
   - Job başlangıcı
   - Tamamlanan uçuşların işlenmesi
   - Miles hesaplamaları
   - Email gönderimleri

**Beklenen Log Çıktısı:**
```
========================================
Starting FlightStatusJob - Nightly Flight Update
========================================
Found X flights to mark as COMPLETED
Processing flight: ABC123 (IST -> JFK)
...
FlightStatusJob completed
```

---

### Senaryo 4: RabbitMQ Event Dinleme

1. **RabbitMQ Management UI'ya erişin:**
   - Browser: `http://localhost:15672`
   - Login: `guest` / `guest`

2. **Queue'yu kontrol edin:**
   - `Queues` sekmesinde `email.queue` görünmeli
   - Scheduler Service başladığında otomatik oluşturulur

3. **Flight Service'ten bir booking oluşturun:**
   - Bu, `BookingCreatedEvent` yayınlayacak
   - Scheduler Service bu event'i dinleyecek ve email gönderecek

4. **Scheduler Service loglarını izleyin:**
   ```
   ========================================
   RABBITMQ EVENT RECEIVED
   ========================================
   Booking Ref: ABC123
   User Email: user@example.com
   ...
   ✅ Booking confirmation email sent successfully
   ```

---

## 🐛 Sorun Giderme

### Problem 1: "Cannot resolve symbol" Hataları

**Neden:** Lombok annotation processing çalışmıyor.

**Çözüm:**
1. `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
2. ✅ "Enable annotation processing" işaretli olmalı
3. `File` → `Invalidate Caches` → `Invalidate and Restart`
4. Maven projesini yeniden yükleyin: Maven tool window → Reload All Projects

---

### Problem 2: Scheduler Service Başlamıyor / Çöküyor

**Neden:** RabbitMQ veya PostgreSQL bağlantı hatası.

**Çözüm:**
1. **Docker servislerini kontrol edin:**
   ```powershell
   docker ps
   ```
   - `airline-postgres` çalışıyor mu?
   - `airline-rabbitmq` çalışıyor mu?

2. **Logları kontrol edin:**
   - IntelliJ Run tool window'unda hata mesajlarını okuyun
   - "Connection refused" hatası görüyorsanız, servisler çalışmıyor demektir

3. **Servisleri başlatın:**
   ```powershell
   docker-compose up -d postgres rabbitmq redis
   ```

4. **Not:** Scheduler Service artık RabbitMQ olmadan da başlayabilir (uyarı verir ama çökmez).

---

### Problem 3: Port Zaten Kullanılıyor

**Hata:** `Port 8083 is already in use`

**Çözüm:**
1. **Port'u kullanan process'i bulun:**
   ```powershell
   netstat -ano | findstr :8083
   ```
2. **Process ID'yi not edin** (son sütun)
3. **Process'i sonlandırın:**
   ```powershell
   taskkill /PID <PID> /F
   ```
4. Veya IntelliJ'de başka bir Run Configuration çalışıyor olabilir, onu durdurun.

---

### Problem 4: Database Connection Hatası

**Hata:** `Connection to localhost:5432 refused`

**Çözüm:**
1. PostgreSQL Docker container'ının çalıştığını doğrulayın:
   ```powershell
   docker ps | findstr postgres
   ```
2. Container çalışmıyorsa:
   ```powershell
   docker-compose up -d postgres
   ```
3. Database şifresini kontrol edin:
   - `application.yml` dosyasında veya environment variable'da
   - Docker Compose'daki şifre ile eşleşmeli

---

### Problem 5: Email Gönderilemiyor

**Hata:** `Authentication failed` veya `535-5.7.8 Username and Password not accepted`

**Çözüm:**
1. **Gmail App Password oluşturun:**
   - https://myaccount.google.com/apppasswords
   - 2FA etkin olmalı
   - "Mail" ve "Other" seçin, "Airline App" yazın
   - Oluşturulan 16 karakterli şifreyi kopyalayın

2. **Şifreyi ayarlayın:**
   - Environment variable: `$env:SMTP_PASSWORD="your_app_password"`
   - Veya `application.yml` içinde (güvenlik için önerilmez)

3. **Test edin:**
   ```http
   GET http://localhost:8083/api/v1/scheduler/test/email?to=your-email@example.com
   ```

---

### Problem 6: Maven Dependencies İndirilemiyor

**Çözüm:**
1. **Maven settings'i kontrol edin:**
   - `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven`
   - Maven home path doğru mu?

2. **Maven repository'yi temizleyin:**
   - Maven tool window → `Reload All Maven Projects`
   - Veya terminal: `mvn clean install -U`

3. **Proxy ayarları:**
   - Şirket ağındaysanız proxy ayarları gerekebilir

---

## 📝 Debugging İpuçları

### Breakpoint Koyma

1. **Kodda breakpoint ekleyin:**
   - Satır numarasının yanına tıklayın (kırmızı nokta görünür)

2. **Debug modda çalıştırın:**
   - Run butonunun yanındaki dropdown'dan "Debug" seçin
   - Veya `Shift+F9`

3. **Debug tool window'u kullanın:**
   - Variables, Watches, Call Stack görüntülenir
   - Step Over (`F8`), Step Into (`F7`), Resume (`F9`)

### Log Seviyesini Değiştirme

`application.yml` dosyasında:

```yaml
logging:
  level:
    com.airline.scheduler: DEBUG  # Tüm scheduler logları
    org.springframework.amqp: DEBUG  # RabbitMQ logları
    org.springframework.mail: DEBUG  # Email logları
```

---

## 🎓 Öğrenme Kaynakları

### Proje İçinde Keşfedilecek Yerler

1. **Scheduler Service:**
   - `FlightStatusJob.java` - Zamanlanmış job implementasyonu
   - `BookingEventListener.java` - RabbitMQ event listener
   - `EmailService.java` - Email gönderim servisi
   - `RabbitMQConfig.java` - RabbitMQ yapılandırması

2. **Event-Driven Architecture:**
   - `BookingCreatedEvent.java` - Event modeli
   - RabbitMQ exchange ve queue yapılandırması

3. **Database Entities:**
   - `Booking.java`, `Flight.java`, `MilesAccount.java` - JPA entity'leri

---

## ✅ Checklist: Demo Öncesi Kontrol

- [ ] IntelliJ IDEA açık ve proje yüklü
- [ ] JDK 17 ayarlı
- [ ] Lombok plugin yüklü ve annotation processing etkin
- [ ] Maven dependencies indirildi
- [ ] Docker Desktop çalışıyor
- [ ] PostgreSQL, RabbitMQ, Redis container'ları çalışıyor (`docker ps`)
- [ ] Database şifresi ayarlı
- [ ] Gmail App Password ayarlı (email test için)
- [ ] Scheduler Service başarıyla başladı (logları kontrol edin)
- [ ] Health check endpoint çalışıyor (`http://localhost:8083/api/v1/scheduler/health`)

---

## 📞 Yardım

Sorun yaşarsanız:

1. **Logları kontrol edin** - IntelliJ Run tool window
2. **Docker container'ları kontrol edin** - `docker ps` ve `docker logs <container_name>`
3. **README.md'yi okuyun** - Genel proje dokümantasyonu
4. **Application.yml dosyalarını kontrol edin** - Yapılandırma hataları

---

**Son Güncelleme:** 14 Ocak 2026

**Hazırlayan:** AI Assistant

**Proje:** Airline Ticketing System - Scheduler Service
