#!/usr/bin/env node
/**
 * 把技能包新版本同步进已有项目的 .harness/，再注册到当前 AI 工具目录。
 *
 * 设计：
 * - 不是整棵 .harness/ 盲覆盖（那会毁掉 wiki / CONTEXT / 进行中的 change）
 * - 复用项目里已渲染的占位符（.apply-params.json + 现有 SKILL.md / owner.md）
 * - 默认不删用户私自加的技能目录（加 --prune 才对齐 apply-harness 的残留清理）
 *
 * 用法（在已用 npx skills add 的业务项目根目录）：
 *   npx skills update
 *   node .agents/skills/apply-harness/scripts/upgrade-harness.mjs --yes
 */

import fs from "node:fs";
import path from "node:path";
import readline from "node:readline/promises";
import { stdin as input, stdout as output } from "node:process";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const PIPELINE_SKILLS = [
  "arch-review",
  "coding-skill",
  "deploy-verify",
  "diagnosing-bugs",
  "expert-reviewer",
  "handoff",
  "harness-me",
  "harnessing",
  "unit-test-ci",
  "unit-test-write",
];

const COMMON_SKILLS = [
  "domain-modeling",
  "research",
  "resolving-merge-conflicts",
  "redis-cache-wrapper",
  "database-migration-toolkit",
  "kafka-toolkit",
  "k8s-release-toolkit",
  "performance-toolkit",
  "security-toolkit",
  "rocketmq-toolkit",
  "http-client-toolkit",
  "logging-toolkit",
  "scheduler-toolkit",
  "oss-toolkit",
  "excel-toolkit",
  "eventbus-toolkit",
  "harness-status",
  "harness-loop-run",
  "harness-quality",
  "harness-relate",
  "harness-e2e",
  "harness-db-design",
  "harness-api-mock",
  "harness-refactor",
  "harness-retro",
  "harness-ship",
  "harness-changelog",
  "harness-standard",
  "legacy-bootstrap",
  "knowledge-health",
];

const LANG_DIRS = {
  java: "harness-java",
  python: "harness-python",
  golang: "harness-golang",
  rust: "harness-rust",
  front: "harness-front",
};

const OWNER_TABLE = {
  "语言/运行时": "LANGUAGE_RUNTIME",
  框架: "FRAMEWORK_VER",
  构建工具: "BUILD_TOOL",
  测试框架: "TEST_FRAMEWORK",
  覆盖率工具: "COV_TOOL",
  代码规范检查: "LINT_TOOL",
  架构约束守护: "ARCH_TEST_TOOL",
  数据库访问: "DB_ACCESS",
};

const TOOL_TARGETS = [
  { env: "REASONIX", dir: ".reasonix", dest: [".reasonix", "skills"], name: "Reasonix" },
  { env: "CLAUDE_CODE", dir: ".claude", dest: [".claude", "skills"], name: "Claude Code" },
  { env: "CLINE", dir: ".cline", dest: [".cline", "skills"], name: "Cline" },
  { dir: ".cursor", dest: [".cursor", "skills"], name: "Cursor" },
  { env: "OPENAI_API_KEY", dir: ".codex", dest: [".codex", "skills"], name: "Codex" },
  { dir: ".qoder", dest: [".qoder", "skills"], name: "Qoder" },
  { dir: ".vscode", dest: [".vscode", "agent-skills"], name: "VS Code Agent Skills" },
  { dir: ".github", dest: [".github", "skills"], name: "GitHub Copilot" },
];

