# Auditoría 0.20.0 — Interfaz avanzada y entrega

Fecha: 2026-09-02

## Objetivo

Cerrar una nueva evolución visible del mod sin convertir la mejora en refactor invisible. Esta entrega concentra cambios perceptibles en pantallas que todavía se sentían demasiado cercanas a vanilla/Forge y documenta con claridad qué queda pendiente de prueba real dentro de Minecraft.

## Alcance visual

### Mundos

- superficie central propia;
- búsqueda integrada;
- scrollbar Jobs;
- separación más clara entre cabecera, lista y acciones.

### Multijugador

- tablero central de servidores;
- tarjeta del servidor oficial reforzada;
- estado visual de selección/protección;
- scrollbar Jobs;
- jerarquía más clara entre lista y botones.

### Mods

- catálogo y panel de detalle separados visualmente;
- buscador integrado;
- scrollbar Jobs;
- mejor supresión de título Forge duplicado;
- conserva búsqueda, orden, Config, logos, panel y abrir carpeta.

### Resource Packs

- doble archivador;
- dos bandejas visuales independientes;
- separación central y raíles;
- conserva las dos listas reales de Minecraft y sus acciones.

### Idioma

- layout responsive;
- estados distintos para hover, pendiente y aplicado;
- código de idioma como badge;
- lectura actual → pendiente;
- buscador y botones adaptativos;
- overlay de aplicación reforzado.

### Sonido

- bandeja interior de mezcla;
- marco secundario;
- marcas de canal;
- raíles laterales;
- scrollbar Jobs.

### Video

- marco de visor/calibración;
- esquinas técnicas;
- regla inferior;
- centro óptico sutil;
- scrollbar Jobs en video vanilla;
- Embeddium se conserva externo.

### Pausa

- oscurecido por capas;
- laterales más profundos;
- sombra mecánica de hoja;
- guías laterales;
- marcas de registro;
- mundo real sigue visible detrás.

## Compatibilidad preservada

No se reemplaza la lógica sensible sólo por estética:

- `SelectWorldScreen` conserva previews y callbacks;
- `JoinMultiplayerScreen` conserva ping, MOTD, LAN y conexión;
- `ModListScreen` conserva la implementación Forge;
- `PackSelectionScreen` conserva las dos listas y aplicar;
- `SoundOptionsScreen` y `VideoSettingsScreen` conservan opciones reales;
- Embeddium conserva su propia UI;
- la pausa mantiene la secuencia real de desconexión/guardado.

## Versión y artefacto

- `mod_version=0.20.0`.
- artefacto esperado: `jobsmenu-0.20.0.jar`.
- destino de prueba: `C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`.

## Certificación automática requerida

Antes de integrar a `main` deben quedar verdes:

1. Java 17;
2. política de versión;
3. validación de fondos;
4. verificador estático general;
5. contratos UI/música;
6. Forge build;
7. artifact versionado.

Después del merge se repite el pipeline completo en `main` y sólo entonces `dev-latest` puede reemplazar su JAR.

## Límite de la certificación

El entorno de desarrollo no ejecuta una instancia gráfica de Minecraft. Por eso un build verde demuestra compatibilidad de compilación y contratos estáticos, pero no permite afirmar que la estética fue observada personalmente en juego.

La aceptación visual final se hace con `docs/checklist-manual.md` y el JAR certificado instalado en `test-1`.

## Criterio de aprobación manual

La entrega se considera visualmente aprobada cuando:

- no hay títulos vanilla superpuestos;
- no hay controles fuera de pantalla ni hitboxes invisibles;
- scrollbars Jobs coinciden con las listas reales;
- Idioma funciona en GUI Scale 4/ventana estrecha;
- Mods/Recursos mantienen toda su lógica;
- Sonido/Video no pierden opciones;
- Embeddium sigue intacto;
- pausa conserva mundo, Escape y salida correcta;
- audio de menú no llega a gameplay.
