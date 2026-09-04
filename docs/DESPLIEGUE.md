# Despliegue de Jobs Menu

Procedimiento unico de prueba local.

## Destino

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

No se compila localmente para una prueba normal. Se instala el JAR que GitHub Actions publico en `dev-latest` despues de un `main` verde.

## Orden de entrega

1. codigo, recursos y documentos actualizados;
2. verificadores estaticos verdes;
3. PR verde;
4. squash/merge a `main`;
5. CI de `main` verde;
6. `dev-latest` actualizado;
7. PowerShell al usuario.

## Version actual

`jobsmenu-0.27.0.jar`

`jobsmenu-latest.jar` esta prohibido. `dev-latest` es solo el tag rodante; el asset siempre lleva version.

## PowerShell canonico

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
    throw "La descarga no creo el JAR."
}

$bytes = [System.IO.File]::ReadAllBytes($temp)
if ($bytes.Length -lt 100000 -or $bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B) {
    Remove-Item $temp -Force -ErrorAction SilentlyContinue
    throw "La descarga no parece un JAR valido. No se modifico test-1."
}

$sha = (Get-FileHash $temp -Algorithm SHA256).Hash.ToLower()

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

## CI

El workflow debe ejecutar Java 17, `verificar_version.py`, `verificar_fondos.py`, verificadores generales/UI-musica, Forge build, artifact versionado y publicacion solo desde `main`.

En 0.27.0 `verificar_fondos.py` comprueba los PNG 10-17 y los JPG directos 18-31.

## Prueba manual posterior

CI no puede certificar la imagen final dentro de Minecraft. Despues del deploy revisar:

- niveles 18-31 y su encuadre;
- animacion sutil de 18-31;
- Movimiento reducido y Bajo consumo, que deben congelarla;
- PNG 10-17, que deben seguir totalmente estaticos;
- GUI Scale 2/3/4;
- `N`, `M`, ESC y retorno desde mundos/servidores;
- hard-stop de audio Jobs dentro de gameplay.