function parseArgs(argv) {
  const opts = {
    source: "",
    project: process.cwd(),
    yes: false,
    dryRun: false,
    register: true,
    prune: false,
    refreshOwner: false,
    allTools: false,
    lang: "",
    withRules: false,
    withTemplates: false,
  };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    const next = () => argv[++i];
    if (a === "--source") opts.source = path.resolve(next());
    else if (a === "--project") opts.project = path.resolve(next());
    else if (a === "--yes" || a === "-y") opts.yes = true;
    else if (a === "--dry-run") opts.dryRun = true;
    else if (a === "--no-register") opts.register = false;
    else if (a === "--prune") opts.prune = true;
    else if (a === "--refresh-owner") opts.refreshOwner = true;
    else if (a === "--all-tools") opts.allTools = true;
    else if (a === "--with-rules") opts.withRules = true;
    else if (a === "--with-templates") opts.withTemplates = true;
    else if (a === "--full") {
      opts.withRules = true;
      opts.withTemplates = true;
    }
    else if (a === "--lang") opts.lang = next();
    else if (a === "--help" || a === "-h") opts.help = true;
    else throw new Error(`未知参数: ${a}（--help 查看用法）`);
  }
  return opts;
}

function help() {
  console.log(`Harness 技能升级（保守覆盖）

  node scripts/upgrade-harness.mjs [选项]

  --project <dir>    目标项目根目录（默认 cwd）
  --source <dir>     技能包位置：仓库根，或 npx 装好的 .agents/skills（默认自动找）
  --lang <name>      java | python | golang | rust | front（默认从 .harness/skills/ 推断）
  --yes / -y         不询问，直接写盘
  --dry-run          只打印计划
  --no-register      不复制到 AI 工具技能目录
  --all-tools        注册到检测到的全部工具目录（默认只装第一个）
  --prune            删除技能包里已不存在的旧技能目录
  --with-rules       连 .harness/rules/ 一起覆盖（会丢掉你改过的规范）
  --with-templates   更新 change/wiki/tech 的 _TEMPLATE 与 *-FORMAT.md
  --full             等同 --with-rules --with-templates
  --refresh-owner    连 owner.md 也按模板重渲染（会丢掉你改过的 Owner 文案）

默认只更新 .harness/skills/ 并注册到工具目录。
已确认过的 rules、wiki、CONTEXT、changes/<id>、owner 一律不覆盖。`);
}

function asPackRoot(dir) {
  if (!dir || !exists(dir)) return null;
  if (exists(path.join(dir, "harness-core", "rules"))) return path.resolve(dir);
  if (exists(path.join(dir, "skills", "harness-core", "rules"))) return path.resolve(dir, "skills");
  return null;
}

function findPackRoot({ source, project, scriptDir }) {
  const tries = [];
  if (source) tries.push(source);
  tries.push(
    path.join(scriptDir, "..", ".."),
    path.join(scriptDir, ".."),
    path.join(project, ".agents", "skills"),
    path.join(project, ".cursor", "skills"),
    path.join(project, ".claude", "skills"),
    path.join(project, ".reasonix", "skills"),
    path.join(project, ".codex", "skills"),
  );
  for (const t of tries) {
    const hit = asPackRoot(t);
    if (hit) return hit;
  }
  throw new Error(`找不到技能包源（需要 harness-core/rules）。
请先在项目根执行：
  npx skills@latest add https://github.com/404notfoundf/TayamaHarness.git
或：
  npx skills update
然后重跑本脚本；也可 --source 指向本仓库根或 .agents/skills`);
}

function exists(p) {
  try {
    fs.accessSync(p);
    return true;
  } catch {
    return false;
  }
}

function listDirs(p) {
  if (!exists(p)) return [];
  return fs.readdirSync(p, { withFileTypes: true }).filter((e) => e.isDirectory()).map((e) => e.name);
}

function copyDir(src, dest, { dryRun, log }) {
  if (!exists(src)) return;
  log(`COPY  ${src} -> ${dest}`);
  if (dryRun) return;
  fs.mkdirSync(dest, { recursive: true });
  for (const ent of fs.readdirSync(src, { withFileTypes: true })) {
    const from = path.join(src, ent.name);
    const to = path.join(dest, ent.name);
    if (ent.isDirectory()) fs.cpSync(from, to, { recursive: true, force: true });
    else fs.copyFileSync(from, to);
  }
}

