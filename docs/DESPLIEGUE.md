# Despliegue de Jobs Menu

Este documento define el único procedimiento de despliegue local aceptado para el proyecto.

## Regla única

El mod se prueba siempre en:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

No se mantienen rutas alternativas. El usuario tampoco tiene que compilar localmente ni guardar scripts `.ps1` para una prueba normal.

## Orden obligatorio de entrega

El PowerShell se entrega **únicamente después** de que se cumpla este orden:

1. código y documentos actualizados;
2. verificadores estáticos sin fallos;
3. PR con CI verde;
4. merge a `main`;
5. build de `main` verde;
6. `dev-latest` actualizado con el JAR versionado nuevo;
7. recién entonces se pasa el PowerShell al usuario.

El bloque no compila el proyecto. Descarga el artefacto ya certificado por GitHub Actions y lo instala solamente en `test-1`.

## Regla obligatoria de versión

Todo JAR instalado o publicado debe incluir la versión en el nombre. Para esta entrega:

`jobsmenu-0.24.0.jar`

El nombre genérico `jobsmenu-latest.jar` queda prohibido. La release sigue usando el tag rodante `dev-latest`, pero su único asset cambia de nombre con `mod_version`.

## PowerShell canónico

El bloque consulta la API pública de la release `dev-latest`, localiza el único asset versionado, descarga primero a `%TEMP%`, valida cabecera ZIP/JAR, calcula SHA-256 y sólo entonces reemplaza la copia instalada.

```powershell
$ErrorActionPreference = "Stop"

$mods = "C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods"
$releaseApi = "https://api.github.com/repos/Santi-PdR/Jobs---Menu/releases/tags/dev-latest"

if (!(Test-Path $mods -PathType Container)) {
    throw "No existe la carpeta de mods: $mods"
}

Write-Host "Consultando dev-latest..." -ForegroundColor Cyan
$release = Invoke-RestMethod -Uri $releaseApi -Headers @{ "User-Agent" = "JobsMenu-Deploy" }
$assets = @(
    $release.assets |
    Where-Object { $_.name -match '^jobsmenu-[0-9]+\.[0-9]+\.[0-9]+(?:[-+][0-9A-Za-z.-]+)?\.jar$' }
)

if ($assets.Count -ne 1) {
    throw "Se esperaba exactamente un JAR versionado en dev-latest y se encontraron $($assets.Count)."
}

$asset = $assets[0]
$temp = Join-Path $env:TEMP $asset.name
$dest = Join-Path $mods $asset.name

Remove-Item $temp -Force -ErrorAction SilentlyContinue

Write-Host "Descargando $($asset.name)..." -ForegroundColor Yellow
Invoke-WebRequest -Uri $asset.browser_download_url -OutFile $temp -UseBasicParsing

if (!(Test-Path $temp -PathType Leaf)) {
    throw "La descarga no creó el JAR."
}

$bytes = [System.IO.File]::ReadAllBytes($temp)
if ($bytes.Length -lt 100000 -or $bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B) {
    Remove-Item $temp -Force -ErrorAction SilentlyContinue
    throw "La descarga no parece un JAR válido. No se modificó test-1."
}

$sha = (Get-FileHash $temp -Algorithm SHA256).Hash.ToLower()
Write-Host "JAR validado: $sha" -ForegroundColor Green

Get-ChildItem $mods -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '^jobsmenu.*\.jar$' } |
    Remove-Item -Force

Move-Item $temp $dest -Force

Write-Host ""
Write-Host "Jobs Menu instalado correctamente." -ForegroundColor Green
Write-Host "Version: $($asset.name)" -ForegroundColor Cyan
Write-Host "Destino: $dest" -ForegroundColor Cyan
Write-Host "SHA-256: $sha"
```

## Qué certifica el CI

`.github/workflows/build.yml` debe ejecutar, en este orden:

1. checkout;
2. Java 17;
3. resolución de `mod_version`;
4. `tools/verificar_version.py`;
5. `tools/verificar_fondos.py`;
6. `tools/verificar.py`;
7. `tools/verificar_ui_musica.py`;
8. build Forge;
9. preparación del JAR versionado;
10. artifact de workflow;
11. limpieza de JARs obsoletos de `dev-latest`;
12. actualización de `dev-latest` sólo desde `main`.

Si falla cualquier paso, la release no se actualiza y el PowerShell no se entrega todavía.

## Qué NO certifica el CI

El pipeline no puede confirmar cómo se ve o se siente la interfaz dentro de Minecraft real. Después del despliegue de `jobsmenu-0.24.0.jar` se debe ejecutar `docs/checklist-manual.md`.

Para esta versión conviene revisar especialmente:

- HUD principal: NXT, tiempo de visita, MUTE/volumen y chips 1–4/F/M;
- atajos numéricos del main y 1–2 de pausa, incluido keypad;
- protección de números mientras se escribe en buscadores/EditBox;
- breadcrumb, KEY/PTR, tipo y posición de control;
- controles vanilla/Forge tematizados;
- scrollbars 0/25/50/75/100 en listas grandes;
- GUI Scale 4, ultrawide, Movimiento reducido, Bajo consumo e Interfaz mínima;
- fondos 10–17 durante fade/apagón/transición y overlays globales.

Los PNG 10–17 no fueron sustituidos ni editados. Siguen sin movimiento propio y usan filtrado lineal al escalarse; los overlays globales están permitidos mientras no deformen ni desplacen la imagen.
