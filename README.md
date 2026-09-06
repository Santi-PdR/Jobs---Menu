# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menús con una interfaz administrativa/industrial propia, audio de sesión y 32 fondos/niveles, conservando la lógica real de Minecraft/Forge/mods cuando intervenir perjudicaría compatibilidad.

| Campo | Valor |
|---|---|
| Versión | **0.46.0** |
| Artefacto | **`jobsmenu-0.46.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0–31)** |

## 0.46.0 · lifecycle seguro de Idioma y buscador robusto

0.46.0 endurece dos flujos que todavía podían quedar en estados inconsistentes sin tocar el contrato de Gráficos 0.44.

- **Idioma + Force Unicode Font forman una única transacción.** Ambos cambios quedan pendientes hasta `Aplicar y cerrar` y disparan una sola recarga de recursos.
- Si `reloadResourcePacks()` falla, Jobs restaura **idioma y Unicode juntos**, guarda el estado anterior y deja la pantalla abierta para reintentar.
- Un callback tardío de recarga sólo vuelve al padre si `PantallaIdiomaJobs` sigue siendo la pantalla actual; no puede secuestrar una navegación posterior.
- Idioma añade cierre idempotente y mantiene filtro, foco, scroll y selección pendiente durante resize.
- **El buscador de Ajustes deja de simular una tecla numérica** sobre su pantalla padre. La categoría se abre mediante una ruta explícita y validada.
- El buscador añade cierre protegido y cachea título, detalle, categoría y contador de resultados para reducir traducciones/formato repetido durante `render()`.
- Nuevo `tools/verificar_lifecycle_046.py`; CI lo ejecuta junto a todos los contratos 0.35–0.45 antes del build Forge.

## 0.45.0 heredado

- `Ctrl+F` abre búsqueda transversal de Ajustes por nombre, descripción y categoría.
- Filtro/foco/scroll sobreviven a resize en Ajustes, Idioma, Mundos y Mods.
- Config recuerda la última categoría de la sesión y muestra `CUSTOM` si ningún preset coincide exactamente.
- Apariencia, Controles y Config usan cierre idempotente.
- Resource Packs sólo puede devolver a Opciones Jobs mientras `PantallaPaquetesJobs` siga activa.
- Sonido cachea el `Field` reflectivo de `OptionsList` una sola vez por JVM.

## Gráficos — contrato 0.44 vigente

- `PantallaOpcionesJobs` es una `Screen` Jobs propia; no hereda de `OptionsScreen` ni reutiliza widgets ocultos.
- Con Embeddium, Gráficos abre la `ConfigScreenHandler.ConfigScreenFactory` registrada por el propio mod.
- Sin Embeddium, usa `VideoSettingsScreen` vanilla.
- La Screen gráfica abierta queda **intocable**: sin chrome, transición, hover/click Jobs, wrapper ni recolocación.
- Jobs no enlaza clases internas de Embeddium/Sodium ni usa reflection para abrir Gráficos.
- **No existe botón MODPACK.**

## Compatibilidad de pantallas externas

Las pantallas cuyo código no pertenece a `net.minecraft.*`, `net.minecraftforge.*` o Jobs se consideran de terceros. Jobs no aplica sobre ellas skin, bandas, transiciones, hover/click reemplazado ni estilizado de listas. Un subflujo iniciado por una GUI externa sigue siendo externo hasta regresar a una Screen Jobs.

## Audio

Catálogo musical empaquetado:

1. **Absurdism** — `musica.tema`.
2. **REQUIEM** — `musica.requiem` — crédito `Emmy Z - Forsaken OST`.
3. **Upon the Hill V2** — `musica.upon_hill` — crédito `ft. @iCosmicCoffee`.

La música pertenece a `SesionMenu`, no a una Screen. Entrar a mundo/servidor aplica hard-stop a música, camas y FX puntuales. `M` controla mute Jobs y `N` solicita siguiente pista sólo en Aleatoria.

## Gameplay

- Chat, inventario, contenedores y UI normal de gameplay quedan fuera de Jobs.
- Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
- Pausa/Config Jobs pueden conservar tematización y feedback breve sin reactivar música/ambiente.
- El main no muestra la antigua barra visible de atajos.

## Configuración Jobs

- `Ctrl+F` abre búsqueda global de preferencias.
- Flechas izquierda/derecha y teclas 1–6 navegan categorías.
- La última categoría usada se conserva durante la sesión del cliente.
- Un preset sólo aparece activo si coinciden todos los valores que controla; cualquier desviación relevante muestra **CUSTOM**.
- Resize no borra filtro/foco/scroll relevante en Ajustes, Idioma, Mundos o Mods.

## Multiplayer

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

- `Ghoul Outbreak` y duplicados legacy no reaparecen.
- ESC/Cancelar, error pre-login y retorno tras servidor conservan el padre Jobs correcto.
- F5 y resize conservan selección online por IP y scroll sin reutilizar Entries viejas.
- `servers.dat` sólo se guarda cuando la normalización realmente cambia datos.

## Fondos

- **10–17:** PNG históricos totalmente estáticos.
- **18–31:** JPG directos 1920×1080 con respiración de cámara mínima y desactivable por Movimiento reducido/Bajo consumo/escena quieta.

## Build y entrega

GitHub Actions ejecuta verificadores de versión/tag, fondos, UI/música, continuidad, optimización, créditos/reload, identidad musical, runtime 0.41, terceros 0.42, UX 0.43, Gráficos 0.44, calidad 0.45 y lifecycle 0.46; después compila Forge con Java 17. `dev-latest` sólo se publica desde `main` verde y su tag debe resolver al mismo SHA publicado.

Instancia de prueba:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro vigente.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y límites de CI.
- [`CHANGELOG.md`](CHANGELOG.md): historial de versiones.
- [`docs/README.md`](docs/README.md): índice vigente/histórico.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): aceptación en `test-1`.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): fronteras con vanilla/Forge/mods.
- [`docs/AUDITORIA_0.46.0_LIFECYCLE_IDIOMA_Y_BUSQUEDA.md`](docs/AUDITORIA_0.46.0_LIFECYCLE_IDIOMA_Y_BUSQUEDA.md): auditoría de esta entrega.