function writeFile(dest, content, { dryRun, log }) {
  log(`WRITE ${dest}`);
  if (dryRun) return;
  fs.mkdirSync(path.dirname(dest), { recursive: true });
  fs.writeFileSync(dest, content, "utf8");
}

function rmDir(dest, { dryRun, log }) {
  log(`RM    ${dest}`);
  if (dryRun) return;
  fs.rmSync(dest, { recursive: true, force: true });
}

function detectLang(harnessDir, forced) {
  if (forced) {
    if (!LANG_DIRS[forced]) throw new Error(`--lang 必须是 ${Object.keys(LANG_DIRS).join(" | ")}`);
    return forced;
  }
  const skills = path.join(harnessDir, "skills");
  const found = Object.keys(LANG_DIRS).filter((k) => exists(path.join(skills, k)));
  if (found.length === 1) return found[0];
  if (found.length === 0) throw new Error("无法从 .harness/skills/ 推断语言，请加 --lang");
  throw new Error(`检测到多种语言目录: ${found.join(", ")}，请加 --lang`);
}

function placeholderKeys(text) {
  return [...new Set([...text.matchAll(/\{\{([A-Z][A-Z0-9_]*)\}\}/g)].map((m) => m[1]))];
}

function applyParams(text, params) {
  return text.replace(/\{\{([A-Z][A-Z0-9_]*)\}\}/g, (all, key) =>
    Object.prototype.hasOwnProperty.call(params, key) ? String(params[key]) : all,
  );
}

function harvestFromOwner(ownerText, params) {
  const mName = ownerText.match(/你是 \*\*(.+?)\*\* 的 Owner Agent/);
  if (mName) params.PROJECT_NAME = mName[1];
  const mDesc = ownerText.match(/精通 (.+?) 的资深应用负责人/);
  if (mDesc) params.LANGUAGE_DESC = mDesc[1];
  for (const [label, key] of Object.entries(OWNER_TABLE)) {
    const re = new RegExp(`\\|\\s*${label}\\s*\\|\\s*(.+?)\\s*\\|`);
    const m = ownerText.match(re);
    if (m) params[key] = m[1].trim();
  }
}

function harvestByLineAlign(template, rendered, params) {
  const tLines = template.split(/\r?\n/);
  const rLines = rendered.split(/\r?\n/);
  for (const line of tLines) {
    if (!line.includes("{{")) continue;
    const parts = line.split(/\{\{([A-Z][A-Z0-9_]*)\}\}/);
    if (parts.length < 3) continue;
    let rx = "^";
    const names = [];
    for (let i = 0; i < parts.length; i++) {
      if (i % 2 === 0) rx += parts[i].replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
      else {
        names.push(parts[i]);
        rx += "([\\s\\S]+?)";
      }
    }
    rx += "$";
    let re;
    try {
      re = new RegExp(rx);
    } catch {
      continue;
    }
    for (const rl of rLines) {
      const m = rl.match(re);
      if (!m) continue;
      names.forEach((name, idx) => {
        if (params[name] === undefined) params[name] = m[idx + 1];
      });
      break;
    }
  }
}

function harvestNameField(skillMd, params, map) {
  const m = skillMd.match(/^name:\s*"?([^"\n]+)"?\s*$/m);
  if (!m) return;
  const name = m[1].trim();
  for (const [prefix, key] of Object.entries(map)) {
    if (name === prefix) {
      params[key] = prefix;
      if (key === "HARNESSING_CMD" && params.LANG_TAG === undefined) params.LANG_TAG = "";
    } else if (name.startsWith(prefix) && name.length > prefix.length) {
      params[key] = name;
      if (key !== "HARNESSING_CMD" && name.slice(prefix.length).startsWith("-") && params.LANG_TAG === undefined) {
        params.LANG_TAG = name.slice(prefix.length);
      }
    }
  }
}

