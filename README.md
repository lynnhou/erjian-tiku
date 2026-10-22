# 二建建筑题库（ErJianTiKu）

参照「驾考宝典」模式制作的二级建造师·建筑工程实务课后练习 App。

## 功能

| 模块 | 说明 |
| --- | --- |
| 顺序练习 | 全部题目逐题练习，单选/判断点击即判分，多选提交后判分，即时显示对错与解析 |
| 章节练习 | 题库按 18 个章节归类（钢筋工程、模板工程、混凝土工程、施工安全等），按章节专项突破 |
| 模拟考试 | 随机组卷（20/100/全量三档），倒计时、答题卡、交卷自动批改，60 分合格 |
| 错题本 | 练习和模考中答错的题自动收入，可重练、可移除 |
| 我的收藏 | 练习中星标收藏的重点题 |
| 练习统计 | 累计答题、正确率、各章节正确率、模考记录 |

题库数据来自课后练习截图 OCR 提取，内置 423 题（单选 167 / 多选 88 / 判断 168），
存放在 `app/src/main/assets/questions.json`，全部本地存储，无需联网、无需后端。

## 技术栈

- Kotlin + Jetpack Compose（Material 3）
- Navigation Compose 单 Activity 架构
- org.json 解析题库，Gson 持久化学习数据（错题/收藏/统计/模考记录）
- minSdk 26，targetSdk 35

## 构建

本地构建（需要 JDK 17 与 Android SDK）：

```bash
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

推送代码到 GitHub 后，Actions 会自动构建 APK，可在仓库 Actions 页面下载
artifact：`erjian-tiku-debug`。

## 目录结构

```
app/src/main/
├── assets/questions.json          # 题库（423 题）
├── java/com/erjian/tiku/
│   ├── data/                      # 题库解析、用户数据持久化
│   └── ui/                        # Compose 界面与 ViewModel
└── res/                           # 图标、主题、字符串
```
