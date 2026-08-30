# Jobs · Aviso a los ocupantes

Mod **de cliente** que reemplaza los menús de Minecraft por los del servidor **Jobs**: un aviso fotocopiado
y pegado con cinta a la pared de un pasillo amarillo que no se termina. Dice en qué nivel estás, cuánto
cuesta la salida al siguiente, y cuánto falta para la próxima ronda de los **Executores**.

Al fondo del pasillo hay un vano oscuro. Cada tanto algo lo cruza.

El fondo va cambiando de nivel solo. Entre uno y otro se corta la luz.

No añade objetos, ni entidades, ni mecánicas. Sólo cambia lo que ves antes de entrar a trabajar.

![Vista previa del menú](docs/vista_previa.png)

| | |
|---|---|
| Versión | **0.10.0** |
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente (el servidor no necesita el mod) |

## Qué trae la 0.10.0

Esta es la versión actual de Jobs Menu. No cambia pantallas de mods ajenos ni
toca el mundo: concentra el trabajo en el ciclo de vida del menú, el audio, la
accesibilidad y la legibilidad de sus diez recintos.

- **Transición coherente por frame.** La escena captura nivel, luz y estado del
  apagón en un mismo instante. La planta, el papel y los eventos ya no pueden
  cruzar la frontera de un nivel en momentos distintos.
- **Accesibilidad respetada.** Movimiento reducido congela la animación completa;
  destellos reducidos conserva la lectura sin parpadeos. Los controles mantienen
  sus hitboxes nativas, entran en Tab y tienen narración vanilla.
- **Audio con lifecycle controlado.** El tema del menú tiene una única instancia,
  depende de `Master` y del volumen del aviso, sobrevive a Opciones/Mods y se
  invalida al recargar recursos o entrar a un mundo. Las camas ambientales se
  detienen sin quedar huérfanas y los eventos respetan silencio y ducking.
- **Personalización útil.** Se añadieron volumen maestro del aviso con M,
  rotación en calma (24 s o 48 s) y fecha del turno. Los cambios de configuración
  se aplican al instante y se guardan con límite de escritura, también al salir.
- **La Suspensión.** Una vez cada aproximadamente 45–52 minutos, el edificio
  queda a oscuras durante 22 segundos: la luz baja sin parpadeos, el ambiente se
  reduce a su respiración más baja, la música cede y el rótulo avisa que el
  edificio suspira. Es un evento raro del fondo, no una mecánica ni un susto.
- **Diez funciones nuevas de percepción.** Alto contraste, texto grande, papel
  limpio, guía de lectura, estado de instalación, respiración de cámara
  independiente, duración configurable de avisos, presencia y eventos
  ambientales separables, y control de La Suspensión. Todo se integra en la
  pantalla de Opciones nativa y conserva los valores por defecto anteriores.
- **Fondos revisados individualmente.** Los diez niveles conservan arquitectura
  propia, materiales distinguibles, luz principal, rebotes y un punto focal. El
  Trono fue ajustado para que el ábside, el haz cenital, las columnas y el
  estrado conduzcan la mirada hacia un asiento vacío realmente legible.
- **Entrega verificable.** La auditoría estática, el procedimiento reproducible de
  compilación y el informe de compatibilidad están sincronizados con Forge 47.x,
  Java 17 y el nombre real del JAR. La evolución vigente está en
  [`docs/EVOLUCION_6.md`](docs/EVOLUCION_6.md) con su catálogo
  [`docs/CATALOGO_MEJORAS_Y_FUNCIONES.md`](docs/CATALOGO_MEJORAS_Y_FUNCIONES.md)
  y su informe final [`docs/INFORME_FINAL_EVOLUCION_6.md`](docs/INFORME_FINAL_EVOLUCION_6.md).
  El historial de decisiones está en
  [`docs/PROPUESTA_EVOLUCION_2.md`](docs/PROPUESTA_EVOLUCION_2.md) y
  [`docs/EVOLUCION_4.md`](docs/EVOLUCION_4.md).

## Evolución reciente

La etapa 1 añadió duración de estancia configurable, salto manual de nivel,
perfil accesible, modo de bajo consumo y continuidad del ambiente al navegar
pantallas hijas. La etapa 2 dio a cada uno de los diez fondos una mejora
artística propia (una fila implementada por escenario en la matriz de
auditoría de fondos) y rediseñó el Trono desde cero. Build con Java 17 y
prueba en Minecraft pendientes: ver [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md).

## Historial resumido

Las versiones anteriores añadieron los diez recintos, la pausa tematizada, la
ruta de música local, las camas ambientales, el Trono y la primera auditoría
profesional. El detalle histórico que todavía importa está en
[`CHANGELOG.md`](CHANGELOG.md); este README conserva sólo el estado vigente para
evitar instrucciones antiguas o afirmaciones desactualizadas sobre REQUIEM.

