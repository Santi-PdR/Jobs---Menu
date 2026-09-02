#!/usr/bin/env python3
from __future__ import annotations

import json
import os
import pathlib
import subprocess
import sys
import urllib.error
import urllib.request

VIDEO_ID = "t9KaSaGEwvI"
SOURCE_URL = f"https://www.youtube.com/watch?v={VIDEO_ID}"
USER_AGENT = (
    "JobsMenu-AudioIntegrator/0.18 "
    "(+https://github.com/Santi-PdR/Jobs---Menu)"
)
DIRECTORY_URLS = [
    "https://instances.cobalt.best/api/instances.json",
    "https://instances.cobalt.best/instances.json",
]
FALLBACKS = [
    ("https://cobalt-api.meowing.de", "https://cobalt.meowing.de"),
    ("https://capi.3kh0.net", "https://cobalt.3kh0.net"),
    ("https://cobalt-backend.canine.tools", "https://cobalt.canine.tools"),
    (
        "https://bergung-api.hoffnungfuerdiezukunft.net",
        "https://bergung.hoffnungfuerdiezukunft.net",
    ),
    ("https://apicobalt.mgytr.top", "https://cobalt.mgytr.top"),
]


def fetch_directory() -> list[dict]:
    for url in DIRECTORY_URLS:
        target = pathlib.Path("/tmp/cobalt-instances.json")
        target.unlink(missing_ok=True)
        result = subprocess.run(
            [
                "curl", "--http1.1", "--location", "--fail",
                "--silent", "--show-error", "--retry", "3",
                "--retry-all-errors", "--connect-timeout", "20",
                "--max-time", "90", "--user-agent", USER_AGENT,
                "--output", str(target), url,
            ],
            check=False,
        )
        if result.returncode != 0 or not target.is_file():
            continue
        try:
            data = json.loads(target.read_text(encoding="utf-8"))
        except Exception as exc:
            print(f"Directorio Cobalt inválido {url}: {exc}", file=sys.stderr)
            continue
        if isinstance(data, list):
            print(f"Directorio Cobalt OK: {url}, {len(data)} instancias")
            return [item for item in data if isinstance(item, dict)]
    return []


def normalize_api(item: dict) -> str | None:
    api = str(item.get("api") or "").strip().rstrip("/")
    if not api:
        return None
    if api.startswith(("http://", "https://")):
        return api
    protocol = str(item.get("protocol") or "https").strip() or "https"
    return f"{protocol}://{api}"


def normalize_frontend(item: dict, api: str) -> str:
    frontend = str(item.get("frontend") or "").strip().rstrip("/")
    if frontend:
        if not frontend.startswith(("http://", "https://")):
            frontend = "https://" + frontend
        return frontend
    return api


def youtube_available(item: dict) -> bool:
    services = item.get("services")
    if not isinstance(services, dict):
        return True
    value = services.get("youtube")
    return value is True or value == "true"


def instance_allowed(item: dict) -> bool:
    online = item.get("online")
    if isinstance(online, dict):
        online = online.get("api")
    if online is False:
        return False
    info = item.get("info")
    if isinstance(info, dict) and info.get("auth") is True:
        return False
    return youtube_available(item)


def post_cobalt(api: str, frontend: str, payload: dict) -> dict:
    body = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        api.rstrip("/") + "/",
        data=body,
        method="POST",
        headers={
            "User-Agent": USER_AGENT,
            "Accept": "application/json",
            "Content-Type": "application/json",
            "Origin": frontend,
            "Referer": frontend.rstrip("/") + "/",
        },
    )
    try:
        with urllib.request.urlopen(request, timeout=90) as response:
            text = response.read().decode("utf-8", "replace")
            return json.loads(text)
    except urllib.error.HTTPError as exc:
        text = exc.read().decode("utf-8", "replace")
        raise RuntimeError(f"HTTP {exc.code}: {text[:500]}") from exc


