# Checklist manual de aceptación — 0.27.0

Este checklist se ejecuta dentro de una instancia Forge 1.20.1 real. CI certifica código, recursos y build; no certifica estética, input, audio perceptivo ni compatibilidad visual dentro de Minecraft.

## Preparación

- [ ] Java 17, Forge 47.x y Minecraft 1.20.1.
- [ ] Instancia `test-1` cerrada antes de sustituir el JAR.
- [ ] Sólo un `jobsmenu-0.27.0.jar` activo en `mods`.
- [ ] Guardar `latest.log` ante crash, pantalla vacía, textura morado/negro o audio huérfano.

## Layout

Probar al menos 854×480, 1280×720, 1920×1080, ventana estrecha y GUI Scale 2/3/4, en español, inglés y Español (Uruguay).

- [ ] no hay texto sobre botones ni controles fuera de pantalla;
- [ ] no hay hitboxes invisibles superpuestos;
- [ ] foco de teclado y hover se distinguen;
- [ ] no aparecen claves `jobsmenu.*` ni títulos vanilla duplicados;
- [ ] barra inferior y metadatos no pisan el contenido principal.

## Main

- [ ] `TitleScreen` entra a Jobs.
- [ ] 1/2/3/4 y keypad 1/2/3/4 activan los cuatro renglones correspondientes.
- [ ] escribir números en un EditBox no activa atajos.
- [ ] F cambia de nivel cuando corresponde.
- [ ] M alterna silencio Jobs.
- [ ] N inicia un cambio real de pista sin apilar crossfades.
- [ ] barra inferior muestra 1-4/F/M/N/TAB/ENTER sin solaparse.
- [ ] no aparece `SHIFT CONTROL`.
- [ ] no aparece un `JOBS / LEVEL` técnico duplicado sobre el fondo.
- [ ] no aparece `%s` literal en la fecha.

## Pausa y navegación

- [ ] el mundo real permanece visible detrás de la pausa Jobs.
- [ ] 1 reanuda y 2 abre Condiciones; 3 no desconecta.
- [ ] ESC reanuda correctamente.
- [ ] Mundos vuelve al main Jobs con una sola pulsación de ESC.
- [ ] Multiplayer vuelve con una sola pulsación de ESC o Cancelar.
- [ ] salir de mundo/servidor/kick recupera Jobs sin quedarse en TitleScreen vanilla.

## Mods / Resource Packs / Idioma

- [ ] Mods muestra la lista completa de Forge, selección, búsqueda, Config y carpeta de mods.
- [ ] la lista real de Mods no queda vacía ni desplazada por la tematización.
- [ ] Resource Packs mantiene dos listas separadas y utilizables.
- [ ] seleccionar, ordenar, aplicar y abrir carpeta funcionan.
- [ ] Idioma conserva lista, búsqueda, Ctrl+F, portapapeles y Aplicar.

## Multiplayer

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez.
- [ ] `Ghoul Outbreak` no aparece.
- [ ] el servidor oficial no se puede editar ni borrar desde Jobs.
- [ ] Direct Connect/Add/Edit/Delete/Refresh funcionan para las demás entradas.

## Música y sesión

- [ ] en visitas distintas pueden iniciar Absurdism, REQUIEM o Upon the Hill V2.
- [ ] N cambia mediante crossfade a una pista distinta.
- [ ] pulsar N durante el crossfade no crea una tercera instancia.
- [ ] el crédito visible corresponde a la pista dominante.
- [ ] REQUIEM acredita `Emmy Z - Forsaken OST`.
- [ ] Upon the Hill V2 acredita `ft. @iCosmicCoffee`.
- [ ] Absurdism no inventa autor.
- [ ] M silencia/restaura sin cambiar accidentalmente de pista.
- [ ] Main → Options → Mods → Recursos → volver no reinicia ni duplica música.
- [ ] F3+T y Alt+Tab no crean instancias fantasma.
- [ ] entrar a gameplay corta música y ambiente Jobs desde el primer tick jugable.

## Fondos 10–17

- [ ] los ocho PNG cargan sin morado/negro.
- [ ] permanecen totalmente estáticos: sin zoom, paneo, parallax, flicker, niebla móvil o deformación.
- [ ] fades, apagones y transiciones globales funcionan sin alterar la imagen.

## Fondos 18–31

Revisar todos: 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 y 31.

- [ ] cada nivel muestra el JPG correcto según `docs/FONDOS_18_31.md`.
- [ ] todos cargan nítidos y sin textura morado/negro.
- [ ] el cover conserva una composición razonable en 16:9, ventana estrecha y GUI Scale 2/3/4.
- [ ] la respiración de cámara es lenta y muy sutil; no parece un zoom evidente.
- [ ] no aparecen objetos o efectos falsos sobre la imagen.
- [ ] Movimiento reducido congela completamente el fondo.
- [ ] Bajo consumo congela completamente el fondo.
- [ ] desactivar escena viva congela completamente el fondo.
- [ ] volver a opciones normales recupera el movimiento sin saltos bruscos.
- [ ] `nivel_fijo` permite seleccionar cualquier valor de 0 a 31.

## Nivel 1 · Depósito

- [ ] se mantiene el renderer `DepositoNuevo` de 0.26.0.
- [ ] `backups/nivel1/Nave_0.25.0.java.txt` sigue siendo sólo backup y no participa en runtime.

## Cierre

Si todo pasa:

- [ ] conservar SHA-256 del JAR probado;
- [ ] anotar resolución y GUI Scale;
- [ ] confirmar que `test-1\mods` contiene un único `jobsmenu-0.27.0.jar`;
- [ ] reportar cualquier defecto visual con captura y `latest.log` cuando afecte recursos/audio/crash.