## Compilar

Requiere JDK 17 instalado.

```powershell
.\gradlew.bat clean build --no-daemon
```

El `.jar` queda en `build\libs\jobsmenu-0.10.0.jar` y se copia a la carpeta `mods` de la instancia.

> El repositorio incluye `gradle\wrapper\gradle-wrapper.jar`; `gradlew.bat` descarga
> únicamente la distribución Gradle si todavía no está en la caché local.

> **Si el build falla por memoria** (`os::commit_memory ... failed (errno=1455)` o *the daemon has
> disappeared*), no es el mod: es que a Windows le falta memoria comprometible. El `gradle.properties` ya va
> contenido a propósito (heap chico, GC serial, sin paralelismo) y `build.gradle` limita el proceso de
> reobfuscación, así que suele alcanzar. Si aun así falla, agrandá el **archivo de paginación** de Windows
> (Ver configuración avanzada del sistema → Rendimiento → Opciones avanzadas → Memoria virtual → Cambiar →
> tamaño administrado por el sistema) y reiniciá. El build tiene que terminar en **`BUILD SUCCESSFUL`**: si
> dice `BUILD FAILED`, el `.jar` que quede está a medio hacer y no sirve.

## Compilar y desplegar en la instancia `test-1`

Abrí una terminal PowerShell nueva, ubicáte en el repositorio y pegá el bloque
completo de abajo. **No se guarda como archivo `.ps1`**: el código se ejecuta una
sola vez en esa terminal.

El bloque es deliberadamente conservador: no cambia de rama, no hace merge con
`main`, no borra nada hasta encontrar y validar el JAR correcto, y no informa
éxito si un comando anterior falló. La rama correcta de esta sesión es
`arena/01a04e24-jobs-menu`; una rama parecida como `arena/01a04e0d-jobs-menu`
corresponde a otro snapshot y debe detener el proceso antes de compilar.

Rutas usadas:

```text
Repositorio: C:\Users\santi\Desktop\Jobs---Menu
Instancia:  C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1
JAR:        build\libs\jobsmenu-0.10.0.jar
```

```powershell
$ErrorActionPreference = "Stop"

# --- 0. Rutas y rama; no se cambia ni se actualiza main --------------------
$repo            = "C:\Users\santi\Desktop\Jobs---Menu"
$instancia       = "C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1"
$branch          = "arena/01a04e24-jobs-menu"
$versionEsperada = "0.10.0"

if (-not (Test-Path (Join-Path $repo "gradle.properties") -PathType Leaf)) {
    throw "No encuentro el repositorio en $repo"
}
if (-not (Test-Path $instancia -PathType Container)) {
    throw "No encuentro la instancia test-1 en $instancia"
}
if (-not (Test-Path (Join-Path $repo "gradlew.bat") -PathType Leaf)) {
    throw "No encuentro gradlew.bat en $repo"
}

Set-Location $repo

$actual = (git branch --show-current).Trim()
if ($actual -ne $branch) {
    throw "La rama actual es '$actual'. No compilo ni despliego. Usa la rama publicada '$branch'."
}

$dirty = @(git status --porcelain)
if ($dirty.Count -gt 0) {
    throw "El repositorio tiene cambios locales. Guardalos o confirmalos antes de compilar."
}

# --- 1. Versión del proyecto ----------------------------------------------
$versionLine = Get-Content .\gradle.properties |
    Where-Object { $_ -match '^mod_version=(.+)$' } |
    Select-Object -First 1
if (-not $versionLine) {
    throw "No pude leer mod_version de gradle.properties"
}
$version = ($versionLine -replace '^mod_version=', '').Trim()
if ($version -ne $versionEsperada) {
    throw "La versión encontrada es $version; este despliegue espera $versionEsperada."
}

# --- 2. Java 17; java.exe escribe su versión por stderr en Windows --------
$java = Get-Command java.exe -ErrorAction SilentlyContinue
if (-not $java) {
    throw "Java no está en el PATH. Instala o activa un JDK 17."
}
$javaText = (& cmd.exe /d /c "java -version 2^>^&1" | Out-String).Trim()
if ($javaText -notmatch 'version "17\.') {
    throw "Se encontró Java distinto de 17:`n$javaText"
}
Write-Host $javaText -ForegroundColor Green

# --- 3. Python real, evitando los alias de Microsoft Store ----------------
$py = Get-Command py.exe -ErrorAction SilentlyContinue
$python = Get-Command python.exe -ErrorAction SilentlyContinue
$pythonOk = $false

