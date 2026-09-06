# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menús con una interfaz administrativa/industrial propia, audio de sesión y 32 fondos/niveles, conservando la lógica real de Minecraft/Forge/mods cuando intervenir perjudicaría compatibilidad.

| Campo | Valor |
|---|---|
| Versión | **0.44.0** |
| Artefacto | **`jobsmenu-0.44.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0–31)** |

## 0.44.0 · Gráficos intocable y navegación de configuración corregida

0.44.0 elimina la integración gráfica compleja introducida en 0.41.1/0.42 y corrige el bucle de navegación asociado al botón MODPACK.

- **MODPACK fue eliminado por completo.** No existe botón, permiso de un solo uso ni `OptionsScreen` natural alternativo.
- `PantallaOpcionesJobs` vuelve a ser una `Screen` Jobs normal. Ya no hereda de `OptionsScreen`, no ejecuta `super.init()`, no crea widgets vanilla/modded ocultos y no usa un botón gráfico invisible como backend.
- **Gráficos no es dibujado, envuelto, recolocado ni modificado por Jobs.** Con Embeddium instalado se pide a Forge la `ConfigScreenFactory` registrada por el propio Embeddium y se abre la Screen devuelta tal cual.
- Sin Embeddium, el fallback es `VideoSettingsScreen` vanilla, también declarado superficie intocable por el listener.
- Jobs no enlaza clases internas de Embeddium/Sodium ni usa reflection para abrir Gráficos.
- Se elimina el estado `permitirOptionsNaturalUnaVez` y la lógica que provocaba que al entrar por MODPACK el retorno pudiera quedar atrapado en Opciones.
- Las sustituciones administrativas ya no se habilitan sólo porque `SesionMenu` esté activa. Ahora sólo se aplican cuando la navegación nace desde padres Jobs concretos (`PantallaNivel`, `PantallaEstancia` u `PantallaOpcionesJobs`).
- Opciones Jobs tiene cierre idempotente para evitar dobles `setScreen()`.
- El callback de resource packs evita reconstruir la misma Screen si ya está activa.
- Nuevo `tools/verificar_graficos_044.py` protege este contrato.

## 0.43.0 · UX, perfiles exactos y navegación endurecida

- Los perfiles EQUILIBRADO, INMERSIVO, RENDIMIENTO, ACCESIBLE y MÍNIMO sólo aparecen activos cuando la configuración coincide realmente con el preset.
- Una edición manual relevante pasa a **CUSTOM**.
- En Mundos y Mods, `Ctrl+F` enfoca búsqueda; ESC limpia filtro, luego suelta foco y después vuelve.
- Mundos y Mods conservan cierre idempotente.
- Los subflujos externos quedan fuera de TitleScreen/pausa Jobs mientras siguen siendo externos.

## Compatibilidad de pantallas externas

Las pantallas cuyo código no pertenece a `net.minecraft.*`, `net.minecraftforge.*` o Jobs se consideran de terceros y quedan fuera de la intervención Jobs. Además, `VideoSettingsScreen` vanilla es intocable de forma explícita.

En esas superficies Jobs no aplica:

- skin o chrome;
- banda contextual;
- transiciones;
- hover/click reemplazado;
- recolocación o estilizado de listas.

El marcador de flujo externo se conserva mientras una GUI ajena abra subpantallas y se limpia al volver a una Screen Jobs.

## Audio

Catálogo musical empaquetado:

1. **Absurdism** — `musica.tema`.
2. **REQUIEM** — `musica.requiem` — crédito `Emmy Z - Forsaken OST`.
3. **Upon the Hill V2** — `musica.upon_hill` — crédito `ft. @iCosmicCoffee`.

La música pertenece a `SesionMenu`, no a una Screen. Entrar a mundo/servidor aplica hard-stop a música, camas y FX puntuales. `M` controla mute Jobs y `N` solicita siguiente pista sólo en Aleatoria.

## Interfaz y gameplay

- **Gráficos se abre original y queda fuera de la tematización Jobs.**
- No existe botón MODPACK.
- Pantallas de terceros y sus subflujos quedan fuera de la intervención visual/input Jobs.
- Chat, inventario, contenedores y UI normal de gameplay tampoco reciben piel, banda, transición ni sustitución global de clicks Jobs.
- Mientras `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
- Pausa/Config Jobs pueden mantener tema y feedback breve sin reactivar música/ambiente.
- El main no muestra la antigua barra visible de atajos.
- Ningún control visible debe quedar cubierto por una hitbox invisible.

## Multiplayer

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

- `Ghoul Outbreak` y duplicados legacy no reaparecen.
- ESC y Cancelar vuelven al padre Jobs con una sola acción.
- Cancelar conexión/error pre-login vuelve a Multiplayer Jobs.
- Salir/kick/pérdida de conexión desde servidor remoto vuelve a Multiplayer Jobs; mundo local vuelve al main Jobs.
- F5/Actualizar reconstruye Jobs directamente y conserva selección por IP **más scroll de lista**.
- Resize/maximizar/cambio de escala GUI conserva también selección y scroll.
- Abrir Multiplayer no reescribe `servers.dat` si el servidor oficial ya está correcto.

## Fondos

- **10–17:** PNG históricos totalmente estáticos.
- **18–31:** JPG directos 1920×1080 con respiración de cámara sólo sutil/no destructiva; Movimiento reducido, Bajo consumo o escena quieta la desactivan.

## Build y entrega

GitHub Actions ejecuta política de versión/tag, fondos, verificador general, UI/música, continuidad Multiplayer/docs, optimización, créditos/reload, identidad musical/hard-stop, runtime 0.41, aislamiento de terceros, UX 0.43 y Gráficos/Opciones 0.44 antes del build Forge real con Java 17. `dev-latest` sólo se publica desde `main` verde.

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
- [`docs/AUDITORIA_0.44.0_GRAFICOS_Y_NAVEGACION.md`](docs/AUDITORIA_0.44.0_GRAFICOS_Y_NAVEGACION.md): cambios y riesgos de esta tanda.
