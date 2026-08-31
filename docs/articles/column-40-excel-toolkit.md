# /excel-toolkit：Excel 处理封装——大数据量导出不会 OOM，才是合格的导出器

> 命令深度拆解 · 第 40 篇 · 约 8000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_x0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_x0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_x0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/excel-toolkit：Excel 四件套</text>
  <rect x="40" y="55" width="340" height="95" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_x0)"/>
  <text x="210" y="80" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">导出侧</text>
  <text x="210" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">SimpleExporter（<1万行）</text>
  <text x="210" y="126" text-anchor="middle" fill="#94a3b8" font-size="10">BigDataExporter（SXSSF 流式）</text>
  <text x="210" y="148" text-anchor="middle" fill="#64748b" font-size="9">TemplateExporter（模板填充）</text>
  <rect x="420" y="55" width="340" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_x0)"/>
  <text x="590" y="80" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">导入侧</text>
  <text x="590" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">StreamImporter（流式读取）</text>
  <text x="590" y="126" text-anchor="middle" fill="#94a3b8" font-size="10">ImportValidator（行级校验）</text>
  <text x="590" y="148" text-anchor="middle" fill="#64748b" font-size="9">错误行下载可修正</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在业务代码中直接操作 POI 而不经过封装层</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_x0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_x0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_x0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 生成测试</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_x0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">大数据量必 SXSSF · 导入先全校验再入库 · 错误行可下载修正</text>
</svg>
```

## 一、/excel-toolkit 解决的是什么问题

### 1.1 Excel 处理最常见的三个翻车现场

每个做过后台系统的团队都见过这三个现场：

1. **导出 OOM**：用户点了"导出全部"，服务端用 XSSFWorkbook 把 50 万行全装进内存——JVM 瞬间爆掉。**`XSSFWorkbook` 是内存模型，数据全在堆里；`SXSSFWorkbook` 是流式模型，边写边刷盘**——选错了模型，百万行导出就是自爆现场；
2. **导入校验拉胯**：用户上传 1 万行的 Excel，第 1 行就错了——导入器立刻停下报错。用户改完第 1 行再传，第 2 行又错……一万行的表要传一万次。**校验应该收集所有错误行，一次性返回**；
3. **模板样式丢失**：导出要求带公司 Logo、固定表头样式——程序硬编码样式一改需求就崩。

`/excel-toolkit` 就是给项目的 Excel 导入导出生成基础设施：模板导出、大数据量分批导出、导入校验（行级别错误收集）、动态列/合并单元格。**禁止在业务代码中直接操作 POI 而不经过封装层**。

### 1.2 五个子命令，覆盖两条链路

| 子命令 | 组件 | 用在什么场景 |
|--------|------|-------------|
| `export` | 导出器 | 有导出需求 |
| `import` | 导入器+校验 | 有导入需求 |
| `template` | 模板导出 | 导出需要固定样式模板 |
| `bigdata` | 大数据量导出 | 导出数据量 > 10000 行 |
| `all`（默认） | 全部组件 | 首次引入 Excel 处理 |

两条链路很清楚：**导出链**（普通/大数据/模板）把数据变成 Excel；**导入链**（流式读 + 校验 + 错误下载）把 Excel 变成数据。两条链路各有各的坑：

- 导出链的坑是**内存**（大数据量 OOM）和**样式**（模板丢失）；
- 导入链的坑是**体验**（遇到第一个错就停）和**数据安全**（逐行写库，半途失败留下半成品数据）。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_x1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_x1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_x1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">两条链路，两种坑</text>
  <rect x="40" y="50" width="340" height="120" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_x1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">导出链：数据 → Excel</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">普通导出 &lt;1万行 XSSF ✓</text>
  <text x="210" y="120" text-anchor="middle" fill="#94a3b8" font-size="10">大数据量 ≥1万行 SXSSF 流式 ✓</text>
  <text x="210" y="142" text-anchor="middle" fill="#64748b" font-size="9">模板导出保留原始样式 ✓</text>
  <text x="210" y="162" text-anchor="middle" fill="#64748b" font-size="8">坑：内存 OOM / 样式丢失</text>
  <rect x="420" y="50" width="340" height="120" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_x1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">导入链：Excel → 数据</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">流式读取逐行处理 ✓</text>
  <text x="590" y="120" text-anchor="middle" fill="#94a3b8" font-size="10">行级校验收集所有错误 ✓</text>
  <text x="590" y="142" text-anchor="middle" fill="#64748b" font-size="9">校验全过才写库（事务） ✓</text>
  <text x="590" y="162" text-anchor="middle" fill="#64748b" font-size="8">坑：遇错即停 / 半成品数据</text>
  <text x="400" y="232" text-anchor="middle" fill="#475569" font-size="10">选型不是看喜好，是看数据量和链路——1 万行是普通/大数据的分界线</text>
</svg>
```

