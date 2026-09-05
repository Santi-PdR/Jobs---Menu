# Registro de cambios

## 0.37.0 — Continuidad de Multiplayer y documentación — 2026-09-05

### Multiplayer

- F5/Actualizar conserva la IP del servidor online seleccionado y restaura una Entry nueva con la misma IP después de reconstruir `PantallaMultijugadorJobs`.
- Nunca se reutiliza una `ServerSelectionList.Entry` de la lista anterior; las entradas LAN efímeras vuelven a depender del detector recién creado.
- `refrescarLista()` activa el guard `cerrando` antes de cambiar de pantalla para impedir recargas repetidas sobre la instancia saliente.
- F5 por teclado reproduce `UI_ALTERNAR` Jobs una vez; el botón Actualizar mantiene su gesto propio sin duplicado.
- El indicador inferior reutiliza la traducción de `selectServer.refresh` y elimina el literal duro `JOBS/SERVER`.

### Documentación y calidad

- Se crea `docs/README.md` como índice de contrato vigente frente a auditorías históricas.
- `CHANGELOG.md` recupera las entradas 0.35.0 y 0.36.0 que no habían quedado registradas.
- README, CONTEXTO, KNOWN_ISSUES, checklist y compatibilidad se sincronizan con 0.37.0.
- Nuevo `tools/verificar_continuidad.py` fija en CI la continuidad F5, el guard de recarga, el feedback y la coherencia documental básica.
- Versión: **0.37.0**.
- Artefacto esperado: **`jobsmenu-0.37.0.jar`**.

## 0.36.0 — Cierre fiable de Multiplayer y cero transiciones en gameplay — 2026-09-05

### Multiplayer

- `PantallaMultijugadorJobs` guarda explícitamente el padre Jobs y ESC/Cancelar convergen en `cerrarAlPadre()`.
- El cierre deja de delegar en `super.onClose()`/`popGuiLayer()` y usa `minecraft.setScreen(padreDestino())` una sola vez con guard idempotente.
- F5/Actualizar reconstruye directamente una pantalla Jobs con el mismo padre, sin una `JoinMultiplayerScreen` vanilla intermedia.
- Conectar mantiene `PantallaMultijugadorJobs` como padre real de `ConnectScreen`.

### Gameplay

- Si `Minecraft.level != null`, no se crea ni dibuja `TransicionInterfazJobs`.
- Login, logout y tick de gameplay cancelan cualquier transición pendiente.
- `PulidoInterfazJobs.notificarApertura()` tampoco registra su animación corta durante gameplay.
- Pausa/Config Jobs conservan tematización y feedback breve; chat, inventario, contenedores y Video Settings permanecen fuera.

### Calidad y entrega

- Los verificadores fijan la ruta directa de cierre/refresh y la frontera absoluta de transiciones.
- Versión: **0.36.0**.
- Artefacto: **`jobsmenu-0.36.0.jar`**.

## 0.35.0 — Feedback Jobs y retorno contextual tras servidor — 2026-09-05

### Sonidos de interfaz

- La sustitución de `minecraft:ui.button.click` se desacopla de la sesión musical: una pantalla propia Jobs puede usar click Jobs incluso dentro de pausa/configuración con mundo cargado.
- Los controles vanilla preservados por compatibilidad reciben `UI_PASAR` al entrar con ratón o foco, con deduplicación por instancia.
- Los widgets Jobs propios quedan fuera de ese seguimiento global para no duplicar hover.
- Video Settings, chat, inventario y demás gameplay no Jobs permanecen excluidos.

### Retorno tras servidor

- Se memoriza si la sesión jugable pertenece a un servidor remoto usando `Minecraft#getCurrentServer()` antes de la limpieza de logout.
- Al salir, ser expulsado o perder conexión de un servidor, Title/Multiplayer vanilla se reconducen a `PantallaMultijugadorJobs`.
- Un mundo local continúa regresando a `PantallaNivel`.
- Cancelar o fallar antes del login sigue volviendo a la misma lista Jobs porque `ConnectScreen` conserva esa pantalla como padre.

### Documentación y entrega

