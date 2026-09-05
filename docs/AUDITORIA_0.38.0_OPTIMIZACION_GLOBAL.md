# Auditoría 0.38.0 — Optimización global

Fecha: 2026-09-05

## Objetivo

Reducir trabajo redundante de CPU/GPU, asignaciones temporales y mutaciones repetidas de estado gráfico sin cambiar el alcance funcional de Jobs Menu ni debilitar sus contratos de compatibilidad.

Esta versión **no añade gameplay**. Video Settings sigue vanilla, chat/inventario/containers siguen fuera de Jobs y música/ambiente continúan con hard-stop al entrar a un mundo o servidor.

## 1. Listas y reflection

### Problema

`ListasExpediente` necesitaba encontrar `AbstractSelectionList` en Screens vanilla/Forge preservadas. La implementación anterior recorría la jerarquía de clases mediante reflection, ejecutaba `setAccessible()` y construía colecciones temporales cada vez que se dibujaba la scrollbar.

Además, varias Screens propias llamaban `renderarBarras()` dentro de su `render()` y `EscuchaCliente` volvía a hacerlo desde `ScreenEvent.Render.Post`, por lo que una misma barra podía recorrer/dibujar dos veces por frame.

### Corrección

- cache de fields compatibles por clase de Screen;
- cache únicamente de la Screen viva + sus listas resueltas;
- invalidación cuando `estilizar()` puede coincidir con un nuevo `init()`/resize;
- liberación explícita al recibir `ScreenEvent.Closing`;
- marco por `Render.Pre` para deduplicar `renderarBarras()` dentro del frame;
- eliminación de `IdentityHashMap` temporal por frame.

La lista vanilla/Forge sigue siendo la autoridad de selección, wheel, drag, scroll, contenido e hitboxes.

## 2. Hover de controles vanilla

El seguimiento global ya no conserva un booleano para todos los botones vistos. Usa un set débil con sólo los `AbstractButton` actualmente hover/focused. Entrar agrega y dispara `UI_PASAR`; salir elimina. Al cerrar la Screen se limpia el estado asociado.

Los widgets Jobs propios siguen excluidos porque ya gestionan su sonido internamente.

## 3. Texturas 10–31

### Problema

`PlantaImagen` llamaba `TextureManager#getTexture(...).setFilter(true, false)` en cada render aunque el filtro ya estuviera configurado.

### Corrección

Se guarda la identidad del `AbstractTexture` filtrado. `setFilter` sólo se ejecuta cuando Minecraft entrega un objeto distinto. Tras F3+T/resource reload la nueva instancia vuelve a recibir el filtro de forma automática.

No cambia el cover, los puntos de interés, el movimiento opcional 18–31 ni el contrato estático 10–17.

## 4. Avisos y calendario

`NotaAviso` ahora:

- captura una hora por render;
- reutiliza el `Component` mientras la clave del aviso no cambia;
- mantiene el cache existente de líneas partidas;
- resuelve las ventanas especiales de calendario como máximo una vez por minuto, precisión suficiente porque todas sus condiciones se expresan en fecha/minuto.

La duración configurada y el orden de los 20 avisos permanecen iguales.

## 5. Pulido global de UI

`PulidoInterfazJobs` hacía recorridos separados de `Screen.children()` para jerarquía y foco. 0.38.0 los fusiona en una pasada que:

- cuenta widgets visibles/activos;
- dibuja foco/hover;
- produce el indicador de jerarquía.

También captura una sola hora para entrada, foco y aviso, y reutiliza el `Component` de “cambio guardado”.

## 6. Multiplayer

Se mantienen las correcciones 0.36/0.37. La optimización sólo elimina trabajo de presentación repetido:

- título/subtítulo/servidor fijado se reutilizan;
- textos dependientes del ancho se preparan en `init()`;
- tooltips protegido/editar/eliminar son reutilizables;
- el tooltip sólo cambia cuando cambia el estado oficial/no oficial;
- el estado visual de botones sigue sincronizado con los botones vanilla reales.

