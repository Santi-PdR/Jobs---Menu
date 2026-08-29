# Música del menú

## Comportamiento

REQUIEM usa un SoundEvent propio y una única instancia tickable en MASTER.

Volumen efectivo: Master × volumen_musica × mezcla × fade × ducking.

Music de vanilla no participa. Master continúa siendo la autoridad global.
Vanilla Music se detiene únicamente durante una sesión Jobs.

La pista:

- usa streaming;
- entra y sale con fade;
- continúa por Opciones, Sonido, Video, Controles, Mods, Recursos, Un jugador,
  Multijugador y Seleccionar mundo;
- deja 40 ticks entre vueltas;
- se invalida y recrea después de F3+T o packs;
- descarta el canal de la visita anterior al volver de un mundo o servidor;
- no se duplica durante resize, reload o cambios rápidos.

Minecraft puede vaciar canales OpenAL al cargar un mundo sin actualizar el
estado Java de la instancia tickable. `SesionMenu` distingue una visita nueva
de una pantalla hija: en la visita nueva invalida esa referencia fantasma y
permite relanzar la pista; al pasar por Opciones o Mods conserva la instancia.

## Sustituir la pista

1. Inicia el juego una vez.
2. Abre .minecraft/jobsmenu-musica/.
3. Coloca un único OGG Vorbis.
4. Reinicia Minecraft.

El mod crea y selecciona un pack interno, comprueba la cabecera y solo copia si
el contenido cambió. Para volver a la pista incluida, retira el archivo y
reinicia; el pack generado se deselecciona.

MP3, WAV, FLAC y M4A se detectan para registrar un aviso, pero no se convierten.

## Crédito

Pista incluida: **REQUIEM — Forsaken OST**, Emmy Z.

Si se usa una pista personalizada, las claves jobsmenu.credito.titulo y
jobsmenu.credito.autor deben representar su autoría real.