function collectParams(harnessDir, lang, packRoot) {
  const paramsPath = path.join(harnessDir, ".apply-params.json");
  const params = exists(paramsPath)
    ? JSON.parse(fs.readFileSync(paramsPath, "utf8").replace(/^\uFEFF/, ""))
    : {};

  const ownerPath = path.join(harnessDir, "agents", "owner.md");
  if (exists(ownerPath)) harvestFromOwner(fs.readFileSync(ownerPath, "utf8"), params);

  const langDir = path.join(harnessDir, "skills", lang);
  for (const skill of PIPELINE_SKILLS) {
    const rendered = path.join(langDir, skill, "SKILL.md");
    const template = path.join(packRoot, "harness-core", "skills", skill, "SKILL.md");
    if (!exists(rendered) || !exists(template)) continue;
    const r = fs.readFileSync(rendered, "utf8");
    const t = fs.readFileSync(template, "utf8");
    harvestByLineAlign(t, r, params);
    if (skill === "coding-skill") {
      const tag = r.match(/^name:\s*"?coding-skill([^"\n]*)"?/m);
      if (tag && params.LANG_TAG === undefined) params.LANG_TAG = tag[1];
    }
    if (skill === "harnessing") harvestNameField(r, params, { harnessing: "HARNESSING_CMD" });
    if (skill === "harness-me") harvestNameField(r, params, { "harness-me": "HARNESS_ME_NAME" });
    if (skill === "arch-review") harvestNameField(r, params, { "arch-review": "ARCH_REVIEW_CMD" });
  }

  const langDefaults = {
    java: { LANGUAGE: "Java", LANG_TAG: "-java", SRC_EXT: "java" },
    python: { LANGUAGE: "Python", LANG_TAG: "-python", SRC_EXT: "py" },
    golang: { LANGUAGE: "Go", LANG_TAG: "-golang", SRC_EXT: "go" },
    rust: { LANGUAGE: "Rust", LANG_TAG: "-rust", SRC_EXT: "rs" },
    front: { LANGUAGE: "Frontend", LANG_TAG: "-front", SRC_EXT: "ts" },
  };
  for (const [k, v] of Object.entries(langDefaults[lang] || {})) {
    if (params[k] === undefined) params[k] = v;
  }
  if (params.HARNESSING_CMD === undefined) params.HARNESSING_CMD = "harnessing";
  if (params.HARNESS_ME_NAME === undefined) params.HARNESS_ME_NAME = "harness-me";
  if (params.ARCH_REVIEW_CMD === undefined) {
    params.ARCH_REVIEW_CMD = `arch-review${params.LANG_TAG || ""}`;
  }
  return params;
}

function isTextSkillFile(filePath) {
  return /\.(md|json|ya?ml|txt)$/i.test(filePath);
}

function renderTree(srcDir, destDir, params, ctx) {
  if (!exists(srcDir)) return;
  ctx.log(`RENDER ${srcDir} -> ${destDir}`);
  const walk = (from, to) => {
    for (const ent of fs.readdirSync(from, { withFileTypes: true })) {
      const s = path.join(from, ent.name);
      const d = path.join(to, ent.name);
      if (ent.isDirectory()) {
        if (!ctx.dryRun) fs.mkdirSync(d, { recursive: true });
        walk(s, d);
        continue;
      }
      if (isTextSkillFile(s)) {
        const out = applyParams(fs.readFileSync(s, "utf8"), params);
        const left = placeholderKeys(out);
        if (left.length) {
          throw new Error(`${path.relative(srcDir, s)} 仍有未替换占位符: ${left.join(", ")}`);
        }
        writeFile(d, out, ctx);
      } else {
        ctx.log(`COPY  ${s} -> ${d}`);
        if (!ctx.dryRun) {
          fs.mkdirSync(path.dirname(d), { recursive: true });
          fs.copyFileSync(s, d);
        }
      }
    }
  };
  if (!ctx.dryRun) fs.mkdirSync(destDir, { recursive: true });
  walk(srcDir, destDir);
}

