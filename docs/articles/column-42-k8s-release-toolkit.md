# /k8s-release-toolkit：K8s 发布封装——无探针、无资源限制，就没有资格上线

> 命令深度拆解 · 第 42 篇 · 约 8000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_k0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_k0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_k0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/k8s-release-toolkit：K8s 发布四件套</text>
  <rect x="40" y="55" width="140" height="90" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="110" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">deploy</text>
  <text x="110" y="104" text-anchor="middle" fill="#64748b" font-size="9">Deployment+Service</text>
  <text x="110" y="126" text-anchor="middle" fill="#64748b" font-size="8">replicas≥2 滚动更新</text>
  <rect x="200" y="55" width="140" height="90" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="270" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">hpa</text>
  <text x="270" y="104" text-anchor="middle" fill="#64748b" font-size="9">自动扩缩容</text>
  <text x="270" y="126" text-anchor="middle" fill="#64748b" font-size="8">CPU70% 内存80%</text>
  <rect x="360" y="55" width="140" height="90" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="430" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">probe</text>
  <text x="430" y="104" text-anchor="middle" fill="#64748b" font-size="9">探针三件套</text>
  <text x="430" y="126" text-anchor="middle" fill="#64748b" font-size="8">startup/liveness/readiness</text>
  <rect x="520" y="55" width="140" height="90" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="590" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">rollout</text>
  <text x="590" y="104" text-anchor="middle" fill="#64748b" font-size="9">灰度+回滚</text>
  <text x="590" y="126" text-anchor="middle" fill="#64748b" font-size="8">金丝雀10%→100%</text>
  <rect x="680" y="55" width="100" height="90" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="730" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">严禁</text>
  <text x="730" y="104" text-anchor="middle" fill="#64748b" font-size="9">latest 标签</text>
  <text x="730" y="126" text-anchor="middle" fill="#64748b" font-size="8">无限制部署</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在无探针、无资源限制的情况下部署到 K8s</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成清单</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 校验</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_k0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 交付</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">脚本只生成不自动执行 · Ingress/Secret/PV 归专属团队管</text>
</svg>
```

## 一、/k8s-release-toolkit 解决的是什么问题

### 1.1 手写 K8s 清单的六个翻车点

K8s 的 Deployment 看起来就是几十行 YAML，但手写最容易翻车：

1. **镜像 `latest`**：有人图省事写 `image: my-app:latest`——哪天有人重新打了一个 latest，线上静默升级，回滚都找不到旧版本。**`latest` 标签禁止使用**；
2. **无资源限制**：Deployment 不写 requests/limits——节点上所有 Pod 抢资源，一个热点把整台机器打爆；
3. **无探针**：没有 liveness/readiness——服务启动慢，流量一来全打到还没就绪的 Pod 上；进程僵死，K8s 也不知道重启；
4. **单副本上生产**：replicas=1，一台节点挂了服务就没了；
5. **无灰度无回滚**：新版本直接全量，出了事才发现没有回滚预案；
6. **环境混用**：dev 的配置直接铺到 prod——环境差异没人管。

`/k8s-release-toolkit` 生成 Kubernetes 部署清单和发布脚本：Deployment 模板、HPA 配置、探针配置、灰度发布策略、回滚检查脚本。**禁止在无探针、无资源限制的情况下部署到 K8s**。

### 1.2 四个子命令，从"能跑"到"能扛"

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `deploy` | Deployment + Service | 无 `deployment.yaml` |
| `hpa` | HPA 配置 | 无 `hpa.yaml` 或副本数 > 2 |
| `probe` | 探针配置 | 无 `livenessProbe` / `readinessProbe` |
| `rollout` | 灰度发布 + 回滚脚本 | 首次部署或生产环境 |
| `all`（默认） | 全部组件 | 首次引入 K8s 部署 |

递进关系很清晰：**deploy 让服务"跑起来"，probe 让服务"活得好"，hpa 让服务"扛得住"，rollout 让发布"退得了"**——从能跑到能扛再到能退，一层比一层要求高。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_k1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_k1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_k1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">从能跑到能退的四层能力</text>
  <rect x="40" y="50" width="340" height="75" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_k1)"/>
  <text x="210" y="70" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">deploy：跑起来</text>
  <text x="210" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">replicas≥2 · RollingUpdate · 资源限制</text>
  <text x="210" y="114" text-anchor="middle" fill="#64748b" font-size="8">镜像标签禁止 latest</text>
  <rect x="420" y="50" width="340" height="75" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_k1)"/>
  <text x="590" y="70" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">probe：活得好</text>
  <text x="590" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">startup 慢启动保护</text>
  <text x="590" y="114" text-anchor="middle" fill="#64748b" font-size="8">liveness 轻量自检 / readiness 依赖就绪</text>
  <rect x="40" y="145" width="340" height="75" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_k1)"/>
  <text x="210" y="165" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">hpa：扛得住</text>
  <text x="210" y="187" text-anchor="middle" fill="#94a3b8" font-size="9">min=2 max=10 · CPU70% 内存80%</text>
  <text x="210" y="207" text-anchor="middle" fill="#64748b" font-size="8">缩放冷却：扩 60s 缩 300s</text>
  <rect x="420" y="145" width="340" height="75" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_k1)"/>
  <text x="590" y="165" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">rollout：退得了</text>
  <text x="590" y="187" text-anchor="middle" fill="#94a3b8" font-size="9">金丝雀 10% → 监控 → 100%</text>
  <text x="590" y="207" text-anchor="middle" fill="#64748b" font-size="8">回滚前检查：镜像/兼容/历史/HPA</text>
  <text x="400" y="242" text-anchor="middle" fill="#475569" font-size="10">发布不是"能上线"，是"上线了能活、出事了能退"</text>
</svg>
```

