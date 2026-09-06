# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menús con una interfaz administrativa/industrial propia, audio de sesión y 32 fondos/niveles, conservando la lógica real de Minecraft/Forge/mods cuando intervenir perjudicaría compatibilidad.

| Campo | Valor |
|---|---|
| Versión | **0.42.0** |
| Artefacto | **`jobsmenu-0.42.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0–31)** |

## 0.42.0 · Compatibilidad natural con el modpack

0.42.0 extiende la idea iniciada en 0.41.1: Jobs deja de conocer proveedores concretos y trata las pantallas de otros mods como propiedad exclusiva de esos mods.

- **Gráficos sigue el botón real de `OptionsScreen`.** Jobs no construye Embeddium, Sodium, Oculus, Iris ni `VideoSettingsScreen` por su cuenta.
- La delegación guarda también la **ranura original** del botón de vídeo. Si un mod reemplaza el control y además cambia su etiqueta, Jobs puede reconocer el sustituto por su posición/tamaño y conservar su callback natural.
- Opciones Jobs añade **MODPACK**, una salida deliberada al `OptionsScreen` completo y natural. Sirve para acceder a botones, categorías e inyecciones que otros mods añadan y que Jobs no conoce.
- Al entrar por MODPACK se marca un **subflujo externo**: si esa pantalla abre después una GUI de otro mod o incluso una Screen vanilla, Jobs no le agrega chrome, bandas, transiciones, hover, clicks ni redirecciones internas.
- Las pantallas cuyo código no pertenece a `net.minecraft.*`, `net.minecraftforge.*` o Jobs se consideran **pantallas de terceros** y quedan fuera de la intervención Jobs.
- `VideoSettingsScreen` vanilla sigue siendo intocable aunque pertenezca al paquete de Minecraft.
- Se eliminan las listas de paquetes específicos de Embeddium/Sodium/Iris del listener: la compatibilidad ya no depende del nombre de clase de un proveedor concreto.
- El trabajo de `ListasExpediente` se omite también en superficies externas, evitando scans/cachés innecesarios dentro de GUI ajenas.
- El pipeline publica primero el JAR, después mueve el tag Git `dev-latest` al `GITHUB_SHA` y al final limpia assets Jobs obsoletos. Así el tag no se adelanta a una publicación fallida.
- Nuevo `tools/verificar_compatibilidad_042.py`; el antiguo verificador gráfico específico de 0.41 se retira.

## Estado heredado 0.41

Se mantienen:

- FX puntuales Jobs rastreados y con hard-stop al cerrar visita o entrar a gameplay;
- sin fallback a `minecraft:ambient.cave` ni `minecraft:music.menu`;
- cierre de sesión idempotente;
- música vanilla sin `stopPlaying()` por tick;
- F5/Actualizar conserva servidor seleccionado y scroll;
- resize/maximizar/cambio de GUI Scale conserva selección y scroll de Multiplayer;
- `servers.dat` sólo se guarda si realmente cambió;
- setters de Config omiten valores idénticos;
- hover de controles vanilla preservados usa caché;
- flujo gráfico natural de `OptionsScreen` sin `CompatGraficos`.

## Audio

Catálogo musical empaquetado:

1. **Absurdism** — `musica.tema`.
2. **REQUIEM** — `musica.requiem` — crédito `Emmy Z - Forsaken OST`.
3. **Upon the Hill V2** — `musica.upon_hill` — crédito `ft. @iCosmicCoffee`.

La música pertenece a `SesionMenu`, no a una Screen. Entrar a mundo/servidor aplica hard-stop a música, camas y FX puntuales. `M` controla mute Jobs y `N` solicita siguiente pista sólo en Aleatoria.

## Interfaz y gameplay

- **Gráficos usa la misma ruta que usaría el `OptionsScreen` real del modpack.**
- **MODPACK** abre el Options completo y natural como garantía de acceso a configuraciones añadidas por terceros;
- pantallas de terceros y sus subflujos quedan completamente fuera de la intervención visual/input de Jobs;
- chat, inventario, contenedores y UI normal de gameplay tampoco reciben piel, banda, transición ni sustitución global de clicks Jobs;
- mientras `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs;
- Pausa/Config Jobs pueden mantener tema y feedback breve sin reactivar música/ambiente;
- el main no muestra la antigua barra visible de atajos;
- ningún control visible debe quedar cubierto por una hitbox invisible.

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

GitHub Actions ejecuta política de versión/tag, fondos, verificador general, UI/música, continuidad Multiplayer/docs, optimización, créditos/reload, identidad musical/hard-stop, runtime 0.41 y compatibilidad 0.42 antes del build Forge real con Java 17. `dev-latest` sólo se publica desde `main` verde. El orden final es **publicar JAR → mover tag → limpiar assets obsoletos** y el tag debe terminar apuntando al mismo commit publicado.

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
- [`docs/AUDITORIA_0.42.0_COMPATIBILIDAD_TERCEROS.md`](docs/AUDITORIA_0.42.0_COMPATIBILIDAD_TERCEROS.md): detalle de esta tanda.
