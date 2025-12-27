# 财芽 - 基金管理App

一个功能完善的Android基金管理应用，采用现代化的Jetpack Compose技术栈开发。

[![GitHub](https://img.shields.io/badge/GitHub-CaiYa-blue)](https://github.com/SleepingLingHuan/CaiYa)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-24+-green.svg)](https://www.android.com/)

## ✨ 主要功能

- 📊 **基金行情**：实时查看基金涨跌幅排行榜，支持多维度排序（日/周/月/年）
- 💼 **持仓管理**：记录和管理基金持仓，自动计算收益和盈亏率
- ⭐ **自选基金**：添加自选基金，实时跟踪关注基金的涨跌情况
- 📝 **交易记录**：记录买入卖出交易，支持先进先出（FIFO）卖出策略
- 📈 **数据可视化**：图表展示持仓分布和收益趋势
- 🔍 **基金搜索**：支持搜索全国25234只基金
- 📱 **基金详情**：查看基金详细信息、净值历史、持仓股等

## 🛠️ 技术栈

- **UI框架**：Jetpack Compose + Material 3
- **架构模式**：MVVM + Repository
- **异步处理**：Kotlin Coroutines + Flow
- **数据持久化**：Room Database
- **网络请求**：Retrofit + OkHttp
- **数据序列化**：Kotlinx Serialization
- **图表库**：MPAndroidChart
- **HTML解析**：Jsoup

## 📋 核心特性

### 多数据源整合
- 整合东方财富、天天基金、腾讯等多个数据源
- 智能选择最优数据源，提供容错机制

### 智能缓存机制
- 基金基本信息缓存（5分钟）
- 基金详情缓存（30天）
- 净值历史缓存（24小时）

### 净值数据增强
- 结合实时估值和净值历史数据
- 提供最准确的基金净值信息

### 持仓聚合显示
- 同一基金的多次购买自动聚合
- 计算加权平均购买净值
- 支持查看详细购买记录

### 先进先出卖出
- 符合税务要求的FIFO卖出策略
- 自动处理份额计算和记录更新

## 📥 直接下载安装

**不想编译？直接下载APK安装包！**

👉 [下载 caiya-v2.2-20251024.apk](https://github.com/SleepingLingHuan/CaiYa/raw/main/caiya-v2.2-20251024.apk)

这是从Android Studio构建的APK安装包，可直接在Android系统上安装使用。

**安装步骤：**
1. 下载APK文件到Android设备
2. 在设备上允许"未知来源"安装（设置 → 安全 → 未知来源）
3. 点击APK文件进行安装

> ⚠️ **注意**：如果浏览器下载后无法直接安装，请使用文件管理器找到下载的APK文件并点击安装。

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 11 或更高版本
- Android SDK 24 或更高版本
- Gradle 8.0 或更高版本

### 构建步骤

1. 克隆项目
```bash
git clone https://github.com/SleepingLingHuan/CaiYa.git
cd CaiYa
```

2. 打开项目
- 使用Android Studio打开项目
- 等待Gradle同步完成

3. 配置本地环境
- 项目会自动创建 `local.properties` 文件（已加入.gitignore）
- 确保Android SDK路径正确

4. 运行项目
- 连接Android设备或启动模拟器
- 点击运行按钮或使用 `./gradlew installDebug`

## 📱 应用截图

（可以添加应用截图）

## 🏗️ 项目结构

```
app/src/main/java/com/example/jjsj/
├── data/                    # 数据层
│   ├── local/              # 本地数据库（Room）
│   ├── model/              # 数据模型
│   ├── remote/             # 网络请求
│   └── repository/         # 数据仓库
├── ui/                      # UI层
│   ├── component/          # UI组件
│   ├── screen/             # 页面
│   ├── navigation/         # 导航
│   └── theme/              # 主题
├── viewmodel/              # ViewModel层
├── util/                    # 工具类
├── MainActivity.kt         # 主Activity
└── FundTrackerApp.kt       # Compose应用入口
```

## 📖 使用说明

### 添加持仓

1. 在基金详情页面点击"买入基金"
2. 输入购买金额和购买日期
3. 系统自动计算份额（购买金额 / 购买日净值）

### 查看收益

- **累计收益**：在持仓页面查看总盈亏和盈亏率
- **今日收益**：在行情页面查看今日实时收益

### 卖出基金

1. 在持仓页面选择要卖出的基金
2. 输入卖出金额
3. 系统按先进先出原则自动计算卖出份额

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 🙏 致谢

- 数据来源：东方财富、天天基金网、腾讯财经
- 感谢所有开源项目的贡献者

## 📧 联系方式

如有问题或建议，请提交Issue。

---

**注意**：本项目仅供学习交流使用，不构成任何投资建议。投资有风险，入市需谨慎。