def probe_audio(path: pathlib.Path) -> bool:
    if not path.is_file() or path.stat().st_size < 100_000:
        return False
    result = subprocess.run(
        [
            "ffprobe", "-v", "error", "-select_streams", "a:0",
            "-show_entries", "stream=codec_name",
            "-of", "default=nw=1:nk=1", str(path),
        ],
        text=True,
        capture_output=True,
        check=False,
        timeout=30,
    )
    return result.returncode == 0 and bool(result.stdout.strip())


def download(url: str, target: pathlib.Path, frontend: str) -> bool:
    target.unlink(missing_ok=True)
    command = [
        "curl", "--http1.1", "--location", "--fail",
        "--silent", "--show-error", "--retry", "3",
        "--retry-all-errors", "--connect-timeout", "20",
        "--max-time", "420", "--user-agent", USER_AGENT,
        "--header", f"Origin: {frontend}",
        "--header", f"Referer: {frontend.rstrip('/')}/",
        "--output", str(target), url,
    ]
    result = subprocess.run(command, check=False)
    return result.returncode == 0 and probe_audio(target)


def main() -> int:
    metadata_path = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "/tmp/jobsmenu-track.json")
    candidates: list[tuple[str, str]] = []
    directory = fetch_directory()
    ranked = sorted(
        directory,
        key=lambda item: int(item.get("score") or 0),
        reverse=True,
    )
    for item in ranked:
        if not instance_allowed(item):
            continue
        api = normalize_api(item)
        if not api:
            continue
        pair = (api, normalize_frontend(item, api))
        if pair not in candidates:
            candidates.append(pair)
    for pair in FALLBACKS:
        if pair not in candidates:
            candidates.append(pair)

    payloads = [
        {
            "url": SOURCE_URL,
            "downloadMode": "audio",
            "audioFormat": "best",
            "audioBitrate": "128",
            "filenameStyle": "basic",
            "localProcessing": "disabled",
            "alwaysProxy": True,
            "disableMetadata": False,
        },
        {
            "url": SOURCE_URL,
            "downloadMode": "audio",
            "audioFormat": "ogg",
            "audioBitrate": "128",
            "filenameStyle": "basic",
            "localProcessing": "disabled",
            "alwaysProxy": True,
            "disableMetadata": False,
        },
    ]

    errors: list[str] = []
    source: pathlib.Path | None = None
    selected_api: str | None = None
    response_metadata: dict = {}

    print(f"Probando {len(candidates)} instancias Cobalt.")
    for api, frontend in candidates[:30]:
        for variant, payload in enumerate(payloads):
            try:
                response = post_cobalt(api, frontend, payload)
                if not isinstance(response, dict):
                    raise RuntimeError("respuesta no JSON-objeto")
                status = str(response.get("status") or "")
                if status == "error":
                    raise RuntimeError(f"cobalt error: {response}")
                media_url = response.get("url")
                if status not in {"tunnel", "redirect"} or not media_url:
                    raise RuntimeError(f"estado no descargable: {status}; {response}")
                candidate = pathlib.Path(f"/tmp/jobsmenu-source-cobalt-{variant}.bin")
                if not download(str(media_url), candidate, frontend):
                    raise RuntimeError("el túnel no produjo audio válido")
                source = candidate
                selected_api = api
                response_metadata = response
                print(f"Cobalt OK: {api} -> {candidate}")
                break
            except Exception as exc:
                message = f"Cobalt {api} variante {variant}: {exc}"
                errors.append(message)
                print(message, file=sys.stderr)
        if source:
            break

    if source is None or selected_api is None:
        print("\n".join(errors[-60:]), file=sys.stderr)
        raise SystemExit("Ninguna instancia Cobalt entregó audio válido.")

    metadata = {
        "id": VIDEO_ID,
        "title": response_metadata.get("filename") or "Pista autorizada",
        "uploader": "Autor autorizado",
        "duration": 0,
        "webpage_url": SOURCE_URL,
        "transport": "cobalt",
        "transport_instance": selected_api,
    }
    metadata_path.write_text(
        json.dumps(metadata, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    with open(os.environ["GITHUB_ENV"], "a", encoding="utf-8") as env:
        env.write(f"SOURCE={source}\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
