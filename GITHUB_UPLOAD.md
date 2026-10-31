# كيفية رفع المشروع على GitHub

> ⚠️ GitHub **لا يفك** الملفات المضغوطة (ZIP) تلقائياً عند الرفع.
> يجب رفع **محتويات المجلد** وليس ملف ZIP وحده.

---

## الطريقة الأسهل (موصى بها)

### 1. أنشئ مستودعاً فارغاً
- ادخل: https://github.com/new
- الاسم: `KitchenVisionPro`
- Public
- **لا** تختر README / .gitignore / License
- Create repository

### 2. على جهازك

```bash
# فك الضغط
unzip KitchenVisionPro.zip
cd KitchenVisionPro

# تهيئة Git (إذا لم يكن مهيأ)
git init
git branch -m main
git add .
git commit -m "Initial commit: KitchenVision Pro with GitHub Actions"

# اربط المستودع (استبدل YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/KitchenVisionPro.git
git push -u origin main
```

### 3. بعد الـ Push
- ادخل تبويب **Actions** في المستودع
- ستجد workflow باسم **Build Android APK** يعمل تلقائياً
- بعد انتهائه اضغط على الـ run ثم حمّل الـ APK من **Artifacts**

---

## طريقة بديلة: رفع من المتصفح

1. أنشئ المستودع على GitHub
2. اضغط **uploading an existing file**
3. اسحب **كل محتويات** مجلد KitchenVisionPro (ليس الـ ZIP)
4. Commit changes

> ملاحظة: رفع آلاف الملفات من المتصفح بطيء — استخدم Git من الطرفية أفضل.

---

## تشغيل البناء يدوياً

Actions → Build Android APK → Run workflow