### 1.3 为什么 10000 行是分水岭

`ExcelExporter` 的规则：数据量 < 10000 行用 XSSFWorkbook（内存中生成），>= 10000 行自动切换 SXSSFWorkbook（流式写入磁盘）。

为什么是 10000？因为 XSSF 每行单元格对象开销大，一万行 × 每行 10 列，内存里就有上百万个对象——再往上就是 GC 风暴。**10000 行是经验阈值，不是数学定理**，但它是"内存模型 vs 流式模型"的安全边界：小数据用内存模型（快、支持随机访问），大数据自动切流式（省内存、永不 OOM）。

这是 toolkit 的核心设计哲学：**把"选对模型"做成自动化，而不是靠程序员记规则**——程序员只需要写业务，模型选择由封装层决定。

## 二、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定要生成哪些组件：

| 子命令 | 组件 | 检测条件 |
|--------|------|--------|
| `export` | 导出器 | 有导出需求 |
| `import` | 导入器+校验 | 有导入需求 |
| `template` | 模板导出 | 导出需要固定样式模板 |
| `bigdata` | 大数据量导出 | 导出数据量 > 10000 行 |
| `all`（默认） | 全部组件 | 首次引入 Excel 处理 |

Step 1 是"按需裁剪"：导出需求就生成导出器，导入需求就生成导入器+校验，要模板加 template，量巨大加 bigdata——**缺什么补什么，首次才 all**。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/excel/` 包：

```
{basePackage}/excel/
├── core/     ExcelExporter / ExcelImporter / ExcelColumn 注解 / ExcelError
├── export/   SimpleExporter / BigDataExporter / TemplateExporter / StyleConfig
├── import/   StreamImporter / ImportValidator / ImportResult
├── config/   ExcelConfig（大数据阈值/默认列宽/最大行数）
└── web/      ExcelWebSupport（响应封装/下载头/进度回调）
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`ExcelExporter`**：数据量 < 10000 行用 XSSFWorkbook（内存中生成），>= 10000 行自动切换 SXSSFWorkbook（流式写入磁盘），支持自定义列顺序、列宽自适应、日期格式化——**模型切换自动化，程序员无感**；
- **`TemplateExporter`**：加载 .xlsx 模板文件，定位占位符（`{varName}`）替换为数据，保留模板样式（字体/颜色/边框/合并单元格），支持列表扩展（`{list}` 标记自动扩展行）——样式来自模板文件，不是硬编码；
- **大数据量导出**：SXSSF 窗口大小 100 行（内存中保持 100 行，其余刷到磁盘），每 1000 行刷一次磁盘，避免 OOM——**窗口 100 + 批刷 1000** 是防 OOM 的双保险；
- **导入校验**：行级别校验（必填/数据类型/长度/枚举值/唯一性），收集所有错误行后统一返回（不遇到第一个错误就停止），校验通过后才写入数据库（事务保证）——**先全校验，再一次性入库**；
- **导入错误 Excel 下载**：将错误行+错误信息写入新 Excel，提供下载，方便用户修正后重新导入——**错误可视化，用户知道错在哪一行哪一列**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_x2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_x2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_x2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">导出链的内存防线</text>
  <rect x="40" y="50" width="340" height="75" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_x2)"/>
  <text x="210" y="70" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">&lt;1万行 → XSSFWorkbook</text>
  <text x="210" y="92" text-anchor="middle" fill="#94a3b8" font-size="9">内存中生成，快速随机访问</text>
  <text x="210" y="112" text-anchor="middle" fill="#64748b" font-size="8">小数据用内存模型：快</text>
  <rect x="420" y="50" width="340" height="75" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_x2)"/>
  <text x="590" y="70" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">≥1万行 → SXSSFWorkbook</text>
  <text x="590" y="92" text-anchor="middle" fill="#94a3b8" font-size="9">窗口 100 行，每 1000 行刷盘</text>
  <text x="590" y="112" text-anchor="middle" fill="#64748b" font-size="8">大数据用流式模型：省内存不 OOM</text>
  <rect x="40" y="145" width="340" height="75" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_x2)"/>
  <text x="210" y="165" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">TemplateExporter：样式归模板</text>
  <text x="210" y="187" text-anchor="middle" fill="#94a3b8" font-size="9">{varName} 占位符替换 + {list} 扩展</text>
  <text x="210" y="207" text-anchor="middle" fill="#64748b" font-size="8">样式来自 .xlsx 模板，不硬编码</text>
  <text x="420" y="145" width="340" height="75" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_x2)"/>
  <text x="590" y="165" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">自动切换是核心体验</text>
  <text x="590" y="187" text-anchor="middle" fill="#94a3b8" font-size="9">程序员只写业务，模型封装层定</text>
  <text x="590" y="207" text-anchor="middle" fill="#64748b" font-size="8">选错模型 = 百万行导出即自爆</text>
  <text x="400" y="248" text-anchor="middle" fill="#475569" font-size="10">把"选对模型"做成自动化，而不是靠程序员记规则</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束：

