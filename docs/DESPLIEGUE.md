# Despliegue de Jobs Menu

Este documento define el unico procedimiento de despliegue local aceptado para el proyecto.

## Regla unica

El mod se prueba siempre en esta instancia de SKLauncher:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

No se documentan ni se mantienen rutas alternativas. `jobs-2`, `Test2.0` y cualquier otra instancia quedan fuera del flujo de prueba de Jobs Menu.

El usuario tampoco tiene que descargar el JAR manualmente ni guardar scripts `.ps1`. El repositorio compila el mod con GitHub Actions usando Java 17. Un build que pasa `tools/verificar.py` y Gradle publica un artefacto rodante llamado `jobsmenu-latest.jar` en el prerelease `dev-latest`.

El unico paso local es abrir una PowerShell nueva y pegar el bloque que se entrega en el chat. Ese bloque debe:

1. descargar `jobsmenu-latest.jar` desde el release `dev-latest`;
2. validar que el archivo descargado tenga cabecera ZIP/JAR antes de tocar la instalacion existente;
3. eliminar solamente JAR anteriores de Jobs Menu en `test-1\mods`;
4. mover el JAR validado a `test-1\mods`;
5. mostrar la ruta final y el SHA-256.

Nunca se debe borrar el JAR instalado antes de que la descarga nueva haya terminado y haya sido validada.

## URL estable del build

`https://github.com/Santi-PdR/Jobs---Menu/releases/download/dev-latest/jobsmenu-latest.jar`

El enlace es estable: cambia el contenido cuando un nuevo commit compila correctamente, no la URL.

## PowerShell canonico

El bloque se entrega como texto, no como archivo `.ps1`:

```powershell
$ErrorActionPreference = "Stop"

$mods = "C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods"
$url = "https://github.com/Santi-PdR/Jobs---Menu/releases/download/dev-latest/jobsmenu-latest.jar"
$temp = Join-Path $env:TEMP "jobsmenu-latest.jar"
$dest = Join-Path $mods "jobsmenu-latest.jar"

if (!(Test-Path $mods)) {
    throw "No existe la carpeta: $mods"
}

Write-Host "Descargando Jobs Menu..." -ForegroundColor Cyan
Invoke-WebRequest -Uri $url -OutFile $temp -UseBasicParsing

$bytes = [System.IO.File]::ReadAllBytes($temp)
if ($bytes.Length -lt 2 -or $bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B) {
    Remove-Item $temp -Force
    throw "El archivo descargado no es un JAR valido. No se modifico la instalacion actual."
}

Get-ChildItem $mods -File |
    Where-Object { $_.Name -like "jobsmenu*.jar" } |
    Remove-Item -Force

Move-Item $temp $dest -Force

Write-Host ""
Write-Host "Jobs Menu instalado correctamente." -ForegroundColor Green
Write-Host "Destino: $dest"
Write-Host "SHA256: $((Get-FileHash $dest -Algorithm SHA256).Hash)"
```

## Responsabilidades del CI

El workflow `.github/workflows/build.yml` ejecuta, en este orden:

- checkout del commit;
- Java 17;
- `python3 tools/verificar.py`;
- `./gradlew build --stacktrace --no-daemon`;
- seleccion del JAR principal, excluyendo sources/javadoc;
- publicacion del artefacto y actualizacion de `dev-latest`.

Si una verificacion o la compilacion falla, el release no debe actualizarse. Por lo tanto el PowerShell siempre apunta al ultimo build que llego a completar correctamente el pipeline.

## Flujo de trabajo

Los cambios grandes se preparan primero en una rama de trabajo. Solo despues de pasar la verificacion y el build se integran en `main`. El deploy local no cambia: siempre consume `dev-latest` y siempre instala en `test-1`.
