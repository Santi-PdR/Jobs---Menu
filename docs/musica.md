# Música del menú

## Estado en 0.10.0

El mod incluye una pista original en
`src/main/resources/assets/jobsmenu/sounds/musica/defecto.ogg`. La referencia
**REQUIEM — Forsaken OST**, de Emmy Z, no está dentro del JAR por defecto: una
obra de terceros sólo puede redistribuirse con autorización escrita para esa
distribución.

La validación estática confirma el recurso y su registro. La reproducción real,
la mezcla y el ciclo de vida de `SoundManager` todavía deben comprobarse dentro
de una instancia Forge con Java 17; compilar no certifica el audio en vivo.

## Contrato de audio

- `GestorMusica` mantiene una única instancia del tema y evita duplicados al
  abrir, redimensionar o cambiar de pantalla.
- **Vigilancia de instancia fantasma (Evolución 6).** Si una segunda instancia
  quedara congelada (p. ej. por una recarga de recursos), el gestor la detecta
  y la invalida. El vigía se desarma cuando el cliente no está tickeando
  (juego pausado o sin foco) para no disparar falsas alarmas: al reanudar, el
  primer tick sólo rearma la vigilancia. `reintentoParaDiagnostico()` alimenta
  el volcado oculto de `DiagnosticoOculto` (Ctrl+D, no documentado en la UI).
- El tema usa `SoundSource.MASTER`. Lo gobiernan el deslizador **Maestro** de
  Minecraft y el volumen de música del mod; no depende del deslizador **Música**
  de vanilla.
- El **volumen maestro del aviso** también puede silenciar el audio del mod. La
  tecla `M` conserva el último valor y alterna entre ese valor y silencio.
- Al abrir el aviso, la música de vanilla se detiene para que no compita con el
  tema del mod. Al salir, el ciclo de vida del gestor se cierra sin dejar
  instancias huérfanas.
- Las camas ambientales siguen usando su canal ambiental y sus propios controles;
  no deben confundirse con el contrato de la música. Desde la Evolución 6, el
  ambiente pertenece a la **visita** del menú: `SesionMenu` + `mantenerCamas()`
  en el tick del cliente mantienen vivas las camas del nivel actual aunque se
  abran Opciones o Mods, y las detienen al entrar a un mundo o salir del menú.
- Una pista de terceros sólo lleva crédito cuando la autoría y el permiso para
  esa distribución están confirmados. La opción `credito_musica` permite ocultar
  un crédito válido, pero no sustituye la autorización.

## Usar una pista local sin recompilar

El mod crea una carpeta en la **raíz de la instancia**, junto a `mods` y `saves`:

```text
jobsmenu-musica/
├── LEEME.txt
└── cualquier-nombre.ogg
```

Coloca allí una pista **OGG Vorbis mono**. No hay que renombrarla ni activar un
paquete desde Opciones. En el arranque, el mod:

1. crea la carpeta y sus instrucciones si faltan;
2. detecta las pistas de audio y alterna entre ellas en cada arranque;
3. genera un paquete de recursos interno y lo activa;
4. recarga recursos sólo cuando la pista cambió.

Si no hay una pista local, se conserva la pista original del mod. Un MP3
renombrado a `.ogg`, una pista estéreo o un formato no compatible puede quedar
mudo; el registro debe indicar el problema. La pista local permanece en la
máquina del usuario y no se incorpora al JAR.

La ruta efectiva depende de la instancia. Para la instalación documentada en
este repositorio es:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\jobsmenu-musica\
```

## Sustituir la pista empaquetada durante el desarrollo

Para una pista propia o con licencia compatible con la distribución:

1. reemplaza `src/main/resources/assets/jobsmenu/sounds/musica/defecto.ogg`;
2. conserva el formato OGG Vorbis mono;
3. conserva en `sounds.json` el evento `musica.tema` con `"stream": false`;
4. ejecuta `.\gradlew.bat clean build --no-daemon` en PowerShell.

El crédito, si corresponde, se edita en `lang/*.json` mediante
`jobsmenu.credito.titulo` y `jobsmenu.credito.autor`. No se debe poner una
grabación de terceros en `music/` ni en los recursos sin permiso de
redistribución. El archivo de referencia de esa carpeta sólo documenta la
licencia y el uso local.

## Referencias

- Instrucciones que el mod copia junto a la carpeta local: [`music/LEEME.txt`](../music/LEEME.txt).
- Pruebas de audio y silencio: [`checklist-manual.md`](checklist-manual.md).
- Riesgos que aún requieren Minecraft real: [`KNOWN_ISSUES.md`](../KNOWN_ISSUES.md).
