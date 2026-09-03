# Auditoria 0.24.0 - 84 mejoras visibles/perceptibles

## Objetivo

0.24.0 revisa la navegacion y los controles del mod despues del pase 0.23.0. El conteo incluye solo cambios que se ven o se sienten usando la interfaz. No cuentan version, imports, refactors ni documentacion.

## Contrato de fondos

Los PNG 10-17 permanecen intactos. No reciben zoom, paneo, parallax, flicker, motas, presencia, deformacion ni movimiento propio. Fades, apagones, transiciones y overlays globales pueden pasar por encima siempre que no muevan ni alteren la imagen.

## 84 mejoras

### Instrumentacion contextual compartida

1. Codigo tecnico de pantalla sigue identificando cada familia Jobs.
2. La cabecera tecnica ahora muestra tambien el titulo real de la Screen cuando hay espacio.
3. El titulo contextual se recorta de forma segura antes de invadir otras zonas.
4. El contador de controles activos/totales gana una barra de proporcion.
5. La barra permite leer visualmente cuantos controles estan disponibles sin depender solo del numero.
6. Se incorpora reloj de visita `T+MM:SS`.
7. El reloj se mantiene al navegar por subpantallas de la misma visita.
8. Se muestra cantidad de pantallas visitadas en la sesion actual.
9. Se muestra el volumen maestro Jobs en la instrumentacion.
10. Volumen cero se identifica como `MUTE` en vez de un numero ambiguo.
11. Se incorpora breadcrumb de las ultimas tres familias de pantalla.
12. El breadcrumb se reinicia al comenzar una visita nueva.
13. El breadcrumb evita repetir consecutivamente el mismo codigo.
14. El control enfocado muestra su etiqueta real en la barra contextual.
15. El control bajo el mouse muestra su etiqueta cuando no existe foco de teclado.
16. La etiqueta contextual se recorta antes de tocar los metadatos laterales.
17. Se diferencia entrada por teclado con codigo `KEY`.
18. Se diferencia entrada por puntero con codigo `PTR`.
19. Toggles se identifican como `TOG`.
20. Sliders se identifican como `SLD`.
21. Campos de texto se identifican como `TXT`.
22. Renglones principales se identifican como `ROW`.
23. Botones se identifican como `BTN`.
24. Otros controles reciben fallback `CTL`.
25. La barra muestra posicion del control actual dentro de los controles activos.
26. Si no hay control actual, la barra recupera el perfil de experiencia como contexto.
27. Main muestra atajos especificos en vez de un rail generico.
28. Mundos, Mods e Idioma muestran `CTRL+F` porque ese atajo existe de verdad.
29. Multiplayer muestra `F5` porque Refresh lo implementa realmente.
30. Config muestra `F1-F5` para los perfiles rapidos existentes.
31. El resto conserva TAB/ENTER/ESC como navegacion comun.
32. En viewports medianos la barra completa se reduce a un rail compacto.
33. En viewports pequenos desaparece primero la informacion secundaria y no el contenido.
34. Interfaz minima omite ruta y telemetria secundaria.
35. Movimiento reducido sustituye la actividad superior movil por una referencia fija.
36. Bajo consumo tambien sustituye actividad por referencia fija.
37. Alto contraste aumenta la lectura de la nueva instrumentacion.
38. El foco de teclado recibe esquinas externas mas claras.
39. El foco de teclado recibe marcas laterales asimetricas.
40. El foco de teclado recibe una marca superior central.
41. Hover usa una marca inferior mas discreta para no confundirse con teclado.
42. El easter egg de expediente raro conserva una ficha lateral propia.
43. 03:33 conserva una marca de registro independiente.
44. El easter egg del minuto 13 suma una referencia inferior extremadamente discreta.

### Main screen / HUD de turno