- **导出**：验证普通导出、大数据量自动切换、列宽自适应、日期格式化；
- **模板导出**：验证占位符替换、列表扩展、样式保留；
- **导入**：验证流式读取、行级别校验、错误收集、错误 Excel 生成。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 导出自动选择普通/大数据量模式
   - [ ] 大数据量导出使用 SXSSF 流式写入
   - [ ] 导入校验在内存中完成（非逐行写入数据库再校验）
   - [ ] 导入错误行可下载修正
   - [ ] 模板导出保留样式

这五条检查清单对应五个"防翻车点"：**自动选模型（防 OOM）、SXSSF（防内存爆）、先校验后入库（防半成品）、错误可下载（防体验崩）、模板保样式（防需求崩）**——五项全过，Excel 处理才算合格。

## 三、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 导出自动选择普通/大数据量模式；
- 大数据量导出使用 SXSSF 流式写入；
- 导入校验在内存中完成（非逐行写入数据库再校验）；
- 导入错误行可下载修正；
- 模板导出保留样式。

**❌ 绝对不能做**：

- 禁止大数据量导出使用 XSSFWorkbook（会 OOM）；
- 禁止导入时逐行写入数据库（应全部校验通过后一次性写入）；
- 禁止导入遇到第一个错误就停止（应收集所有错误）；
- 禁止模板导出硬编码样式（应使用模板文件）。

门禁的分水岭是"**用户体验和系统稳定性**"：

- OOM 是系统的命——大数据量必须流式；
- 半成品数据是数据的命——必须全部校验过再入库；
- 遇错即停是用户的命——一次传一万次要一万次，人早疯了；
- 硬编码样式是维护的命——改个 Logo 就要改代码。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_x3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_x3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_x3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四条红线 = 四条命</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_x3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">大数据用 XSSF = 系统的命</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">几百 MB 全进堆，JVM 当场爆掉</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_x3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">逐行写库 = 数据的命</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">第 5000 行崩了，前 4999 行已是半成品</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_x3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">遇错即停 = 用户的命</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">一万行传一万次，用户先疯</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_x3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">硬编码样式 = 维护的命</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">改个 Logo 就要动代码，需求方不理解</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">四条红线不是技术偏好，是四条命的底线</text>
</svg>
```

## 四、四条约束：边界在哪里

- **不生成 Excel 模板文件**——用户提供模板，技能填充数据——模板的样式是业务方的审美，AI 不越俎代庖；
- **不修改已有的 Excel 处理代码**——只新增封装层，建议用户逐步迁移；
- **不引入未确认的 Excel 库版本**——库选型是架构决策；
- **不生成 Word/PDF 处理代码**——只处理 Excel，其他文档格式是别的技能的活。

边界一句话：**本技能生成"Excel 怎么读写"的管道，不碰"模板长什么样"和"除了 Excel 以外的文档"**。

## 五、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| Excel 导入导出封装 | **本技能**（`/excel-toolkit`） |
| 导出数据来源查询 | `coding-skill`（写查询与序列化） |
| 导出结果接口链路 | `harness-e2e`（主链路验证） |

- **本技能 vs coding-skill**：一个生成 Excel 管道，一个写业务查询——管道先把"流式/校验/错误收集"钉死，业务代码只关心查什么数据；
- **本技能 vs harness-e2e**：导出是不是真能下载、导入是不是真能入库，最终由端到端链路验证。

## 六、完成标志

`/excel-toolkit` 的完成标志有三个：

1. **目标组件已生成**：导出/导入/模板/大数据按需产出，模型自动切换逻辑就位——该有的都有；
2. **测试通过 + 检查清单全过**：导出自动切换、SXSSF 流式、先校验后入库、错误可下载、模板保样式五项全过——不是"写了"，是"验证了"；
3. **业务代码不再直接操作 POI**：所有 Excel 读写经过封装层（或已给出迁移建议）——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再裸操作）？——三个都答"是"，Excel 基础设施才算真正落地。

## 七、写在最后

`/excel-toolkit` 的全部设计，浓缩成四句话：

1. **选对内存模型是 Excel 处理的生死线**——大数据量用 XSSF 就是自爆，SXSSF 流式才是出路。
2. **先校验后入库，收集所有错误**——不让用户为一个错误传一万次，不让半成品数据进库。
3. **样式归模板，不硬编码**——改需求不动代码，改模板就行。
4. **错误行可下载**——用户知道错在哪一行哪一列，修正后重传。

一句话记住它：**/excel-toolkit 是 Excel 导入导出的"基建队"——它生成流式导出、模板导出、行级校验、错误下载四件套，用"大数据必流式、导入先校验、样式归模板"三条纪律，让 Excel 处理既不 OOM、不半成品，也不让用户传一万次。**