- Se documenta la separación entre feedback corto de UI y lifecycle de música/ambiente.
- Se limpia `docs/AUDITORIA_SEGUNDA.md` para marcarla como referencia histórica y no como contrato vigente.
- Versión: **0.35.0**.
- Artefacto: **`jobsmenu-0.35.0.jar`**.

## 0.34.0 — Navegación fiable y frontera de gameplay — 2026-09-05

### Multiplayer

- Escape y Cancelar convergen en una única llamada idempotente a la navegación vanilla de `JoinMultiplayerScreen`.
- Se elimina el padre manual duplicado y la captura paralela de Escape que podían reprocesar la salida.
- Conectar sigue usando la pantalla Jobs como padre real, por lo que Cancelar y los errores regresan a Multiplayer.

### Menús frente a gameplay

- Las transiciones sólo se crean cuando participa una pantalla propia del paquete `client.screen`.
- Cualquier cambio ajeno cancela la transición pendiente.
- Con un mundo cargado, chat, inventario y demás pantallas no Jobs quedan fuera de piel, banda contextual, transición y reemplazo de clicks.
- Pausa Jobs y sus pantallas de configuración siguen tematizadas; Video Settings continúa vanilla.

### Calidad y entrega

- Los dos verificadores fallan si reaparecen rutas duplicadas de Multiplayer o si se pierde la frontera de gameplay.
- Versión: **0.34.0**.
- Artefacto esperado: **`jobsmenu-0.34.0.jar`**.
- Fondos, música, ambiente, servidor fijado y lógica de conexión permanecen intactos.


## 0.33.0 — Video Settings vanilla y aislamiento de compatibilidad — 2026-09-05

### Restauración de Video Settings

- Se elimina `PantallaVideoJobs`: ya no se recoloca la lista vanilla ni se oculta su botón Done.
- `PantallaOpcionesJobs` abre directamente `VideoSettingsScreen`, sin reflection ni reconstrucción parcial de Embeddium.
- Video Settings queda excluida de pieles, marcos, transiciones y reemplazo de clicks Jobs durante toda la sesión.
- El resultado conserva el catálogo completo de opciones que proporcione Minecraft en esa pantalla.

### Mejoras del hub

- Se elimina el slider FOV duplicado; FOV vuelve a tener una única fuente de verdad dentro de Video Settings.
- Online ocupa el ancho completo de la última fila, evitando un hueco y ampliando su zona de interacción.
- El tooltip de Video aclara en ES/EN que abre la pantalla vanilla completa.

### Calidad y entrega

- CI falla si reaparece `PantallaVideoJobs`, reflection de Embeddium o cualquier capa Jobs sobre Video Settings.
- Versión: **0.33.0**.
- Artefacto esperado: **`jobsmenu-0.33.0.jar`**.
- Audio, música, fondos, multiplayer y configuración Jobs permanecen intactos.


## 0.32.0 — Ambientes completos y controles precisos — 2026-09-04

### Audio ambiental

- Los niveles 18-31 reciben combinaciones explícitas de base, carácter y actividad; ya no heredan silenciosamente el ambiente del nivel 0.
- Cada uno define repertorio de sucesos, intervalo, balance de capas y afinación estable según su composición.
- Se reutilizan los OGG Jobs existentes y autorizados; no se incorporan muestras externas.
- El cambio de nivel conserva los crossfades de tres capas y el silencio intencional entre eventos.

### Interacción y ajustes

- Los sliders dejan de usar un porcentaje universal: volumen muestra `%`, los tiempos `s` y nivel/pista muestran su posición real en el catálogo.
- Reactivar Sonidos de interfaz confirma la acción con el gesto Jobs de alternar.
- Pulsar `N` cuando la pista es fija, la música está apagada o hay un crossfade mantiene el estado y responde con `UI_NEGADO`.

### Calidad y entrega

- `tools/verificar_ui_musica.py` certifica las tres camas, repertorio, frecuencia, balance y afinación de los 14 fondos nuevos.
- El mismo verificador impide reintroducir porcentajes engañosos en sliders no porcentuales.
- Versión: **0.32.0**.
- Artefacto esperado: **`jobsmenu-0.32.0.jar`**.
- Los JPG 18-31, PNG 10-17 y pistas musicales permanecen intactos.