45. HUD lateral aumenta ligeramente de ancho para evitar compresion de metadatos.
46. HUD aumenta de alto para separar estado, progreso, volumen y atajos.
47. Se conserva doble sombra y se refuerza la profundidad de placa.
48. El lado derecho obtiene una referencia vertical secundaria.
49. Los cuatro LEDs ahora tienen etiquetas `R`, `A`, `M` y `U`.
50. La lectura permite asociar cada LED a rotacion, ambiente, musica e interfaz.
51. Perfil no reconocido se muestra como `CUSTOM` en vez de desaparecer.
52. La barra de estancia mantiene marcas de cuartos.
53. La punta actual del progreso recibe un cursor propio.
54. Se muestra tiempo estimado hasta el siguiente traslado como `NXT MM:SS`.
55. Rotacion fija se expresa como `NXT HOLD`.
56. Una transicion activa se expresa como `NXT MOVE`.
57. El HUD muestra el tiempo transcurrido de la visita.
58. Se muestra el volumen maestro Jobs como numero de tres digitos.
59. Volumen cero se convierte visualmente en `MUTE`.
60. El volumen obtiene una barra horizontal propia.
61. La barra de volumen incorpora seis referencias de escala.
62. Main incorpora chip `1-4` para los nuevos atajos numericos reales.
63. Los chips F/M/TAB/ENTER reciben una referencia lateral para leerse como teclas.
64. Toda la nueva informacion sigue modulada por luz y Alto contraste sin alterar el background.

### Controles vanilla / Forge preservados

65. Botones vanilla tematizados reciben sombra inferior independiente.
66. Botones reciben doble marco sin cambiar su hitbox.
67. Botones reciben un highlight superior de ficha.
68. Foco de teclado se diferencia visualmente del hover de mouse.
69. Foco de teclado añade marcadores laterales externos.
70. Etiquetas truncadas reciben una marca visual de recorte.
71. Botones disabled reciben referencias laterales y centrales mas claras.
72. Sliders vanilla reciben sombra y doble marco.
73. Sliders muestran escala de diez pasos con 0/50/100 reforzados.
74. Slider enfocado por teclado recibe notch superior.
75. Sliders disabled reciben marcas de indisponibilidad.
76. EditBox recibe doble marco sin cubrir cursor, seleccion ni texto vanilla.
77. EditBox enfocado recibe notch superior central.
78. EditBox enfocado recibe rail inferior corto.
79. Campos no editables reciben marcas laterales de estado.
80. Archivo oscuro y formulario claro conservan paletas distintas en todos estos estados.

### Scrollbars y atajos funcionales

81. Scrollbars ganan tramo recorrido, escala 0/25/50/75/100, topes, chevrons, doble cursor de posicion, doble sombra de thumb, grip ampliado y marcas de extremo sin modificar rueda/click/drag.
82. Teclas 1-4 y teclado numerico 1-4 activan los cuatro renglones del main de arriba hacia abajo.
83. En pausa, 1-2 y teclado numerico 1-2 activan Reanudar y Condiciones; no se asigna salida/desconexion a 3 para evitar abandonos accidentales.
84. Los atajos numericos quedan automaticamente desactivados cuando un EditBox tiene foco o existen modificadores, para no robar escritura ni combinaciones del usuario.

## Limites y compatibilidad

- `AtajosInterfazJobs` usa el evento real de teclado de Screen, igual que las herramientas internas ya existentes.
- No cambia gameplay.
- No modifica hitboxes.
- No reconstruye listas vanilla/Forge.
- Embeddium conserva su pantalla real.
- Gameplay sigue siendo frontera dura para audio de menu.
- La instrumentacion de sesion es local, temporal y no se guarda ni transmite.
- CI certifica recursos y compilacion; la estetica final requiere prueba manual en Minecraft.

## Prueba manual prioritaria

1. Main: probar 1, 2, 3 y 4, incluido doble 4 para confirmar salida.
2. Pause: probar 1 y 2; verificar que 3 no desconecta.
3. Probar teclado numerico.
4. Probar que los numeros no actuan dentro de buscadores/EditBox.
5. Recorrer Main > Options > Audio > volver y revisar breadcrumb/reloj.
6. Revisar foco KEY vs PTR en botones, sliders y campos.
7. Revisar scrollbars en Mundos, Multiplayer, Mods, Resources e Idioma.
8. Probar Movimiento reducido, Bajo consumo, Alto contraste e Interfaz minima.
9. Verificar PNG 10-17 durante navegacion: la imagen debe seguir inmovil.