### 1.3 为什么"不能自动执行部署"

SKILL.md 明确：**脚本只生成，不自动执行**。理由和数据库迁移一样：

- 部署是**动生产环境**的操作，涉及流量切换、蓝绿切换、灰度观察——必须有人的审批、窗口期、观察环节；
- AI 生成的是"地图"（清单+脚本），人类决定"什么时候开车、走哪条路"；
- 拆开职责：AI 把清单写对、把检查做透，人类挑时机执行、盯监控收尾。

### 1.4 为什么用 Kustomize 的组织结构

`base/ + overlays/` 是 Kustomize 的标准组织方式：

- `base/` 放服务本身的通用配置（Deployment/Service/HPA 模板）；
- `overlays/{env}/` 放各环境的差异覆盖（dev 副本数少、prod 带 HPA）；
- 环境差异**显式分层**，而不是三个独立的 deployment.yaml 各自飘——改通用配置只改 base，各环境自动继承。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/k8s-release-toolkit [deploy|hpa|probe|rollout|all]`；
- 自动加载：coding-skill 阶段检测到 `k8s/` / `deployment.yaml` / `Dockerfile` 时。

**前置条件**：

1. 已确定服务名称、镜像名称、端口号；
2. 已确定部署环境（开发/测试/生产）对应的副本数和资源规格；
3. 已确定是否启用 HPA（自动扩缩容）；已确定探针路径（health check endpoint）；
4. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**连服务名、镜像名、端口都不知道的 K8s 清单，是瞎写**。四样要素齐了，清单才有谱。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定生成哪些组件：

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `deploy` | Deployment + Service | 无 `deployment.yaml` |
| `hpa` | HPA 配置 | 无 `hpa.yaml` 或副本数 > 2 |
| `probe` | 探针配置 | 无 `livenessProbe` / `readinessProbe` |
| `rollout` | 灰度发布 + 回滚脚本 | 首次部署或生产环境 |
| `all`（默认） | 全部组件 | 首次引入 K8s 部署 |

Step 1 是"按需裁剪"：已有 Deployment 只缺探针？生成 probe。首次引入 K8s？生成 all。**缺什么补什么，不重复造轮子**；同时"检测条件"也承担了"体检"功能——没有探针、没有 HPA、生产单副本，都会被照出来。

### Step 2: 生成清单文件

以 Kustomize 组织目录：

```
k8s/
├── base/
│   ├── deployment.yaml          — Deployment 模板
│   ├── service.yaml             — Service 模板
│   ├── hpa.yaml                 — HPA 配置
│   └── kustomization.yaml       — Kustomize 入口
├── overlays/
│   ├── dev/                     — 开发环境副本数/资源覆盖
│   ├── staging/                 — 测试环境覆盖
│   └── prod/                    — 生产环境覆盖 + HPA
├── scripts/
│   ├── rollout-check.sh         — 回滚前检查脚本
│   └── canary-deploy.sh         — 金丝雀发布脚本
└── README.md                    — 部署说明
```

**每个文件的铁打约束**——本技能绝不妥协的点：

- **`deployment.yaml`**：replicas ≥ 2（高可用），RollingUpdate 滚动更新——生产单副本直接违规；
- **镜像标签**：必须使用具体版本号，**禁止 `latest`**——`latest` 每次重新打 tag 都会静默改变线上内容，回滚无从谈起；
- **资源限制**：所有容器必须有 requests + limits——没有限制，一个热点 Pod 能把整台节点打爆；
- **探针**：所有 Deployment 必须有完整探针——livenessProbe（存活，进程僵死可重启）+ readinessProbe（就绪，流量只进就绪 Pod），另配 startupProbe 保护慢启动应用；
- **HPA**：min=2、max=10，CPU 目标 70%、内存目标 80%，缩放冷却时间（扩容 60s、缩容 300s）——CPU 70% 触发扩容，冷却时间防止抖动；
- **回滚脚本 + 灰度脚本**：生产环境必须有——没有回滚预案的发布不是发布，是赌博。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_k2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_k2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_k2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">清单的五条红线</text>
  <rect x="40" y="50" width="340" height="60" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_k2)"/>
  <text x="210" y="70" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">禁止 latest 标签</text>
  <text x="210" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">重新打 tag = 线上静默变更</text>
  <rect x="420" y="50" width="340" height="60" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_k2)"/>
  <text x="590" y="70" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">禁止无资源限制</text>
  <text x="590" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">一个热点 Pod 打爆节点</text>
  <rect x="40" y="130" width="340" height="60" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_k2)"/>
  <text x="210" y="150" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">禁止无探针</text>
  <text x="210" y="174" text-anchor="middle" fill="#94a3b8" font-size="9">流量打进未就绪的 Pod</text>
  <rect x="420" y="130" width="340" height="60" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_k2)"/>
  <text x="590" y="150" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">禁止生产单副本</text>
  <text x="590" y="174" text-anchor="middle" fill="#94a3b8" font-size="9">一台节点挂了服务就没了</text>
  <rect x="180" y="210" width="440" height="40" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_k2)"/>
  <text x="400" y="236" text-anchor="middle" fill="#93c5fd" font-size="11">无回滚预案的发布 = 赌博</text>
  <text x="400" y="272" text-anchor="middle" fill="#475569" font-size="10">红线不是风格偏好，是事故清单</text>
</svg>
```

