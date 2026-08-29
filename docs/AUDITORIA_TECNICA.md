# Auditoría técnica y de diseño — Jobs Menu 1.0.0

Fecha de corte: 2026-08-28. Base oficial auditada:
`dc9ccca960ba3d797c6980c7cc3f34bccffd3747` de `main`.

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

- La antigua geometría de primer plano común fue eliminada. Ningún escenario
  nuevo depende de un filtro u overlay para adquirir identidad.
- `Arquitectura` contiene primitivas raster pequeñas y sin listas temporales.
- Los eventos raros retornan antes de calcular cuando su ventana no está activa.
- El polvo genérico pasó de una densidad global a 0–24 motas según material.
- Se redujo la deriva de cámara y se respetan movimiento/destellos reducidos.
- Se eliminaron `PrimerPlano` y gran parte de la infraestructura duplicada de
  `Trazo`; quedaron matemática, profundidad, perspectiva y niebla reutilizables.

## Los diez fondos reconstruidos

1. **Administración:** encuadre lateral de una recepción abandonada, puertas de
   personal, archivo y mostrador cercano; escala humana e institucional.
2. **Nave:** vista baja de hangar con cerchas en A, pilares, puente grúa, cabina
   y portón distante; la altura domina la lectura.
3. **Servicio:** corredor técnico estrecho con tuberías a distintas cotas, codo
   ciego, panel, manómetros y válvula foreground.
4. **Natatorio:** el vaso ocupa la composición; calles, escalerilla, gradería,
   vidrio y reflejos separan agua, azulejo y aire húmedo.
5. **Cripta:** nave de piedra monumental con arco central, arcadas laterales,
   altar, braseros, pavimento y sarcófago cercano.
6. **Biblioteca:** doble altura en madera, estantes profundos, balcón, escalera,
   ventana, lámparas de lectura, mesa y papeles.
7. **Invernadero:** cubierta inclinada de vidrio con nervios metálicos, bancales,
   vegetación, luz filtrada y condensación.
8. **Catacumbas:** bóveda baja e irregular, nichos, restos, ramal oscuro,
   derrumbe y una única luz; más cerrada que la cripta.
9. **Cisterna:** espacio vertical con columnas que entran en agua negra,
   arcadas, pasarela, luces sumergidas y reflejos.
10. **Trono:** ábside ceremonial, columnata, eje de alfombra, cinco gradas,
    trono vacío coronado, estandartes, haz cenital y ruinas foreground.

Cada lugar recibe un suceso raro propio: fluorescente, gancho, presión, onda,
brasas, papel, hoja, sombra, anillos de agua o estandarte.

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
