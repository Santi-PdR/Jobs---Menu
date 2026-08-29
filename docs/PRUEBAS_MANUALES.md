# Matriz manual de validación — Jobs Menu 1.0.1

Registrar para cada caso: resultado, captura si es visual, dispositivo de audio,
resolución, GUI Scale, mods activos y extracto de `latest.log` si falla.

## Preparación

- [ ] Iniciar Forge 1.20.1 con Java 17 y solo Jobs Menu.
- [ ] Repetir la pasada de compatibilidad con Embeddium, Oculus y los mods de UI
      realmente usados en el pack.
- [ ] Confirmar que solo existe un `jobsmenu-*.jar` en `mods`.
- [ ] Vaciar o documentar el contenido de `jobsmenu-musica` antes de cada prueba
      de pista personalizada.
- [ ] Conservar `latest.log` y comprobar ausencia de excepciones de `jobsmenu`.

## Navegación y ciclo de pantallas

- [ ] Abrir Jobs Menu desde el arranque y cerrarlo de forma normal.
- [ ] Abrir y volver desde Multijugador, Lista de servidores, Mods y Opciones.
- [ ] Desde Opciones recorrer Sonido, Video, Controles, Teclas, Accesibilidad,
      Idioma y Resource Packs.
- [ ] Recorrer Un jugador, Seleccionar mundo y todos los diálogos de confirmación
      alcanzables sin crear ni borrar datos de prueba importantes.
- [ ] Abrir la pausa real dentro de un mundo y volver al juego con Escape.
- [ ] Confirmar que la superposición de pausa sin menú conserva la UI vanilla.
- [ ] Entrar en un mundo desde el menú y salir de nuevo al título.
- [ ] Cambiar rápidamente entre pantallas durante al menos 30 segundos.
- [ ] Cerrar una pantalla durante su transición y revisar el log.
- [ ] Probar doble confirmación de salida y la caducidad de 3,5 segundos.

## Botones, foco e hitboxes

- [ ] Probar los cuatro renglones con ratón: centro, cuatro bordes y cuatro esquinas.
- [ ] Pulsar un píxel dentro y un píxel fuera de cada borde; solo el interior
      debe activar hover y clic.
- [ ] Confirmar que no hay solapamiento con nota, cabecera, crédito ni contador.
- [ ] Recorrer todos los controles con Tab y Shift+Tab.
- [ ] Activar cada control enfocado con Enter y Espacio cuando corresponda.
- [ ] Probar Escape en cada pantalla y verificar el padre correcto.
- [ ] Probar flechas en selectores y sliders enfocados.
- [ ] Probar scroll de rueda y arrastre de la barra en Ajustes del aviso.
- [ ] Escuchar hover, clic, confirmar, volver y error; no debe haber ráfagas.

## Sliders y configuración

- [ ] Probar volumen de música y ambiente en 0, 1, 50, 99 y 100 %.
- [ ] Hacer clic directo en extremos y centro de cada pista.
- [ ] Arrastrar el knob lentamente y con movimientos bruscos fuera de la pista.
- [ ] Confirmar que knob, texto y valor real coinciden y quedan en 0–100.
- [ ] Cambiar el valor con teclado y verificar pasos, narración y texto.
- [ ] Probar el selector de nivel fijo en 0 y 9 y sus límites.
- [ ] Desactivar rotación, recorrer los diez niveles fijos y reiniciar el juego.
- [ ] Reiniciar tras cambiar cada opción y confirmar persistencia en el TOML.
- [ ] Probar TOML con entero fuera de rango y comprobar corrección de Forge.
- [ ] Probar TOML malformado sobre una copia de la instancia y observar la
      recuperación o diagnóstico de Forge sin atribuirlo al renderer.

## Resolución, escala y accesibilidad

- [ ] GUI Scale 1 a 720p, 1080p, 1440p y 4K.
- [ ] GUI Scale 2 a 720p, 1080p, 1440p y 4K.
- [ ] GUI Scale 3 a 720p, 1080p, 1440p y 4K.
- [ ] GUI Scale 4 a 720p, 1080p, 1440p y 4K.
- [ ] GUI Scale Auto en las cuatro resoluciones.
- [ ] Relaciones 4:3, 16:9, 16:10 y ultrawide.
- [ ] Ventana estrecha y ventana baja hasta activar el modo compacto.
- [ ] Alternar fullscreen y ventana repetidamente.
- [ ] Cambiar resolución mientras el menú está abierto.
- [ ] Hacer Alt+Tab, minimizar, recuperar foco y mantener teclas durante el cambio.
- [ ] Confirmar texto sin clipping en español e inglés.
- [ ] Confirmar tooltips dentro de pantalla y narración comprensible.
- [ ] Probar movimiento reducido, destellos reducidos e interfaz mínima.