## 0.31.0 — Identidad sonora, pista fija y retorno multijugador — 2026-09-04

### Sonidos de interfaz

- Se elimina el fallback a `minecraft:ui.button.click`: click, hover, confirmación, volver, alternar y negado conservan siempre los OGG Jobs.
- Los controles vanilla mantenidos por compatibilidad sustituyen su click mediante `PlaySoundEvent` mientras la sesión Jobs está activa.
- Hover, confirmación, negado y volver reciben franjas de tono diferentes y variación mínima propia.
- Si un registro aún no está listo, el gesto se omite de forma segura en lugar de sonar vanilla o provocar un crash.

### Música

- Audio incorpora un selector persistente: Aleatoria, Absurdism, REQUIEM o Upon the Hill V2.
- Una pista fija entra con crossfade, no rota automáticamente y no es reemplazada por `N`.
- Volver a Aleatoria conserva la pista actual y reactiva la rotación 2–4 minutos.
- El selector convive con encendido, volumen, ambiente, gestos y créditos sin agrandar el panel.

### Multijugador y ajustes

- Conectar a un servidor usa `PantallaMultijugadorJobs` como padre real de `ConnectScreen`; Cancelar y los errores regresan a la lista multijugador, no al main.
- El selector visual de `nivel_fijo` se corrige de 0–17 a 0–31.
- Se amplían los verificadores y el checklist para cubrir los cuatro contratos.

### Entrega

- Versión: **0.31.0**.
- Artefacto esperado: **`jobsmenu-0.31.0.jar`**.
- Fondos, pistas y archivos OGG empaquetados permanecen intactos.

## 0.30.0 — Composición adaptativa del main — 2026-09-04

### Legibilidad y layout

- El rótulo inferior del nivel reserva ahora una franja propia para el estado de instalación; ambos dejan de ocupar las mismas coordenadas en ventanas bajas o nombres de dos líneas.
- El crédito musical se adapta al ancho disponible, envuelve título/autor y se oculta limpiamente en el modo compacto, donde la prioridad es el aviso.
- Las pistas sin autor, como Absurdism, ya no reservan ni dibujan una segunda línea vacía.
- El bloque de crédito recibe un respaldo oscuro proporcional a su contenido y nunca empieza fuera de la pantalla.
- La lógica no cambia hitboxes, acciones, atajos, música ni recursos de los niveles.

### Calidad y entrega

- `tools/verificar_ui_musica.py` incorpora un contrato automático para impedir que se pierdan las reservas de layout.
- `README`, `CONTEXTO`, `KNOWN_ISSUES` y el checklist manual quedan sincronizados.
- Versión: **0.30.0**.
- Artefacto esperado: **`jobsmenu-0.30.0.jar`**.
- Los JPG 18–31 y los PNG estáticos 10–17 permanecen intactos.

## 0.29.0 — Revisión visual real y encuadre por fondo — 2026-09-04

### Fondos 18–31

- Los 14 JPG se abrieron desde el artefacto generado y se revisaron uno por uno contra el texto visible.
- Se corrigen especialmente los niveles 19, 21, 23, 25, 26, 28, 29, 30 y 31, cuyos nombres anteriores eran demasiado genéricos o no correspondían bien a la composición.
- Nombres vigentes: Interferencia carmesí, La estrella del vacío, El huésped de tinta, El claro de los centinelas, La caverna del vigía, La maraña orgánica, El umbral escarlata, La señal sobre el bosque, El observador lunar, La fortaleza roja, El núcleo fragmentado, El soberano escarlata, La figura fragmentada y El coloso del vacío.
- Se reescriben las tres notas ES/EN de cada JPG para que mencionen únicamente rasgos visibles de la escena.
- `PlantaImagen` gana punto de interés por nivel y reduce el paneo; la respiración conserva mejor sujetos cercanos a los bordes.
- Los niveles 10–17 siguen totalmente estáticos y sin cambios.

### Lectura / localización

