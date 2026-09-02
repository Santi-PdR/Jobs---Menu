# Auditoria visual 0.21.0 - 90 mejoras perceptibles

Este documento cuenta solo cambios que el usuario puede ver o percibir usando Jobs Menu. No cuenta refactors, cambios de nombres, limpieza interna ni ajustes de build.

## Botones de expediente - 16

1. La presion conserva el hitbox pero hunde visualmente el boton.
2. La confirmacion deja una linea de respuesta temporal tras el click.
3. Los botones JOBS tienen una presencia de acento mas fuerte.
4. Los botones PRINCIPAL tienen una banda superior propia.
5. Los botones PRINCIPAL ganan una marca lateral secundaria.
6. Los botones TERMINAL usan doble lectura lateral.
7. Los botones TERMINAL reciben un contraste de fondo propio.
8. El hover agrega una pelicula interna de acento muy tenue.
9. El foco de raton dibuja muescas en esquinas opuestas.
10. El foco de teclado agrega una marca central superior.
11. El foco de teclado diferencia ambos laterales.
12. La sombra inferior cambia segun la jerarquia del boton.
13. El texto truncado muestra una marca de continuacion mas clara.
14. El subrayado de foco crece de forma progresiva.
15. Los botones deshabilitados muestran marcas a ambos lados.
16. La marca JOBS incorpora un nucleo central que responde al foco.

## Toggles - 14

17. El estado ON/OFF interpola visualmente en vez de saltar.
18. La casilla llena su interior progresivamente al activar.
19. La marca de activacion se construye en varias piezas.
20. El estado activo tinta muy levemente el fondo del control.
21. La capsula de valor cambia intensidad con el estado.
22. La capsula incorpora un indicador fisico lateral.
23. El toggle tiene un rail inferior que representa el estado.
24. El cambio deja una confirmacion breve en el borde inferior.
25. La presion produce un flash interior contenido.
26. El foco de teclado agrega marcadores laterales externos.
27. La cabecera interna del toggle incluye una marca de registro.
28. El divisor entre etiqueta y estado gana mejor jerarquia.
29. Los estados deshabilitados tienen marcas laterales propias.
30. La etiqueta conserva elipsis sin invadir la capsula de estado.

## Sliders - 15

31. El tirador visual interpola hacia el valor real.
32. El valor real deja una guia temporal si el tirador visual aun se esta asentando.
33. El porcentaje vive dentro de una capsula independiente.
34. La capsula de porcentaje recibe un subrayado de acento.
35. La pista tiene una canaleta fisica de dos capas.
36. La escala mantiene diez divisiones y tres marcas mayores.
37. Las marcas mayores reciben topes inferiores secundarios.
38. El tramo recorrido usa doble rail de acento.
39. El tirador incorpora sombra propia.
40. El tirador tiene marco, nucleo y linea central diferenciados.
41. El tirador incorpora una pequena marca horizontal de agarre.
42. En sliders anchos aparecen los valores minimo y maximo.
43. El foco de teclado agrega una mira sobre el valor real.
44. Cada cambio deja una confirmacion breve centrada en el pie.
45. El estado deshabilitado reduce contraste y agrega marcas laterales.

## Renglones del menu principal - 14

46. El hover usa una banda de lectura aun sin Guia de lectura.
47. Guia de lectura refuerza esa banda sin cambiar hitbox.
48. El foco de teclado usa el acento fuerte en el rail izquierdo.
49. Cada orden numerico recibe una pequena placa de archivo.
50. La placa de orden incorpora un subrayado propio.
51. La casilla de seleccion usa el acento Jobs al llenarse.
52. La casilla construye una marca interior al completar el foco.
53. Los puntos de relleno responden progresivamente al foco.
54. El extremo derecho usa un indicador direccional de varias piezas.
55. Los renglones terminales reciben doble regla horizontal.
56. Los renglones terminales ganan un segundo rail interno.
57. La presion oscurece brevemente el interior sin mover el hitbox.
58. La confirmacion dibuja una linea centrada posterior a la accion.
59. Los estados deshabilitados muestran topes en ambos extremos.

## Pulido global - 13

60. Las cuatro esquinas globales incluyen un punto de registro.
61. El rail superior incorpora una mira central.
62. El rail inferior incorpora una marca central secundaria.
63. Los laterales reciben marcas de media altura.
64. Las marcas de inventario superiores se complementan con marcas laterales.
65. El contador de widgets distingue visibles de activos.
66. El pie muestra una segunda lectura de densidad de controles.
67. El foco global agrega una marca central superior sobre widgets anchos.
68. El foco de teclado muestra un segundo indicador en el lado derecho.
69. El hover de raton extiende su rail inferior.
70. La entrada de pantalla usa un cierre de bordes mas suave.
71. La entrada agrega una linea central que se contrae al asentarse.
72. El aviso de cambio guardado incluye una barra de tiempo restante.

## Transiciones - 9

73. El barrido de expediente dura ligeramente mas para evitar un golpe seco.
74. La hoja de transicion incorpora una cola de sombra separada.
75. La hoja tiene dos rails laterales internos.
76. El lomo central conserva doble linea de tinta.
77. Se agregan perforaciones verticales de archivo.
78. La parte superior usa una mira de registro.
79. El pie de la hoja recibe una marca central propia.
80. El rail contextual incluye una marca de tercio.
81. Movimiento reducido conserva una transicion minima con marca central sin barrido.

## Scrollbars Jobs - 9

82. La canaleta gana sombra en ambos laterales.
83. El carril central incorpora un acento interior tenue.
84. Las marcas de recorrido pasan a nueve posiciones.
85. Las marcas 0/50/100 se distinguen de las intermedias.
86. La posicion actual tiene un indicador independiente junto al carril.
87. El tirador tiene una sombra exterior mas amplia.
88. El tirador incorpora highlight superior y sombra inferior.
89. Las lineas de agarre son mas numerosas y centradas.
90. El centro del tirador tiene una marca de acento propia.

## Alcance real

Estos cambios se propagan a las pantallas que reutilizan los widgets y capas compartidas: menu principal, Options, Config Jobs, Idioma, Controles, Piel, Sonido, Video vanilla, Chat, Accesibilidad, Online, Mouse, Teclas, Pausa y varios dialogos integrados. Las scrollbars mejoran tambien listas compatibles de Mundos, Multiplayer, Mods, Recursos e Idioma.

## Fondos 10-17

Los PNG 10-17 no se modifican en 0.21.0. Se conserva la correccion anterior: filtrado lineal al escalarlos para evitar pixelado. No se agrega zoom, paneo, parallax, flicker ni animacion a las imagenes.

## Criterio

0.21.0 busca que Jobs se sienta como un sistema administrativo fisico y viejo, no como una UI futurista. La mejora se concentra en jerarquia, respuesta, material, registro, lectura y continuidad. Movimiento reducido y Bajo consumo siguen eliminando o simplificando interpolaciones decorativas.