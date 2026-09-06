# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado vigente. Las auditorías antiguas son históricas y no deben revertir este contrato.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.40.0** |
| Artefacto esperado | **`jobsmenu-0.40.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **32 (0–31)** |
| Destino de prueba | `C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods` |

## Reglas duras

1. `main` es la única rama entregable.
2. Todo JAR lleva versión: `jobsmenu-<mod_version>.jar`; nunca `jobsmenu-latest.jar`.
3. `gradle.properties` es la fuente de verdad de versión.
4. CI debe estar verde antes de publicar.
5. `dev-latest` conserva un único JAR Jobs versionado y sólo se actualiza desde `main`.
6. Java fuente permanece ASCII; texto visible vive en `lang` cuando corresponde.
7. Video Settings queda completamente vanilla.
8. Chat, inventario, contenedores y UI normal de gameplay no reciben skin, banda, transición ni reemplazo global de clicks Jobs.
9. Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
10. Música y ambiente del menú aplican hard-stop al entrar a gameplay.
11. Pausa/Config Jobs pueden conservar tema y gestos breves sin reabrir la sesión musical.
12. PNG 10–17 son totalmente estáticos.
13. JPG 18–31 sólo admiten respiración de cámara sutil, no destructiva y desactivable.
14. El rojo queda reservado a Executores.
15. Accesibilidad, Movimiento reducido y Bajo consumo tienen prioridad sobre decoración.
16. Ningún control visible puede tener una hitbox invisible superpuesta.
17. Pantallas complejas conservan lógica Minecraft/Forge real cuando eso protege compatibilidad.
18. El servidor oficial único es `JobsDosh.exaroton.me:56477`.
19. ESC y Cancelar de Multiplayer deben volver al padre Jobs con una sola acción.
20. F5/Actualizar conserva selección online por IP y no crea una pantalla Multiplayer vanilla intermedia.
21. Un servidor remoto vuelve a Multiplayer Jobs tras salida/kick/pérdida de conexión; un mundo local vuelve al main Jobs.
22. Las tres pistas musicales actuales son Absurdism, REQUIEM y Upon the Hill V2.
23. El build no descarga música ni fondos externos.
24. El catálogo musical acreditado requiere `assets/jobsmenu/musica_creditada.txt` y ese marcador debe corresponder a las tres pistas empaquetadas.
25. Los callbacks de resource reload nunca manipulan `SoundInstance` directamente desde el executor de recursos.
26. **La música Jobs nunca usa `minecraft:music.menu` como fallback**: registro propio o silencio/reintento.
27. El catálogo musical se construye una sola vez por JVM; consultas de HUD/crossfade no deben recrearlo.
28. El hard-stop musical ordena también el corte directo al `SoundManager`.

## Estado 0.40.0

### Identidad musical

`GestorMusica` deja de resolver pistas propias contra `SoundEvents.MUSIC_MENU`. Si un `RegistryObject` no está disponible, la pista entrante no se crea y la actual no se retira por error. El gestor programa reintento y registra una sola advertencia por visita/reload.

### Catálogo estable

Las tres entradas viven en `CATALOGO`, creado una vez. `catalogo()` devuelve esa misma estructura y título/autor/cantidad consultan directamente el catálogo estático.

### Hard-stop reforzado

Al cortar una instancia musical se ponen sus ganancias/volumen a cero, se marca `stop()` y se ordena `SoundManager.stop(instance)`. El objetivo es que gameplay y resource reload no dependan únicamente del siguiente tick del motor.

## Estado 0.39.0

- `musica_creditada.txt` vuelve a representar las tres pistas acreditadas actuales;
- resource reload usa generación atómica y reprograma si llega otra generación;
- `SesionMenu.abrir()` no reinicializa una visita ya activa;
- diagnóstico oculto incluye pista dominante y generación de reload.

## Estado heredado importante

### 0.38.0 — rendimiento

- reflection de listas cacheada por clase/Screen;
- scrollbars Jobs deduplicadas por frame;
- cachés de listas/hover liberadas al cerrar Screen;
- filtrado de fondos por instancia de textura;
- menos asignaciones de avisos/UI;
- snapshot compartido de `RotacionNiveles` dentro del mismo milisegundo;
- Bajo consumo reduce draw calls reales;
- JAR reproducible sin timestamp variable.

### 0.35–0.37 — navegación/audio

- clicks/hover Jobs también en controles vanilla preservados de superficies Jobs;
- retorno contextual después de servidor;
- ESC/Cancelar Multiplayer directos e idempotentes;
- cero transiciones en gameplay;
- F5 conserva selección por IP y feedback Jobs.

## Música

1. Absurdism
2. REQUIEM — `Emmy Z - Forsaken OST`
3. Upon the Hill V2 — `ft. @iCosmicCoffee`

Reglas: inicio aleatorio o pista fija persistente; crossfade sin repetición inmediata; `N` sólo en Aleatoria; `M` controla silencio Jobs; F3+T invalida instancias del motor anterior; gameplay corta inmediatamente; no existe fallback musical vanilla.

## Multiplayer

Servidor oficial primero, único y protegido; `Ghoul Outbreak` no reaparece; conectar usa `PantallaMultijugadorJobs` como padre de `ConnectScreen`; F5/Actualizar reconstruye Jobs directamente y restaura selección online por IP; logout/kick/pérdida de conexión remota vuelve a Multiplayer Jobs.

## Fondos

- 0–9: escenas procedurales Jobs.
- 10–17: PNG históricos estrictamente estáticos.
- 18–31: JPG 1920×1080 directos, movimiento de cámara mínimo opcional.

## Verificación

CI ejecuta política de versión, fondos, verificador general, UI/música, continuidad Multiplayer/documentación, optimización 0.38, créditos/reload 0.39, identidad musical/hard-stop 0.40, build Forge Java 17 y publicación versionada sólo desde `main` verde.

La validación visual, input, audio perceptivo y compatibilidad final con el modpack siguen siendo manuales en `test-1`.
