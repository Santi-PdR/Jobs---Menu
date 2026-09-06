# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menús con una interfaz administrativa/industrial propia, audio de sesión y 32 fondos/niveles, conservando la lógica vanilla/Forge cuando reimplementarla perjudicaría compatibilidad.

| Campo | Valor |
|---|---|
| Versión | **0.39.0** |
| Artefacto | **`jobsmenu-0.39.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0–31)** |

## 0.39.0 · Créditos y reload robusto

0.39.0 continúa la optimización de 0.38.0 y corrige una deuda real del lifecycle musical.

- Se restaura `assets/jobsmenu/musica_creditada.txt` como marcador explícito del catálogo musical acreditado. `GestorMusica` ya dependía de ese recurso para permitir el bloque de créditos, pero el marcador había sido eliminado en una etapa anterior.
- El marcador actual identifica **Absurdism**, **REQUIEM** y **Upon the Hill V2**; no incorpora audio nuevo ni descarga material externo.
- `RecargaRecursosCliente` usa ahora una **generación atómica**: idioma, F3+T y resource packs pueden encadenarse sin que un callback viejo haga perder una recarga posterior.
- Los cierres de instancias siguen ejecutándose en el hilo cliente, nunca desde el executor de recursos.
- `SesionMenu.abrir()` deja de reabrir mantenimiento ambiental al pasar de una pantalla Jobs a otra dentro de la misma visita.
- El diagnóstico oculto informa la pista dominante y la generación de resource reload.
- Nuevo `tools/verificar_reload_creditos.py` fija estos contratos en CI.

## Mejoras heredadas de 0.38.0

- `ListasExpediente` cachea reflection por clase/listas por Screen viva y deduplica scrollbars por frame.
- Las cachés de listas/hover se liberan al cerrar cada pantalla.
- Fondos de imagen configuran filtrado lineal una vez por objeto de textura y lo revalidan después de reload.
- `NotaAviso` reutiliza Components y cachea calendario por minuto.
- `PulidoInterfazJobs` reduce recorridos de widgets.
- `RotacionNiveles` comparte snapshots dentro del mismo milisegundo entre escena/audio/chrome.
- Multiplayer precalcula rótulos y reutiliza tooltips.
- **Bajo consumo** reduce draw calls en tratamientos de escena sin cambiar el modo normal.
- El JAR usa orden reproducible y no incorpora timestamps variables en el manifest.

## Música

Catálogo empaquetado:

1. **Absurdism** — `musica.tema`.
2. **REQUIEM** — `musica.requiem` — crédito `Emmy Z - Forsaken OST`.
3. **Upon the Hill V2** — `musica.upon_hill` — crédito `ft. @iCosmicCoffee`.

La música pertenece a `SesionMenu`, no a una Screen concreta. Navegar entre subpantallas no reinicia la pista. Entrar a un mundo/servidor aplica hard-stop inmediato a música y ambiente. `M` controla mute Jobs y `N` solicita la siguiente pista sólo cuando el selector está en Aleatoria.

## Interfaz y gameplay

Jobs usa dos familias visuales:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Resource Packs.

Contratos permanentes:

- **Video Settings permanece completamente vanilla**, también con Embeddium/Sodium cuando sustituyen esa pantalla.
- chat, inventario, contenedores y UI normal de gameplay no reciben piel, banda, transición ni sustitución global de clicks Jobs;
- mientras `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs;
- Pausa/Config Jobs pueden mantener tema y feedback breve sin reactivar música/ambiente;
- el main reserva su zona inferior para nombre/nota del nivel, no para la antigua barra visible de atajos;
- ningún control visible debe quedar cubierto por una hitbox invisible vanilla.

## Multiplayer

Servidor fijado único:

`JobsDosh.exaroton.me:56477`

Nombre localizado: `Jobs Official Server` / `Servidor oficial de Jobs`.

- `Ghoul Outbreak` y duplicados legacy no deben reaparecer.
- ESC y Cancelar vuelven directamente al padre Jobs con una sola acción.
- Cancelar conexión/error antes del login vuelve a Multiplayer Jobs.
- salir/kick/pérdida de conexión desde servidor remoto vuelve a Multiplayer Jobs; un mundo local vuelve al main Jobs.
- F5/Actualizar reconstruye directamente la pantalla Jobs y conserva la selección online por IP.

## Fondos

### Niveles 10–17

PNG históricos totalmente estáticos: sin zoom, paneo, parallax, flicker, deformación ni movimiento interno.

### Niveles 18–31

JPG directos 1920×1080 en `assets/jobsmenu/textures/backgrounds/`. Pueden recibir únicamente respiración de cámara muy sutil y no destructiva. Movimiento reducido, Bajo consumo o escena quieta la desactivan.

## Build y entrega

GitHub Actions ejecuta:

1. Java 17 y política de versión;
2. validación de fondos;
3. verificación estática general;
4. contratos UI/música;
5. continuidad Multiplayer/documentación;
6. contratos de optimización 0.38;
7. créditos/reload 0.39;
8. Forge build real;
9. JAR versionado;
10. publicación de `dev-latest` sólo desde `main` verde.

Instancia de prueba:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

CI no sustituye la prueba visual/sonora dentro de Minecraft.

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro vigente.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y límites de CI.
- [`CHANGELOG.md`](CHANGELOG.md): versiones recientes y referencias históricas.
- [`docs/README.md`](docs/README.md): índice vigente/histórico.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): aceptación en `test-1`.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): fronteras con vanilla/Forge/mods.
- [`docs/musica.md`](docs/musica.md): catálogo y lifecycle de audio.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): publicación e instalación.
- [`docs/AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md`](docs/AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md): detalle técnico de esta entrega.
