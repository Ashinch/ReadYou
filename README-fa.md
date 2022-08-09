<div align="center">
    <img width="200" height="200" style="display: block; border: 1px solid #f5f5f5; border-radius: 9999px;" src="./fastlane/metadata/android/en-US/images/icon.png">
</div>

<br>
<br>
<br>

<div align="center">
    <img alt="GitHub" src="https://img.shields.io/github/license/Ashinch/ReadYou?color=c3e7ff&style=flat-square">
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/releases">
        <img alt="Version" src="https://img.shields.io/github/v/release/Ashinch/ReadYou?color=c3e7ff&label=version&style=flat-square">
    </a>
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/Ashinch/ReadYou?color=c3e7ff&style=flat-square">
    <br>
    <a target="_blank" href="https://t.me/ReadYouApp">
        <img alt="Telegram" src="https://img.shields.io/badge/Telegram-ReadYouApp-c3e7ff?logo=telegram&style=flat-square">
    </a>
    <a target="_blank" href="https://www.figma.com/file/ViBW8GbUgkTMmK6a80h8X1/Read-You?node-id=7028%3A23673">
        <img alt="Figma" src="https://img.shields.io/badge/Figma-ReadYou-c3e7ff?logo=figma&style=flat-square">
    </a>
</div>

<div dir="rtl" align="center">
    <h1 dir="ltr">Read You</h1>
    <p>این یک کپی از  <a href="https://reederapp.com/">Reeder</a>,به منظور ارائه یک RSS Reader مشابه Reeder به اندروید است.</p>
    <p>فارسی&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README-de.md">Deutsch</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README-zh-CN.md">简体中文</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README-zh-TW.md">繁體中文</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README.md">English by DeepL</a></p>
    <br/>
    <br/>
    <img src="./fastlane/metadata/android/en-US/images/startup.png" width="19.2%" alt="startup" />
    <img src="./fastlane/metadata/android/en-US/images/feeds.png" width="19.2%" alt="startup" />
    <img src="./fastlane/metadata/android/en-US/images/flow.png" width="19.2%" alt="startup" />
    <img src="./fastlane/metadata/android/en-US/images/read.png" width="19.2%" alt="startup" />
    <img src="./fastlane/metadata/android/en-US/images/settings.png" width="19.2%" alt="startup" />
    <br/>
    <br/>
</div>

## ویژگی ها

برنامه **Read You** یک منطق تعاملی برای Reeder است که به توسط [Material Design 3 (You)](https://m3.material.io/) طراحی شده است.

پیشرفت های انجام شده تا کنون و اهدافی که به زودی روی آن ها کار خواهد شد به شرح زیر است:

<div dir="rtl">

-   [x] لوکال
    -   [x] مشترک شدن در فید ها
    -   [x] وارد کردن از OPML
    -   [x] همگام سازی مقاله
    -   [x] ارسال نوتیفیکیشن در صورت بروزرسانی مقاله
    -   [x] تجزیه کامل مطالب
    -   [x] فیلتر بر اساس نخوانده ها و ستاره ها
    -   [x] گروه‌بندی فید ها
    -   [x] محلی‌سازی
    -   [x] خروجی OPML
    -   [x] جست وجو برای مقاله
    -   [ ] تنظیمات ترجیحی
    -   [ ] انتشار APK
    -   [ ] ویجت ها
    -   [ ] ...

-   [ ] پشتیبانی از Fever API
-   [ ] پشتیبانی از Google Reader API
-   [ ] پشتیبانی از Inoreader API
-   [ ] ...

</div>

> ویژگی های فوق فقط پیاده سازی اولیه هستند و ممکن است مسائل ناشناخته ای وجود داشته باشد.

## دانلود

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="دانلود از F-Droid"
     height="80">](https://f-droid.org/packages/me.ash.reader/)

یا از [خروجی گیت‌هاب اکشن](https://github.com/Ashinch/ReadYou/releases) دانلود کنید.

## ترجمه

<a target="_blank" href="https://hosted.weblate.org/engage/readyou/">
<img src="https://hosted.weblate.org/widgets/readyou/-/287x66-white.png" alt="" />
</a>

## بیلد

> اگر به نسخه پیش نمایش برنامه Read You نیاز دارید،  میتوانید فایل های APK **preview version**  را از [تلگرام](https://t.me/ReadYouApp) دریافت کنید.

برنامه **Read You** در معماری اصلی [Jetpack Compose](https://developer.android.com/jetpack/compose) اندروید پیاده سازی شده است.

۱. برای شروع شما نیاز دارید سورس کد **Read You** را کلون کنید:

```shell
    git clone https://github.com/Ashinch/ReadYou.git
```

۲. سپس آن را با [ااندروید استادیو (آخرین ورژن)](https://developer.android.com/studio) باز کنید.

۳. سپس روی دکمه `▶ Run` کلیک کنید. برنامه به صورت خودکار بیلد و اجرا خواهد شد.

    > اگر با لگ همراه بود، لطفا `Release version build` را انتخاب کنید.

## الهام گرفته از:

-   [MusicYou](https://github.com/Kyant0/MusicYou)
-   [ParseRSS](https://github.com/muhrifqii/ParseRSS): [MIT](https://github.com/muhrifqii/ParseRSS/blob/master/LICENSE)
-   [Readability4J](https://github.com/dankito/Readability4J): [Apache License 2.0](https://github.com/dankito/Readability4J/blob/master/LICENSE)
-   [opml-parser](https://github.com/mdewilde/opml-parser): [Apache License 2.0](https://github.com/mdewilde/opml-parser/blob/master/LICENSE)
-   [compose-html](https://github.com/ireward/compose-html): [Apache License 2.0](https://github.com/ireward/compose-html/blob/main/LICENSE.txt)
-   [Rome](https://github.com/rometools/rome): [Apache License 2.0](https://github.com/rometools/rome/blob/master/LICENSE)
-   [Feeder](https://gitlab.com/spacecowboy/Feeder): [GPL v3.0](https://gitlab.com/spacecowboy/Feeder/-/blob/master/LICENSE)

## لاینسس

[GNU GPL v3.0](https://github.com/Ashinch/ReadYou/blob/main/LICENSE)
