# Dirección artística — Jobs · Aviso a los ocupantes

Documenta el lenguaje visual **tal como está implementado** en la rama
`arena/01a04ff1-jobs-menu` (0.10.0 + Evolución 6). No es una declaración de
intenciones: cada punto tiene su clase y su código.

## 1. El lenguaje visual

El menú es un aviso fotocopiado pegado en la pared de un edificio que no se
termina. El fondo es el edificio; la hoja es el papel. Las reglas de la
dirección artística:

- **Papel envejecido.** `HojaPapel` (tinta, grano, doblez, cinta); `papel_limpio`
  retira decoración sin quitar contenido.
- **Arquitectura industrial.** Los diez recintos se construyen con
  `Trazo` (paredes, planos, juntas, manchas, transversales) sobre la geometría
  de `Marco`: cada recinto define su fuga y sus cuatro semibordes.
- **Metal usado, piedra, madera, humedad.** `MaterialesEscena` y `Paleta`
  diferencian materiales por color y brillo; `TratamientoEscena` añade grano,
  humedad y desgaste sin manchar la lectura.
- **Iluminación verde controlada y luces cálidas puntuales.** Cada recinto
  tiene una luz principal (verde de tubo o cálida de fuego) y rebotes; la
  paleta reserva el rojo para Executores.
- **Sombras profundas y desgaste.** Juntas, bordes, cantos de luz y reflejos
  rotos; `humedad` y `reflejo` por nivel controlan cuánto.
- **Animación ambiental sutil.** `EventosAmbientales`, `Presencia`,
  respiración de cámara; todo congelable con `movimiento_reducido` y
  `bajo_consumo`.

## 2. Cámara y composición

`Marco` define por nivel: `fuga` (dónde converge la perspectiva) y los cuatro
semibordes `(semiIzq, semiDer, semiAlto, semiBajo)` — dos paredes y dos planos
independientes, de modo que ningún recinto es un pasillo simétrico salvo cuando
le conviene serlo.

| Nivel | Fuga (x,y) | Semibordes (izq,der,alto,bajo) | Lectura de cámara |
|---|---|---|---|
| 0 Sala | 0.68, 0.47 | 0.330, 0.105, 0.150, 0.135 | Esquina: pared izquierda domina, la derecha se va rápido |
| 1 Nave | 0.505, 0.72 | 0.235, 0.255, 0.300, 0.098 | Desde el suelo: horizonte bajo, techo lejísimos, volumen |
| 2 Servicio | 0.395, 0.505 | 0.062, 0.078, 0.108, 0.098 | Pasillo real, estrecho y alto; la fuga cae a la izquierda |
| 3 Natatorio | 0.455, 0.33 | 0.300, 0.270, 0.080, 0.124 | Fuga baja: la mitad inferior es agua, no suelo |
| 4 Cripta | 0.505, 0.50 | 0.150, 0.150, 0.185, 0.150 | Nave abovedada, horizonte centrado, profundidad honda |
| 5 Biblioteca | 0.50, 0.50 | 0.140, 0.140, 0.150, 0.140 | Corredor entre estanterías con galería vertical |
| 6 Invernadero | 0.50, 0.50 | 0.165, 0.165, 0.175, 0.130 | Nave de cristal, techo alto que pesa menos que el follaje |
| 7 Catacumbas | 0.47, 0.47 | 0.070, 0.082, 0.130, 0.112 | Túnel que se estrangula; fuga corrida, paredes cercanas |
| 8 Cisterna | 0.50, 0.50 | 0.190, 0.190, 0.092, 0.118 | Vaso ancho: techo bajo, agua que lo refleja todo |
| 9 Trono | 0.47, 0.53 | 0.160, 0.140, 0.185, 0.140 | Ligeramente lateral: el eje del trono sigue la fuga sin espejo |

Reglas de composición implementadas: el foco (candil, portón, reloj, trono…)
nunca queda tapado por la hoja (`EscenaNivel` coloca la UI y reserva la lectura);
cada recinto tiene un elemento de silueta propio (torres de luz, cerchas, haz,
trampolín, candil, escalera, hojas, nichos, columnas, estandartes); la
profundidad se ordena en planos (fondo → plano → paredes → objetos → primer
plano).

## 3. Materiales

`Paleta` y `MaterialesEscena` distinguen: piedra (sillares, juntas profundas),
metal (óxido en `inv_pasarela`, hierro en `cis_galeria`, chapa en portones),
madera (estanterías, bancos), vidrio (cristalera, ventanal roto), azulejo
(natatorio), agua (reflejos partidos por ondulación), vegetación (verdes
desaturados), fuego (llama con halo), humedad (brillos mínimos, condensación
localizada) y óxido (sarro, cal mineral).

Cada superficie tiene: junta o borde, desgaste determinista (semillas de
`Trazo.pseudo`), y un canto de luz o sombra de contacto que la ancla.

## 4. Iluminación

Por recinto hay una fuente principal y secundarias justificadas:

- **Sala:** tubos fríos arriba; torres de luz al fondo.
- **Nave:** campanas a medio prender, frías; rendija de luz bajo el portón.
- **Servicio:** piloto en el tablero (único punto saturado, en la fuga);
  apliques de trabajo.
- **Natatorio:** luz cenital difusa; reflejo de tubos sobre el agua.
- **Cripta:** candil y antorchas cálidas; piedra fría; cera bajo el candil.
- **Biblioteca:** lámparas verdes muy bajas; franja cálida sobre la mesa;
  reloj con reflejo metálico.
- **Invernadero:** haces cenitales con polvo; vaho.
- **Catacumbas:** farol colgado, único; velas votivas en nichos fijos.
- **Cisterna:** focos sumergidos verdes; teñido desde abajo.
- **Trono:** haz cenital que se ensancha hacia la tarima; oro solo en el arco
  interno del ábside; rebote frío de la piedra.

`TratamientoEscena` y `EscenaNivel` controlan la luz por frame (transición,
apagón, suspensión al 4 %), con `luz` entre 0 y 1.

## 5. Qué NO se dibuja

- Rojo fuera de Executores (regla de identidad).
- Overlays sobre pantallas de otros mods (la escena vive solo en las pantallas
  propias).
- Partículas ni listas por frame: todo es `fill` determinista con culling por
  profundidad (`bajo_consumo` elimina polvo/grano/presencia/motas).
- Formas sin ancla: todo objeto tiene sombra de contacto o punto de soporte
  visible (regla verificada por las filas de `AUDITORIA_FONDOS_50X10.md`).

## 6. Evolución 6 — decisiones artísticas

- Una mejora nueva por recinto (filas AD-15, DE-17, SE-11, NA-22, SA-11,
  BI-12, IN-14, CA-13, CI-11 y TR-09/10/11/16/17), cada una con su espejo en
  `tools/vista_previa.py` (misma geometría en píxeles) y su registro en
  `AUDITORIA_FONDOS_50X10.md`.
- El Trono se rediseñó desde cero: ver `FONDOS_EXPLICADOS.md` (nivel 9).
- Regla de sincronía: si se toca una planta Java, se toca su espejo Python en
  el mismo commit (`tools/vista_previa.py`).
