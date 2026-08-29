# Historial de cambios

## 1.0.1 — 2026-08-29

### Música

- Corregido el caso en que REQUIEM no regresaba después de entrar en un mundo
  local o servidor y volver al título.
- Una visita nueva invalida la instancia Java cuyo canal OpenAL pudo haber sido
  retirado durante la carga; Opciones, Mods y demás pantallas hijas siguen
  conservando la reproducción actual.
- Se reinicia también el temporizador de reintento al comenzar una visita nueva,
  sin permitir dos instancias simultáneas.

### Dirección visual V2

- Eliminadas completamente las diez implementaciones visuales 1.0.0 y su
  lenguaje de pasillos, cerchas, naves axiales y rectángulos planos.
- Eliminado `Arquitectura`; `Lienzo` lo sustituye como taller de materiales sin
  composiciones prefabricadas: revoque, piedra, metal, madera, vidrio, azulejo
  y agua incorporan juntas, desgaste, vetas, humedad y reflejos.
- Los diez recintos reciben cámaras, masas y focos nuevos: vestíbulo brutalista,
  terminal de carga, cámara de calderas, natatorio desde plataforma, rotonda,
  archivo circular, conservatorio colapsado, descenso funerario, pozo hidráulico
  cenital y estrado suspendido.
- `PulsoLugar` reemplaza `EventosAmbientales`: ventanas raras de 137–236 segundos,
  retorno temprano fuera de evento y un gesto visual exclusivo por nivel.
- Eliminadas las partículas flotantes globales. Cada planta decide si su material
  justifica polvo, vapor, gotas, hojas o reflejos.
- La deriva reducida mueve la escena, foreground, presencia y evento como una
  sola cámara; la viñeta permanece fija y movimiento reducido la desactiva.
- Reconstruida la hoja conceptual `docs/vista_previa.png` para representar las
  diez siluetas V2.

### Proyecto

- Versión 1.0.1, documentación y matriz de regresión actualizadas.
- Nuevo backup previo y nueva rama de trabajo, sin modificar `main`.

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
