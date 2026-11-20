import argparse, json, re
from pathlib import Path
from xml.sax.saxutils import escape
from validate_input import validate

ROOT = Path(__file__).resolve().parents[1]
ap = argparse.ArgumentParser()
ap.add_argument("--url", required=True)
ap.add_argument("--app-name", required=True)
ap.add_argument("--application-id", required=True)
ap.add_argument("--version-name", default="1.0.0")
a = ap.parse_args()
validate(a.url, a.app_name, a.application_id, a.version_name)

cfg = ROOT / "android/app/src/main/java/dev/pocketforge/shell/GeneratedConfig.kt"
cfg.write_text(
    "package dev.pocketforge.shell\n\n"
    "internal object GeneratedConfig {\n"
    f"    const val START_URL: String = {json.dumps(a.url, ensure_ascii=True)}\n"
    "}\n", encoding="utf-8"
)
strings = ROOT / "android/app/src/main/res/values/strings.xml"
strings.write_text(
    f'<resources><string name="app_name">{escape(a.app_name)}</string></resources>\n',
    encoding="utf-8"
)
print("Generated app configuration")
