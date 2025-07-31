# 🔍 **Cursor IDE Debugging Rehberi**

Bu rehber, Cursor IDE'de debugging araçlarını kullanarak Java kodundaki bugları nasıl bulacağınızı ve düzelteceğinizi öğretir.

## 🎯 **Debugging Nedir ve Neden Önemlidir?**

### **Debugging Nedir?**
Debugging, programınızdaki hataları (bugları) bulma ve düzeltme sürecidir. Bu süreç şunları içerir:
- **Hata tespiti**: Kodun nerede yanlış çalıştığını bulma
- **Hata analizi**: Hatanın neden oluştuğunu anlama
- **Hata düzeltme**: Kodu doğru şekilde çalışacak şekilde düzenleme

### **Neden Debugging Önemlidir?**
- ✅ **Hataları hızlı bulma**: Kod satır satır inceleme
- ✅ **Değişken değerlerini görme**: Runtime'da değerleri kontrol etme
- ✅ **Mantık hatalarını anlama**: Kod akışını takip etme
- ✅ **Performans sorunlarını tespit etme**: Yavaş çalışan kısımları bulma

---

## 🛠️ **Cursor IDE Debugging Araçları**

### **1. Breakpoint (Kesme Noktası)**

#### **Breakpoint Nedir?**
Breakpoint, kodunuzun belirli bir noktada durmasını sağlayan işaretleyicidir. Kod bu noktaya geldiğinde durur ve siz değişkenleri inceleyebilirsiniz.

#### **Breakpoint Nasıl Koyulur?**
1. **Satır numarasının yanındaki boşluğa tıklayın**
2. **Kırmızı nokta görünecek** - bu breakpoint'tir
3. **Kod çalışırken bu satıra geldiğinde duracak**

#### **Breakpoint Türleri:**
- **Line Breakpoint**: Belirli bir satırda durma
- **Conditional Breakpoint**: Belirli koşul sağlandığında durma
- **Exception Breakpoint**: Exception oluştuğunda durma

### **2. Variable Inspection (Değişken İnceleme)**

#### **Variables Panel:**
- **Local Variables**: Metod içindeki değişkenler
- **Instance Variables**: Sınıf değişkenleri
- **Static Variables**: Statik değişkenler

#### **Değişken Değerlerini İnceleme:**
```java
// BREAKPOINT: Bu satıra breakpoint koyun
double dailyFineRate = 1.5; // BUG: Should be 2.0

// Variables panelinde şunları göreceksiniz:
// dailyFineRate = 1.5 (WRONG VALUE!)
// Expected: 2.0
```

### **3. Step Through Execution (Adım Adım Çalıştırma)**

#### **Step Over (F8):**
- **Ne yapar**: Mevcut satırı çalıştırır ve bir sonraki satıra geçer
- **Ne zaman kullanılır**: Metod çağrılarını atlamak istediğinizde

#### **Step Into (F7):**
- **Ne yapar**: Metod çağrısının içine girer
- **Ne zaman kullanılır**: Metodun içini detaylı incelemek istediğinizde

#### **Step Out (Shift+F8):**
- **Ne yapar**: Mevcut metodtan çıkar
- **Ne zaman kullanılır**: Metodun geri kalanını atlamak istediğinizde

### **4. Expression Evaluation (İfade Değerlendirme)**

#### **Debug Console:**
- **Değişken değerlerini kontrol etme**
- **Yeni ifadeler çalıştırma**
- **Hesaplamalar yapma**

#### **Örnek Kullanım:**
```java
// Debug console'da şunları yazabilirsiniz:
dailyFineRate * 10  // Sonuç: 15.0 (yanlış)
2.0 * 10           // Sonuç: 20.0 (doğru)
```

---

## 🐛 **Wrong Constant Buglarını Bulma**

### **Adım 1: Breakpoint Koyma**

```java
// FineCalculationService.java - Line 25
private static final double DAILY_FINE_RATE = 1.5; // BUG: Should be 2.0

// Bu satıra breakpoint koyun
```

