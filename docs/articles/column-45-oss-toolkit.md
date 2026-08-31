# /oss-toolkit：对象存储封装——统一接口、分片续传、图片处理、CDN 刷新，一套全包

> 命令深度拆解 · 第 45 篇 · 约 8000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_o0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/oss-toolkit：对象存储五件套</text>
  <rect x="40" y="55" width="140" height="90" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="110" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">upload</text>
  <text x="110" y="104" text-anchor="middle" fill="#64748b" font-size="9">上传/下载/删除</text>
  <text x="110" y="126" text-anchor="middle" fill="#64748b" font-size="8">100MB 自动分片</text>
  <rect x="200" y="55" width="140" height="90" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="270" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">presign</text>
  <text x="270" y="104" text-anchor="middle" fill="#64748b" font-size="9">预签名 URL</text>
  <text x="270" y="126" text-anchor="middle" fill="#64748b" font-size="8">GET 1h~7d / PUT 30min</text>
  <rect x="360" y="55" width="140" height="90" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="430" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">multipart</text>
  <text x="430" y="104" text-anchor="middle" fill="#64748b" font-size="9">分片 + 断点续传</text>
  <text x="430" y="126" text-anchor="middle" fill="#64748b" font-size="8">5MB/片 · 10000 片</text>
  <rect x="520" y="55" width="140" height="90" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="590" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">image</text>
  <text x="590" y="104" text-anchor="middle" fill="#64748b" font-size="9">缩略图/水印/裁剪</text>
  <text x="590" y="126" text-anchor="middle" fill="#64748b" font-size="8">模板配置不硬编码</text>
  <rect x="680" y="55" width="100" height="90" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="730" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">cdn</text>
  <text x="730" y="104" text-anchor="middle" fill="#64748b" font-size="9">刷新/预热</text>
  <text x="730" y="126" text-anchor="middle" fill="#64748b" font-size="8">有记录可查</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在业务代码中直接操作 OSS 客户端而不经过封装层</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 测试钉死</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_o0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">桶的创建删改归运维 · STS 归云平台 · CDN 域名归运维</text>
</svg>
```

## 一、/oss-toolkit 解决的是什么问题

### 1.1 裸写 OSS 的五种事故

对象存储（阿里云 OSS / AWS S3 / MinIO / 腾讯 COS / 华为 OBS）的 SDK 用起来简单——但裸用几乎必然踩坑：

1. **大文件直接 PUT**：几百 MB 的文件一次性上传，网络一抖全都重来——没有分片、没有断点续传，传一半失败只能从头再来；
2. **预签名 URL 永久有效**：给客户端直传/下载的 URL 没有有效期——链接被转发出去，等于把存储桶的钥匙给了别人；
3. **上传没有统一入口**：每个业务自己 new 一个 OSS 客户端、自己拼 URL——域名、桶名散落各处，换存储服务要改全项目；
4. **图片处理靠写死的尺寸**：头像、商品图、封面图尺寸硬编码在业务代码里——改一个尺寸要改 N 处；
5. **CDN 缓存不失效**：文件更新了，CDN 还服务着旧版本——用户刷新十遍看到的还是旧图，运维只能人工去刷。

`/oss-toolkit` 为项目生成对象存储基础设施代码：统一存储接口（上传/下载/删除/列表/预签名URL）、分片上传/断点续传、图片处理（缩略图/水印/裁剪）、CDN 刷新。**禁止在业务代码中直接操作 OSS 客户端而不经过封装层**。

### 1.2 五个子命令，一条完整的链路

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `upload` | 上传/下载/删除 | 直接操作 OSS 客户端上传文件 |
| `presign` | 预签名 URL | 需要客户端直传或临时下载 |
| `multipart` | 分片上传/断点续传 | 大文件上传（>100MB） |
| `image` | 图片处理 | 上传图片需要缩略图/水印 |
| `cdn` | CDN 刷新 | 文件更新后需要刷新 CDN |
| `all`（默认） | 全部组件 | 首次引入对象存储 |

五个子命令覆盖文件全生命周期：**upload 让文件进得去出得来，multipart 让大文件传得完、断了能续，presign 让第三方拿得到、但过期就失效，image 让图处理得统一、不硬编码，cdn 让更新立刻见效、不缓存旧版**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_o1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">文件全生命周期五环</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_o1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">去：统一入口上传</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9"><100MB 普通 · >=100MB 自动分片</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">达：分片可断点续传</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">5MB/片 · uploadId 记录 · MD5 校验</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o1)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">取：预签名限时领取</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">GET 下载 1h~7d · PUT 上传 30min</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_o1)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">美：图片模板处理</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">头像/商品图/封面图模板 · 不硬编码</text>
  <rect x="180" y="225" width="440" height="0" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_o1)"/>
</svg>
```

