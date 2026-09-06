# Auditoría 0.44.0 — Gráficos y navegación de configuración

## Problema observado

La arquitectura 0.41.1/0.42 hacía que `PantallaOpcionesJobs` heredara de `OptionsScreen`, ejecutara `super.init()`, ocultara todos los widgets vanilla/modded y reutilizara el botón gráfico oculto como backend. Además añadía un botón `MODPACK` que abría un `OptionsScreen` completo con un permiso especial de un solo uso.

Ese diseño tenía dos problemas:

1. Jobs seguía entrando dentro del ciclo de vida de Options/Gráficos aunque la intención fuera respetar la GUI gráfica original.
2. El flujo MODPACK podía crear una cadena de padres/redirecciones donde salir de configuración volvía a abrir otra pantalla de opciones, produciendo un bucle perceptible.

## Cambio principal

`PantallaOpcionesJobs` vuelve a ser una `Screen` Jobs normal.

Se eliminan:

- `extends OptionsScreen`;
- `super.init()`;
- `botonVideoNatural`;
- detección por texto/ranura;
- `sincronizarControlesNaturales()`;
- ocultado de widgets externos;
- `MODPACK`;
- `abrirOpcionesModpack()`;
- `permitirOptionsNaturalUnaVez` y `optionsNaturalSolicitado`.

## Gráficos

El botón Gráficos no reconstruye ni tematiza la pantalla gráfica.

### Embeddium presente

`CompatGraficos` consulta el contenedor `embeddium` y pide a Forge:

`ConfigScreenHandler.ConfigScreenFactory`

La `screenFunction()` registrada por el propio mod produce la Screen que se abre. Jobs no llama métodos de render, no modifica widgets y no enlaza clases internas del proveedor.

### Embeddium ausente/factory no disponible

Se usa `VideoSettingsScreen` vanilla.

`EscuchaCliente.esSuperficieAjenaIntocable()` mantiene explícitamente `VideoSettingsScreen` fuera de Jobs. Las pantallas Embeddium quedan fuera además por la regla genérica de terceros.

## Navegación

La autorización administrativa se estrecha. Antes `SesionMenu.activa()` podía habilitar sustituciones de Options/Multiplayer/Mundos/Mods desde cualquier pantalla no externa durante una visita Jobs.

Ahora las sustituciones sólo nacen desde:

- `PantallaNivel`;
- `PantallaEstancia`;
- `PantallaOpcionesJobs`.

Esto evita que una navegación vanilla/Forge incidental sea capturada sólo porque la música/sesión Jobs continúe activa.

`PantallaOpcionesJobs.onClose()` añade `cerrando` idempotente. El retorno de resource packs comprueba también si Opciones Jobs ya es la Screen actual antes de hacer `setScreen(this)`.

## Verificación automática

`tools/verificar_graficos_044.py` exige:

- Opciones Jobs como `Screen`, no `OptionsScreen`;
- ausencia completa de MODPACK y del sistema de captura gráfica oculto;
- uso del extension point Forge de Embeddium;
- ausencia de reflection y clases internas del proveedor;
- fallback vanilla explícito;
- aislamiento de `VideoSettingsScreen`/terceros;
- ausencia del permiso MODPACK en `EscuchaCliente`;
- redirecciones administrativas acotadas.

`tools/verificar_ui_musica.py` y `tools/verificar_compatibilidad_042.py` se ajustan al mismo contrato.

## Pruebas manuales prioritarias

1. Embeddium: Opciones → Gráficos abre la GUI original sin decoración Jobs.
2. ESC/Done vuelve exactamente una vez a Opciones Jobs.
3. Repetir abrir/cerrar Gráficos muchas veces no altera la UI.
4. Confirmar que no existe MODPACK ni hitbox residual.
5. Abrir/cerrar Opciones Jobs muchas veces sin quedar atrapado.
6. Sin Embeddium, comprobar Video Settings vanilla intacto.
7. Probar resource packs y retorno a Opciones sin doble reconstrucción.
8. Probar una GUI externa que abra submenús vanilla y verificar que Jobs no la captura por `SesionMenu.activa()`.
