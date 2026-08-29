# Auditoría técnica y de diseño — Jobs Menu 1.0.1

Fecha de corte inicial: 2026-08-28. Revisión V2: 2026-08-29. Base oficial:
`dc9ccca960ba3d797c6980c7cc3f34bccffd3747` de `main`.

La evolución 1.0.1 parte del estado 1.0.0 preservado en
`backup/codex-v100-d-before-backgrounds-v2-2026-08-29`. No modifica ni fusiona
`main`; sustituye la dirección visual 1.0.0 porque su estilo no cumplía el
resultado artístico esperado y corrige una regresión real observada al volver
de un mundo o servidor.

## Alcance revisado

Se leyó el árbol completo de Java, recursos, audio, idiomas, metadatos Forge,
Gradle, configuración, herramientas y documentación. La ruta de audio se siguió
desde `sounds.json` hasta `SoundManager`; la ruta visual, desde `Nivel` y `Marco`
hasta cada `Planta`, presencia, rotación y transición. También se compararon
todas las ramas locales y remotas antes de escoger cambios.

## Revisión de ramas

Las veinte referencias disponibles reducían a siete estados de código
distintos. No se hizo un merge completo de ninguna rama antigua:

| Familia | Decisión | Motivo |
|---|---|---|
| Base oficial `main` | Base obligatoria | Estado recuperable y coherente |
| Gradle Wrapper | Reimplementado | Aporta build reproducible sin arrastrar cambios viejos |
| Ciclo musical y confirmación de salida | Reimplementado | Ideas válidas, adaptadas al lifecycle actual |
| Overlays de tratamiento artístico | Rechazado | Maquillaba geometría antigua con capas comunes |
| Reescritura genérica de escenas | Rechazado | Convertía los lugares en variantes de una composición |
| Catálogo de trece escenas | Rechazado | Contradecía el alcance de diez y acumulaba documentación obsoleta |
| Implementaciones de cuatro niveles | Rechazado | Arquitectura y contenido superados |

El resultado es una integración curada sobre `main`, no un arrastre de historial.

## Hallazgos y correcciones

### Ciclo de vida

- La música estaba ligada demasiado estrechamente a una `Screen`; podía
  reiniciarse o cortarse al entrar en pantallas hijas. `SesionMenu` representa
  ahora la visita completa.
- F3+T y los packs reconstruyen el SoundEngine. Un listener de reload invalida
  música y ambiente para no conservar referencias del motor anterior.
- Se endurecieron comprobaciones de pantalla con clases vanilla exactas. Esto
  evita sustituir subclases de otros mods por accidente.
- La salida terminal requería protección contra clic accidental; ahora exige
  confirmación temporal y comunica el estado en el mismo control.

### Música

- REQUIEM usaba la categoría Music y por ello el slider vanilla formaba parte
  de su volumen. Ahora usa `MASTER`: Music 0 % no la silencia y Master 0 % sí.
- El volumen propio, fade, transición y ducking se multiplican en tiempo real.
- Solo existe una referencia activa; resize e `init()` no crean copias.
- El loop usa streaming y `delay=40`; se evita reiniciar por cambio de nivel.
- La música vanilla se detiene durante la visita, sin cambiar las opciones del
  jugador, y vuelve a quedar bajo control vanilla al salir.
- Se detectó una referencia fantasma al volver de mundo/servidor: el SoundEngine
  podía retirar el canal OpenAL sin ejecutar otro tick de la instancia Java.
  `SesionMenu.abrir()` distingue una visita realmente nueva, llama a
  `GestorMusica.nuevaVisita()` y fuerza una instancia limpia. Volver desde una
  pantalla hija no invalida el tema ni pierde su posición.
- El OGG personalizado se valida por contenedor y firma Vorbis, se copia solo
  cuando cambia y el pack obsoleto se deselecciona si la fuente deja de servir.
- Un archivo truncado después de una cabecera válida sigue siendo responsabilidad
  del decodificador; permanece como prueba de runtime.

### Audio ambiental

- Se verificaron 74 declaraciones, 74 referencias y 74 OGG sin archivos
  huérfanos, ausentes ni hashes duplicados en la base auditada.
- El pitch antiguo podía llegar a 1.23 y desmaterializaba las camas superiores;
  ahora el rango completo es 0.97–1.024.
- Cerrar el ambiente detiene sus instancias y un resize no reinicia agendas.
- El hover se limita a 80 ms y la mezcla conserva silencios entre sucesos.
- No se añadieron sonidos por cantidad: se mantuvo el inventario existente.

### UI/UX, sliders y accesibilidad

- La hoja antigua dependía de alturas fijas y anclajes separados: textos largos
  podían invadir renglones o pie. Ahora mide las líneas reales del idioma.
- Ancho, márgenes y espaciado se adaptan; por debajo de 310 px lógicos se activa
  un modo compacto que conserva las cuatro acciones.
- Los renglones dibujan hover, pressed, focus y decoración dentro de los mismos
  límites que usa la hitbox.