- `RotulosNivelesImagen` deja de duplicar 14×2 catálogos literales: los `lang/*.json` vuelven a ser la única fuente de verdad.
- El rótulo del nivel ya no desaparece cuando nombre/nota no caben en una sola línea; ahora ajusta el ancho y envuelve texto.
- La rotación de notas se reinicia al cambiar de nivel y usa un fundido corto, desactivado visualmente por Movimiento reducido o Bajo consumo.
- Se corrige el checklist manual que todavía pedía ver la antigua barra de atajos retirada en 0.28.0.

### Entrega

- Versión: **0.29.0**.
- Artefacto esperado: **`jobsmenu-0.29.0.jar`**.
- `nivel_fijo` permanece en **0–31** y no se reescribe ningún JPG.

## 0.28.0 — Lectura limpia y catálogo visual — 2026-09-04

### Menú principal

- La capa profesional genérica deja de dibujarse sobre `PantallaNivel`: desaparecen del pie del menú los rótulos visibles `1-4`, `F`, `M`, `N`, `TAB` y `ENTER` que competían con el nombre y la nota del nivel.
- Los atajos continúan funcionando exactamente igual; se elimina únicamente su rotulación redundante en el main.
- Las pantallas secundarias conservan su instrumentación contextual y ayudas de teclado.
- El nombre y la nota del nivel recuperan una zona inferior limpia y vuelven a ser la información dominante del fondo.

### Fondos 18–31

- Se revisaron visualmente los **14 JPG reales** usados por los niveles 18–31.
- Se sustituye el naming genérico anterior por un catálogo visual explícito ES/EN, evitando nombres deducidos únicamente por color, índice o paleta.
- Nuevos nombres: Interferencia carmesí, La anomalía púrpura, El huésped de tinta, El claro del centinela, La caverna del vigía, La cámara de pánico, El umbral escarlata, El bosque bajo la señal, La luna del observador, La fortaleza roja, El registro corrompido, La entidad del borde, El distrito de caza y El nexo de contención.
- Cada uno recibe tres notas nuevas relacionadas con elementos realmente visibles en su imagen.
- Los niveles 0–17 mantienen su sistema histórico de traducciones y comportamiento intactos.

### Entrega

- Versión: **0.28.0**.
- Artefacto esperado: **`jobsmenu-0.28.0.jar`**.
- Se mantiene `nivel_fijo` en **0–31**, los fondos 10–17 estáticos y el movimiento opcional/no destructivo de 18–31.

## 0.27.0 — Fondos 18–31 directos — 2026-09-04

### Fondos / escena

- Se agregan **14 niveles nuevos, 18–31**, usando los JPG que el usuario subió directamente a `src/main/resources/assets/jobsmenu/textures/backgrounds/`.
- Los archivos `nivel18.jpg` a `nivel31.jpg` se conservan como recursos reales **1920×1080, 16:9**; no se usa ZIP, Base64, extracción en Gradle ni conversión intermedia.
- El catálogo pasa de 18 a **32 niveles (0–31)**.
- `nivel_fijo` y su setter pasan a admitir todo el rango **0–31**.
- ES/EN reciben nombre y tres notas propias para cada nivel nuevo.
- `tools/verificar_fondos.py` valida directamente PNG 10–17 y JPEG 18–31.
- Los PNG 10–17 permanecen sin cambios y conservan su contrato totalmente estático.
- Los JPG 18–31 pueden recibir una respiración de cámara muy leve y no destructiva durante el render. Movimiento reducido, Bajo consumo o escena quieta la desactivan.
- El movimiento no reescribe el JPG, no deforma la imagen y no agrega objetos falsos.

### Entrega

- Versión: **0.27.0**.
- Artefacto: **`jobsmenu-0.27.0.jar`**.
- Catálogo y mapeo: `docs/FONDOS_18_31.md`.
- La validación visual final continúa siendo manual dentro de Minecraft.

## 0.26.0 — Correcciones de capturas y nuevo Depósito — 2026-09-03

- Retirado por completo `SHIFT CONTROL` y el `JOBS / LEVEL` técnico duplicado del main.
- Mods vuelve a conservar la geometría real de Forge.
- Resource Packs conserva las dos listas reales de Minecraft sin reposicionarlas de forma destructiva.
- Mundos y Multiplayer vuelven al padre Jobs con una sola acción de ESC/Volver.
- `N` queda conectado al cambio real de pista y anunciado en la barra inferior contextual.
- Corregido el `%s` literal de la fecha del turno.
- Reescritos los avisos rotativos ES/EN.
- Absurdism de runtime fue actualizado desde la nueva fuente del repositorio.
- Nivel 1 usa `DepositoNuevo`; el renderer anterior permanece respaldado.

