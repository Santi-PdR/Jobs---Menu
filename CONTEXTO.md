# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado vigente. Las auditorías antiguas son históricas y no deben revertir este contrato.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.41.0** |
| Artefacto esperado | **`jobsmenu-0.41.0.jar`** |
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
7. Gráficos debe abrir la pantalla registrada por Embeddium cuando `embeddium` está instalado; `VideoSettingsScreen` vanilla es sólo fallback. Jobs no reconstruye ni tematiza la GUI gráfica externa.
8. Chat, inventario, contenedores y UI normal de gameplay no reciben skin, banda, transición ni reemplazo global de clicks Jobs.
9. Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
10. Música, camas ambientales y FX puntuales del menú aplican hard-stop al entrar a gameplay.
11. Pausa/Config Jobs pueden conservar tema y gestos breves sin reabrir la sesión musical.
12. PNG 10–17 son totalmente estáticos.
13. JPG 18–31 sólo admiten respiración de cámara sutil, no destructiva y desactivable.
14. El rojo queda reservado a Executores.
15. Accesibilidad, Movimiento reducido y Bajo consumo tienen prioridad sobre decoración.
16. Ningún control visible puede tener una hitbox invisible superpuesta.
17. Pantallas complejas conservan lógica Minecraft/Forge real cuando eso protege compatibilidad.
18. El servidor oficial único es `JobsDosh.exaroton.me:56477`.
19. ESC y Cancelar de Multiplayer deben volver al padre Jobs con una sola acción.
20. F5/Actualizar y resize/maximizar/cambio de escala GUI conservan selección online por IP y posición de scroll; no crean Multiplayer vanilla intermedio.
21. Un servidor remoto vuelve a Multiplayer Jobs tras salida/kick/pérdida de conexión; un mundo local vuelve al main Jobs.
22. Las tres pistas musicales son Absurdism, REQUIEM y Upon the Hill V2.
23. El build no descarga música ni fondos externos.
24. `assets/jobsmenu/musica_creditada.txt` debe representar las tres pistas empaquetadas.
25. Los callbacks de resource reload nunca manipulan `SoundInstance` desde el executor de recursos.
26. La música Jobs nunca usa `minecraft:music.menu` como fallback.
27. Los FX ambientales Jobs nunca usan `minecraft:ambient.cave` como fallback.
28. El catálogo musical se construye una sola vez por JVM.
29. El hard-stop musical ordena también `SoundManager.stop(instance)`.
30. Los FX puntuales Jobs se rastrean mientras están activos y se cortan al cerrar la visita.
31. Multiplayer sólo guarda `servers.dat` cuando su normalización realmente modifica datos.
32. Config Jobs no programa guardado cuando el valor solicitado ya coincide con el actual.
33. El hover vanilla preservado cachea botones por Screen/init en vez de recorrer todos los hijos por frame.
34. Jobs corta `MusicManager` una vez al abrir visita y bloquea nuevas instancias `SoundSource.MUSIC` mientras la sesión está activa; no hace polling de stop por tick.
35. La integración con Embeddium usa el `ConfigScreenHandler.ConfigScreenFactory` de Forge, no reflection ni una dependencia binaria a clases internas de Embeddium.
36. `SodiumOptionsGUI`, las GUI de Embeddium y las pantallas gráficas de Iris/Oculus quedan excluidas de chrome, transiciones y reemplazo de clicks Jobs.

## Estado 0.41.0

### Gráficos / Embeddium

`CompatGraficos` consulta el contenedor `embeddium` y consume su `ConfigScreenHandler.ConfigScreenFactory`. En Embeddium 1.20.1 ese extension point produce su GUI real de opciones; Jobs no importa ninguna clase de Embeddium y por eso sigue siendo compatible cuando el mod no está instalado. Si no existe factory o éste falla, el botón Gráficos cae a `VideoSettingsScreen` vanilla.

`EscuchaCliente.esVideoIntocable()` reconoce `VideoSettingsScreen`, `me.jellysquid.mods.sodium.client.gui.*`, `org.embeddedt.embeddium.gui.*`, `org.embeddedt.embeddium.impl.gui.*` y las pantallas gráficas de Iris/Oculus. Esas superficies no reciben piel, banda, transición ni sustitución de clicks Jobs.

### Audio puntual y hard-stop