function detectTools(project, allTools) {
  const hits = [];
  for (const t of TOOL_TARGETS) {
    const envHit = t.env && process.env[t.env];
    const dirHit = exists(path.join(project, t.dir));
    if (envHit || dirHit) hits.push(t);
  }
  if (!hits.length) return [];
  return allTools ? hits : [hits[0]];
}

function registerSkills(project, harnessDir, tools, ctx) {
  const skillDirs = [];
  const skillsRoot = path.join(harnessDir, "skills");
  for (const top of listDirs(skillsRoot)) {
    const nested = path.join(skillsRoot, top);
    for (const name of listDirs(nested)) {
      if (exists(path.join(nested, name, "SKILL.md"))) skillDirs.push({ name, src: path.join(nested, name) });
    }
    if (exists(path.join(nested, "SKILL.md"))) skillDirs.push({ name: top, src: nested });
  }
  for (const tool of tools) {
    const destRoot = path.join(project, ...tool.dest);
    ctx.log(`REGISTER -> ${tool.name} (${destRoot})`);
    if (!ctx.dryRun) fs.mkdirSync(destRoot, { recursive: true });
    for (const s of skillDirs) {
      copyDir(s.src, path.join(destRoot, s.name), ctx);
    }
  }
}

async function confirm(opts, summary) {
  console.log(summary);
  if (opts.dryRun || opts.yes) return true;
  const rl = readline.createInterface({ input, output });
  try {
    const ans = (await rl.question("确认升级技能？（默认不改 rules / wiki / change）[y/N] ")).trim().toLowerCase();
    return ans === "y" || ans === "yes";
  } finally {
    rl.close();
  }
}