if ($py -and $py.Source -notlike '*\WindowsApps\*') {
    & $py.Source -3 tools\verificar.py
    $pythonOk = ($LASTEXITCODE -eq 0)
} elseif ($python -and $python.Source -notlike '*\WindowsApps\*') {
    & $python.Source tools\verificar.py
    $pythonOk = ($LASTEXITCODE -eq 0)
} else {
    throw "Python no está instalado. Instala Python 3 y vuelve a ejecutar el bloque."
}

if (-not $pythonOk) {
    throw "tools\verificar.py falló. No se desplegará ningún JAR."
}

# --- 4. Compilación limpia -------------------------------------------------
& .\gradlew.bat clean build --no-daemon
if ($LASTEXITCODE -ne 0) {
    throw "La compilación falló. No se desplegará ningún JAR."
}

$jar = Join-Path $repo "build\libs\jobsmenu-$version.jar"
if (-not (Test-Path $jar -PathType Leaf)) {
    $producidos = @(Get-ChildItem (Join-Path $repo "build\libs") -Filter "jobsmenu-*.jar" -File -ErrorAction SilentlyContinue)
    $lista = if ($producidos.Count) { ($producidos.Name -join ', ') } else { '(ninguno)' }
    throw "No aparece el JAR esperado $jar. JARs encontrados: $lista"
}
if ((Get-Item $jar).Length -le 0) {
    throw "El JAR esperado está vacío: $jar"
}

# --- 5. Backup y despliegue; Minecraft debe estar cerrado -----------------
$mods = Join-Path $instancia "mods"
New-Item -ItemType Directory -Force -Path $mods | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupDir = Join-Path $instancia "jobsmenu-backups\$stamp"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

$config = Join-Path $instancia "config\jobsmenu-client.toml"
if (Test-Path $config -PathType Leaf) {
    Copy-Item -LiteralPath $config -Destination (Join-Path $backupDir "jobsmenu-client.toml") -Force
}

$anteriores = @(Get-ChildItem -LiteralPath $mods -Filter "jobsmenu-*.jar" -File -ErrorAction SilentlyContinue)
foreach ($viejo in $anteriores) {
    # Usar FullName es importante: Copy-Item $viejo puede resolver solo el
    # nombre contra el directorio actual en Windows PowerShell.
    Copy-Item -LiteralPath $viejo.FullName -Destination (Join-Path $backupDir $viejo.Name) -Force
}
foreach ($viejo in $anteriores) {
    Remove-Item -LiteralPath $viejo.FullName -Force
}
Copy-Item -LiteralPath $jar -Destination (Join-Path $mods (Split-Path $jar -Leaf)) -Force

$hash = (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash
Write-Host "OK: desplegado jobsmenu-$version.jar en $mods" -ForegroundColor Green
Write-Host "Backup: $backupDir"
Write-Host "SHA256: $hash"
```

El `-LiteralPath $viejo.FullName` evita el fallo que puede intentar copiar un
JAR desde el directorio del repositorio en vez de desde `test-1\mods`. Si falla
la versión, Java, Python, la auditoría, Gradle o el artefacto, el bloque termina
y conserva los JARs existentes. No copies backups a `mods`.

## Herramientas sin JDK

```powershell
python tools\verificar.py       # versiones, idiomas, JSON, ASCII, llaves, símbolos, audio y niveles
python tools\vista_previa.py    # dibuja el menú a PNG para revisar la escena
python tools\vista_previa.py --contacto docs\contacto-actual.png   # los diez niveles juntos
python tools\vista_previa.py --presencia docs\presencia.png     # la manifestación del fondo, paso a paso
python tools\sonidos.py         # regenera las 73 piezas sintetizadas (74 OGG con la música; requiere numpy, scipy y soundfile)
```

## Documentación

Todo el diseño —canon del servidor, identidad, paleta, voz, alcance por fases y reglas de trabajo— está en
[`CONTEXTO.md`](CONTEXTO.md). Para la entrega de esta evolución: [`CHANGELOG.md`](CHANGELOG.md),
[`KNOWN_ISSUES.md`](KNOWN_ISSUES.md), [`docs/checklist-manual.md`](docs/checklist-manual.md),
[`docs/compatibilidad.md`](docs/compatibilidad.md), [`docs/musica.md`](docs/musica.md),
[`docs/EVOLUCION_5.md`](docs/EVOLUCION_5.md), [`docs/AUDITORIA_FONDOS_50X10.md`](docs/AUDITORIA_FONDOS_50X10.md),
[`docs/INFORME_FINAL_EVOLUCION_3.md`](docs/INFORME_FINAL_EVOLUCION_3.md) y
[`docs/EVOLUCION_4.md`](docs/EVOLUCION_4.md).
