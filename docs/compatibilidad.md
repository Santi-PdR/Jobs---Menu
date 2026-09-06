# Compatibilidad — Jobs Menu 0.46.0

## Perfil soportado

| Componente | Estado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente |
| Artefacto | `jobsmenu-0.46.0.jar` |

Jobs distingue entre pantallas que controla y pantallas ajenas que debe respetar completamente.

## Frontera de gameplay

Con mundo/servidor cargado:

- no se crea ni dibuja `TransicionInterfazJobs`;
- chat, inventario, contenedores y pantallas no Jobs quedan fuera de skin/banda/reemplazo global de click;
- música, camas ambientales y FX puntuales reciben hard-stop;
- Pausa/Config Jobs pueden mantener tema/feedback breve sin reactivar la sesión sonora.

## Gráficos — contrato 0.44 preservado

`PantallaOpcionesJobs` no hereda de `OptionsScreen`, no oculta widgets vanilla/modded y no usa controles invisibles como backend.

- con Embeddium, `CompatGraficos` consume su `ConfigScreenHandler.ConfigScreenFactory` registrada en Forge;
- sin Embeddium o si la factory no entrega una Screen válida, se abre `VideoSettingsScreen` vanilla;
- la Screen devuelta queda intacta: sin wrapper, chrome, overlay, transición, recolocación ni reemplazo de hover/click;
- `CompatGraficos` no usa reflection ni enlaza clases internas de Sodium/Embeddium;
- MODPACK y sus permisos/rutas antiguas siguen eliminados.

## Propiedad de pantallas de terceros

`EscuchaCliente.esPantallaTerceros()` trata como ajena cualquier `Screen` cuyo namespace no sea Jobs, `net.minecraft.*` o `net.minecraftforge.*`. Una GUI externa y sus subflujos no reciben piel, bandas, pulido, transición, hover/click Jobs ni gestión visual de listas. `VideoSettingsScreen` vanilla también es intocable de forma explícita.

Las conversiones administrativas sólo nacen desde padres Jobs concretos; `SesionMenu.activa()` no basta por sí sola.

## Configuración / buscador — 0.46

- `Ctrl+F` abre `PantallaBuscarAjustesJobs`.
- Filtra nombre, detalle y categoría y conserva filtro/foco/scroll durante resize.
- Enter/doble clic abre la categoría real mediante `PantallaAjustesAviso.abrirCategoriaDesdeBusqueda(int)`.
- Ya no existe la ruta frágil de volver al padre y sintetizar una tecla 1–6.
- El índice de categoría se valida antes de navegar.
- Título, detalle, categoría y contador de resultados se calculan al reconstruir el filtro, no en cada frame.
- Buscador y Config usan cierres protegidos.
- Config recuerda la última categoría de la sesión y muestra `CUSTOM` si ningún preset coincide.

## Idioma + Unicode — 0.46

`PantallaIdiomaJobs` administra idioma y Force Unicode Font como una sola transacción:

1. selección de idioma y Unicode quedan pendientes;
2. `Aplicar y cerrar` compara ambos con el estado efectivo;
3. si no hay cambios, cierra sin resource reload;
4. si cambia cualquiera, escribe ambos valores y ejecuta una sola `reloadResourcePacks()`;
5. éxito confirma ambos estados;
6. fallo restaura `Options.languageCode`, `LanguageManager` y `forceUnicodeFont`, persiste el rollback y mantiene la pantalla abierta.

El callback vuelve al padre sólo si `minecraft.screen == this`. Una finalización tardía no puede secuestrar otra navegación. Filtro, foco, scroll y selección pendiente sobreviven a resize.

Esto corrige especialmente el caso de cambiar **sólo Force Unicode Font**, que antes podía quedar guardado sin una recarga inmediata de recursos.

## Mundos / Mods / Resource Packs

- Mundos y Mods conservan filtro/foco relevante en resize y usan ESC por etapas.
- Ambos tienen cierre idempotente.
- Resource Packs sólo devuelve a Opciones Jobs si `PantallaPaquetesJobs` sigue siendo la Screen activa; un callback tardío no puede volver después de que el usuario haya salido.

## Apariencia / Controles / Sonido

- Apariencia y Controles protegen cierre y retorno al padre.
- Sonido reutiliza la lista vanilla real y resuelve/cachea el `Field` de `OptionsList` una sola vez por JVM.

## Perfiles

`PerfilesJobs.actual()` exige coincidencia exacta de todos los valores que cada preset controla. Una modificación relevante muestra `CUSTOM`. Opciones deliberadamente libres, como pista musical o nivel fijo, no invalidan un preset.

## Audio / resource reload / sesión

- `RastreadorAudioJobs` corta FX puntuales al cerrar visita.
- No existe fallback a `AMBIENT_CAVE` ni `MUSIC_MENU`.
- Resource reload de audio usa generaciones y vuelve al hilo cliente.
- `SesionMenu.cerrar()` es idempotente.
- `MusicManager.stopPlaying()` no se ejecuta por tick.

## Config / persistencia

Setters Jobs comprueban el valor actual antes de guardar. Los cambios reales mantienen throttle y `guardarPendiente()` al abandonar/cambiar pantalla.

## Multiplayer

- ESC/Cancelar usan padre Jobs directo y guard idempotente;
- F5/Actualizar reconstruye Jobs directamente;
- selección online se conserva por IP y scroll en F5/resize;
- `ServerList.save()` sólo se ejecuta si la normalización cambió datos;
- cancelar/error pre-login vuelve a Jobs;
- logout/kick remoto vuelve a Multiplayer Jobs.

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

## Pipeline

1. verificar contratos históricos + 0.46;
2. compilar Forge Java 17;
3. publicar JAR versionado desde `main`;
4. mover `dev-latest` al `$GITHUB_SHA` publicado;
5. eliminar assets Jobs obsoletos.

La limpieza de branches históricas existe como mantenimiento guardado y sólo se ejecuta con marcador explícito `[cleanup-branches]` después de una entrega verde.

## Fondos

- 10–17: PNG estrictamente estáticos;
- 18–31: JPG 1920×1080 con respiración opcional mínima;
- Movimiento reducido/Bajo consumo/escena quieta congelan 18–31.

## Compatibilidad manual prioritaria

Probar cambio sólo de Unicode, cambio combinado idioma+Unicode, fallo/reintento de reload, callback tardío, búsqueda global y navegación explícita, resize en Config/Idioma/Mundos/Mods, Resource Packs, Embeddium real, fallback vanilla, configuraciones de otros mods, Multiplayer con listas largas y GUI Scale extremos.

Regla general: **si tematizar exige duplicar o adivinar lógica ajena, se conserva la Screen original y Jobs no la toca**.
