# GitHub推送指南

本指南将帮助你将项目推送到GitHub并开源。

## 📋 准备工作

### 1. 检查项目状态

项目已经初始化了Git仓库，但还没有提交。当前状态：
- ✅ Git仓库已初始化
- ✅ .gitignore已配置
- ✅ README.md已创建
- ✅ LICENSE已创建
- ⚠️ 需要提交代码并推送到GitHub

### 2. 检查敏感信息

确保以下文件不会被提交（已在.gitignore中）：
- ✅ `local.properties` - 包含SDK路径
- ✅ `*.keystore` / `*.jks` - 签名文件
- ✅ `build/` - 构建输出
- ✅ `.gradle/` - Gradle缓存

## 🚀 推送步骤

### 步骤1：添加所有文件到暂存区

```bash
# 添加所有文件（.gitignore会自动排除不需要的文件）
git add .

# 或者分步添加
git add app/
git add build.gradle.kts
git add settings.gradle.kts
git add gradle/
git add README.md
git add LICENSE
git add .gitignore
```

### 步骤2：提交代码

```bash
git commit -m "Initial commit: 财芽基金管理App

- 实现基金行情查询功能
- 实现持仓管理功能
- 实现自选基金功能
- 实现交易记录功能
- 实现数据可视化功能
- 采用MVVM架构 + Jetpack Compose
- 支持多数据源整合
- 实现智能缓存机制"
```

### 步骤3：在GitHub上创建仓库

1. 登录GitHub
2. 点击右上角的 "+" 按钮，选择 "New repository"
3. 填写仓库信息：
   - **Repository name**: `jjsj` 或 `fund-tracker`（你喜欢的名字）
   - **Description**: `一个功能完善的Android基金管理应用`
   - **Visibility**: 选择 `Public`（开源）
   - **不要**勾选 "Initialize this repository with a README"（因为我们已经有了）
4. 点击 "Create repository"

### 步骤4：添加远程仓库并推送

```bash
# 添加远程仓库（将YOUR_USERNAME替换为你的GitHub用户名）
git remote add origin https://github.com/YOUR_USERNAME/jjsj.git

# 或者使用SSH（如果你配置了SSH密钥）
# git remote add origin git@github.com:YOUR_USERNAME/jjsj.git

# 查看远程仓库配置
git remote -v

# 推送代码到GitHub
git branch -M main
git push -u origin main
```

### 步骤5：验证推送结果

1. 访问你的GitHub仓库页面
2. 确认所有文件都已上传
3. 检查README.md是否正确显示

## 🔧 后续操作

### 添加仓库描述和标签

在GitHub仓库页面：
1. 点击 "Settings" → "General"
2. 添加仓库描述和主题标签（Topics）：
   - `android`
   - `kotlin`
   - `jetpack-compose`
   - `fund-management`
   - `mvvm`
   - `room-database`

### 添加仓库徽章（可选）

在README.md中添加构建状态等徽章。

### 创建Release（可选）

1. 在GitHub仓库页面，点击 "Releases" → "Create a new release"
2. 填写版本信息：
   - **Tag version**: `v1.0.0`
   - **Release title**: `v1.0.0 - 初始版本`
   - **Description**: 描述主要功能

## ⚠️ 注意事项

### 1. 不要提交敏感信息

确保以下内容不会被提交：
- API密钥（如果有）
- 签名密钥文件
- 个人配置信息
- 测试数据（如果包含敏感信息）

### 2. 大文件处理

如果项目中有大文件（>100MB），考虑使用Git LFS：
```bash
git lfs install
git lfs track "*.largefile"
```

### 3. 提交历史清理（可选）

如果之前有不想公开的提交历史，可以创建新的初始提交：
```bash
# 删除.git目录（谨慎操作）
rm -rf .git

# 重新初始化
git init
git add .
git commit -m "Initial commit"
```

## 📝 提交信息规范

建议使用以下格式的提交信息：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型（type）**：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例**：
```
feat(position): 实现持仓聚合显示功能

- 支持同一基金的多次购买合并显示
- 计算加权平均购买净值
- 显示购买次数和日期范围
```

## 🎉 完成！

推送完成后，你的项目就已经在GitHub上开源了！

如果遇到问题，可以：
1. 查看Git错误信息
2. 检查网络连接
3. 确认GitHub账户权限
4. 查看GitHub帮助文档

---

**提示**：推送后记得在README.md中更新仓库链接！

