# Registro de cambios

## 0.20.0 — Interfaz avanzada y cierre visual — 2026-09-02

### Pantallas

- Mundos: superficie central propia, scrollbar Jobs, búsqueda integrada y pie contextual.
- Multiplayer: tablero central, tarjeta reforzada del servidor oficial, selección/protección visible y scrollbar Jobs.
- Mods: catálogo y detalle separados visualmente, buscador integrado, scrollbar Jobs y mejor eliminación del aspecto “Forge con un marco”.
- Resource Packs: doble archivador visual conservando las dos listas y acciones reales de Minecraft.
- Idioma: layout responsive, hover/selección/idioma aplicado diferenciados, badges de código y estado actual → pendiente.
- Sonido: bandeja interior de mezcla, raíles laterales, marcas de canal y scrollbar Jobs.
- Video vanilla: ficha de calibración con marco de visor, esquinas, escala y scrollbar Jobs. Embeddium conserva su UI real.
- Pausa: oscurecido por capas, sombra de hoja reforzada, guías laterales y marcas de registro sobre el mundo real pausado.

### Calidad

- Se conservan los 73 refinamientos visibles previos de botones, toggles, sliders, renglones y pulido global.
- Bajo consumo sigue eliminando tweens decorativos continuos.
- No se cambian hitboxes reales ni acciones de Minecraft/Forge para lograr el nuevo acabado.
- Se actualizan README, CONTEXTO, KNOWN_ISSUES, checklist manual, despliegue y auditoría de entrega.

### Entrega

- Versión: **0.20.0**.
- Artefacto: **`jobsmenu-0.20.0.jar`**.
- La prueba visual real sigue siendo manual en `test-1`; CI certifica estructura, recursos y build.

## 0.19.0 — Robustez y múltiples pases visuales — 2026-09-02

- Bajo consumo se extiende a widgets compartidos.
- Idioma mejora aplicación/recarga y teclado.
- Gameplay se convierte en frontera dura para audio de menú.
- Multiplayer mejora distribución responsive.
- Pase de 73 mejoras visibles sobre botones, toggles, sliders, renglones y pulido global.
- Segundo pase profundiza Mundos y Multiplayer.
- Tercer pase rehace Mods, Recursos e Idioma con composiciones más propias de Jobs.

## 0.18.0 — Pulido profesional e integración de música preparada — 2026-09-02

- Microinteracciones y transiciones globales refinadas.
- Workflow `Integrar OGG subido` preparado para `music/menu_nueva.ogg`.
- Eliminada infraestructura obsoleta de integración de audio.
- Mantiene superficies UI frías/neutras y reglas de sesión musical.

## 0.17.x — Cohesión de interfaz y sesión — 2026-09-01

- Se consolida separación escena/UI.
- Se endurece lifecycle de música/ambiente.
- Se mantienen variantes españolas localizadas y servidor oficial protegido.

## 0.16.2 — Interfaz neutra y ciclo de sesión seguro — 2026-09-01

- Paleta administrativa pasa a hueso frío, grafito y gris verdoso desaturado.
- Se eliminan títulos vanilla que sangraban detrás de Jobs.
- Multiplayer deduplica `JobsDosh.exaroton.me:56477` y elimina `Ghoul Outbreak`.
- Entrar a gameplay corta inmediatamente música/ambiente de menú.

## 0.16.1 — Corrección visual basada en capturas — 2026-09-01

- Idioma recupera lista completa y campo de búsqueda Jobs.
- Mundos, Multiplayer y Recursos eliminan títulos/bandas vanilla visibles.
- Mods y campos oscuros reciben neutralidad visual.
- Config Jobs corrige solapes y etiquetas largas.

## 0.16.0 — Interfaz profesional y respuesta inmediata — 2026-09-01

- Sistema global de foco, entrada, confirmación y microinteracciones.
- Idioma añade búsqueda/códigos/atajos.
- Multiplayer fija y protege servidor oficial.
- Mods/Recursos adoptan archivo oscuro.

## 0.15.x — Archivos compactos y navegación profesional — 2026-09-01

- Options/Config se compactan.
- Sonido, Video, Chat, Accesibilidad, Online, Mouse y Teclas comparten geometría Jobs.
- Singleplayer pasa a Archivo de turnos.
- Multiplayer pasa a Puestos de acceso.
- Se elimina el resource pack musical legado.

## 0.14.x — Profesionalización del sistema de interfaz — 2026-09-01

- `PantallaOpcionesJobs`, `PantallaAjustesAviso`, widgets de segunda generación, `ChromeExpediente`, `PielVanillaJobs`, scrollbar Jobs y transición entre expedientes.
- Mundos, Mods y Resource Packs comienzan su integración Jobs preservando lógica real.

## 0.13.0 — PNG estáticos y pulido de controles — 2026-08-31

- PNG 10–17 pasan a ser estrictamente estáticos.
- Scrollbar Jobs y correcciones de hitboxes/layout.
- Se restaura Mods → Jobs Menu → Config.

## 0.12.0 — Familia de interfaces Jobs — 2026-08-31

- Nacen wrappers y pantallas propias para Options, Sonido, Video, Chat, Accesibilidad, Mouse, Teclas, Online, Recursos, Piel, Idioma, Controles y Multiplayer.
- `ChromeExpediente`, widgets Jobs, `ListasExpediente` y transiciones forman un sistema compartido.

## 0.11.0 — Pulido profesional y entregas versionadas — 2026-08-31

- JARs pasan a llevar versión obligatoria.
- GitHub Actions publica `dev-latest` con un único asset versionado.
- PowerShell de despliegue consulta la release y valida el JAR antes de instalar.

## 0.10.x y anteriores — Resumen histórico

- Base Forge/config cliente.
- Menú principal diegético y pausa tematizada.
- 18 niveles (0–17), fondos procedurales y PNG.
- Audio por capas, eventos, música con lifecycle propio y presencia de fondo.
- Cuenta regresiva, La Suspensión, accesibilidad y herramientas de auditoría.

Los documentos históricos detallados permanecen en `docs/` y en el historial Git. Para el estado vigente mandan `CONTEXTO.md`, `README.md`, `KNOWN_ISSUES.md`, `docs/checklist-manual.md` y este changelog.
