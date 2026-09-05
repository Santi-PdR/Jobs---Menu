# Despliegue de Jobs Menu

Procedimiento único de prueba local para la entrega **0.37.0**.

## Destino

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

No se compila localmente para una prueba normal. Se instala el JAR que GitHub Actions publicó en `dev-latest` después de un `main` verde.

## Orden de entrega

1. código, recursos y documentos actualizados;
2. verificadores estáticos verdes;
3. PR verde;
4. squash/merge a `main`;
5. CI de `main` verde;
6. `dev-latest` actualizado;
7. PowerShell al usuario.

## Versión actual

`jobsmenu-0.37.0.jar`

`jobsmenu-latest.jar` está prohibido. `dev-latest` es sólo el tag rodante; el asset siempre lleva versión.

## PowerShell canónico

El script consulta `dev-latest` y exige exactamente un asset Jobs versionado, por lo que no necesita que la versión se escriba dos veces a mano.

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

Get-ChildItem $mods -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '^jobsmenu.*\.jar$' } |
    Remove-Item -Force

Move-Item $temp $dest -Force

Write-Host ""
Write-Host "Jobs Menu instalado correctamente." -ForegroundColor Green
Write-Host "Versión: $($asset.name)" -ForegroundColor Cyan
Write-Host "Destino: $dest" -ForegroundColor Cyan
Write-Host "SHA-256: $sha"
```

## CI

El workflow ejecuta Java 17, `verificar_version.py`, `verificar_fondos.py`, `verificar.py`, `verificar_ui_musica.py`, `verificar_continuidad.py`, Forge build, artifact versionado y publicación sólo desde `main`.

- `verificar_fondos.py` cubre PNG 10–17 y JPG directos 18–31.
- `verificar_ui_musica.py` fija frontera de gameplay, Video Settings vanilla, gestos Jobs, música y ambientes.
- `verificar_continuidad.py` fija desde 0.37.0 la selección F5 de Multiplayer, el guard de recarga y la coherencia mínima de documentación.

## Prueba manual posterior

CI no puede certificar la imagen, input ni audio final dentro de Minecraft. Después del deploy revisar:

- niveles 18–31 y su encuadre;
- animación sutil de 18–31;
- Movimiento reducido y Bajo consumo, que deben congelarla;
- PNG 10–17, que deben seguir totalmente estáticos;
- GUI Scale 2/3/4;
- `N`, `M`, ESC y retorno desde mundos/servidores;
- F5/Actualizar en Multiplayer, incluyendo selección conservada, LAN, ping y favicons;
- hard-stop de audio Jobs dentro de gameplay y ausencia de transiciones en pausa/configuración.
