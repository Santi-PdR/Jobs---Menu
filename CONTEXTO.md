# Contexto técnico — Jobs Menu 1.0.0

Versión mantenida: **1.0.0**.

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

## Escenas

Nivel conserva paleta, cámara y Planta. Marco resuelve fuga y cuatro semiejes.
Cada Planta construye una arquitectura distinta. Arquitectura solo aporta
raster de trapecios, líneas, arcos, círculos, halos y reflejos.

Se eliminaron PrimerPlano y la geometría anterior. Los foregrounds viven en
cada recinto. EventosAmbientales habilita cinco segundos en uno de cada cuatro
ciclos de 97 segundos y dibuja un suceso distinto por nivel.

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
