# KitchenVision Pro 🍳

تطبيق أندرويد متكامل لتصميم المطابخ باحترافية — مستوحى من أسلوب الرسم على الصور الحقيقية مع خطوط المنظور.

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Kotlin-purple)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange)

## المميزات

- 📷 استيراد صورة الغرفة من **الكاميرا** أو **المعرض**
- 📐 أداة رسم **خطوط المنظور** (Perspective Lines)
- 🗄️ مكتبة عناصر حقيقية (25 صورة PNG): خزائن، أجهزة، ديكور، إضاءة، أسطح
- 🎨 تطبيق مواد (خشب، رخام، معدن، زجاج...)
- 📚 **نظام طبقات** كامل: ترتيب، إخفاء، قفل
- ↔️ سحب وتحريك + Pinch-to-zoom
- 💾 حفظ المشاريع محلياً (Room Database)
- 📤 **تصدير الصورة النهائية** للمعرض + مشاركة
- 🌐 واجهة عربية كاملة

## لقطات الشاشة

| الشاشة الرئيسية | لوحة التصميم | الطبقات |
|-----------------|--------------|---------|
| قائمة المشاريع + تصميم جديد | صورة الغرفة + خطوط + عناصر | إدارة z-index والإخفاء والقفل |

## التقنيات

| التقنية | الاستخدام |
|---------|-----------|
| Kotlin | لغة البرمجة |
| Jetpack Compose + Material 3 | الواجهة |
| Navigation Compose | التنقل |
| Room | قاعدة البيانات المحلية |
| Coil | تحميل الصور |
| CameraX / TakePicture | الكاميرا |
| ViewModel + StateFlow | إدارة الحالة |
| Bitmap compositing | تصدير الصورة النهائية |

## متطلبات التشغيل

- Android Studio Hedgehog أو أحدث
- JDK 17
- Android SDK 34
- جهاز/محاكي API 26+

## التثبيت والتشغيل

```bash
# 1. استنساخ المستودع
git clone https://github.com/YOUR_USERNAME/KitchenVisionPro.git
cd KitchenVisionPro

# 2. فتح المشروع في Android Studio
# File → Open → اختر مجلد KitchenVisionPro

# 3. انتظر Gradle Sync ثم اضغط Run ▶
```

أو من سطر الأوامر:

```bash
./gradlew assembleDebug
# الـ APK سيكون في: app/build/outputs/apk/debug/app-debug.apk
```

## هيكل المشروع

```
app/src/main/
├── assets/elements/          # 25 صورة PNG للعناصر
├── java/com/kitchenvision/pro/
│   ├── MainActivity.kt
│   ├── ui/
│   │   ├── screens/          # Home, DesignCanvas, Library
│   │   ├── components/       # AssetImage
│   │   └── theme/
│   ├── data/
│   │   ├── model/            # DesignElement, Layers, Library
│   │   ├── db/               # Room
│   │   └── repository/
│   ├── viewmodel/
│   └── utils/ExportUtils.kt  # تصدير الصورة
└── res/
```

## طريقة الاستخدام

1. **تصميم جديد** ← أضف صورة الغرفة (كاميرا / معرض)
2. أداة **منظور** ← ارسم خطوط المنظور
3. **عناصر** ← اختر خزائن وأجهزة من المكتبة
4. اسحب / كبّر / طبّق مواد
5. أيقونة **الطبقات** ← أدر الترتيب والإخفاء والقفل
6. **تصدير** ← احفظ الصورة وشاركها

## المساهمة

المساهمات مرحب بها! افتح Issue أو Pull Request.

## الترخيص

MIT License — للاستخدام التعليمي والشخصي والتجاري.

---

Built with ❤️ using Jetpack Compose
