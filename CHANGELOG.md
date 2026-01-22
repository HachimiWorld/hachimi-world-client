# Unreleased

## Features 新功能

1. Added support for minimizing to system tray. 支持最小化到系统托盘。
2. Added multiple language support. 添加多语言支持。
3. Show recommend tags based on play history. 根据播放历史显示推荐标签。
4. Add searching and adding playlists to favorites feature. 新增搜索和收藏歌单功能。

## Enhancements 改进

1. Added streaming playback support on Windows, macOS and Linux. 在 Windows、macOS 和 Linux 系统上支持了流式播放。
2. Refactored more UI with the custom design system. 用新设计重做更多了界面。
3. Added support for headset media controls on Android. 安卓支持耳机控制。
4. Show more song information in player screen. 播放页面显示更多歌曲信息。
5. Highlight matched query in search results. 在搜索结果中高亮匹配的查询文本。
6. Added sorting functionality for song searching. 添加歌曲搜索的排序功能。
7. Show playlists containing the song before clicking adding button. 在添加歌单对话框中显示已添加到的歌单标识。
8. Loudness normalization settings now take effect immediately. 响度均衡设定现在会立即生效。


## Fixes 修复

1. Fixed the flickering issue when expanding the player. 修复展开播放器时闪烁的问题。
2. Fixed issues where UI could be obscured by navigation bar. 修复界面会被导航栏遮挡的问题。

# 1.0.8-beta (2025-12-16)

## Fixes 修复

1. Fixed a issue where the cached music can't be played. 修复无法播放刚缓存的音乐的问题
2. Fixed a memory leak in player screen. 修复播放器页面的内存泄漏问题
3. Performance optimization. 性能优化

# 1.0.7-beta (2025-12-12)

## Changelog 更新内容

1. New Player UI. 全新播放器界面。
2. Supports streaming playback under certain conditions. 部分情况下支持流式播放。

# 1.0.6-beta (2025-11-19)

## New 新功能

1. It's now able to modify artwork . 支持修改稿件
2. Support customizing JMID. 支持发布稿件自定义基米ID
3. Added music sharing link. 支持分享音乐链接

## Enhancements 改进

1. (web) Support js fallback for old browser compatibility. Web 端支持回退到 JS 引擎以兼容老旧浏览器
2. (android) Enabled r8 minify. 启用 R8 以减小安装包大小。
3. Load zones in home screen earlier. 更早地加载首页每个分区
4. Fixed some issue. 修复一些问题

# 1.0.5-beta (2025-11-07)

## Features 新功能

1. Add explicit mark and kids mode. 新增作品的儿童不宜标识，以及相应的宝宝模式
2. Support loudness normalization and enable by default. 新增响度均衡并默认启用
3. Add daily recommend, hot songs, categories in home page. 首页新增每日推荐、热门歌曲、风格分类

## Enhancements 改进

1. Make volume adjustment on desktop perceptually linear. 使桌面端音量调节从感官上是线性的
2. Auto retry when audio playback fails. 当音频播放失败时会自动重试

# 1.0.4-beta (2025-10-25)

## Overview 概览

This version brings iOS support and general UX enhancements. 本次更新带来 iOS 端的支持，以及常规用户体验改进

## Enhancements 改进

1. Support Media Session on web platform. 网页端支持音频会话
2. Support next/previous button in notification on Android platform. 安卓端通知栏支持上一曲/下一曲
3. Adding external links in publishing page now accepts BV/SM numbers instead. 发布作品的外部链接现在改为使用 BV 号 / SM 号
4. Add support for querying by display ID and user IDs. 支持基米 ID 搜歌和 UID 搜用户
5. Added an interface to show all producers of the artwork. 添加一个界面展示作品的所有制作人
6. Add "Play All" button in user space. 在神人空间中添加了播放全部按钮

## Fixes 修复

1. Fixed a issue where a multiple-line text could be paste to single line text field. 修复发布作品页面中可以将多行文本粘贴到单行输入框的错误
2. General bug fixes. 其他常规 Bug 修复

# 1.0.3-beta (2025-10-14)

## Features 功能

1. Support random/repeat playback. 支持随机/重复播放模式
2. Show MV links. 显示 MV 链接
3. Support removing song from playlist. 支持从歌单中移出歌曲

## Fixes 修复

1. Fixed a error when playing 24bit audio. 修复播放 24bit 音频出现的错误
2. Fixed some issues in lyrics. 修复滚动歌词的部分问题

## Improvements 改进

1. Persistent player state and music queue. 保存播放器状态和播放队列

# 1.0.2-beta (2025-10-10)

## Features 功能

1. Support volume control for web and desktop platform. 为 Web 端和桌面端支持音量控制 1f921ce

## Improvements 改进

1. Show more detailed origin info. 显示更详细的原作信息 c9be8ab
2. Refine the wording of guide texts in publishing page. 调整发布作品页面的引导文字

## Fixes 修复

1. Fixed mismatched sample rate in playback. 修复播放时采样率不匹配的问题 3319c26

# 1.0.1-beta (2025-10-07)

## Features 功能改进

1. Adjusted the song card UI. 调整了歌曲卡片界面
2. Show origin song info in player screen. 在全屏播放器中显示原作信息
3. Support filling in original author when publishing. 可在发布作品时填写原作者
4. Hide player progress bar in compact mode. 为防止误触暂时隐藏移动端底部播放器的进度条，待重做改进

## Fixes 修复

1. Fixed many issues when change playing songs. 修复切歌的许多问题
2. Fixed tags being overflowed. 修复标签溢出界面

# 1.0.0-dev8 (2025-09-27)

## Features

1. Support search users
2. Show song works in user space

# 1.0.0-dev7 (2025-09-24)

## Improvements

1. Improve the loading speed of the player. 8971937
2. Available to input pure text lyrics.
3. [web] Load fonts from web, to support more browsers. 8988f95
4. [web] Cache songs to avoid download every time. e1bfd77
5. [web] Add back handler support.

## Fix

1. [web] Fix crashing on many situations because we didn't correctly catch JsException. f1185b0
2. Fix error on first play. f99b1f8
3. Fix download progress is not correctly displayed. 113f95c

# 1.0.0-dev6 (2025-09-19)

## Features

1. Refactor Auth pages.
2. Adjust UI styles for lots of pages.
3. Remove system title bar in macOS. 93e66a8
4. Close navigation drawer after clicking.
5. Add song play count in home page.

## Bugfixes

1. Load avatar after login. 664f580
2. Fix playing bugs on Android. 87f8a17

# 1.0.0-dev5 (2025-09-15)

## Features

1. Experimentally support wasm target. #9
2. Add Forget Password. 2e37303

# 1.0.0-dev4 (2025-09-11)

添加版本自动发布与自动更新