## 0.25.0 — Catálogo musical real — 2026-09-03

- Absurdism, REQUIEM y Upon the Hill V2 pasan a ser tres pistas independientes.
- Inicio aleatorio por visita, sin repetición inmediata, rotación automática y crossfade.
- `N` solicita la siguiente pista; `M` conserva el mute Jobs.
- Créditos musicales siguen a la pista dominante.
- Hard-stop de música/ambiente al entrar a gameplay.
- F3+T, navegación por subpantallas y watchdog de sesión quedan integrados al lifecycle musical.

## 0.24.0 — Navegación contextual y controles — 2026-09-02

- Barra inferior contextual y navegación por teclado ampliadas.
- Atajos 1–4 en main y 1–2 en pausa, protegidos frente a EditBox y modificadores.
- Controles vanilla/Forge reciben tematización sin alterar callbacks ni hitboxes.
- Scrollbars Jobs ganan lectura de progreso sin sustituir el scroll real.
- Los PNG 10–17 quedan explícitamente sin zoom, paneo, parallax, flicker o deformación.

## 0.23.0 — Instrumentación y acabado — 2026-09-02

- `CapaProfesionalJobs` centraliza código de pantalla, estado de navegación y ayudas contextuales.
- Se profundizan transiciones, feedback de foco y pulido de controles.
- Posteriormente, en 0.26.0, se elimina el HUD `SHIFT CONTROL` que había nacido en esta etapa.

## 0.22.x — Main, pausa y atmósfera — 2026-09-02

- Mejora de composición del main y pausa.
- Transiciones de expediente y easter eggs administrativos discretos.
- Contrato de imagen estática para PNG 10–17.

## 0.21.0 — Profesionalización transversal — 2026-09-02

- Perfiles Jobs: Equilibrado, Inmersivo, Rendimiento, Accesible y Mínimo.
- Mejoras transversales sobre botones, toggles, sliders, renglones y accesibilidad.

## 0.20.0 — Interfaces avanzadas — 2026-09-02

- Mundos, Multiplayer, Mods, Resource Packs, Idioma, Sonido, Video y Pausa reciben composiciones Jobs más completas.
- Se mantiene comportamiento real de Minecraft/Forge en pantallas sensibles.

## 0.19.x — Robustez de UI y sesión — 2026-09-02

- Bajo consumo se extiende a widgets compartidos.
- Gameplay se convierte en frontera dura para audio de menú.
- Se mejoran Idioma, Multiplayer y pantallas de archivo.

## 0.18.x — Música y microinteracciones — 2026-09-02

- Refino de microinteracciones y transiciones.
- Se prepara integración controlada de música sin descargas externas durante build.

## 0.17.x / 0.16.x — Cohesión de interfaz — 2026-09-01

- Separación de paleta escena/UI.
- Ciclo de sesión musical endurecido.
- Servidor oficial protegido y deduplicado.
- Correcciones de títulos vanilla, búsquedas, scroll y layout basadas en capturas.

## 0.15.x / 0.14.x — Sistema Jobs de pantallas

- Nacen las principales pantallas y wrappers Jobs.
- Options/Config, widgets, `ChromeExpediente`, `PielVanillaJobs`, scrollbar Jobs y transición entre expedientes se consolidan como sistema compartido.

## 0.13.0 — Fondos de imagen estáticos

- PNG 10–17 pasan a ser estrictamente estáticos.
- Correcciones de scroll, hitboxes y navegación.

## 0.12.0 y anteriores

- Base Forge/config cliente.
- Menú principal diegético, pausa tematizada, audio por capas, rotación de niveles, accesibilidad y herramientas de auditoría.

Para el estado vigente mandan `CONTEXTO.md`, `README.md`, `KNOWN_ISSUES.md`, `docs/README.md`, `docs/checklist-manual.md` y este archivo. Las auditorías históricas detalladas permanecen en `docs/` y en el historial Git.
