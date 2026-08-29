# Compatibilidad con otros mods

> Anotado a partir de la lista de mods de la instancia `test-1` (Forge 47.x / 1.20.1).
> Es un analisis por conocimiento de los mods, **no** una prueba en vivo: el sandbox donde
> se genera este mod no tiene JDK ni puede cargar los otros `.jar`. Lo definitivo se
> confirma en el PC del owner.

## Resumen

De los ~110 mods de la instancia, casi ninguno toca los menus: son de juego, de
optimizacion, de render del mundo o cosmeticos in-game. Jobs vive en las pantallas
(titulo, pausa, opciones), asi que el solapamiento es minimo. Hay pocos puntos a vigilar.

## Pantalla de Opciones: regla de oro

El unico menu de vanilla que Jobs toca es `OptionsScreen`, y lo hace con el
**chequeo EXACTO de clase** (`getClass() == OptionsScreen.class`) en
`AjustesAviso`. Solo agrega un boton discreto arriba a la derecha y no
reemplaza la pantalla. Nunca se usa `instanceof OptionsScreen` (eso aceptaria
pantallas de otros mods que extienden la de vanilla y les ensuciaria su
esquina) — y `tools/verificar.py` falla si aparece. Los demas menus (titulo y
pausa) solo se sustituyen con la clase exacta de vanilla, y siempre hay
bandera de config para devolverlos. Ninguna otra pantalla del juego se toca.

## Puntos que importan

### AmbientSounds (audio) — atencion media

`AmbientSounds` es el unico mod de ambiente sonoro fuerte del pack. Segun su config puede
sonar tambien en el menu principal; si lo hace, se **solapa** con REQUIEM y con las camas
de ambiente de Jobs (que viajan por `SoundSource.MASTER`; ver `docs/musica.md`). No es un
fallo ni un crash: son dos capas de sonido a la vez. Si molesta, apagar el ambiente de
menu en uno de los dos. Todo el audio de Jobs se puede bajar o apagar desde el boton
**Ajustes del aviso** en Opciones, y con la tecla **M** en el aviso.

### FastQuit (pausa) — resuelto por diseno

`FastQuit` acelera el guardado al salir de un mundo, enganchando la secuencia de guardado
por debajo con mixins no intrusivos (no toca el boton de la pausa). La pausa propia de
Jobs (`PantallaEstancia`) **replica exactamente** la secuencia de salida de vanilla
verificada contra el codigo de 1.20.1 (`level.disconnect()` -> `clearLevel(...)` ->
titulo / multijugador / Realms), asi que FastQuit sigue operando igual. Si aun asi
apareciera un choque, la pausa propia se apaga con `pausa_propia = false` y vuelve la de
vanilla.

### Sin competencia por el menu de titulo

No hay ningun mod de menu custom (FancyMenu, CustomMainMenu, Prism, etc.) en la lista, asi
que la sustitucion de la pantalla de titulo de Jobs queda sola.

### Ctrl+S (atajo oculto) — sin conflicto

Ningun mod de la lista captura Ctrl+S en pantallas. `Controlling`, `MouseTweaks`,
`clientsort` y `zergatul.freecam` usan otras teclas y otros contextos.

## Higiene del pack (no es por Jobs, pero conviene saberlo)

- **`notenoughcrashes` esta duplicado**: `4.4.7` y `4.4.9` a la vez. Dos versiones del
  mismo mod pueden impedir el arranque. Conviene dejar solo la `4.4.9`.
- **`jobsmenu-0.6.5.jar` esta viejo**: la instancia trae la 0.6.5; la version al dia
  sale de `mod_version` en `gradle.properties`. El despliegue (ver chat / manual de
  despliegue) borra los `jobsmenu-*.jar` viejos antes de copiar, asi que se corrige solo.
- `cullleaves` **y** `CullLessLeaves` hacen casi lo mismo (culling de hojas). Redundante,
  no rompe nada.

## Lo que no se pudo verificar

Algunos nombres renombrados de la lista (`IMPR`, `paulbear`, `panasonic`, `turbopaja`,
"Archivos Secretos...", etc.) no se pudieron identificar con certeza. Por el nombre no
parecen mods de menu, pero sin abrirlos no hay garantia. Si alguno reemplaza el titulo o
la pausa, se notaria al instante en el juego, y se resuelve con `menu_propio = false` o
`pausa_propia = false`.
