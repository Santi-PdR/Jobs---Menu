# Compatibilidad y despliegue — 0.38.0

## Perfil soportado

| Componente | Estado documentado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente; el servidor no necesita Jobs Menu |
| Versión del mod | **0.38.0** |
| Artefacto | `build/libs/jobsmenu-0.38.0.jar` |

Jobs distingue entre pantallas propias, pantallas vanilla/Forge cuya lógica debe conservar y pantallas de otros mods que debe respetar. Compatibilidad y accesibilidad tienen prioridad sobre una reimplementación cosmética frágil.

## Frontera menú / gameplay

Las sustituciones principales siguen siendo por clase exacta. Title, pausa, Options, Multiplayer, Mundos y Mods pueden entrar al flujo Jobs cuando corresponde. Una subclase de otro mod no se reemplaza automáticamente sólo por herencia.

Con un mundo o servidor cargado no existe transición Jobs. `usaTransicionJobs()` rechaza gameplay, login/logout/tick cancelan residuos, el render no dibuja `TransicionInterfazJobs` y `PulidoInterfazJobs.notificarApertura()` no se registra. Pausa/Config Jobs pueden conservar tema y feedback breve; chat, inventario, contenedores, Video Settings y pantallas de gameplay no Jobs quedan fuera.

El hard-stop de música/ambiente sigue separado del feedback corto de interfaz: un click/hover permitido en Pausa/Config Jobs no abre `SesionMenu`.

## Video Settings vanilla

`PantallaOpcionesJobs` abre `VideoSettingsScreen` directamente. Jobs no reconstruye opciones, no mueve listas, no oculta Done, no dibuja chrome/transiciones/hover Jobs y también excluye sustitutos conocidos de Embeddium/Sodium por nombre de clase. Oculus/addons siguen siendo responsabilidad de sus propias integraciones.

## Listas vanilla/Forge — optimización 0.38.0

`ListasExpediente` mantiene `AbstractSelectionList` como fuente de verdad para contenido, selección, wheel, click, drag y scroll. Jobs sólo dibuja una capa visual sobre la scrollbar real.

Antes de 0.38.0 el helper recorría por reflection toda la jerarquía de fields en cada render y construía colecciones temporales repetidamente. Ahora:

- los fields compatibles se descubren una vez por clase y se guardan en `CAMPOS_LISTA_POR_CLASE`;
- las instancias de lista se resuelven una vez para la Screen viva;
- `estilizar()` invalida la cache porque `init()`/resize pueden reconstruir widgets;
- `ScreenEvent.Closing` libera la referencia a la Screen y sus listas;
- `Render.Pre` inicia una deduplicación por frame;
- si una Screen propia y `Render.Post` solicitan la scrollbar en el mismo frame, sólo la primera llamada la dibuja;
- si reflection defensiva no puede resolver una lista modificada, Jobs no altera la lógica interna ni inventa hitboxes.

Esto reduce trabajo en Mundos, Multiplayer, Mods, Resource Packs, Idioma y cualquier otra pantalla Jobs con `AbstractSelectionList`, sin reemplazar la implementación vanilla/Forge.

## Controles vanilla tematizados

`PielVanillaJobs` dibuja papel/tinta, foco, slider y campos sobre controles reales sin cambiar callbacks, listeners, validación o hitboxes. En 0.38.0 Alto contraste se consulta una sola vez por pasada de piel y se comparte entre todos los widgets de esa Screen.

El seguimiento sonoro de hover usa un set débil que contiene sólo botones actualmente hover/focused. Al cerrar la Screen se limpia su estado; widgets Jobs propios siguen excluidos para evitar doble `UI_PASAR`.

## Render y escena

### Fondos 10–17

Son PNG estáticos por contrato permanente. No reciben zoom, paneo, respiración, parallax, flicker, niebla móvil ni deformación.

### Fondos 18–31

Son JPG directos 1920×1080 con cover y respiración de cámara muy leve/desactivable. `PlantaImagen` valida el recurso una vez con `NativeImage` y mantiene fallback procedural ante fallo.