async function main() {
  let opts;
  try {
    opts = parseArgs(process.argv.slice(2));
  } catch (e) {
    console.error(e.message);
    process.exit(1);
  }
  if (opts.help) {
    help();
    return;
  }

  const project = opts.project;
  const packRoot = findPackRoot({ source: opts.source, project, scriptDir: __dirname });
  const harness = path.join(project, ".harness");
  const core = path.join(packRoot, "harness-core");
  if (!exists(harness)) {
    console.error(`${harness} 不存在。请先在项目里跑 /apply-harness，不要用本脚本做首次安装。`);
    process.exit(1);
  }

  const lang = detectLang(harness, opts.lang);
  const pack = LANG_DIRS[lang];
  const params = collectParams(harness, lang, packRoot);
  const tools = opts.register ? detectTools(project, opts.allTools) : [];
  const ctx = {
    dryRun: opts.dryRun,
    log: (m) => console.log(m),
  };

  const summary = [
    `源技能包: ${packRoot}`,
    `目标项目: ${project}`,
    `语言目录: ${lang} (${pack})`,
    `占位符: ${Object.keys(params).length} 个（将写入 .harness/.apply-params.json）`,
    `工具注册: ${tools.length ? tools.map((t) => t.name).join(", ") : "跳过"}`,
    `覆盖规则: ${opts.withRules}  覆盖模板: ${opts.withTemplates}  prune: ${opts.prune}`,
    `refresh-owner: ${opts.refreshOwner}  dry-run: ${opts.dryRun}`,
  ].join("\n");

  if (!(await confirm(opts, summary))) {
    console.log("已取消。");
    return;
  }

  const stamp = new Date().toISOString().replace(/[:.]/g, "-");
  const backup = path.join(harness, ".upgrade-backups", stamp);
  ctx.log(`BACKUP ${path.join(harness, "rules")} / skills -> ${backup}`);
  if (!opts.dryRun) {
    fs.mkdirSync(backup, { recursive: true });
    if (exists(path.join(harness, "rules"))) fs.cpSync(path.join(harness, "rules"), path.join(backup, "rules"), { recursive: true });
    if (exists(path.join(harness, "skills"))) fs.cpSync(path.join(harness, "skills"), path.join(backup, "skills"), { recursive: true });
    if (exists(path.join(harness, "agents"))) fs.cpSync(path.join(harness, "agents"), path.join(backup, "agents"), { recursive: true });
  }

  if (opts.withRules) {
    copyDir(path.join(core, "rules"), path.join(harness, "rules"), ctx);
    copyDir(path.join(packRoot, pack, "rules"), path.join(harness, "rules"), ctx);
  }

  const langDest = path.join(harness, "skills", lang);
  for (const skill of PIPELINE_SKILLS) {
    const src = path.join(core, "skills", skill);
    if (!exists(src)) continue;
    renderTree(src, path.join(langDest, skill), params, ctx);
  }

  const exclusiveRoot = path.join(packRoot, pack, "skills");
  const exclusive = listDirs(exclusiveRoot).filter((n) => !PIPELINE_SKILLS.includes(n));
  for (const name of exclusive) {
    renderTree(path.join(exclusiveRoot, name), path.join(langDest, name), params, ctx);
  }

  const commonDest = path.join(harness, "skills", "common");
  for (const name of COMMON_SKILLS) {
    const src = path.join(core, "skills", name);
    if (!exists(src)) continue;
    copyDir(src, path.join(commonDest, name), ctx);
  }

  if (opts.withTemplates) {
    copyDir(path.join(core, "templates", "changes"), path.join(harness, "changes"), ctx);
    const iterSrc = path.join(core, "templates", "iterations");
    if (exists(iterSrc)) copyDir(iterSrc, path.join(harness, "iterations"), ctx);

    const wikiTpl = path.join(core, "templates", "wiki", "_TEMPLATE");
    if (exists(wikiTpl)) copyDir(wikiTpl, path.join(harness, "wiki", "_TEMPLATE"), ctx);
    const adrFmt = path.join(core, "templates", "wiki", "ADR-FORMAT.md");
    if (exists(adrFmt)) writeFile(path.join(harness, "wiki", "ADR-FORMAT.md"), fs.readFileSync(adrFmt, "utf8"), ctx);
    const techTpl = path.join(core, "templates", "tech", "_TEMPLATE");
    if (exists(techTpl)) copyDir(techTpl, path.join(harness, "tech", "_TEMPLATE"), ctx);
    const ctxFmt = path.join(core, "templates", "CONTEXT-FORMAT.md");
    if (exists(ctxFmt)) writeFile(path.join(harness, "CONTEXT-FORMAT.md"), fs.readFileSync(ctxFmt, "utf8"), ctx);
  }

  if (opts.refreshOwner) {
    const ownerTpl = path.join(core, "templates", "agents", "owner.md");
    const rendered = applyParams(fs.readFileSync(ownerTpl, "utf8"), params);
    const left = placeholderKeys(rendered);
    if (left.length) throw new Error(`owner.md 未替换: ${left.join(", ")}`);
    writeFile(path.join(harness, "agents", "owner.md"), rendered, ctx);
  }

  if (opts.prune) {
    const allowedLang = new Set([...PIPELINE_SKILLS, ...exclusive]);
    for (const name of listDirs(langDest)) {
      if (!allowedLang.has(name)) rmDir(path.join(langDest, name), ctx);
    }
    const allowedCommon = new Set(COMMON_SKILLS);
    for (const name of listDirs(commonDest)) {
      if (!allowedCommon.has(name)) rmDir(path.join(commonDest, name), ctx);
    }
  }

  writeFile(path.join(harness, ".apply-params.json"), `${JSON.stringify(params, null, 2)}\n`, ctx);

  if (opts.register) {
    if (!tools.length) {
      console.log("未检测到 AI 工具目录，跳过注册。可稍后 /install-skill，或加 --all-tools 前先建 .cursor/");
    } else {
      registerSkills(project, harness, tools, ctx);
    }
  }

  console.log(`
升级完成。
  备份: ${backup}
  下一步: 新开 AI 会话，确认 /harnessing 能弹出命令。
  若斜杠命令仍旧: 再跑 /install-skill，或本脚本去掉 --no-register 重跑。`);
}

main().catch((err) => {
  console.error(err.stack || err.message);
  process.exit(1);
});