### **Adım 2: Test Çalıştırma**

```bash
# DebuggingPracticeTest'i debug modunda çalıştırın
mvn test -Dtest="DebuggingPracticeTest#testDebugFineCalculationConstants"
```

### **Adım 3: Değişken İnceleme**

**Variables Panel'de şunları göreceksiniz:**
```
DAILY_FINE_RATE = 1.5 (WRONG!)
Expected: 2.0
```

### **Adım 4: Step Through Execution**

1. **F8 (Step Over)** ile satır satır ilerleyin
2. **Her satırda değişken değerlerini kontrol edin**
3. **Yanlış değerleri tespit edin**

---

## 🔍 **Debugging Pratik Örnekleri**

### **Örnek 1: Fine Calculation Constants**

```java
// BREAKPOINT 1: Bu satıra breakpoint koyun
System.out.println("Step 1: Set breakpoint here to start debugging");

// BREAKPOINT 2: Bu satıra breakpoint koyun
Fine calculatedFine = fineCalculationService.calculateOverdueFine(testLoan);

// Variables panelinde şunları kontrol edin:
// - testLoan.getDueDate() = 10 gün önce (doğru)
// - calculatedFine.getAmount() = 15.0 (yanlış - 20.0 olmalı)
```

### **Örnek 2: Reservation Constants**

```java
// BREAKPOINT: Bu satıra breakpoint koyun
Reservation reservation = reservationService.createReservation(1L, 1L, "Test reservation");

// Variables panelinde şunları kontrol edin:
// - maxReservationsPerMember = 3 (yanlış - 5 olmalı)
// - reservationExpiryDays = 5 (yanlış - 7 olmalı)
```

### **Örnek 3: Notification Constants**

```java
// BREAKPOINT: Bu satıra breakpoint koyun
var overdueNotification = notificationService.sendOverdueReminder(1L, "Test Book", 5);

// Variables panelinde şunları kontrol edin:
// - notification.getPriority() = MEDIUM (yanlış - HIGH olmalı)
// - maxRetryCount = 5 (yanlış - 3 olmalı)
```

---

## 🎯 **Debugging Stratejileri**

### **1. Top-Down Approach (Yukarıdan Aşağıya)**
- **Başlangıç**: Ana metodun başına breakpoint koyun
- **İlerleme**: Satır satır aşağı inin
- **Avantaj**: Genel akışı anlama

### **2. Bottom-Up Approach (Aşağıdan Yukarıya)**
- **Başlangıç**: Sorunlu satıra breakpoint koyun
- **İlerleme**: Call stack'i yukarı takip edin
- **Avantaj**: Hatanın kaynağını bulma

### **3. Binary Search Approach (İkili Arama)**
- **Başlangıç**: Metodun ortasına breakpoint koyun
- **Kontrol**: Sorun burada mı?
- **İlerleme**: Sorun varsa ilk yarıya, yoksa ikinci yarıya geçin

---

## 🔧 **Debugging Best Practices**

### **1. Breakpoint Yerleştirme**
- ✅ **Mantıklı noktalara koyun**: Değişken değişimlerinden önce
- ✅ **Koşullu breakpoint kullanın**: Belirli durumlarda durma
- ✅ **Exception breakpoint kullanın**: Hataları yakalama

### **2. Değişken İnceleme**
- ✅ **Tüm değişkenleri kontrol edin**: Local, instance, static
- ✅ **Değerleri karşılaştırın**: Beklenen vs gerçek değerler
- ✅ **Nesne durumlarını inceleyin**: Object state'lerini kontrol edin

### **3. Step Through Execution**
- ✅ **Yavaş ilerleyin**: Her adımı anlayın
- ✅ **Call stack'i takip edin**: Hangi metodlar çağrılıyor
- ✅ **Return değerlerini kontrol edin**: Metodlar ne döndürüyor

### **4. Expression Evaluation**
- ✅ **Hesaplamalar yapın**: Matematiksel işlemleri test edin
- ✅ **Koşulları test edin**: if/else mantığını kontrol edin
- ✅ **String işlemlerini test edin**: String manipulation'ları kontrol edin

