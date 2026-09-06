# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menús con una interfaz administrativa/industrial propia, audio de sesión y 32 fondos/niveles, conservando la lógica vanilla/Forge cuando reimplementarla perjudicaría compatibilidad.

| Campo | Valor |
|---|---|
| Versión | **0.40.0** |
| Artefacto | **`jobsmenu-0.40.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0–31)** |

## 0.40.0 · Identidad musical y corte absoluto

0.40.0 endurece el audio del menú sin ampliar Jobs hacia gameplay.

- El catálogo musical pasa a una estructura estática construida una sola vez por JVM; consultar título, autor, selector o crossfade ya no crea un array nuevo.
- Las pistas Jobs **no usan fallback a `minecraft:music.menu`**. Si un registro propio no está disponible, la pista se omite temporalmente y se reintenta en vez de sonar como Minecraft vanilla.
- El cambio fijo/manual/automático sólo empieza a retirar la pista actual después de haber resuelto correctamente la entrante.
- El hard-stop de música ordena también `SoundManager.stop(instance)` además de marcar la instancia detenida, reforzando el corte al entrar a mundo/servidor o reconstruir el motor de sonido.
- El estado de aviso de pista faltante se reinicia por visita/reload para permitir diagnóstico útil sin spam de log.
- Nuevo `tools/verificar_audio_identidad.py` bloquea regresiones de fallback vanilla, catálogo recreado y hard-stop incompleto.

## Mejoras 0.39.0

- `assets/jobsmenu/musica_creditada.txt` identifica Absurdism, REQUIEM y Upon the Hill V2 y habilita sus créditos.
- `RecargaRecursosCliente` usa generación atómica para no perder reloads encadenados.
- `SesionMenu.abrir()` no reabre mantenimiento ambiental dentro de la misma visita.
- El diagnóstico oculto informa pista dominante y generación de resource reload.

## Rendimiento 0.38.0

- reflection de listas cacheada por clase/Screen;
- scrollbars deduplicadas por frame;
- filtrado de fondos por objeto de textura;
- menos asignaciones en avisos/UI;
- snapshots de rotación compartidos dentro del mismo milisegundo;
- Bajo consumo reduce draw calls reales;
- JAR con orden reproducible y sin timestamp variable de build.

## Música

Catálogo empaquetado:

1. **Absurdism** — `musica.tema`.
2. **REQUIEM** — `musica.requiem` — crédito `Emmy Z - Forsaken OST`.
3. **Upon the Hill V2** — `musica.upon_hill` — crédito `ft. @iCosmicCoffee`.

La música pertenece a `SesionMenu`, no a una Screen. Navegar entre subpantallas no reinicia la pista. Entrar a un mundo/servidor aplica hard-stop inmediato a música y ambiente. `M` controla mute Jobs y `N` solicita la siguiente pista sólo cuando el selector está en Aleatoria.

## Interfaz y gameplay

- **Video Settings permanece completamente vanilla**, también con Embeddium/Sodium cuando sustituyen esa pantalla.
- chat, inventario, contenedores y UI normal de gameplay no reciben piel, banda, transición ni sustitución global de clicks Jobs;
- mientras `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs;
- Pausa/Config Jobs pueden mantener tema y feedback breve sin reactivar música/ambiente;
- el main reserva su zona inferior para nombre/nota del nivel, no para la antigua barra visible de atajos;
- ningún control visible debe quedar cubierto por una hitbox invisible vanilla.

## Multiplayer

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

- `Ghoul Outbreak` y duplicados legacy no deben reaparecer.
- ESC y Cancelar vuelven directamente al padre Jobs con una sola acción.
- Cancelar conexión/error antes del login vuelve a Multiplayer Jobs.
- salir/kick/pérdida de conexión desde servidor remoto vuelve a Multiplayer Jobs; un mundo local vuelve al main Jobs.
- F5/Actualizar reconstruye directamente la pantalla Jobs y conserva la selección online por IP.

## Fondos

- **10–17:** PNG históricos totalmente estáticos.
- **18–31:** JPG directos 1920×1080 con respiración de cámara sólo sutil/no destructiva; Movimiento reducido, Bajo consumo o escena quieta la desactivan.

## Build y entrega

GitHub Actions ejecuta política de versión, fondos, verificador general, UI/música, continuidad Multiplayer/docs, optimización 0.38, créditos/reload 0.39, identidad musical/hard-stop 0.40 y finalmente el build Forge real con Java 17. `dev-latest` sólo se publica desde `main` verde.

Instancia de prueba:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

CI no sustituye la prueba visual/sonora dentro de Minecraft.

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro vigente.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y límites de CI.
- [`CHANGELOG.md`](CHANGELOG.md): evolución reciente.
- [`docs/README.md`](docs/README.md): índice vigente/histórico.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): aceptación en `test-1`.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): fronteras con vanilla/Forge/mods.
- [`docs/musica.md`](docs/musica.md): catálogo y lifecycle de audio.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): publicación e instalación.