## Música REQUIEM

- [ ] Music vanilla 0 % y Master 100 %: REQUIEM debe seguir sonando.
- [ ] Music vanilla 100 %: no debe superponerse música vanilla durante la visita.
- [ ] Master 0 %: todo sonido del mod debe quedar silenciado por el motor.
- [ ] Master 100 %: respetar los sliders propios sin clipping audible.
- [ ] Cambiar Master mientras REQUIEM está sonando.
- [ ] Cambiar el slider propio mientras REQUIEM está sonando.
- [ ] Desactivar música, esperar el fade y volver a activarla.
- [ ] Abrir y cerrar Jobs Menu varias veces; nunca deben coexistir dos copias.
- [ ] Pasar por Options, Sound, Video, Controls, Mods, Resource Packs,
      Singleplayer, Multiplayer y Select World sin reiniciar la pista.
- [ ] Entrar a un mundo local y confirmar salida; volver al título y confirmar
      que REQUIEM reaparece una sola vez.
- [ ] Repetir entrando en un servidor dedicado y desconectando al título; la
      música debe volver sin duplicarse ni conservar un canal fantasma.
- [ ] Repetir mundo → título → mundo → título cinco veces seguidas y revisar OpenAL/log.
- [ ] Hacer Alt+Tab, minimizar y perder foco durante la pista.
- [ ] Ejecutar F3+T durante la pista y revisar recuperación sin duplicados.
- [ ] Activar y desactivar un resource pack durante la pista.
- [ ] Dejar terminar REQUIEM; medir 40 ticks aproximados de silencio y el loop.
- [ ] Cambiar de pantalla y de nivel exactamente al terminar la pista.
- [ ] Probar OGG ausente, archivo renombrado que no es OGG y OGG corrupto.
- [ ] Probar OGG Vorbis válido personalizado, sustituirlo y retirarlo.
- [ ] Cerrar el juego mientras suena y revisar el siguiente arranque.

## Ambiente, transiciones y presencia

- [ ] Confirmar una sola cama BASE, CARÁCTER y ACTIVIDAD por nivel.
- [ ] Cambiar ambiente 0–100 % mientras suena.
- [ ] Desactivar/reanudar ambiente y cambiar de nivel inmediatamente.
- [ ] Escuchar que ningún sonido del nivel anterior queda después del apagón.
- [ ] Observar y escuchar la transición completa entre niveles.
- [ ] Confirmar ducking durante transición y presencia.
- [ ] Esperar apariciones de presencia sin forzar frecuencia de producción.
- [ ] Confirmar que movimiento reducido elimina presencia y partículas.
- [ ] Dejar una sesión larga y registrar repetición, silencios y fatiga sonora.

## Diez backgrounds

- [ ] Nivel 0, Administración: mampara, archivo, banda institucional y techo roto.
- [ ] Nivel 1, Nave: dársenas, contenedores, vías diagonales y puente grúa.
- [ ] Nivel 2, Servicio: caldera circular, colectores, pasarela y válvula.
- [ ] Nivel 3, Natatorio: vista alta, vaso diagonal, torre, graderío y reflejos.
- [ ] Nivel 4, Cripta: rotonda, capillas, óculo, relicario, piedra y velas.
- [ ] Nivel 5, Biblioteca: archivo circular, galerías, pozo y escalera helicoidal.
- [ ] Nivel 6, Invernadero: cúpula rota, árbol, raíces, vidrio y condensación.
- [ ] Nivel 7, Catacumbas: rellanos descendentes, nichos y un único farol.
- [ ] Nivel 8, Cisterna: perspectiva cenital, anillos, escalera y agua profunda.
- [ ] Nivel 9, Trono: óculo, abismo, puentes rotos y estrado suspendido.
- [ ] En cada nivel distinguir foreground, midground y background sin el nombre.
- [ ] En cada nivel revisar materiales, oscuridad, fuente principal y rebotes.
- [ ] Esperar o registrar los eventos raros propios sin aumentar su frecuencia en
      una compilación destinada a jugadores.

## Rendimiento y cierre

- [ ] Medir FPS y frametime en menú quieto, transición, presencia y evento raro.
- [ ] Repetir con GPU integrada, Embeddium y Oculus si están disponibles.
- [ ] Observar memoria durante 20 minutos de cambios de pantalla y resolución.
- [ ] Revisar `latest.log` en busca de recursos, OpenAL, reload, NPE y listeners.
- [ ] Confirmar ausencia de sonidos después de salir a mundo o cerrar Minecraft.
