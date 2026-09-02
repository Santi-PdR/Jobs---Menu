# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Sustituye el flujo de menús por la interfaz de Jobs: expedientes administrativos, recintos, audio continuo y una navegación que conserva la lógica sensible de Minecraft cuando hacerlo mejora compatibilidad.

| | |
|---|---|
| Versión | **0.18.0** |
| Artefacto | **`jobsmenu-0.18.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **18 (0–17)** |

## 0.18.0 · Pase profesional + integración de música por una sola subida

Este pase profundiza la presentación sin cambiar la identidad del mod.

La interfaz compartida recibe microinteracciones más finas: foco de teclado con respiración mínima, hover como marca de lectura, entrada de pantallas con curva más suave y confirmaciones que aparecen/desaparecen progresivamente. Movimiento reducido y bajo consumo anulan cualquier animación decorativa adicional.

Las transiciones entre pantallas se vuelven más sobrias: menos velo, menos sombra, ancho controlado y papel frío/gris. La intención es que parezcan carpetas/expedientes que se reemplazan, no un wipe genérico de videojuego.

También se simplifica por completo el flujo de la próxima pista. Ya no se descarga audio desde una fuente externa ni se reconstruyen scripts temporales. Cuando quieras añadir la nueva canción, sólo hay que subir:

`music/menu_nueva.ogg`

El workflow `Integrar OGG subido` valida que sea Vorbis real, normaliza loudness/true peak, genera el recurso final, registra la segunda pista, activa el catálogo de dos temas, ejecuta verificaciones y compila con Java 17. Si algo falla, no publica la integración.

El identificador interno preparado para la pista es `upon_the_hill_v2` y el recurso final será `assets/jobsmenu/sounds/musica/tema_nuevo.ogg`.

## Interfaz

Jobs usa dos superficies principales:

- **Formulario claro:** ajustes, opciones compactas, idioma y controles propios.
- **Archivo oscuro:** mundos, servidores, Mods y Resource Packs.

Reglas vigentes:

- ningún título vanilla puede sangrar debajo de una cabecera Jobs;
- ningún widget visible puede tener un hitbox vanilla invisible superpuesto;
- los campos de búsqueda deben mantener foco, portapapeles y teclado aunque su presentación sea propia;
- las listas conservan rueda/click/drag de Minecraft;
- las pantallas complejas de terceros no se reconstruyen por reflection sólo por estética;
- accesibilidad, movimiento reducido y bajo consumo tienen prioridad sobre decoración.

El servidor oficial queda fijado como `JobsDosh.exaroton.me:56477`, deduplicado y protegido frente a edición/borrado desde la interfaz Jobs. El acceso legado `Ghoul Outbreak` no debe reaparecer.

## Ciclo de sesión

`SesionMenu` representa una visita completa al menú. Abrir una subpantalla no crea otra visita ni reinicia música/ambiente.

Al entrar a un mundo o servidor:

1. se cierra la sesión Jobs;
2. música y camas ambientales se detienen inmediatamente;
3. gameplay no recibe audio del menú.

Al salir de un mundo, servidor, kick o desconexión, el flujo vuelve a `PantallaNivel` cuando el menú propio está habilitado, no al título vanilla.

## Música y ambiente

- Música: reproductor de sesión con fades, ducking y catálogo preparado para crossfade.
- Ambiente: BASE + CARÁCTER + ACTIVIDAD por Nivel, más eventos ocasionales.
- UI: gestos propios para foco, selección, alternancia, confirmación, apertura, cierre y rechazo.
- La música Jobs usa Maestro + volumen Jobs; no depende del slider Música vanilla.
- Los PNG 10–17 permanecen **estáticos** por requisito del proyecto; los apagones/transiciones generales sí continúan.

Detalles: [`docs/musica.md`](docs/musica.md) y [`music/LEEME.txt`](music/LEEME.txt).

## Compatibilidad

Las redirecciones automáticas importantes usan **clase exacta** para no barrer subclases de otros mods. Cuando una pantalla vanilla aporta lógica compleja (servidores, mundos, recursos, confirmaciones), Jobs conserva esa lógica y sustituye sólo la presentación necesaria.

Embeddium conserva su propia interfaz de vídeo cuando corresponde. Jobs no intenta recrear internamente una UI externa desconocida.

## Build y entrega

GitHub Actions es la certificación de la entrega. El pipeline ejecuta:

1. Java 17;
2. política de versión y JAR versionado;
3. validación de PNG 10–17;
4. auditoría estática general;
5. contratos de UI neutra + música;
6. `./gradlew build --stacktrace --no-daemon`;
7. publicación de **`jobsmenu-0.18.0.jar`** en `dev-latest` sólo desde `main`.

La release rodante debe conservar un único JAR versionado. `jobsmenu-latest.jar` está prohibido.

## Despliegue

El flujo previsto no requiere compilar en el PC de juego: el PowerShell de despliegue consume el JAR certificado de `dev-latest`, elimina versiones anteriores de Jobs Menu en `test-1` y copia únicamente el artefacto vigente.

## Documentación vigente

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro actual.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos/pruebas que siguen requiriendo Minecraft real.
- [`CHANGELOG.md`](CHANGELOG.md): historial de versiones anteriores.
- [`docs/AUDITORIA_0.18.0_PROFESIONAL.md`](docs/AUDITORIA_0.18.0_PROFESIONAL.md): auditoría de este pase.
- [`docs/musica.md`](docs/musica.md): catálogo y lifecycle musical.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalación del build.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): convivencia con otros mods.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): validación dentro de Minecraft.

Los documentos históricos de `docs/EVOLUCION_*`, propuestas y auditorías anteriores se conservan como registro; **README y CONTEXTO describen siempre el estado vigente**.
