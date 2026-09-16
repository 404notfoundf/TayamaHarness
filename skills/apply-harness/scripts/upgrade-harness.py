#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""把技能包新版本同步进已有项目的 .harness/，再注册到当前 AI 工具目录。

默认只更新 .harness/skills/，不覆盖已确认的 rules / wiki / change。

用法（业务项目根目录，Cursor 项目级示例）::

    npx skills update
    python .agents/skills/apply-harness/scripts/upgrade-harness.py --yes
"""
from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import sys
from datetime import datetime, timezone
from pathlib import Path

PIPELINE_SKILLS = [
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
]

COMMON_SKILLS = [
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
]

LANG_DIRS = {
    "java": "harness-java",
    "python": "harness-python",
    "golang": "harness-golang",
    "rust": "harness-rust",
    "front": "harness-front",
}

OWNER_TABLE = {
    "语言/运行时": "LANGUAGE_RUNTIME",
    "框架": "FRAMEWORK_VER",
    "构建工具": "BUILD_TOOL",
    "测试框架": "TEST_FRAMEWORK",
    "覆盖率工具": "COV_TOOL",
    "代码规范检查": "LINT_TOOL",
    "架构约束守护": "ARCH_TEST_TOOL",
    "数据库访问": "DB_ACCESS",
}

TOOL_TARGETS = [
    {"env": "REASONIX", "dir": ".reasonix", "dest": (".reasonix", "skills"), "name": "Reasonix"},
    {"env": "CLAUDE_CODE", "dir": ".claude", "dest": (".claude", "skills"), "name": "Claude Code"},
    {"env": "CLINE", "dir": ".cline", "dest": (".cline", "skills"), "name": "Cline"},
    {"dir": ".cursor", "dest": (".cursor", "skills"), "name": "Cursor"},
    {"env": "OPENAI_API_KEY", "dir": ".codex", "dest": (".codex", "skills"), "name": "Codex"},
    {"dir": ".qoder", "dest": (".qoder", "skills"), "name": "Qoder"},
    {"dir": ".vscode", "dest": (".vscode", "agent-skills"), "name": "VS Code Agent Skills"},
    {"dir": ".github", "dest": (".github", "skills"), "name": "GitHub Copilot"},
]

PLACEHOLDER_RE = re.compile(r"\{\{([A-Z][A-Z0-9_]*)\}\}")
TEXT_FILE_RE = re.compile(r"\.(md|json|ya?ml|txt)$", re.I)


class Ctx:
    def __init__(self, dry_run: bool):
        self.dry_run = dry_run

    def log(self, msg: str) -> None:
        print(msg)


def parse_args(argv: list[str]) -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="Harness 技能升级（默认只覆盖 .harness/skills）",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="已确认过的 rules、wiki、CONTEXT、changes/<id>、owner 一律不覆盖。",
    )
    p.add_argument("--project", default=str(Path.cwd()), help="目标项目根目录（默认 cwd）")
    p.add_argument("--source", default="", help="技能包位置：仓库根，或 npx 装好的 .agents/skills")
    p.add_argument("--lang", default="", help="java | python | golang | rust | front")
    p.add_argument("--yes", "-y", action="store_true", help="不询问，直接写盘")
    p.add_argument("--dry-run", action="store_true", help="只打印计划")
    p.add_argument("--no-register", action="store_true", help="不复制到 AI 工具技能目录")
    p.add_argument("--all-tools", action="store_true", help="注册到检测到的全部工具目录")
    p.add_argument("--prune", action="store_true", help="删除技能包里已不存在的旧技能目录")
    p.add_argument("--with-rules", action="store_true", help="连 .harness/rules/ 一起覆盖")
    p.add_argument("--with-templates", action="store_true", help="更新 _TEMPLATE 与 *-FORMAT.md")
    p.add_argument("--full", action="store_true", help="等同 --with-rules --with-templates")
    p.add_argument("--refresh-owner", action="store_true", help="按模板重渲染 owner.md")
    args = p.parse_args(argv)
    if args.full:
        args.with_rules = True
        args.with_templates = True
    args.project = str(Path(args.project).resolve())
    if args.source:
        args.source = str(Path(args.source).resolve())
    args.register = not args.no_register
    return args


def as_pack_root(directory: Path | None) -> Path | None:
    if directory is None or not directory.is_dir():
        return None
    if (directory / "harness-core" / "rules").exists():
        return directory.resolve()
    nested = directory / "skills" / "harness-core" / "rules"
    if nested.exists():
        return (directory / "skills").resolve()
    return None


def find_pack_root(source: str, project: Path, script_dir: Path) -> Path:
    tries = []
    if source:
        tries.append(Path(source))
    tries.extend(
        [
            script_dir.parent.parent,
            script_dir.parent,
            project / ".agents" / "skills",
            project / ".cursor" / "skills",
            project / ".claude" / "skills",
            project / ".reasonix" / "skills",
            project / ".codex" / "skills",
        ]
    )
    for t in tries:
        hit = as_pack_root(t)
        if hit:
            return hit
    raise SystemExit(
        "找不到技能包源（需要 harness-core/rules）。\n"
        "请先在项目根执行 npx skills update，或 --source 指向本仓库根 / .agents/skills"
    )


def list_dirs(p: Path) -> list[str]:
    if not p.is_dir():
        return []
    return sorted(e.name for e in p.iterdir() if e.is_dir())


def copy_dir(src: Path, dest: Path, ctx: Ctx) -> None:
    if not src.exists():
        return
    ctx.log(f"COPY  {src} -> {dest}")
    if ctx.dry_run:
        return
    dest.mkdir(parents=True, exist_ok=True)
    for ent in src.iterdir():
        to = dest / ent.name
        if ent.is_dir():
            if to.exists():
                shutil.rmtree(to)
            shutil.copytree(ent, to)
        else:
            shutil.copy2(ent, to)


def write_file(dest: Path, content: str, ctx: Ctx) -> None:
    ctx.log(f"WRITE {dest}")
    if ctx.dry_run:
        return
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(content, encoding="utf-8", newline="\n")


def rm_dir(dest: Path, ctx: Ctx) -> None:
    ctx.log(f"RM    {dest}")
    if ctx.dry_run:
        return
    shutil.rmtree(dest, ignore_errors=True)


def detect_lang(harness_dir: Path, forced: str) -> str:
    if forced:
        if forced not in LANG_DIRS:
            raise SystemExit(f"--lang 必须是 {' | '.join(LANG_DIRS)}")
        return forced
    skills = harness_dir / "skills"
    found = [k for k in LANG_DIRS if (skills / k).is_dir()]
    if len(found) == 1:
        return found[0]
    if not found:
        raise SystemExit("无法从 .harness/skills/ 推断语言，请加 --lang")
    raise SystemExit(f"检测到多种语言目录: {', '.join(found)}，请加 --lang")


def placeholder_keys(text: str) -> list[str]:
    return sorted(set(PLACEHOLDER_RE.findall(text)))


def apply_params(text: str, params: dict[str, str]) -> str:
    def repl(m: re.Match[str]) -> str:
        key = m.group(1)
        return str(params[key]) if key in params else m.group(0)

    return PLACEHOLDER_RE.sub(repl, text)


def harvest_from_owner(owner_text: str, params: dict[str, str]) -> None:
    m_name = re.search(r"你是 \*\*(.+?)\*\* 的 Owner Agent", owner_text)
    if m_name:
        params["PROJECT_NAME"] = m_name.group(1)
    m_desc = re.search(r"精通 (.+?) 的资深应用负责人", owner_text)
    if m_desc:
        params["LANGUAGE_DESC"] = m_desc.group(1)
    for label, key in OWNER_TABLE.items():
        m = re.search(rf"\|\s*{re.escape(label)}\s*\|\s*(.+?)\s*\|", owner_text)
        if m:
            params[key] = m.group(1).strip()


def harvest_by_line_align(template: str, rendered: str, params: dict[str, str]) -> None:
    r_lines = rendered.splitlines()
    for line in template.splitlines():
        if "{{" not in line:
            continue
        parts = re.split(r"\{\{([A-Z][A-Z0-9_]*)\}\}", line)
        if len(parts) < 3:
            continue
        rx = "^"
        names: list[str] = []
        for i, part in enumerate(parts):
            if i % 2 == 0:
                rx += re.escape(part)
            else:
                names.append(part)
                rx += r"([\s\S]+?)"
        rx += "$"
        try:
            cre = re.compile(rx)
        except re.error:
            continue
        for rl in r_lines:
            m = cre.match(rl)
            if not m:
                continue
            for idx, name in enumerate(names):
                if name not in params:
                    params[name] = m.group(idx + 1)
            break


def harvest_name_field(skill_md: str, params: dict[str, str], mapping: dict[str, str]) -> None:
    m = re.search(r'^name:\s*"?([^"\n]+)"?\s*$', skill_md, re.M)
    if not m:
        return
    name = m.group(1).strip()
    for prefix, key in mapping.items():
        if name == prefix:
            params[key] = prefix
            if key == "HARNESSING_CMD" and "LANG_TAG" not in params:
                params["LANG_TAG"] = ""
        elif name.startswith(prefix) and len(name) > len(prefix):
            params[key] = name
            rest = name[len(prefix) :]
            if key != "HARNESSING_CMD" and rest.startswith("-") and "LANG_TAG" not in params:
                params["LANG_TAG"] = rest


def collect_params(harness_dir: Path, lang: str, pack_root: Path) -> dict[str, str]:
    params_path = harness_dir / ".apply-params.json"
    params: dict[str, str] = {}
    if params_path.is_file():
        raw = params_path.read_text(encoding="utf-8-sig")
        loaded = json.loads(raw)
        params = {str(k): "" if v is None else str(v) for k, v in loaded.items()}

    owner_path = harness_dir / "agents" / "owner.md"
    if owner_path.is_file():
        harvest_from_owner(owner_path.read_text(encoding="utf-8"), params)

    lang_dir = harness_dir / "skills" / lang
    for skill in PIPELINE_SKILLS:
        rendered = lang_dir / skill / "SKILL.md"
        template = pack_root / "harness-core" / "skills" / skill / "SKILL.md"
        if not rendered.is_file() or not template.is_file():
            continue
        r = rendered.read_text(encoding="utf-8")
        t = template.read_text(encoding="utf-8")
        harvest_by_line_align(t, r, params)
        if skill == "coding-skill":
            tag = re.search(r'^name:\s*"?coding-skill([^"\n]*)"?', r, re.M)
            if tag and "LANG_TAG" not in params:
                params["LANG_TAG"] = tag.group(1)
        if skill == "harnessing":
            harvest_name_field(r, params, {"harnessing": "HARNESSING_CMD"})
        if skill == "harness-me":
            harvest_name_field(r, params, {"harness-me": "HARNESS_ME_NAME"})
        if skill == "arch-review":
            harvest_name_field(r, params, {"arch-review": "ARCH_REVIEW_CMD"})

    lang_defaults = {
        "java": {
            "LANGUAGE": "Java",
            "LANG_TAG": "-java",
            "SRC_EXT": "java",
            "FORMAT_CHECK_CMD": "mvn spotless:check",
        },
        "python": {
            "LANGUAGE": "Python",
            "LANG_TAG": "-python",
            "SRC_EXT": "py",
            "FORMAT_CHECK_CMD": "black --check .",
        },
        "golang": {
            "LANGUAGE": "Go",
            "LANG_TAG": "-golang",
            "SRC_EXT": "go",
            "FORMAT_CHECK_CMD": "gofmt -l .",
        },
        "rust": {
            "LANGUAGE": "Rust",
            "LANG_TAG": "-rust",
            "SRC_EXT": "rs",
            "FORMAT_CHECK_CMD": "cargo fmt -- --check",
        },
        "front": {
            "LANGUAGE": "Frontend",
            "LANG_TAG": "-front",
            "SRC_EXT": "ts",
            "FORMAT_CHECK_CMD": "npx prettier --check .",
        },
    }
    for k, v in lang_defaults.get(lang, {}).items():
        params.setdefault(k, v)
    params.setdefault("HARNESSING_CMD", "harnessing")
    params.setdefault("HARNESS_ME_NAME", "harness-me")
    params.setdefault("ARCH_REVIEW_CMD", f"arch-review{params.get('LANG_TAG', '')}")
    params.setdefault("FORMAT_CHECK_CMD", "")
    params.setdefault("SRC_EXT", "")
    return params


def render_tree(src_dir: Path, dest_dir: Path, params: dict[str, str], ctx: Ctx) -> None:
    if not src_dir.exists():
        return
    ctx.log(f"RENDER {src_dir} -> {dest_dir}")

    def walk(from_dir: Path, to_dir: Path) -> None:
        for ent in from_dir.iterdir():
            s = from_dir / ent.name
            d = to_dir / ent.name
            if ent.is_dir():
                if not ctx.dry_run:
                    d.mkdir(parents=True, exist_ok=True)
                walk(s, d)
                continue
            if TEXT_FILE_RE.search(s.name):
                out = apply_params(s.read_text(encoding="utf-8"), params)
                left = placeholder_keys(out)
                if left:
                    raise SystemExit(f"{s.relative_to(src_dir)} 仍有未替换占位符: {', '.join(left)}")
                write_file(d, out, ctx)
            else:
                ctx.log(f"COPY  {s} -> {d}")
                if not ctx.dry_run:
                    d.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(s, d)

    if not ctx.dry_run:
        dest_dir.mkdir(parents=True, exist_ok=True)
    walk(src_dir, dest_dir)


def detect_tools(project: Path, all_tools: bool) -> list[dict]:
    hits = []
    for t in TOOL_TARGETS:
        env_hit = bool(t.get("env") and os.environ.get(t["env"]))
        dir_hit = (project / t["dir"]).exists()
        if env_hit or dir_hit:
            hits.append(t)
    if not hits:
        return []
    return hits if all_tools else [hits[0]]


def register_skills(project: Path, harness_dir: Path, tools: list[dict], ctx: Ctx) -> None:
    skills_root = harness_dir / "skills"
    skill_dirs: list[tuple[str, Path]] = []
    for top in list_dirs(skills_root):
        nested = skills_root / top
        for name in list_dirs(nested):
            if (nested / name / "SKILL.md").is_file():
                skill_dirs.append((name, nested / name))
        if (nested / "SKILL.md").is_file():
            skill_dirs.append((top, nested))
    for tool in tools:
        dest_root = project.joinpath(*tool["dest"])
        ctx.log(f"REGISTER -> {tool['name']} ({dest_root})")
        if not ctx.dry_run:
            dest_root.mkdir(parents=True, exist_ok=True)
        for name, src in skill_dirs:
            copy_dir(src, dest_root / name, ctx)


def confirm(args: argparse.Namespace, summary: str) -> bool:
    print(summary)
    if args.dry_run or args.yes:
        return True
    try:
        ans = input("确认升级技能？（默认不改 rules / wiki / change）[y/N] ").strip().lower()
    except EOFError:
        return False
    return ans in ("y", "yes")


def main(argv: list[str] | None = None) -> None:
    args = parse_args(argv if argv is not None else sys.argv[1:])
    script_dir = Path(__file__).resolve().parent
    project = Path(args.project)
    pack_root = find_pack_root(args.source, project, script_dir)
    harness = project / ".harness"
    core = pack_root / "harness-core"
    if not harness.is_dir():
        raise SystemExit(f"{harness} 不存在。请先在项目里跑 /apply-harness，不要用本脚本做首次安装。")

    lang = detect_lang(harness, args.lang)
    pack = LANG_DIRS[lang]
    params = collect_params(harness, lang, pack_root)
    tools = detect_tools(project, args.all_tools) if args.register else []
    ctx = Ctx(args.dry_run)

    summary = "\n".join(
        [
            f"源技能包: {pack_root}",
            f"目标项目: {project}",
            f"语言目录: {lang} ({pack})",
            f"占位符: {len(params)} 个（将写入 .harness/.apply-params.json）",
            f"工具注册: {', '.join(t['name'] for t in tools) if tools else '跳过'}",
            f"覆盖规则: {args.with_rules}  覆盖模板: {args.with_templates}  prune: {args.prune}",
            f"refresh-owner: {args.refresh_owner}  dry-run: {args.dry_run}",
        ]
    )
    if not confirm(args, summary):
        print("已取消。")
        return

    stamp = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H-%M-%S-%f")[:-3] + "Z"
    backup = harness / ".upgrade-backups" / stamp
    ctx.log(f"BACKUP {harness / 'rules'} / skills -> {backup}")
    if not args.dry_run:
        backup.mkdir(parents=True, exist_ok=True)
        for name in ("rules", "skills", "agents"):
            src = harness / name
            if src.exists():
                dest = backup / name
                if dest.exists():
                    shutil.rmtree(dest)
                shutil.copytree(src, dest)

    if args.with_rules:
        copy_dir(core / "rules", harness / "rules", ctx)
        copy_dir(pack_root / pack / "rules", harness / "rules", ctx)

    lang_dest = harness / "skills" / lang
    for skill in PIPELINE_SKILLS:
        src = core / "skills" / skill
        if src.exists():
            render_tree(src, lang_dest / skill, params, ctx)

    exclusive_root = pack_root / pack / "skills"
    exclusive = [n for n in list_dirs(exclusive_root) if n not in PIPELINE_SKILLS]
    for name in exclusive:
        render_tree(exclusive_root / name, lang_dest / name, params, ctx)

    common_dest = harness / "skills" / "common"
    for name in COMMON_SKILLS:
        src = core / "skills" / name
        if src.exists():
            copy_dir(src, common_dest / name, ctx)

    if args.with_templates:
        copy_dir(core / "templates" / "changes", harness / "changes", ctx)
        iter_src = core / "templates" / "iterations"
        if iter_src.exists():
            copy_dir(iter_src, harness / "iterations", ctx)
        wiki_tpl = core / "templates" / "wiki" / "_TEMPLATE"
        if wiki_tpl.exists():
            copy_dir(wiki_tpl, harness / "wiki" / "_TEMPLATE", ctx)
        adr_fmt = core / "templates" / "wiki" / "ADR-FORMAT.md"
        if adr_fmt.is_file():
            write_file(harness / "wiki" / "ADR-FORMAT.md", adr_fmt.read_text(encoding="utf-8"), ctx)
        tech_tpl = core / "templates" / "tech" / "_TEMPLATE"
        if tech_tpl.exists():
            copy_dir(tech_tpl, harness / "tech" / "_TEMPLATE", ctx)
        ctx_fmt = core / "templates" / "CONTEXT-FORMAT.md"
        if ctx_fmt.is_file():
            write_file(harness / "CONTEXT-FORMAT.md", ctx_fmt.read_text(encoding="utf-8"), ctx)

    if args.refresh_owner:
        owner_tpl = core / "templates" / "agents" / "owner.md"
        rendered = apply_params(owner_tpl.read_text(encoding="utf-8"), params)
        left = placeholder_keys(rendered)
        if left:
            raise SystemExit(f"owner.md 未替换: {', '.join(left)}")
        write_file(harness / "agents" / "owner.md", rendered, ctx)

    if args.prune:
        allowed_lang = set(PIPELINE_SKILLS) | set(exclusive)
        for name in list_dirs(lang_dest):
            if name not in allowed_lang:
                rm_dir(lang_dest / name, ctx)
        allowed_common = set(COMMON_SKILLS)
        for name in list_dirs(common_dest):
            if name not in allowed_common:
                rm_dir(common_dest / name, ctx)

    write_file(harness / ".apply-params.json", json.dumps(params, ensure_ascii=False, indent=2) + "\n", ctx)

    if args.register:
        if not tools:
            print("未检测到 AI 工具目录，跳过注册。可稍后 /install-skill。")
        else:
            register_skills(project, harness, tools, ctx)

    print(
        f"\n升级完成。\n  备份: {backup}\n"
        "  下一步: 新开 AI 会话，确认 /harnessing 能弹出命令。\n"
        "  若斜杠命令仍旧: 再跑 /install-skill，或本脚本去掉 --no-register 重跑。"
    )


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(130)