Desde 0.38.0 el filtrado lineal ya no se vuelve a configurar en cada frame. Se guarda la identidad del `AbstractTexture`; `setFilter(true, false)` sólo se ejecuta cuando aparece un objeto de textura nuevo. Por eso F3+T/resource reload vuelve a aplicar el filtro automáticamente sin mantener referencias a un objeto viejo.

### Bajo consumo

Bajo consumo ahora reduce trabajo de render además de desactivar movimiento caro:

- vignette usa bandas más anchas;
- profundidad procedural usa menos capas;
- rebote de suelo usa menos bandas;
- humedad usa menos líneas;
- grano/presencia/motas/movimiento siguen obedeciendo sus guardas anteriores.

Con Bajo consumo desactivado se conservan las cantidades de capas del modo normal anterior.

## Estado compartido de escena/audio

Renderer, chrome, música y varias camas pueden solicitar `RotacionNiveles.capturar()` casi simultáneamente. 0.38.0 reutiliza el mismo `Estado` únicamente cuando las consultas caen en el mismo milisegundo. Al siguiente milisegundo se recalcula normalmente y `adelantar()` invalida el cache de inmediato.

La optimización no cambia estancias, transiciones, Suspensión, nivel fijo, crossfades ni hard-stop; sólo evita records/cálculos equivalentes repetidos y alinea consumidores simultáneos al mismo snapshot.

## Multiplayer

`PantallaMultijugadorJobs` conserva `ServerSelectionList`, servers.dat, ping, MOTD, favicons, LAN y acciones vanilla.

ESC y Cancelar convergen en `cerrarAlPadre()` con guard idempotente. F5/Actualizar transporta únicamente la IP de una entrada online seleccionada, crea una pantalla/lista nueva y busca una Entry fresca con la misma IP. Las entradas LAN no se fuerzan porque pertenecen al detector nuevo.

Desde 0.38.0 los rótulos que dependen sólo del tamaño/idioma de la Screen se preparan durante `init()` y los tooltips protegido/editar/eliminar se reutilizan; ya no se crean Components/Tooltips equivalentes en cada render. Esto no cambia botones ni permisos del servidor oficial.

Servidor fijado único: `JobsDosh.exaroton.me:56477`. `Ghoul Outbreak` no debe reaparecer.

## Resource Packs, idioma y recarga

Idioma y Resource Packs usan las recargas reales de Minecraft. `RecargaRecursosCliente` sigue invalidando audio asociado a un SoundEngine anterior. Variantes `es_ar`, `es_cl`, `es_ec`, `es_mx`, `es_uy`, `es_ve` se generan desde `es_es` durante `processResources`.

Después de F3+T se deben comprobar tanto audio como fondos de imagen: una nueva textura debe recuperar el filtro lineal y una nueva sesión de sonido no debe dejar instancias huérfanas.

## Build reproducible

El `Jar` usa `preserveFileTimestamps = false` y `reproducibleFileOrder = true`. Se retiró `Implementation-Timestamp`, que introducía una diferencia de manifest basada sólo en la hora de build.

Esto mejora estabilidad del binario, aunque el hash publicado sigue dependiendo también de ForgeGradle/reobf y las herramientas exactas del runner. El SHA-256 de `dev-latest` es la autoridad para instalar.

## Instancia de referencia

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\`

JAR esperado:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods\jobsmenu-0.38.0.jar`

## Certificación y límites

CI comprueba Java 17, versión, fondos, recursos/idiomas, UI/música, frontera gameplay, continuidad Multiplayer/docs, contratos de optimización y Forge build. Sólo un `main` verde publica `dev-latest`.

CI no mide FPS ni certifica percepción. Requieren prueba manual: GUI Scale extremos, listas largas, scrollbar/drag, Bajo consumo ON/OFF, F3+T, Embeddium/Oculus del modpack real, narración/teclado, audio perceptivo, LAN/pinger/favicons y retorno tras kick/desconexión.
