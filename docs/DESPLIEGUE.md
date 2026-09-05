# Despliegue de Jobs Menu

Procedimiento único de prueba local para la entrega **0.38.0**.

## Destino

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

No se compila localmente para una prueba normal. Se instala el JAR que GitHub Actions publicó en `dev-latest` después de un `main` verde.

## Orden de entrega

1. código, recursos y documentos actualizados;
2. verificadores estáticos verdes;
3. PR verde;
4. squash/merge a `main`;
5. CI de `main` verde;
6. `dev-latest` actualizado con un único asset 0.38.0;
7. SHA-256 verificado;
8. PowerShell al usuario.

## Versión actual

`jobsmenu-0.38.0.jar`

`jobsmenu-latest.jar` está prohibido. `dev-latest` es sólo el tag rodante; el asset siempre lleva versión.

## PowerShell canónico

El script de entrega final debe comprobar la versión y el SHA-256 publicados **antes** de borrar un JAR Jobs anterior. Nunca toca otros mods de `test-1`.

```powershell
$ErrorActionPreference = "Stop"

$mods = "C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods"
$version = "0.38.0"
$file = "jobsmenu-$version.jar"
$url = "https://github.com/Santi-PdR/Jobs---Menu/releases/download/dev-latest/$file"
$expectedSha = "REEMPLAZAR_POR_SHA256_CERTIFICADO_DE_MAIN"

New-Item -ItemType Directory -Force -Path $mods | Out-Null
$temp = Join-Path $env:TEMP $file
$dest = Join-Path $mods $file

Remove-Item $temp -Force -ErrorAction SilentlyContinue
Invoke-WebRequest -Uri $url -OutFile $temp -UseBasicParsing

if (!(Test-Path $temp -PathType Leaf)) {
    throw "La descarga no creó el JAR."
}

$bytes = [System.IO.File]::ReadAllBytes($temp)
if ($bytes.Length -lt 100000 -or $bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B) {
    Remove-Item $temp -Force -ErrorAction SilentlyContinue
    throw "La descarga no parece un JAR válido. No se modificó test-1."
}

$sha = (Get-FileHash $temp -Algorithm SHA256).Hash.ToUpperInvariant()
if ($sha -ne $expectedSha) {
    Remove-Item $temp -Force -ErrorAction SilentlyContinue
    throw "SHA-256 inválido. Esperado: $expectedSha / recibido: $sha"
}

Get-ChildItem $mods -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '^jobsmenu(?:-.*)?\.jar$' } |
    Remove-Item -Force

Move-Item $temp $dest -Force

Write-Host "Jobs Menu $version instalado correctamente." -ForegroundColor Green
Write-Host "Destino: $dest" -ForegroundColor Cyan
Write-Host "SHA-256: $sha" -ForegroundColor Cyan
```

El valor real de `$expectedSha` sólo se escribe después de verificar el asset publicado por el CI de `main`; no se toma del artefacto de un PR.

## CI

El workflow ejecuta Java 17, `verificar_version.py`, `verificar_fondos.py`, `verificar.py`, `verificar_ui_musica.py`, `verificar_continuidad.py`, `verificar_optimizacion.py`, Forge build, artifact versionado y publicación sólo desde `main`.

- `verificar_fondos.py` cubre PNG 10–17 y JPG directos 18–31.
- `verificar_ui_musica.py` fija frontera de gameplay, Video Settings vanilla, gestos Jobs, música y ambientes.
- `verificar_continuidad.py` fija selección F5 de Multiplayer, guard de recarga y coherencia mínima de documentación.
- `verificar_optimizacion.py` protege cachés de listas/texturas/texto, deduplicación por frame, Bajo consumo real y build reproducible.

## Prueba manual posterior

CI no puede certificar imagen, input, audio ni FPS dentro de Minecraft. Después del deploy revisar:

- niveles 10–31 y F3+T;
- Movimiento reducido y Bajo consumo;
- GUI Scale 2/3/4;
- listas largas y scrollbar Jobs en Mundos/Mods/Recursos/Idioma/Multiplayer;
- `N`, `M`, ESC y retorno desde mundos/servidores;
- F5/Actualizar, selección conservada, LAN, ping y favicons;
- hard-stop de audio Jobs dentro de gameplay;
- ausencia de transiciones Jobs en chat, inventario, containers, pausa/config durante gameplay;
- Video Settings completamente vanilla.