`RastreadorAudioJobs` conserva las instancias de eventos/FX ambientales Jobs que antes se lanzaban sin una referencia de lifecycle. Al entrar a gameplay o cerrar visita, cada instancia conocida recibe `SoundManager.stop`. Antes de registrar otra, el rastreador purga las ya finalizadas mediante `SoundManager.isActive` para no retener referencias muertas.

`MezclaAudio.ambiental()` resuelve el SoundEvent con respaldo `null`: un registro faltante produce silencio controlado, no una cueva vanilla.

### Música vanilla sin polling

`GestorMusica.atender()` ya no llama `MusicManager.stopPlaying()` cada tick. Una visita nueva corta la música vanilla una vez y `BloqueoMusicaVanillaJobs` intercepta cualquier `SoundSource.MUSIC` nuevo mientras Jobs posee el menú. Las pistas Jobs usan `MASTER`, por lo que su catálogo no se bloquea.

### Sesión idempotente

`SesionMenu.cerrar()` sólo repite un hard-stop si todavía existe sesión interna, música viva, camas ambientales o FX puntuales registrados. Esto conserva la defensa contra audio residual sin ejecutar cierres completos durante cada tick jugable después del primer corte.

### Multiplayer

F5/Actualizar guarda IP seleccionada y `getScrollAmount()`, reconstruye la pantalla Jobs y restaura una Entry nueva más `setScrollAmount()`. `resize()` captura ese mismo estado antes de que Minecraft vuelva a ejecutar `init()`, de modo que maximizar, redimensionar o cambiar escala GUI conserva contexto. El servidor oficial se deduplica/mueve/renombra como antes, pero `ServerList.save()` sólo se ejecuta cuando `cambiado` es verdadero.

### Config y UI hot-path

Los setters boolean/int comparan el valor actual antes de `set()`. Valores idénticos se omiten y no generan guardado. El perfil accesible también evita reescribir sus cinco valores cuando ya están correctos. `EscuchaCliente` mantiene una lista cacheada de botones vanilla relevantes para hover y la reconstruye al cambiar/inicializar la Screen o variar su cantidad de hijos.

### Diagnóstico

El diagnóstico oculto añade: estado interno de sesión, número de cierres efectivos, FX puntuales activos/registrados/purgados, generaciones de reload, métricas de cambios/guardados de configuración y presencia/uso/fallback del proveedor gráfico Embeddium.

## Estado heredado importante

### 0.40.0 — identidad musical

- sin fallback a `SoundEvents.MUSIC_MENU`;
- catálogo estático `CATALOGO`;
- pista entrante se resuelve antes de retirar la actual;
- hard-stop directo al `SoundManager`.

### 0.39.0 — reload/créditos

- `musica_creditada.txt` representa las tres pistas;
- resource reload usa generación atómica;
- una visita Jobs no se reinicializa al navegar entre subpantallas.

### 0.38.0 — rendimiento

- reflection/listas cacheadas;
- scrollbars deduplicadas;
- filtrado por instancia de textura;
- menos asignaciones UI;
- snapshots de rotación compartidos;
- Bajo consumo reduce draw calls;
- JAR reproducible.

## Música

1. Absurdism
2. REQUIEM — `Emmy Z - Forsaken OST`
3. Upon the Hill V2 — `ft. @iCosmicCoffee`

Inicio aleatorio o fijo, crossfade sin repetición inmediata, `N` sólo en Aleatoria, `M` mute Jobs, reload invalida instancias viejas y gameplay corta toda la sesión sonora Jobs.

## Multiplayer

Servidor oficial primero, único y protegido; `Ghoul Outbreak` no reaparece; conectar usa Jobs como padre; F5 y resize conservan selección+scroll; Cancelar/error pre-login vuelve a Jobs; logout/kick remoto vuelve a Multiplayer Jobs.

## Fondos

- 0–9: escenas procedurales Jobs.
- 10–17: PNG históricos estrictamente estáticos.
- 18–31: JPG 1920×1080 directos con respiración mínima opcional.

## Verificación

CI ejecuta política de versión, fondos, verificador general, UI/música, continuidad Multiplayer/documentación, optimización, créditos/reload, identidad musical/hard-stop, `tools/verificar_runtime_041.py`, `tools/verificar_graficos_041.py`, build Forge Java 17 y publicación versionada sólo desde `main` verde.

La validación visual, input, audio perceptivo y compatibilidad final con el modpack siguen siendo manuales en `test-1`.