---

## 🚀 **Pratik Debugging Egzersizi**

### **Egzersiz 1: Fine Calculation Debugging**

1. **Breakpoint koyun**: `FineCalculationService.calculateOverdueFine()` metodunun başına
2. **Test çalıştırın**: `testDebugFineCalculationConstants()`
3. **Değişkenleri inceleyin**: `DAILY_FINE_RATE`, `GRACE_PERIOD_DAYS`, `MAX_FINE_AMOUNT`
4. **Yanlış değerleri tespit edin**: 1.5, 5, 30.0 (yanlış değerler)
5. **Doğru değerleri not edin**: 2.0, 7, 50.0 (doğru değerler)

### **Egzersiz 2: Reservation Debugging**

1. **Breakpoint koyun**: `ReservationService.createReservation()` metodunun başına
2. **Test çalıştırın**: `testDebugReservationConstants()`
3. **Değişkenleri inceleyin**: `MAX_RESERVATIONS_PER_MEMBER`, `RESERVATION_EXPIRY_DAYS`
4. **Yanlış değerleri tespit edin**: 3, 5 (yanlış değerler)
5. **Doğru değerleri not edin**: 5, 7 (doğru değerler)

### **Egzersiz 3: Notification Debugging**

1. **Breakpoint koyun**: `NotificationService.sendOverdueReminder()` metodunun başına
2. **Test çalıştırın**: `testDebugNotificationConstants()`
3. **Değişkenleri inceleyin**: `MAX_RETRY_COUNT`, `NOTIFICATION_EXPIRY_DAYS`
4. **Yanlış değerleri tespit edin**: 5, 15 (yanlış değerler)
5. **Doğru değerleri not edin**: 3, 30 (doğru değerler)

---

## 📝 **Debugging Notları**

### **Önemli Kontrol Noktaları:**
- ✅ **Constants**: Sabit değerler doğru mu?
- ✅ **Variables**: Değişken değerleri beklenen mi?
- ✅ **Conditions**: Koşullar doğru çalışıyor mu?
- ✅ **Calculations**: Hesaplamalar doğru mu?
- ✅ **Return values**: Dönüş değerleri beklenen mi?

### **Yaygın Hata Türleri:**
- ❌ **Wrong constants**: Yanlış sabit değerler
- ❌ **Logic errors**: Mantık hataları
- ❌ **Null pointer exceptions**: Null referans hataları
- ❌ **Type mismatches**: Tip uyumsuzlukları
- ❌ **Boundary conditions**: Sınır koşulları

---

## 🎉 **Debugging Başarı Kriterleri**

### **Başarılı Debugging:**
- ✅ **Tüm wrong constants tespit edildi**
- ✅ **Değişken değerleri doğru anlaşıldı**
- ✅ **Hata kaynakları bulundu**
- ✅ **Düzeltme planı hazırlandı**
- ✅ **Test sonuçları doğrulandı**

### **Öğrenme Hedefleri:**
- ✅ **Breakpoint kullanımı öğrenildi**
- ✅ **Variable inspection becerisi geliştirildi**
- ✅ **Step through execution anlaşıldı**
- ✅ **Expression evaluation öğrenildi**
- ✅ **Debugging stratejileri uygulandı**

---

## 🚀 **Sonraki Adımlar**

1. **Debugging pratiği yapın**: Test sınıflarını çalıştırın
2. **Breakpoint'ler koyun**: Her test metoduna breakpoint koyun
3. **Değişkenleri inceleyin**: Variables panelini kullanın
4. **Step through yapın**: F8, F7, Shift+F8 kullanın
5. **Expression evaluation yapın**: Debug console kullanın
6. **Bug'ları tespit edin**: Wrong constants'ları bulun
7. **Düzeltme planı hazırlayın**: Hangi değerler değişecek

**Debugging becerilerinizi geliştirmek için bu rehberi takip edin ve pratik yapın!** 🎯 