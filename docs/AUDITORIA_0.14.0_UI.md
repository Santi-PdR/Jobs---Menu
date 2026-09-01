# Auditoría de interfaz — Jobs Menu 0.14.0

## Objetivo

0.14.0 no busca sumar pantallas por cantidad. Busca eliminar señales de que el usuario está saltando constantemente entre una interfaz Jobs y Minecraft vanilla.

Criterios usados para aceptar un cambio:

1. identidad Jobs visible;
2. jerarquía clara;
3. controles coherentes entre pantallas;
4. lógica real de Minecraft preservada cuando es compleja;
5. compatibilidad con otros mods antes que una skin forzada;
6. accesibilidad y layout antes que decoración;
7. PNG 10–17 completamente estáticos.

## Diagnóstico previo

La familia 0.12/0.13 había mejorado mucho el marco general, pero todavía quedaban cuatro señales fuertes de UI vanilla:

- Config del mod era visualmente un `OptionsList` de Minecraft dentro de papel Jobs;
- el botón de Config competía con opciones Minecraft en vez de ser una función propia del mod;
- diálogos secundarios como Direct Connect/Add Server podían volver de golpe a botones grises vanilla;
- sliders/toggles propios todavía eran demasiado cercanos a controles rectangulares genéricos.

También había margen para mejorar profundidad del papel, jerarquía de secciones, scrollbar y transición entre expedientes.

## Cambios aplicados

### 1. Centro de control

`PantallaOpcionesJobs` diferencia dos dominios:

- **Jobs**: configuración del propio sistema, con acción principal de ancho completo;
- **Minecraft**: opciones de juego que siguen siendo útiles pero quedan subordinadas visualmente.

Esto evita que el usuario tenga que recordar dónde está escondido Config y hace que el mod se presente como sistema, no como complemento decorativo.

### 2. Config nativa de Jobs

`PantallaAjustesAviso` fue reescrita como `Screen` propia.

Se retiró de su superficie visible:

- `OptionsList`;
- `OptionInstance`;
- filas/controles con aspecto vanilla.

La nueva interfaz usa cinco categorías y widgets Jobs conectados directamente a `ConfigTurno`.

Resultado esperado: Config debe ser una de las pantallas más reconocibles del mod, no una pantalla vanilla tematizada parcialmente.

### 3. Jerarquía de widgets

`BotonExpediente` diferencia ahora:

- NORMAL;
- PRINCIPAL;
- JOBS;
- TERMINAL.

El tipo JOBS se reserva para acciones propias del sistema. Tiene más profundidad, marca lateral y presencia de foco, sin utilizar rojo.

`ToggleExpediente` separa visualmente la pregunta del estado. `SliderExpediente` incorpora una escala y un tirador mecánico de expediente.

### 4. Pantallas auxiliares vanilla

`PielVanillaJobs` se dibuja sobre controles vanilla de pantallas `net.minecraft.*` durante una sesión Jobs.

La decisión importante es **no sustituir su lógica**. Así pueden tematizarse botones/campos de conexión o confirmación conservando validaciones, listeners e hitboxes originales.

Las pantallas de terceros quedan excluidas para evitar incompatibilidades.

### 5. Chrome común

`ChromeExpediente` gana:

- profundidad en dos planos;
- pestaña y perforaciones de archivo;
- reglas de cabecera;
- rótulos de sección;
- truncado seguro;
- vignette estática;
- banda externa revisada.

El chrome debe ser reconocible incluso si el usuario cambia de subpantalla.

### 6. Scrollbar

La scrollbar visual mantiene el `AbstractSelectionList` real. Se añadieron canaleta, topes, marcas de recorrido y agarres, pero no se inventó un segundo modelo de scroll.

### 7. Transición

La transición entre expedientes usa papel/carpeta con sombra y fibras en vez de un simple wipe digital. Con movimiento reducido se degrada a fade.

## Cambios deliberadamente no hechos

- No se reimplementó Embeddium.
- No se reimplementaron protocolos de multijugador.
- No se añadieron partículas decorativas sobre todas las pantallas.
- No se añadieron flashes o glitch genérico.
- No se animaron los PNG 10–17.
- No se añadió rojo a acciones peligrosas; sigue reservado a Executores.
- No se duplicaron valores de configuración para facilitar la UI.

Estas decisiones reducen fragilidad y evitan que “más efectos” empeore la coherencia.

## Riesgos técnicos

### Render posterior sobre vanilla

`PielVanillaJobs` depende de que el control vanilla haya renderizado antes. Un resource pack o mod que cambie profundamente la secuencia visual puede necesitar una adaptación específica. Como la capa no toca lógica, el riesgo funcional es menor que reemplazar widgets.

### Reflection de listas

La scrollbar usa reflection defensiva sobre `AbstractSelectionList`. Si no puede recuperar los campos necesarios, debe fallar visualmente de forma segura y conservar una lista utilizable.

### GUI Scale

La Config nueva tiene más jerarquía visual. Debe probarse especialmente en 854×480 y GUI Scale altos para confirmar tabs, filas y footer.

## Matriz de aceptación manual

| Área | Aceptación |
|---|---|
| Options | Config Jobs visible de inmediato; sin solapes |
| Config | 5 categorías; todos los valores guardan; sin controles vanilla visibles |
| Botones | hover/foco/press coherentes y sin doble click sonoro |
| Toggles | estado legible y narración completa |
| Sliders | valor, escala, drag y teclado funcionales |
| Scrollbar | rueda/click/drag coinciden con posición visual |
| Direct Connect | lógica vanilla intacta, controles visualmente Jobs |
| Add Server | campos y botones usables; validación intacta |
| Terceros | no reciben skin invasiva |
| Accesibilidad | movimiento reducido simplifica animaciones |
| PNG 10–17 | completamente inmóviles una vez estable el Nivel |
| Pausa/retorno | navegación vuelve al padre correcto |
| Audio | sin duplicados al navegar/recargar |

## Resultado esperado

Después de 0.14.0, la percepción buscada es que Minecraft aporta el motor de opciones y compatibilidad, pero **Jobs controla la experiencia de uso**. El usuario debería notar menos “pantallas grises de Minecraft” y más continuidad de un mismo sistema administrativo, sin pagar por ello con controles rotos o incompatibilidad innecesaria.
