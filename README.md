# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1 que reemplaza los menús por la interfaz del servidor **Jobs**: un aviso administrativo pegado a la pared de un recinto que cambia con el tiempo.

La salida existe. Cuesta. Los **Executores** vuelven. El menú no es una pantalla separada del mundo: intenta sentirse como otro lugar del servidor antes de entrar a jugar.

| | |
|---|---|
| Versión | **0.11.0** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |

## Estado actual

La rotación contiene diez recintos procedurales (0–9) y ocho fondos suministrados (10–17). Todos participan del mismo sistema de luz, apagones, transición, ambiente, música, avisos, ronda de Executores y accesibilidad.

Los fondos de imagen 10–17 se validan antes de compilar: firma PNG, CRC, flujo IDAT, descompresión y dimensiones. En runtime `PlantaImagen` vuelve a comprobar el recurso con `NativeImage`, el mismo decodificador que usa Minecraft. Un recurso inválido cae a una escena procedural segura en lugar de dejar la textura morado/negro.

## Qué cambia en 0.11.0

- **Política de versión obligatoria.** Todo JAR publicado lleva versión en el nombre. El CI falla si vuelve a aparecer un artefacto genérico sin versión.
- **Release versionada.** `dev-latest` continúa siendo la release rodante, pero el asset es `jobsmenu-0.11.0.jar` (y en futuras versiones cambia junto con `mod_version`).
- **Pulido cinematográfico común.** Los 18 niveles reciben halo residual de fluorescente, barrido de exposición sutil y una transición con masa visual en vez de depender únicamente del cambio de brillo.
- **Transiciones reforzadas.** Durante el traslado la oscuridad entra desde los bordes y la recuperación eléctrica deja una banda de luz breve antes de estabilizarse. La Suspensión conserva su lenguaje propio y no añade flashes.
- **Fondos animados.** Los niveles 10–17 mantienen zoom/paneo lento, vignette y efectos propios por escena; movimiento reducido y bajo consumo siguen teniendo prioridad.
- **Ambiente menos repetitivo.** Las camas BASE y CARÁCTER incorporan una microderiva tonal de ciclo largo, suficientemente pequeña para no cambiar el material del sonido. ACTIVIDAD permanece estable para no deformar objetos reconocibles.
- **Papel más físico.** El aviso comparte una sombra en dos planos, degradado de luz, marcas de fotocopia deterministas, desgaste mínimo de borde y cinta con volumen visual. `papel_limpio` continúa eliminando ese ruido decorativo.
- **Pipeline endurecido.** Se verifican versión, fondos, recursos, idiomas y build Forge/Java 17 antes de publicar.

## Sistemas principales

- Rotación automática de 18 niveles con salto manual de diagnóstico/recorrido.
- Transición diegética por apagado/encendido sincronizada con audio.
- Evento raro **La Suspensión**.
- Cuenta regresiva ambiental de rondas de Executores.
- Presencia rara de fondo sin jumpscare.
- Tres camas ambientales por nivel: BASE, CARÁCTER y ACTIVIDAD.
- Eventos ambientales ponderados, con silencios largos deliberados.
- Música de menú independiente de la cama ambiental y con ducking contextual.
- Avisos rotativos y notas específicas por nivel.
- Pantalla principal y pausa tematizadas.
- Ajustes integrados dentro de Opciones de Minecraft.
- Alto contraste, texto grande, papel limpio, guía de lectura, movimiento reducido, destellos reducidos y bajo consumo.

## Regla obligatoria de versión

**El mod debe tener siempre una versión en el nombre del JAR entregado.**

Ejemplo correcto:

```text
jobsmenu-0.11.0.jar
```

Ejemplo prohibido:

```text
jobsmenu-latest.jar
```

`gradle.properties` es la fuente de verdad (`mod_version`). `tools/verificar_version.py` y GitHub Actions hacen cumplir esta regla.

## Build y entrega

GitHub Actions ejecuta:

1. Java 17.
2. `tools/verificar_version.py`.
3. `tools/verificar_fondos.py`.
4. `tools/verificar.py`.
5. `./gradlew build --stacktrace --no-daemon`.
6. Publicación del JAR **versionado** en `dev-latest` si el commit está en `main`.

El JAR local de Gradle queda en:

```text
build/libs/jobsmenu-0.11.0.jar
```

## Despliegue de prueba

La única instancia de prueba documentada es:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods
```

No hace falta compilar localmente para una prueba normal. El PowerShell canónico consulta `dev-latest`, localiza el asset `jobsmenu-<version>.jar`, lo descarga y lo valida antes de sustituir la instalación anterior. Ver [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md).

## Herramientas

```powershell
python tools\verificar_version.py
python tools\verificar_fondos.py
python tools\verificar.py
python tools\vista_previa.py
python tools\sonidos.py
```

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): documento maestro y reglas duras del proyecto.
- [`CHANGELOG.md`](CHANGELOG.md): cambios por versión/evolución.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): pruebas reales y riesgos pendientes.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): flujo de instalación en `test-1`.
- [`docs/AUDITORIA_0.11.0.md`](docs/AUDITORIA_0.11.0.md): revisión completa de esta pasada.
- [`docs/DIRECCION_ARTISTICA.md`](docs/DIRECCION_ARTISTICA.md): lenguaje visual.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): convivencia con otros mods.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.

El historial largo de auditorías y evoluciones anteriores se conserva en `docs/` como referencia, pero README y CONTEXTO describen siempre el estado vigente.
