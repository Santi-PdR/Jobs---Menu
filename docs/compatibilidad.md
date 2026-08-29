# Compatibilidad

Jobs Menu 1.0.1 es de cliente para Minecraft 1.20.1 y Forge 47.x.

## Integración

- No usa mixins.
- Solo sustituye clases vanilla exactas de título y pausa.
- Solo inserta ajustes en OptionsScreen.class.
- No sustituye subclases creadas por otros mods.
- No modifica slots, HUD, red ni datos de mundo.
- No registra controles públicos adicionales.

Esto favorece Embeddium, Oculus, mods de UI y resource packs. Un mod que
reemplace la pantalla mediante una subclase conserva prioridad.

## Matriz necesaria

- GUI Scale 1, 2, 3, 4 y Auto.
- 720p, 1080p, 1440p y 4K.
- 4:3, 16:9, 16:10 y ultrawide.
- Ventana pequeña y fullscreen.

F3+T y packs reconstruyen el SoundEngine; el listener descarta instancias
anteriores y las recrea de forma segura.

Si otro mod modifica las mismas clases exactas, el orden de eventos puede
decidir el resultado. Jobs Menu no fuerza prioridad global.