### 1.3 为什么"上传必须自动选路"

`StorageService` 的关键设计是：**上传自动判断文件大小**——小于 100MB 走普通上传，大于等于 100MB 走分片上传。业务代码只调 `upload(file)`，选路是封装层的事。

- 让业务手动选路 → 总有开发忘了大文件要分片 → 传一半失败从头再来；
- 自动选路 + 分片断点续传 → 大文件传断网了，下次从断点继续，不重传。

### 1.4 为什么"预签名 URL 必须有有效期"

预签名 URL 的本质是"**把存储桶的钥匙借给第三方用一会儿**"：

- GET（下载）：客户端直连 OSS 下载，有效期 1h~7d——用户点开链接后下载，链接过期即失效；
- PUT（上传）：客户端直连 OSS 直传，有效期 30min——前端选完文件直接传，不用经过后端；
- **没有有效期 = 永久钥匙**：链接被转发、被爬虫抓取，等于桶里的文件裸奔。所以"禁止预签名 URL 无有效期（永久有效)"是绝对红线。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/oss-toolkit [upload|presign|multipart|image|cdn|all]`；
- 自动加载：coding-skill 阶段检测到 OSS/S3/MinIO/COS/OBS 客户端代码时。

**前置条件**：

1. 已识别技术栈 + 存储服务（阿里云 OSS / AWS S3 / MinIO / 腾讯 COS / 华为 OBS）；
2. 已确定目标包路径（按语言规范）；
3. 已确定存储桶名称和区域；
4. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**封装层生成的是"按你的存储服务和桶的代码"**——不知道用哪个云、哪个桶、哪个区域，生成的封装连配置都写不对。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定要生成哪些组件：

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `upload` | 上传/下载/删除 | 直接操作 OSS 客户端上传文件 |
| `presign` | 预签名 URL | 需要客户端直传或临时下载 |
| `multipart` | 分片上传/断点续传 | 大文件上传（>100MB） |
| `image` | 图片处理 | 上传图片需要缩略图/水印 |
| `cdn` | CDN 刷新 | 文件更新后需要刷新 CDN |
| `all`（默认） | 全部组件 | 首次引入对象存储 |

Step 1 是"按需裁剪"：只有上传没分片？生成 multipart。需要客户端直传？生成 presign。首次引入？生成 all。**缺什么补什么，检测条件同时是体检**——裸操作 OSS 客户端、大文件普通上传、无 CDN 刷新，都会被照出来。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/oss/` 包：