F5 conserva selección por IP, reconstruye una Entry fresca y mantiene `UI_ALTERNAR` únicamente para el atajo de teclado.

## 7. Snapshot compartido de rotación

Renderer, chrome, música y varias capas ambientales pueden consultar `RotacionNiveles.capturar()` casi al mismo tiempo. El método ahora conserva el último `Estado` únicamente para el mismo valor de `System.currentTimeMillis()`.

Por tanto:

- varias consultas en el mismo milisegundo comparten record/cálculo;
- al siguiente milisegundo se recalcula;
- `adelantar()` invalida inmediatamente;
- no se introduce un tick cacheado ni retraso perceptible.

## 8. Bajo consumo

Antes Bajo consumo apagaba varias animaciones, pero algunas capas procedurales seguían usando casi el mismo número de draw calls del perfil normal.

0.38.0 reduce deliberadamente detalle cuando la opción está activa:

- vignette: paso 6 en lugar de 3;
- profundidad: 3 capas en lugar de 6;
- halos: 3 en lugar de 5;
- rebote de suelo: 4 bandas en lugar de 8;
- humedad: aproximadamente la mitad de líneas;
- grano/presencia/motas y movimiento mantienen sus exclusiones anteriores.

Con Bajo consumo desactivado se mantienen las cantidades del modo normal previo.

## 9. Piel vanilla

`PielVanillaJobs` calcula Alto contraste una vez por pasada y comparte ese valor con botones, sliders y EditBox. No cambia listeners, callbacks, foco, validación ni hitboxes.

## 10. Build reproducible

El task `jar` ahora usa:

- `preserveFileTimestamps = false`;
- `reproducibleFileOrder = true`.

Se retiró `Implementation-Timestamp` del manifest porque generaba una diferencia binaria basada únicamente en la hora de compilación.

Esto mejora reproducibilidad, aunque el SHA final sigue dependiendo de ForgeGradle/reobf y del toolchain exacto. El digest de `dev-latest` continúa siendo la autoridad de instalación.

## 11. CI

Se añade `tools/verificar_optimizacion.py`, que comprueba estáticamente los contratos principales de esta versión:

- cachés/deduplicación de listas;
- liberación en lifecycle;
- filtro por instancia de textura;
- cache de aviso/calendario;
- snapshot de rotación;
- reloj/recorrido unificado de UI;
- Alto contraste una vez por pasada;
- reducción de capas en Bajo consumo;
- cache de rótulos/tooltips de Multiplayer;
- flags de JAR reproducible y ausencia de timestamp variable.

El verificador no pretende medir FPS. El rendimiento perceptivo sigue siendo parte del checklist manual.

## 12. Contratos preservados

- Video Settings completamente vanilla.
- Chat, inventario y containers fuera de Jobs.
- Cero transiciones Jobs durante gameplay.
- Hard-stop de música y ambiente dentro de gameplay.
- Feedback corto Jobs permitido únicamente en superficies Jobs válidas.
- Retorno de servidor remoto a Multijugador Jobs; mundo local al main.
- ESC/Cancelar/F5 de Multiplayer según 0.36/0.37.
- Servidor oficial único `JobsDosh.exaroton.me:56477`.
- PNG 10–17 estáticos.
- JPG 18–31 sin reescritura ni movimiento destructivo.
- Tres pistas musicales y su selección actual.

## 13. Aceptación manual prioritaria

Después de desplegar `jobsmenu-0.38.0.jar` revisar especialmente:

1. listas largas y scrollbar en Mundos/Mods/Recursos/Idioma/Multiplayer;
2. Bajo consumo ON/OFF en escenas 0–9;
3. F3+T después de cargar fondos de imagen;
4. aviso rotativo y cambios de nivel/audio;
5. Multiplayer F5/ESC/Cancelar/LAN/ping/favicons;
6. Video Settings vanilla;
7. ausencia total de Jobs sobre chat/inventario/gameplay no permitido.
