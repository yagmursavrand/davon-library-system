# 🚀 Genel Debug Konfigürasyonu Kullanım Kılavuzu

## ✅ **Yeni Genel Launch.json Özellikleri**

Artık tek bir konfigürasyon ile tüm Java sınıflarını debug edebilirsiniz!

## 🎯 **3 Farklı Debug Yöntemi:**

### **1. Debug Current File (En Kolay)**
- **Ne yapar**: Açık olan dosyayı debug eder
- **Nasıl kullanılır**: 
  1. Debug etmek istediğiniz `.java` dosyasını açın
  2. `main` metodunun olduğundan emin olun
  3. Run and Debug panelinden "Debug Current File" seçin
  4. ▶️ Play butonuna basın

### **2. Debug Any Class (En Esnek)**
- **Ne yapar**: İstediğiniz herhangi bir sınıfı debug eder
- **Nasıl kullanılır**:
  1. Run and Debug panelinden "Debug Any Class" seçin
  2. ▶️ Play butonuna basın
  3. Sınıf adını girin (örn: `com.davon.library.debug.VerySimpleDebugTest`)

### **3. Hazır Konfigürasyonlar**
- **Debug VerySimpleDebugTest**: Basit debug testi
- **Debug BasicDebugTest**: Temel debug testi  
- **Debug SimpleDebuggingTest**: Kapsamlı debug testi
- **Debug SimpleTest**: Çok basit test

## 🎯 **Pratik Örnekler:**

### **Örnek 1: Yeni bir test sınıfı debug etmek**
```java
// YeniTest.java dosyası oluşturun
public class YeniTest {
    public static void main(String[] args) {
        // Kodunuz buraya
    }
}
```
**Kullanım**: Dosyayı açın → "Debug Current File" seçin → ▶️ Play

### **Örnek 2: Mevcut bir sınıfı debug etmek**
**Kullanım**: "Debug Any Class" seçin → `com.davon.library.debug.VerySimpleDebugTest` yazın → ▶️ Play

### **Örnek 3: Hazır testlerden birini kullanmak**
**Kullanım**: "Debug VerySimpleDebugTest" seçin → ▶️ Play

## 🔧 **Breakpoint Nasıl Koyulur:**

1. **Satır numarasının soluna tıklayın** (kırmızı nokta çıkar)
2. **Veya** `Cmd+F9` (Mac) / `Ctrl+F9` (Windows/Linux)
3. **Veya** Sağ tık → "Toggle Breakpoint"

## 🎮 **Debug Kontrolleri:**

- **F5**: Continue (devam et)
- **F10**: Step Over (satırı çalıştır, sonraki satıra geç)
- **F11**: Step Into (metoda gir)
- **Shift+F11**: Step Out (metoddan çık)
- **F9**: Toggle Breakpoint (breakpoint koy/kaldır)

## 📊 **Debug Panelleri:**

- **Variables**: Yerel değişkenleri gösterir
- **Watch**: İzlemek istediğiniz ifadeleri ekler
- **Call Stack**: Metod çağrı zincirini gösterir
- **Breakpoints**: Tüm breakpoint'leri listeler

## 🐛 **Debug Etmeye Hazır Sınıflar:**

### **VerySimpleDebugTest.java**
```java
// BREAKPOINT 1: Buraya breakpoint koyun
System.out.println("=== VERY SIMPLE DEBUG TEST ===");

// BREAKPOINT 2: Buraya breakpoint koyun
int days = 10;
double fine = days * FINE_RATE; // FINE_RATE = 1.5 (BUG!)

// BREAKPOINT 3: Buraya breakpoint koyun
System.out.println("Fine: " + fine);
```

### **BasicDebugTest.java**
```java
// BREAKPOINT 1: Sabitleri inceleyin
System.out.println("Daily Fine Rate: " + DAILY_FINE_RATE); // 1.5 (BUG!)

// BREAKPOINT 2: Hesaplamayı inceleyin
double calculatedFine = calculateFine(overdueDays);

// BREAKPOINT 3: Sonucu inceleyin
System.out.println("Calculated fine: " + calculatedFine);
```

## 🎉 **Başarı İşaretleri:**

✅ **Debug toolbar açılır**  
✅ **Variables panel değerleri gösterir**  
✅ **Breakpoint'lere durur**  
✅ **Kod satır satır çalışır**  
✅ **Değişkenleri inceleyebilirsiniz**  

## 🆘 **Sorun Giderme:**

### **"Failed to find the default configuration" hatası alırsanız:**
1. **Cursor IDE'yi yeniden başlatın**
2. **Cmd+Shift+P** → "Developer: Reload Window"
3. **Java extension'ın yüklü olduğundan emin olun**

### **Debug toolbar açılmazsa:**
1. **Breakpoint koyduğunuzdan emin olun**
2. **Doğru konfigürasyonu seçtiğinizden emin olun**
3. **Sınıfın `main` metodunun olduğundan emin olun**

## 🚀 **Hızlı Başlangıç:**

1. **Cmd+Shift+D** (Run and Debug panelini aç)
2. **"Debug Current File"** seç
3. **VerySimpleDebugTest.java** dosyasını aç
4. **Breakpoint koy** (satır 15'e tıkla)
5. **▶️ Play butonuna bas**
6. **F10 ile adım adım ilerle**

**Artık herhangi bir Java sınıfını kolayca debug edebilirsiniz! 🎉** 