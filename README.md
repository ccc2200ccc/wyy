# NCM Auto Claim

网易云音乐「看广告领免费听歌时长」辅助工具 —— 私有自用，不上架。

## 这是什么

一个安卓无障碍辅助 APP，帮你自动完成两类广告任务：

- **类型 A**：4 秒倒计时广告，读秒结束自动点「领取」
- **类型 B**：跳转第三方 App 浏览广告，自动点跳转、停留若干秒、自动返回网易云并点「领取」

右上角悬浮圆点实时反馈状态，你不用碰屏幕。

## 免责声明

- 本项目仅做「系统级辅助点击」，不破解、不伪造网易云接口、不重放完成信号
- 网易云服务条款禁止以自动化方式参与活动，使用存在账号风控可能，后果自负
- 仅供个人本地学习使用，禁止分发、售卖、代挂
- 建议每天领取 1~2 次，不要挂循环狂刷

## 构建方式一：GitHub Actions（推荐，无需装 Android Studio）

1. 在 GitHub 新建空仓库（不要勾 README / .gitignore）
2. 把本目录所有文件上传到仓库
3. 推送后进入仓库 **Actions** 页，等待任务变绿
4. 点进任务 → 右侧 **Artifacts** → 下载 `ncm-autoclaim-apk`
5. 解压得到 `app-debug.apk`，传到手机安装

> 需要联网下载 Gradle 与依赖，首次构建约 3~6 分钟。

## 构建方式二：本地命令行

```bash
# 需要 JDK 17
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

Windows 用 `gradlew.bat assembleDebug`。

## 构建方式三：Android Studio

打开项目根目录 → 等待同步完成 →
**Build → Generate Signed Bundle / APK → APK** → 选 release 并创建 keystore。

> 注意：`gradle/wrapper/gradle-wrapper.jar` 不在仓库内（二进制文件）。
> - **本地构建**：装好 Gradle 后在项目根目录执行一次 `gradle wrapper`，会自动生成 jar 和脚本，之后就能用 `./gradlew assembleDebug`
> - **GitHub Actions**：无需任何操作，已在 workflow 里自动处理（见下方说明），直接推送即可出包

## 安装与使用

1. 手机允许「未知来源安装」，安装 APK
2. 首次打开进入引导页，依次授权：
   - 无障碍服务 → 找到「NCM自动领歌」→ 开启
   - 悬浮窗权限 → 允许
   - 电池优化 → 选「不限制 / 忽略优化」
3. 点「启动悬浮窗」，右上角出现灰色圆点
4. **校准一次**（关键）：
   - 打开网易云 → 我的 → 免费听歌 → 看广告
   - 广告页出现后，长按悬浮圆点 → 提示进入校准
   - 手动点一次「领取」按钮，完成记录
   - 跳转类广告同样校准「去逛逛」和「关闭」
5. 之后再看广告全程自动，只看圆点颜色

## 圆点颜色含义

| 颜色 | 含义 |
|---|---|
| 灰 | 空闲，未识别到广告 |
| 黄 | 扫描到广告页 |
| 蓝 | 倒计时广告读秒中 |
| 紫 | 已跳第三方 App，浏览计时中 |
| 青 | 正在点领取 |
| 绿 | 领取成功（闪一下缩回） |
| 橙 | 校准模式 |

## 校准数据迁移

主界面有「导出 / 导入校准数据」，通过剪贴板传递 JSON，换手机不用重新校准。

## 改关键词

编辑 `app/src/main/assets/rules.json`：

- `claim_keywords`：领取按钮文字
- `jump_keywords`：跳转按钮文字
- `close_keywords`：关闭按钮文字
- `browse_seconds`：第三方 App 停留秒数

网易云改版后按钮文案变了，改这里重新构建即可。

## 常见坑

- **构建失败 Gradle 版本**：确认 `gradle-wrapper.properties` 为 8.7，AGP 8.7.3 需 JDK 17
- **AndroidX 报错**：确认 `gradle.properties` 里有 `android.useAndroidX=true`
- **无障碍不触发**：确认 `accessibility_config.xml` 在 `res/xml/`，且 Manifest 里 service 已注册
- **圆点不显示**：检查悬浮窗权限，部分国产 ROM 需在「应用管理 → 权限 → 悬浮窗」单独开
- **后台被杀**：把 APP 加入自启动白名单 / 电池无限制 / 多任务界面下拉加锁
- **点击无效**：重新校准一次；部分广告是 WebView 自绘，只能靠坐标兜底
