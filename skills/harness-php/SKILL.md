---
name: harness-php
description: PHP 语言规范包 — 编码规范、工程结构（SDD-TDD/开发流程/运行时可靠性共享自 harness-core）
---

# Harness PHP — PHP 语言规范包

本包为 PHP 项目提供完整的 Harness 开发规范体系，基于：

- **Web 框架**: Laravel / Symfony 2+ / ThinkPHP / Hyperf / Yii / Yii 3 / Workerman / webman / CodeIgniter (CI) / Slim / Phalcon / CakePHP / Yaf / Swoole
- **CMS / 电商**: WordPress / WooCommerce / Drupal / Joomla / phpcms / dedecms / discuz / Craft CMS / October CMS / OpenCart / GravCMS
- **AI / LLM 库**: Neuron AI / Laravel AI SDK / LLPhant / Prism / PocketFlow PHP / Cognesy Instructor PHP / Papiai
- **运行时**: PHP 8.2+（composer 依赖管理）
- **测试框架**: PHPUnit + Mockery / Pest
- **代码规范**: PSR-12（php-cs-fixer + phpstan + psalm）
- **依赖管理**: Composer

包含 rules（2 个语言特有 + 3 个通用来自 harness-core）。流水线技能（10 个）与辅助技能由 `apply-harness` 从 `harness-core/skills/` 模板渲染，语言包不再维护副本。

> **辅助技能**: `/harness-me-php`（需求打磨）、`domain-modeling`（领域语言维护）、`research`（外部事实查证）、`resolving-merge-conflicts`（合并冲突解决）、`/diagnosing-bugs`（Bug 诊断）、`/handoff`（上下文交接）、`/arch-review-php`（架构体检）——在流水线各阶段按需调用。
---

> **来源 & 作者**
> - 公众号：华仔聊技术
> - 知识星球：华仔·AI高并发全栈训练营
> - 作者：王江华@huazai