### Step 3: 校验

1. **`kubectl apply --dry-run=client -f k8s/` 通过**——清单语法、schema 无错；
2. **检查清单逐项确认**：
   - [ ] 所有镜像标签使用具体版本号（非 latest）
   - [ ] 所有容器有 requests + limits
   - [ ] 所有 Deployment 有完整探针配置
   - [ ] 生产环境 replica ≥ 2
   - [ ] 回滚脚本和灰度发布脚本已生成

校验是"纸上验收"：dry-run 保证清单能进集群，清单保证红线没踩。**两关都过，清单才交付**。

### Step 4: 交付与说明

生成 README.md 说明部署步骤：环境差异、回滚步骤、灰度流程、常用 kubectl 命令。脚本只生成、不自动执行——把执行时机和人的审批留在流程里。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- `kubectl apply --dry-run=client` 通过；
- 镜像标签使用具体版本号；
- 容器有 requests + limits；
- Deployment 有完整探针；
- 生产环境 replica ≥ 2；
- 回滚脚本和灰度发布脚本已生成。

**❌ 绝对不能做**：

- 禁止 `latest` 镜像标签；
- 禁止无资源限制的容器；
- 禁止无探针的 Deployment；
- 禁止生产环境单副本。

门禁的共同点：**全都是"上线后无法轻易补救"的事项**。

