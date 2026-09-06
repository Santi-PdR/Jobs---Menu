# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado vigente. Las auditorías antiguas son históricas y no deben revertir este contrato.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.39.0** |
| Artefacto esperado | **`jobsmenu-0.39.0.jar`** |
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

## Estado 0.39.0

### Créditos musicales

`GestorMusica.creditoAlfa()` ya dependía de `musica_creditada.txt`, pero ese marcador había sido eliminado durante una etapa anterior. 0.39.0 lo restaura como evidencia interna del catálogo acreditado actual:

- `absurdism`;
- `requiem`;
- `upon_the_hill_v2`.

Esto vuelve coherentes el código, los créditos del HUD y la documentación. No añade ni reemplaza archivos OGG.

### Resource reload

`RecargaRecursosCliente` usa una generación atómica además del guard de tarea pendiente. Una ráfaga de recargas puede compartir la misma pasada, pero si una nueva generación llega mientras se cierran instancias se agenda otra vuelta en el hilo cliente.

Objetivo: que combinaciones rápidas de idioma, F3+T y resource packs no dejen una recarga posterior representada por estado viejo.

### Sesión

`SesionMenu.abrir()` ya no vuelve a invocar el arranque ambiental cuando la visita ya estaba activa. Navegar Main → Options → Mods → Recursos sigue siendo una sola visita y el mantenimiento normal continúa por tick.

### Diagnóstico interno

El diagnóstico oculto registra ahora:

- pista musical dominante;
- contador/generación de resource reload;
- capas ambientales activas;
- reintento musical y opciones principales.

No se añade ningún control visible ni se documenta el atajo como función de usuario.

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

Catálogo real:

1. Absurdism
2. REQUIEM — `Emmy Z - Forsaken OST`
3. Upon the Hill V2 — `ft. @iCosmicCoffee`

Reglas:

- inicio aleatorio por visita si el selector está en Aleatoria;
- pista fija persistente 1–3;
- crossfade sin repetición inmediata en Aleatoria;
- `N` sólo adelanta en Aleatoria;
- `M` controla silencio Jobs;
- F3+T/reload invalida instancias ligadas al SoundEngine anterior;
- gameplay corta música y ambiente inmediatamente.

## Multiplayer

- servidor oficial primero, único y protegido;
- `Ghoul Outbreak` no reaparece;
- Direct Connect/Add/Edit/Delete permanecen sobre lógica real de Minecraft;
- conectar usa `PantallaMultijugadorJobs` como padre de `ConnectScreen`;
- Cancelar/error pre-login vuelve a la misma lista;
- F5/Actualizar reconstruye Jobs directamente y restaura selección online por IP;
- LAN, ping, MOTD y favicons siguen siendo responsabilidad de `JoinMultiplayerScreen`.

## Fondos

- 0–9: escenas procedurales Jobs.
- 10–17: PNG históricos estrictamente estáticos.
- 18–31: JPG 1920×1080 directos, movimiento de cámara mínimo opcional.

Movimiento reducido, Bajo consumo o escena quieta desactivan el movimiento de 18–31.

## Verificación

CI ejecuta:

1. política de versión;
2. fondos 10–31;
3. verificador estático general;
4. UI/música;
5. continuidad Multiplayer/documentación;
6. contratos de optimización;
7. créditos + generaciones de reload;
8. build Forge real con Java 17;
9. artefacto versionado;
10. publicación a `dev-latest` sólo desde `main`.

La validación visual, input, audio perceptivo y compatibilidad final con el modpack siguen siendo manuales en `test-1`.
