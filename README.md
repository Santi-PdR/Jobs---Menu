# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menús con una interfaz administrativa/industrial propia, audio de sesión y 32 fondos/niveles, conservando la lógica vanilla/Forge cuando reimplementarla perjudicaría compatibilidad.

| Campo | Valor |
|---|---|
| Versión | **0.41.1** |
| Artefacto | **`jobsmenu-0.41.1.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0–31)** |

## 0.41.1 · Flujo gráfico natural del modpack

0.41.1 corrige una regresión de 0.41.0: Jobs ya **no abre Embeddium ni ninguna pantalla gráfica directamente**.

- `PantallaOpcionesJobs` hereda del `OptionsScreen` real y ejecuta primero su `init()`, por lo que mixins y modificaciones de otros mods trabajan sobre la misma pantalla que usaría Minecraft normalmente.
- Jobs conserva el botón real `options.video` como fuente de comportamiento y su botón visual **Gráficos** ejecuta exactamente ese `onPress()` natural.
- La captura se vuelve a sincronizar en el primer render, después de los eventos de inicialización de Forge, para recoger sustituciones hechas por otros mods después de `init()`.
- Los controles vanilla/modded usados como backend quedan ocultos, sin hitboxes visibles debajo de Jobs; sólo se renderizan los widgets Jobs.
- No existe `CompatGraficos`, no se consulta `ConfigScreenFactory`, no hay reflection y Jobs no importa clases de Embeddium/Sodium.
- Si Embeddium, Oculus u otro mod cambia la ruta de Gráficos de forma natural, Jobs hereda esa ruta. Si no la cambia nadie, se conserva el comportamiento vanilla.
- La pantalla gráfica resultante continúa completamente fuera de chrome, transiciones, hover/click y recolocación Jobs.

## 0.41.0 · Runtime, audio y continuidad

Se mantienen todas las mejoras de 0.41.0:

- FX puntuales Jobs rastreados y con hard-stop al cerrar visita o entrar a gameplay;
- sin fallback a `minecraft:ambient.cave`;
- cierre de sesión idempotente;
- música vanilla sin `stopPlaying()` por tick;
- F5/Actualizar conserva servidor seleccionado y scroll;
- resize/maximizar/cambio de GUI Scale conserva selección y scroll de Multiplayer;
- `servers.dat` sólo se guarda si realmente cambió;
- setters de Config omiten valores idénticos;
- hover de controles vanilla preservados usa caché;
- diagnóstico interno y verificadores de runtime ampliados.

## Audio

Catálogo musical empaquetado:

1. **Absurdism** — `musica.tema`.
2. **REQUIEM** — `musica.requiem` — crédito `Emmy Z - Forsaken OST`.
3. **Upon the Hill V2** — `musica.upon_hill` — crédito `ft. @iCosmicCoffee`.

La música pertenece a `SesionMenu`, no a una Screen. No existe fallback a `minecraft:music.menu`. Entrar a mundo/servidor aplica hard-stop a música, camas y FX puntuales. `M` controla mute Jobs y `N` solicita siguiente pista sólo en Aleatoria.

## Interfaz y gameplay

- **Gráficos usa la misma ruta que usaría el `OptionsScreen` real del modpack.** Jobs no decide qué proveedor abrir.
- chat, inventario, contenedores y UI normal de gameplay no reciben piel, banda, transición ni sustitución global de clicks Jobs;
- mientras `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs;
- Pausa/Config Jobs pueden mantener tema y feedback breve sin reactivar música/ambiente;
- el main no muestra la antigua barra visible de atajos;
- ningún control visible debe quedar cubierto por una hitbox invisible vanilla.

## Multiplayer

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

- `Ghoul Outbreak` y duplicados legacy no reaparecen.
- ESC y Cancelar vuelven al padre Jobs con una sola acción.
- Cancelar conexión/error pre-login vuelve a Multiplayer Jobs.
- salir/kick/pérdida de conexión desde servidor remoto vuelve a Multiplayer Jobs; mundo local vuelve al main Jobs.
- F5/Actualizar reconstruye Jobs directamente y conserva selección por IP **más scroll de lista**.
- resize/maximizar/cambio de escala GUI conserva también selección y scroll.
- abrir Multiplayer no reescribe `servers.dat` si el servidor oficial ya está correcto.

## Fondos

- **10–17:** PNG históricos totalmente estáticos.
- **18–31:** JPG directos 1920×1080 con respiración de cámara sólo sutil/no destructiva; Movimiento reducido, Bajo consumo o escena quieta la desactivan.

## Build y entrega

GitHub Actions ejecuta política de versión, fondos, verificador general, UI/música, continuidad Multiplayer/docs, optimización, créditos/reload, identidad musical/hard-stop, runtime 0.41 y el contrato de navegación gráfica natural antes del build Forge real con Java 17. `dev-latest` sólo se publica desde `main` verde.

Instancia de prueba:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro vigente.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y límites de CI.
- [`CHANGELOG.md`](CHANGELOG.md): evolución reciente.
- [`docs/README.md`](docs/README.md): índice vigente/histórico.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): aceptación en `test-1`.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): fronteras con vanilla/Forge/mods.
- [`docs/musica.md`](docs/musica.md): catálogo y lifecycle de audio.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): publicación e instalación.
- [`docs/AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md`](docs/AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md): detalle técnico de runtime 0.41.
