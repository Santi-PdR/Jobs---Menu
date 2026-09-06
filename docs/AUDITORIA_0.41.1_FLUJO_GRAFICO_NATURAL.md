# Auditoría 0.41.1 — Flujo gráfico natural

## Motivo

0.41.0 corrigió que Jobs forzara siempre `VideoSettingsScreen`, pero lo hizo abriendo Embeddium mediante su `ConfigScreenFactory`. Esa solución seguía siendo demasiado específica: al saltarse el botón real de `OptionsScreen`, otros mods podían perder modificaciones, opciones o rutas que agregaban al flujo gráfico normal.

## Regla nueva

Jobs no decide qué pantalla gráfica abrir. El proveedor lo decide el mismo flujo que usaría Minecraft con el modpack cargado.

## Implementación

`PantallaOpcionesJobs` hereda de `OptionsScreen` y ejecuta `super.init()` antes de construir sus controles. De ese modo:

1. Minecraft crea el árbol real de opciones.
2. Mixins y hooks de mods modifican esa misma instancia.
3. Jobs localiza el `AbstractButton` de `options.video` y conserva el objeto real.
4. Los widgets externos se dejan invisibles para evitar superposición/hitboxes visuales, pero su callback permanece intacto.
5. El botón Gráficos de Jobs ejecuta `botonVideoNatural.onPress()`.

La captura se repite en el primer render, después del ciclo de inicialización de Forge, y de nuevo justo antes del click. Esto recoge reemplazos tardíos sin escanear la jerarquía gráfica durante todos los frames normales.

## Render

`OptionsScreen.render()` dibuja su propio fondo y título. Jobs no lo invoca. El render de `PantallaOpcionesJobs` dibuja el chrome Jobs y recorre únicamente widgets cuyo paquete pertenece a Jobs.

Así se reutiliza la **lógica** de OptionsScreen sin recuperar su capa visual vanilla.

## Eliminado

Se elimina `CompatGraficos` y con él:

- lookup directo de `embeddium`;
- `ConfigScreenHandler.ConfigScreenFactory` para navegación;
- fallback construido manualmente a `VideoSettingsScreen`;
- métricas de proveedor gráfico específicas de Embeddium.

No se introduce reflection ni dependencia a clases internas de mods gráficos.

## Aislamiento externo

`EscuchaCliente.esVideoIntocable()` continúa excluyendo `VideoSettingsScreen` y paquetes conocidos de Sodium/Embeddium/Iris. La pantalla final que abra el flujo natural no recibe chrome, transición ni sustitución de clicks Jobs.

## Verificación

`tools/verificar_graficos_041.py` exige:

- `PantallaOpcionesJobs extends OptionsScreen`;
- `super.init()`;
- conservación de `AbstractButton botonVideoNatural`;
- delegación mediante `onPress()`;
- ausencia de `CompatGraficos`, `ConfigScreenHandler`, reflection y `new VideoSettingsScreen`;
- aislamiento de pantallas gráficas externas.

`tools/verificar_ui_musica.py` se actualiza al mismo contrato y mantiene las comprobaciones de UI, gameplay, Multiplayer y audio.

## Prueba manual crítica

En `test-1`, comparar el resultado de Gráficos desde un flujo OptionsScreen normal con Gráficos desde Jobs. Deben aparecer la misma pantalla y las mismas opciones añadidas por el modpack. También comprobar regreso a Opciones Jobs, ausencia de título/fondo vanilla superpuesto y ausencia de hitboxes invisibles.
