# Despliegue de Jobs Menu

Procedimiento único de prueba local para la entrega **0.41.1**.

## Destino

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

No se compila localmente para una prueba normal. Se instala el JAR que GitHub Actions publica en `dev-latest` después de un `main` verde.

## Orden de entrega

1. código, recursos y documentos actualizados;
2. verificadores estáticos verdes;
3. PR verde;
4. squash/merge a `main`;
5. CI de `main` verde;
6. `dev-latest` actualizado;
7. instalación en `test-1`.

## Versión actual

`jobsmenu-0.41.1.jar`

`jobsmenu-latest.jar` está prohibido. `dev-latest` es sólo el tag rodante; el asset siempre lleva versión.

## PowerShell canónico

El script consulta `dev-latest`, exige exactamente el JAR 0.41.1, comprueba el digest SHA-256 publicado por GitHub y sólo después reemplaza Jobs Menu.

```powershell
$ErrorActionPreference = "Stop"

$mods = "C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods"
$version = "0.41.1"
$name = "jobsmenu-$version.jar"
$releaseApi = "https://api.github.com/repos/Santi-PdR/Jobs---Menu/releases/tags/dev-latest"

if (!(Test-Path $mods -PathType Container)) {
    New-Item -ItemType Directory -Path $mods -Force | Out-Null
}

$release = Invoke-RestMethod -Uri $releaseApi -Headers @{ "User-Agent" = "JobsMenu-Deploy" }
$assets = @($release.assets | Where-Object { $_.name -match '^jobsmenu-.*\.jar$' })
$asset = @($assets | Where-Object { $_.name -eq $name })

if ($assets.Count -ne 1 -or $asset.Count -ne 1) {
    throw "dev-latest no contiene exactamente $name. No se modificó test-1."
}

$asset = $asset[0]
if ([string]::IsNullOrWhiteSpace($asset.digest) -or !$asset.digest.StartsWith("sha256:")) {
    throw "GitHub no devolvió SHA-256 para $name."
}

$expected = $asset.digest.Substring(7).ToUpperInvariant()
$temp = Join-Path $env:TEMP $name
$dest = Join-Path $mods $name

Remove-Item $temp -Force -ErrorAction SilentlyContinue
Invoke-WebRequest -Uri $asset.browser_download_url -OutFile $temp -UseBasicParsing

$bytes = [System.IO.File]::ReadAllBytes($temp)
if ($bytes.Length -lt 100000 -or $bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B) {
    Remove-Item $temp -Force -ErrorAction SilentlyContinue
    throw "La descarga no parece un JAR válido."
}

$sha = (Get-FileHash $temp -Algorithm SHA256).Hash.ToUpperInvariant()
if ($sha -ne $expected) {
    Remove-Item $temp -Force -ErrorAction SilentlyContinue
    throw "SHA-256 incorrecto. Esperado: $expected | Recibido: $sha"
}

Get-ChildItem $mods -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '^jobsmenu(?:-.*)?\.jar$' } |
    Remove-Item -Force

Move-Item $temp $dest -Force

Write-Host "Jobs Menu $version instalado." -ForegroundColor Green
Write-Host "Destino: $dest"
Write-Host "SHA-256: $sha"
```

## CI 0.41.1

El workflow ejecuta:

- `tools/verificar_version.py`;
- `tools/verificar_fondos.py`;
- `tools/verificar.py`;
- `tools/verificar_ui_musica.py`;
- `tools/verificar_continuidad.py`;
- `tools/verificar_optimizacion.py`;
- `tools/verificar_reload_creditos.py`;
- `tools/verificar_audio_identidad.py`;
- `tools/verificar_runtime_041.py`;
- `tools/verificar_graficos_041.py`;
- Forge build con Java 17;
- preparación/upload de `jobsmenu-0.41.1.jar`;
- limpieza de JARs Jobs obsoletos en `dev-latest`;
- publicación sólo desde `main`.

## Prueba manual posterior

Revisar especialmente que Gráficos conserve todas las opciones del flujo normal del modpack, que no reaparezca chrome vanilla en Opciones Jobs, hard-stop de FX puntuales, reloads consecutivos, config persistente, F5/resize con selección+scroll, servidor oficial, PNG 10–17 estáticos, JPG 18–31 y GUI Scale 2/3/4.
