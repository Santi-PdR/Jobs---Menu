# Historial de cambios

## 1.0.0 — 2026-08-28

### Estabilidad y ciclo de vida

- La visita al menú ahora sobrevive a Opciones, Sonido, Video, Controles, Mods,
  Recursos, Un jugador, Multijugador y Seleccionar mundo sin reiniciar música.
- Entrar a un mundo, desactivar el menú o cerrar la visita libera sus estados.
- F3+T y las recargas de resource packs invalidan de forma explícita las
  instancias de música y ambiente del motor anterior.
- La sustitución se limita a las clases vanilla exactas de título, pausa y
  opciones para respetar pantallas aportadas por otros mods.
- La salida del juego exige una segunda confirmación durante una ventana breve.

### Interfaz y accesibilidad

- La hoja se mide a partir del texto real y se adapta a ventanas estrechas.
- El modo compacto mantiene las cuatro acciones dentro de pantalla y retira
  solo contenido decorativo.
- Regiones visuales y regiones pulsables de los renglones ahora coinciden.
- Se añadieron nivel fijo 0–9, movimiento reducido, destellos reducidos e
  interfaz mínima a la pantalla nativa de ajustes.
- La configuración conserva navegación, narración, Tab, Enter y sliders vanilla.
- El hover sonoro tiene un limitador de 80 ms para evitar ráfagas al mover el
  puntero entre bordes.

### Música y sonido

- REQUIEM pasó a `MASTER`: la controlan Master y el volumen propio, no Music.
- Se garantiza una sola instancia, streaming y 40 ticks de silencio entre
  vueltas completas.
- La música vanilla se detiene solo durante la visita Jobs.
- Los OGG personalizados se validan antes de entregarlos al motor de sonido.
- El pack generado se deselecciona si la fuente desaparece o deja de ser válida.
- El ambiente se detiene al cerrar y no reinicia temporizadores al redimensionar.
- El pitch de las camas ambientales queda entre 0.97 y 1.024.

### Dirección artística

- Se reconstruyeron desde cero los diez recintos con composiciones propias.
- Se eliminó la antigua capa de primer plano común y cada lugar define ahora
  su foreground, arquitectura, materiales, iluminación y profundidad.
- Los sucesos visuales raros son específicos del recinto y no se calculan fuera
  de su ventana activa.
- Las partículas genéricas se sustituyeron por densidades coherentes con cada
  material; agua, vapor y condensación no reciben polvo indiscriminado.

### Proyecto

- Versión 1.0.0 y Gradle Wrapper reproducible.
- Documentación, verificador estático y hoja de dirección visual alineados con
  el código actual.

