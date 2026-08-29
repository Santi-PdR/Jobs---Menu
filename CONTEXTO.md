# Contexto técnico — Jobs Menu 1.0.1

Versión mantenida: **1.0.1**.

Este documento describe el código real. La historia vive en CHANGELOG.md; los
riesgos abiertos, en KNOWN_ISSUES.md.

## Ciclo de vida

- EscuchaCliente sustituye solo TitleScreen.class y la pausa vanilla real.
- SesionMenu representa toda la visita, incluso en Opciones, Sonido, Video,
  Controles, Mods, Recursos, Un jugador o Multijugador.
- Entrar a un mundo, cerrar pantallas o desactivar el menú inicia la salida.
- RecargaRecursosCliente limpia música y ambiente después de F3+T o packs.

## Pantallas y widgets

- PantallaNivel: título, documento adaptable y crédito.
- PantallaEstancia: pausa sobre el mundo, con papel de luz estable.
- PantallaAjustesAviso: OptionsSubScreen, OptionsList y OptionInstance.
- RenglonTablon: hover contenido exactamente dentro de su hitbox.
- NotaAviso: aviso enfocable, narrable y accionable.

En menos de 310 píxeles lógicos se activa modo compacto: conserva las cuatro
acciones, reduce márgenes y omite solo la nota decorativa. La salida requiere
una segunda confirmación dentro de 3,5 segundos.

## Escenas V2

`Nivel` conserva paleta, cámara de presencia y `Planta`. `Marco` sigue siendo
la referencia para presencia y perspectiva analítica. Las diez plantas
componen arquitectura independiente en vez de heredar un corredor común.

`Lienzo` es únicamente un taller de superficie: revoque, piedra, metal, madera,
vidrio, azulejo y agua con juntas, desgaste, vetas, condensación y reflejos. No
contiene edificios prefabricados. Foreground, midground, background, foco e
iluminación se definen dentro de cada recinto.

`PulsoLugar` reemplaza los eventos visuales genéricos. Cada nivel tiene una
ventana breve dentro de un periodo de 137–236 segundos y un incidente propio;
fuera de esa ventana retorna antes de crear geometría. Las motas permanentes
solo existen donde el material las justifica.

## Sonido

- 74 eventos y 74 OGG referenciados; sin recursos huérfanos ni ausentes.
- Tres camas por nivel: BASE, CARÁCTER y ACTIVIDAD.
- Eventos de lugar, transición, presencia y ocho gestos de interfaz.
- Pitch de camas entre 0.97 y 1.024; el antiguo 1.23 deformaba materiales.
- Hover limitado a un disparo cada 80 ms.
- Cerrar ambiente detiene las instancias; resize no reinicia temporizadores.

## Música

GestorMusica usa SoundSource.MASTER:

- Music vanilla no altera REQUIEM; Master sí.
- El slider propio se aplica en tiempo real.
- Vanilla Music se detiene solo durante la sesión Jobs.
- La pista continúa por pantallas hijas y sale al entrar a mundo.
- Una visita nueva después de un mundo o servidor invalida el canal Java
  superviviente y relanza la pista; una pantalla hija no la reinicia.
- Existe una única instancia, usa streaming y espera 40 ticks entre vueltas.

MusicaPropia valida OGG Vorbis, compara contenido con Files.mismatch, registra
fallos y desactiva el pack generado si el archivo desapareció.

## Compatibilidad

- Sin mixins, shaders ni APIs externas.
- Sin tematización global de pantallas vanilla.
- Sin sustituir subclases de otros mods.
- Sin tocar servidor, red, datos de mundo, inventarios o slots.

La estética se extiende a las pantallas propias. El resto conserva widgets
vanilla para proteger mods de UI y accesibilidad.

## Herramientas

- tools/verificar.py: versiones, TOML, lang, recursos y OGG.
- tools/vista_previa.py: hoja conceptual de diez siluetas; requiere Pillow.
- tools/sonidos.py: generación reproducible de audio sintetizado.
- Gradle Wrapper versionado para el build.

Un build correcto no sustituye las pruebas dentro de Minecraft.