- `latest` 是"回滚失效"——旧版本找不回来；
- 无资源限制是"雪崩起点"——一个热点拖垮整台节点，连带其他服务；
- 无探针是"服务假死"——Pod 还在，请求全挂；
- 单副本是"单点故障"——节点一挂服务就断。

所以门禁不是审美，是**事故清单的事前预防**：现在多写两行 YAML，比上线后凌晨三点爬起来救火便宜得多。

## 五、四条约束：边界在哪里

- **不生成 Ingress/证书配置**——由网关团队管理；
- **不生成 ConfigMap/Secret 中的敏感数据**——只生成占位符，密钥不落地；
- **不修改已有 K8s 清单**——只新增，不覆盖——动了别人的清单，等于动了别人的线上；
- **不擅自执行部署**——脚本只生成，不自动执行；不生成持久化卷配置（PV/PVC 由存储团队管理）。

边界一句话：**本技能写"部署图纸和应急预案"，不动"线上开关"和"别人领地"**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_k3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_k3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_k3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">本技能管什么、不管什么</text>
  <rect x="40" y="50" width="340" height="90" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_k3)"/>
  <text x="210" y="70" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">管 ✅</text>
  <text x="210" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">Deployment / Service / HPA / 探针</text>
  <text x="210" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">灰度策略 / 回滚检查脚本 / README</text>
  <rect x="420" y="50" width="340" height="90" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_k3)"/>
  <text x="590" y="70" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">不管 ❌</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">Ingress/证书 · Secret 敏感数据· PV/PVC</text>
  <text x="590" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">不执行部署 · 不改已有清单</text>
  <text x="400" y="180" text-anchor="middle" fill="#475569" font-size="10">写图纸和预案，不动线上开关和别人的领地</text>
</svg>
```

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 容器化 / Dockerfile | `dockerfile` 相关约定 |
| K8s 部署清单 / 发布脚本 | **本技能**（`/k8s-release-toolkit`） |
| 部署后健康检查 | `deploy-verify`（探针之外的链路验证） |

- **本技能 vs deploy-verify**：k8s-release 管"怎么部署对"（清单+脚本），deploy-verify 管"部署后怎么确认活"（冒烟+健康检查）——一个保证能上去，一个保证上去能活。

## 七、完成标志

`/k8s-release-toolkit` 的完成标志有三个：

1. **清单文件齐备**：Deployment/Service/HPA/探针/kustomization 按需产出——该有的都有；
2. **dry-run 通过 + 检查清单全过**：`kubectl apply --dry-run=client` 无错，五条红线逐项确认——不是"写了"，是"验证了"；
3. **灰度 + 回滚脚本已生成**（生产环境）：README 说明部署步骤——发布有预案、有步骤、有退路。

三个标志对应三问：**齐备吗**（清单）？**验证过吗**（dry-run+红线）？**有退路吗**（灰度+回滚）？——三个都答"是"，部署图纸才允许交付。

## 八、写在最后

`/k8s-release-toolkit` 的全部设计，浓缩成四句话：

1. **无探针、无资源限制，就没有资格部署到 K8s**——这不是风格，是铁律。
2. **`latest` 标签禁止**——镜像版本是回滚的凭据，没有版本号就没有退路。
3. **生产环境至少两个副本、必须有回滚预案**——单点是事故，无预案是赌博。
4. **脚本只生成，不自动执行**——AI 画好地图，人类挑时机开车。

一句话记住它：**/k8s-release-toolkit 是 K8s 上线的"图纸和应急预案"——它生成 Deployment、Service、HPA、探针、灰度与回滚脚本六件套，用"无探针不上线、无限制不乱跑、latest 不用、单副本不许"四条铁律，让每一次部署都既上得去、又活得下、更退得回。**