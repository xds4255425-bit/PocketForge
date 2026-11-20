import argparse, re, sys
from urllib.parse import urlsplit

def validate(url: str, app_name: str, application_id: str, version_name: str):
    p = urlsplit(url.strip())
    if p.scheme != "https" or not p.hostname or p.username or p.password:
        raise ValueError("website_url 必须是无账号密码的 HTTPS 网址")
    if any(c in url for c in ('"', "'", "\\", "\n", "\r", " ")):
        raise ValueError("网址包含不允许的字符")
    host = p.hostname.lower()
    if host in {"localhost"} or host.endswith((".localhost", ".local", ".internal")):
        raise ValueError("不允许 localhost 或内部域名")
    if not app_name.strip() or len(app_name.strip()) > 40:
        raise ValueError("app_name 长度必须为 1–40 个字符")
    if not re.fullmatch(r"[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*){1,5}", application_id):
        raise ValueError("application_id 格式错误，例如 com.example.portal")
    if not re.fullmatch(r"[0-9A-Za-z][0-9A-Za-z._+-]{0,31}", version_name):
        raise ValueError("version_name 格式错误")

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--url", required=True)
    ap.add_argument("--app-name", required=True)
    ap.add_argument("--application-id", required=True)
    ap.add_argument("--version-name", default="1.0.0")
    args = ap.parse_args()
    try:
        validate(args.url, args.app_name, args.application_id, args.version_name)
    except ValueError as exc:
        print(f"输入无效：{exc}", file=sys.stderr)
        sys.exit(2)
    print("输入验证通过")
