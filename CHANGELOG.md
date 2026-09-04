# Registro de cambios

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

Para el estado vigente mandan `CONTEXTO.md`, `README.md`, `KNOWN_ISSUES.md`, `docs/checklist-manual.md` y este archivo. Las auditorías históricas detalladas permanecen en `docs/` y en el historial Git.
