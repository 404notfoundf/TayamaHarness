#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""转发到随 apply-harness 分发的升级脚本。"""
from pathlib import Path
import runpy

target = (
    Path(__file__).resolve().parent.parent
    / "skills"
    / "apply-harness"
    / "scripts"
    / "upgrade-harness.py"
)
runpy.run_path(str(target), run_name="__main__")