- Los ajustes usan `OptionsList`, `OptionInstance`, botones y sliders nativos:
  conservan Tab, Enter, flechas, narración, scroll y semántica de accesibilidad.
- Los sliders comparten valor normalizado, pista y knob de vanilla; sus setters
  limitan a 0–100 y guardan al cambiar.
- Se añadió selector nativo 0–9 para evitar editar el TOML al fijar escenario.
- Solo las pantallas propias reciben fondo temático. Las pantallas vanilla se
  mantienen para reducir incompatibilidades y duplicación de widgets.

### Render y rendimiento

- Las diez clases 1.0.0 fueron reemplazadas por composiciones nuevas; no se
  superpuso una capa de luces, niebla o partículas sobre la geometría anterior.
- `Arquitectura` fue eliminado. `Lienzo` no sabe construir habitaciones: solo
  pinta materiales procedurales en bandas anchas y primitivas raster acotadas.
- Revoque, piedra, metal, madera, vidrio, azulejo y agua tienen tratamientos
  diferentes; el agua usa masa estratificada y luces quebradas, no color plano.
- `EventosAmbientales` y las motas globales fueron eliminados. `PulsoLugar`
  retorna inmediatamente fuera de una ventana breve de 137–236 segundos.
- Polvo, vapor, condensación, hojas y gotas existen solo en las plantas donde
  aportan lectura material. El movimiento se apaga con movimiento reducido.
- Las primitivas evitan listas y objetos temporales por frame. Los bucles más
  densos avanzan en 2–5 píxeles y la cantidad de detalle escala con el tamaño.
- La deriva traslada arquitectura, primer plano, presencia y pulso en conjunto;
  la viñeta se dibuja después y cubre los mínimos bordes expuestos.

## Los diez fondos reconstruidos V2

1. **Administración:** vestíbulo brutalista visto desde la esquina de atención.
   Techo suspendido roto, banda institucional, archivo, reloj, mamparas y fila
   describen un lugar de trabajo abandonado. Un escritorio corta el foreground.
2. **Nave:** terminal subterránea de carga, no hangar axial. Cinco dársenas,
   vías diagonales, torres desiguales de contenedores y un puente grúa forman
   un patio de escala industrial con haces de luz muy localizados.
3. **Servicio:** cámara de calderas dominada por un recipiente circular remachado.
   Colectores con cotas y diámetros distintos, manómetros, válvula y pasarela
   sustituyen por completo el antiguo corredor técnico.
4. **Natatorio:** cámara alta desde una plataforma de salto. El vaso se abre en
   diagonal; torre, graderío, ventanales, calles y baranda separan aire, azulejo,
   estructura y agua estratificada.
5. **Cripta:** rotonda funeraria radial con tambor, nervios, óculo, siete capillas
   y relicario central. La piedra pesada y cuatro velas controlan el foco.
6. **Biblioteca:** archivo circular de tres galerías alrededor de un pozo de
   lectura, con escalera helicoidal, lámpara central y atril cercano. No existen
   dos paredes paralelas de estantes.
7. **Invernadero:** conservatorio de cúpula rota atravesado por un árbol maduro.
   El boquete, raíces, bancales absorbidos, hojas, hierro y condensación crean
   un volumen orgánico y asimétrico.
8. **Catacumbas:** excavación que desciende en tres rellanos de ejes distintos.
   Nichos irregulares, escalones y un farol producen profundidad subterránea sin
   recurrir a un túnel frontal.
9. **Cisterna:** pozo hidráulico observado desde arriba. Anillos concéntricos,
   contrafuertes, bajante, escalera y plataformas en U hacen que el agua negra
   se perciba muy lejos bajo el jugador.
10. **Trono:** cámara ceremonial fracturada bajo un óculo inmenso. Un abismo,
    puentes incompletos, estandartes y estrado suspendido aíslan el trono como
    único foco, con columnas rotas y cadenas en los bordes.

Cada lugar recibe un pulso raro propio: sombra de mampara, chispas de grúa,
descarga de presión, onda, humo de vela, hoja, gota sobre vidrio, sombra de
rellano, impacto en agua o polvo del óculo.

## Decisiones deliberadas de no cambio

- No se tematizaron globalmente Sonido, Video, Controles, Idioma, Mods o packs:
  sustituirlas rompería compatibilidad y duplicaría accesibilidad vanilla.
- No se añadieron shaders, mixins ni dependencias visuales.
- No se cambió el inventario de audio sin una escucha comparativa dentro del
  juego; frecuencia, ducking y pitch sí se ajustaron donde había evidencia.
- No se modifican configuraciones globales de volumen ni selección de Music.
- No se toca servidor, mundo, red, HUD ni datos de juego.

## Verificación

La auditoría estática comprueba versiones, TOML Forge, idiomas, claves, JSON,
recursos de sonido, cabeceras OGG y estructura Java. El build confirma
compilación y empaquetado. Ninguno de ambos equivale a haber probado el mod
dentro de Minecraft; la matriz manual separa expresamente esa fase.
