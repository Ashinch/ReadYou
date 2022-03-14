# Reader

这是一个在 Android 上的 [Reeder](https://reederapp.com/) 仿制品，为了让 Android 拥有一个与 Reeder 相似的 RSS 阅读器。

## 特性

本项目依照 Reeder 的用户界面和交互逻辑，融入动态色彩等来自 Material 3 (You) 的新特性。

以下是目前取得的进展和近期将要努力的目标：

-   [x] 本地
    -   [x] [Figma 文件](https://www.figma.com/file/ViBW8GbUgkTMmK6a80h8X1/Reader-You?node-id=7028%3A23673)
    -   [x] 订阅 Feed 链接
    -   [x] 导入 OPML 文件
    -   [x] 文章同步
    -   [x] 文章更新通知
    -   [x] 全文解析
    -   [x] 过滤已读、未读、星标
    -   [ ] 文章搜索
    -   [ ] Feed 分组
    -   [ ] 设置相关
    -   [ ] 小组件
    -   [ ] ...

-   [ ] FreshRSS API 支持
-   [ ] 发布 APK
-   [ ] Inoreader API 支持
-   [ ] ...

[看板视图](https://github.com/Ashinch/Reader/projects/1)中记录了更详细的项目进展。

> 以上特性仅是初步实现，可能存在某些未知 BUG，仍需进行测试和优化。

## 构建

本项目基于 Android 原生的 Jetpack Compose 架构实现。目前处于开发阶段，所以暂未发布 APK。

1. 如果你想要预览该应用，首先需要获取本项目的源代码：

```shell
git clone https://github.com/Ashinch/Reader.git
```

2. 然后通过 [Android Studio (最新版本)](https://developer.android.com/studio) 打开。

3. 点击 `▶ 运行（Run）` 按钮后将会自动构建并运行。

> 如遇卡顿现象，请选择 Release 版本构建。

## Credit

-   [MusicYou](https://github.com/Kyant0/MusicYou)
-   [ParseRSS](https://github.com/muhrifqii/ParseRSS)
-   [Readability4J](https://github.com/dankito/Readability4J)
-   （待完善）
