<div align="center">
    <img width="200" height="200" style="display: block; border: 1px solid #f5f5f5; border-radius: 9999px;" src="./APP_ICON.png">
</div>

<br>
<br>
<br>

<div align="center">
    <img alt="GitHub" src="https://img.shields.io/github/license/Ashinch/ReadYou?color=c3e7ff&style=flat-square">
    <img alt="GitHub repo size" src="https://img.shields.io/github/repo-size/Ashinch/ReadYou?color=c3e7ff&style=flat-square">
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/Ashinch/ReadYou?color=c3e7ff&style=flat-square">
    <br>
    <a target="_blank" href="https://t.me/ReadYouApp">
        <img alt="Telegram" src="https://img.shields.io/badge/Telegram-ReadYouApp-c3e7ff?logo=telegram&style=flat-square">
    </a>
    <a target="_blank" href="https://www.figma.com/file/ViBW8GbUgkTMmK6a80h8X1/Read-You?node-id=7028%3A23673">
        <img alt="Figma" src="https://img.shields.io/badge/Figma-ReadYou-c3e7ff?logo=figma&style=flat-square">
    </a>
</div>

<div align="center">
    <h1>Read You</h1>
    <p>这是一个在 Android 上的  <a href="https://reederapp.com/">Reeder</a> 仿制品，为了让 Android 拥有一个与 Reeder 相似的 RSS 阅读器。</p>
    <br/>
    <p><a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README.md">English by DeepL</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    简体中文</p>
</div>
<br>
<br>
<br>

## 特性

**Read You** 结合了 Reeder 的交互逻辑与 [Material Design 3 (You)](https://m3.material.io/) 的设计风格。

以下是目前取得的进展和近期将要努力的目标：

-   [x] 本地

    -   [x] 订阅 RSS 链接
    -   [x] 导入 OPML 文件
    -   [x] 文章同步
    -   [x] 文章更新通知
    -   [x] 全文解析
    -   [x] 过滤未读、星标
    -   [x] 订阅源分组
    -   [x] 本地化
    -   [x] 导出 OPML 文件
    -   [x] 文章搜索
    -   [ ] 偏好设置
    -   [ ] 发布 APK
    -   [ ] 小组件
    -   [ ] ...

-   [ ] Fever API 支持
-   [ ] Google Reader API 支持
-   [ ] Inoreader API 支持
-   [ ] ...

> 以上特性仅是初步实现，可能存在某些未知 BUG，仍需进行测试和优化。

## 构建

> 如果你想要预览 Read You 应用，可以在 [Telegram 频道](https://t.me/ReadYouApp) 中获取 **预览版本** 的 APK 文件。

**Read You** 基于 Android 原生的 [Jetpack Compose](https://developer.android.com/jetpack/compose) 架构实现。

1. 首先需要获取 **Read You** 的源代码：

    ```shell
    git clone https://github.com/Ashinch/ReadYou.git
    ```

2. 然后通过 [Android Studio (最新版本)](https://developer.android.com/studio) 打开。

3. 点击 `▶ 运行（Run）` 按钮后将会自动构建并运行。

    > 如遇卡顿现象，请选择 Release 版本构建。

## 信用

-   [MusicYou](https://github.com/Kyant0/MusicYou)
-   [ParseRSS](https://github.com/muhrifqii/ParseRSS): [MIT](https://github.com/muhrifqii/ParseRSS/blob/master/LICENSE)
-   [Readability4J](https://github.com/dankito/Readability4J): [Apache License 2.0](https://github.com/dankito/Readability4J/blob/master/LICENSE)
-   [opml-parser](https://github.com/mdewilde/opml-parser): [Apache License 2.0](https://github.com/mdewilde/opml-parser/blob/master/LICENSE)
-   [compose-html](https://github.com/ireward/compose-html): [Apache License 2.0](https://github.com/ireward/compose-html/blob/main/LICENSE.txt)
-   （待完善）

## 许可证

[GNU GPL v3.0](https://github.com/Ashinch/ReadYou/blob/main/LICENSE)