```
{basePackage}/oss/
├── core/
│   ├── StorageService.java             — 统一存储接口（上传/下载/删除/列表/预签名）
│   ├── StorageProperties.java          — 配置（endpoint/bucket/region）
│   └── StorageFile.java                — 文件元数据（文件名/大小/MD5/URL）
├── upload/
│   ├── FileUploader.java               — 文件上传器（普通/分片自动选择）
│   ├── MultipartUploader.java          — 分片上传（初始化/上传分片/完成/取消）
│   └── UploadTokenGenerator.java       — 上传凭证（STS 临时凭证）
├── download/
│   ├── FileDownloader.java             — 文件下载器（普通/断点续传）
│   └── PresignedUrlGenerator.java      — 预签名 URL 生成
├── image/
│   ├── ImageProcessor.java             — 图片处理（缩略图/水印/裁剪/格式转换）
│   └── ImageProcessTemplate.java       — 图片处理模板（头像/商品图/封面图）
├── cdn/
│   ├── CdnRefreshService.java          — CDN 刷新（文件/目录/全站）
│   └── CdnPrefetchService.java         — CDN 预热
└── config/
    └── OssAutoConfig.java              — 自动配置
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`StorageService`**：统一接口封装所有存储操作，上传自动判断文件大小选择普通上传（<100MB）或分片上传（>=100MB），上传返回完整 URL（含 CDN 域名）——**业务只调 upload(file)，选路是封装层的事**；
- **分片上传**：分片大小 5MB（除最后一片），最大 10000 片，支持断点续传（记录 uploadId 到本地/数据库），上传完成自动校验 MD5——**传得完、断了能续、完了有验**；
- **预签名 URL**：支持 GET（下载，有效期 1h~7d）和 PUT（上传，有效期 30min），支持自定义文件名和 Content-Type——**临时凭证，过期作废**；
- **图片处理**：对上传的图片自动生成缩略图（大/中/小三种尺寸），水印（文字/图片），裁剪（按比例/按尺寸），格式转换（webp 自动判断）——**模板驱动，尺寸不硬编码**；
- **CDN 刷新**：单个文件刷新（URL）、目录刷新（批量）、全站刷新（紧急），支持刷新记录查询；CDN 预热（预先把内容推送到 CDN 节点）——**刷新有记录，急事能全站**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_o2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">最容易搞错的两个约束</text>
  <rect x="40" y="50" width="340" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">分片：5MB/片 · 10000 片</text>
  <text x="210" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">uploadId 存本地/数据库</text>
  <text x="210" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">断点续传从断点恢复</text>
  <text x="210" y="136" text-anchor="middle" fill="#64748b" font-size="8">完成自动校验 MD5</text>
  <rect x="420" y="50" width="340" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_o2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">预签名：终生有效 = 裸奔</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">GET 下载 1h~7d</text>
  <text x="590" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">PUT 上传 30min</text>
  <text x="590" y="136" text-anchor="middle" fill="#64748b" font-size="8">链接被转发 = 桶的钥匙被复制</text>
  <rect x="120" y="175" width="560" height="60" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o2)"/>
  <text x="400" y="200" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">大文件普通上传 = 传一半全重来</text>
  <text x="400" y="224" text-anchor="middle" fill="#94a3b8" font-size="9">>=100MB 自动切分片，业务无感</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束（用 Mock 存储服务测试）：

- **存储服务**：验证上传/下载/删除/列表/预签名 URL 五种操作；
- **分片上传**：验证初始化/上传分片/完成/取消，验证断点续传恢复——**断点续传必须测：中断后从断点恢复，不从头传**；
- **预签名 URL**：验证 URL 生成、有效期、访问控制——**有效期边界必须测：过期后访问被拒**。

测试的作用是把约束**钉死**：自动选路、断点续传、有效期限制——红线靠测试保证，不靠 review 保证。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 所有文件操作经过 `StorageService` 封装（非直接操作 OSS 客户端）
   - [ ] 上传自动判断普通/分片（非手动选择）
   - [ ] 分片上传支持断点续传
   - [ ] 预签名 URL 有有效期限制（非永久有效）
   - [ ] 图片处理自动生成缩略图
   - [ ] CDN 刷新有刷新记录查询

六条清单对应六道红线复查：**统一入口、自动选路、断点可续、限时凭证、模板出图、刷新留痕**——六项全过，对象存储基础设施才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 编译通过 + 测试通过；
- 所有文件操作经过 `StorageService` 封装；
- 上传自动选择普通/分片；
- 分片上传支持断点续传；
- 预签名 URL 有有效期；
- 图片处理自动生成缩略图。

**❌ 绝对不能做**：

- 禁止直接操作 OSS 客户端（应经过 `StorageService`）；
- 禁止预签名 URL 无有效期（永久有效 = 把桶钥匙送人）；
- 禁止大文件普通上传（应使用分片上传）；
- 禁止图片处理硬编码尺寸（应使用配置模板）。

门禁的逻辑是一次"**文件的安全与顺畅**"：

- 统一入口管住"谁在碰文件"——换存储服务不用改业务；
- 自动分片管住"大文件能不能传完"——断网不重来；
- 有效期管住"链接会不会被滥用"——钥匙按分钟借出；
- 模板出图管住"尺寸改起来麻不麻烦"——改模板不改代码。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_o3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四道红线 = 四类事故</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">禁裸操作：配置散落</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">换存储服务要改全项目</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">禁永久 URL：钥匙复制</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">链接被转发 = 文件裸奔</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">禁大文件直传：半途全废</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">>=100MB 必须分片</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">禁硬编码尺寸：改不动</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">图片模板配置驱动</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">文件要进得去、传得完、拿得到、美得统一、更新得即时</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不生成存储桶创建/删除操作**——由运维管理（桶的生死是运维的事）；
- **不修改已有的 OSS 代码**——只新增封装层，覆盖别人的代码等于破坏别人的稳定性；
- **不引入未确认的 OSS SDK 版本**——项目中已有的优先复用；
- **不生成 STS 服务端代码**——由云平台提供（STS 临时凭证是云的能力，不是项目代码）；
- **不生成 CDN 域名配置**——由运维团队管理。

边界一句话：**本技能管"文件怎么存取、怎么处理"，不碰"桶怎么建删、凭证怎么签发、域名怎么配"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 上传/下载/分片/预签名/图片/CDN | **本技能**（`/oss-toolkit`） |
| 大文件分片思路复用（非对象存储） | `file-upload` 通用封装（如有） |
| 文件访问权限与令牌 | `security-toolkit`（鉴权上下文） |

- **本技能 vs security-toolkit**：oss-toolkit 管"文件存取的手续"（预签名时效、STS 凭证）；security-toolkit 管"谁能访问"（登录态、鉴权上下文）——一个管文件，一个管人；
- **本技能 vs 存储选型决策**：用哪个对象存储服务（OSS/S3/MinIO/COS/OBS）是架构决策，本技能只负责把选定服务的封装层生成对。

## 七、完成标志

`/oss-toolkit` 的完成标志有三个：

1. **目标组件已生成**：upload/presign/multipart/image/cdn 按需产出——该有的都有；
2. **测试通过 + 检查清单全过**：六条红线逐项确认——不是"写了"，是"验证了"；
3. **业务代码不再直接操作 OSS 客户端**：所有文件操作经过 `StorageService` 封装——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再裸写）？——三个都答"是"，对象存储封装才算真正落地。

## 八、写在最后

`/oss-toolkit` 的全部设计，浓缩成四句话：

1. **统一接口管入口**——业务只调 upload(file)，选路、续传、验 MD5 都是封装层的事。
2. **限时凭证管安全**——预签名 URL 有有效期，钥匙按分钟借出，过期作废。
3. **模板出图管统一**——缩略图/水印/裁剪走模板配置，尺寸改模板不改代码。
4. **刷新留痕管一致**——文件更新后 CDN 刷新，有记录可查，老版本不留。

一句话记住它：**/oss-toolkit 是对象存储的"五件套管家"——统一接口上传下载、分片断点续传、限时预签名 URL、模板图片处理、留痕 CDN 刷新，用"不裸写、不直传、不过期、不硬编、不留旧"五条纪律，让文件进得去、传得完、拿得到、美得统一、更新得即时。**