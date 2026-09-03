# Auditoria 0.23.0 - 72 mejoras visibles

## Objetivo

0.23.0 es un pase de profesionalizacion perceptible. El conteo de esta auditoria incluye solo cambios que pueden verse o sentirse al navegar el mod. No se cuentan refactors, imports, cambios de version ni documentacion.

## Contrato de fondos

Los PNG 10-17 permanecen intactos como archivos y sin movimiento interno. No se agrega zoom, paneo, parallax, flicker, motas, presencia ni deformacion a la imagen. Las capas globales de interfaz y las transiciones de pantalla pueden pasar por encima porque no alteran el contenido ni la geometria del PNG.

## 72 mejoras visibles/perceptibles

### Capa profesional compartida

1. Codigo tecnico discreto identifica cada familia de pantalla Jobs.
2. Regla corta debajo del codigo tecnico mejora jerarquia de cabecera.
3. Segunda regla tenue prolonga la lectura sin competir con el titulo principal.
4. Contador visible de controles activos/totales en pantallas amplias.
5. Marca grafica junto al contador de controles.
6. Badge del perfil de experiencia reconocido actualmente.
7. Codigo EQ para el perfil Equilibrado.
8. Codigo IMM para el perfil Inmersivo.
9. Codigo PERF para el perfil Rendimiento.
10. Codigo ACC para el perfil Accesible.
11. Codigo MIN para el perfil Minimo.
12. Indicador compacto del sonido de interfaz.
13. Indicador compacto del ambiente.
14. Indicador compacto de la musica.
15. Los tres indicadores distinguen encendido y apagado visualmente.
16. Rail inferior con tecla TAB.
17. Rail inferior con tecla ENTER.
18. Rail inferior con tecla ESC.
19. Main screen suma teclas F y M al rail contextual.
20. Pause screen suma tecla M al rail contextual.
21. Linea inferior segmentada refleja cantidad de controles disponibles.
22. Segmentos activos e inactivos se distinguen visualmente.
23. Foco de teclado recibe esquinas externas reforzadas.
24. Hover de raton recibe una lectura mas suave que el foco de teclado.
25. Foco de teclado puede respirar muy sutilmente si el movimiento esta permitido.
26. Movimiento reducido congela esa respiracion sin perder foco visible.
27. Marcas laterales de registro unifican la familia de pantallas.
28. Actividad superior muy tenue aporta vida sin tocar el background.
29. Bajo consumo sustituye actividad animada por una marca estatica.
30. Alto contraste aumenta la presencia de la instrumentacion compartida.
31. El expediente raro obtiene una ficha visual propia fuera del contenido principal.
32. La marca de 03:33 recibe un registro grafico adicional.
33. Todo el sistema se adapta automaticamente a ventanas estrechas.
34. Interfaz minima elimina capas secundarias antes de comprometer legibilidad.

### Main screen / HUD de turno

35. HUD lateral del main screen ahora tiene mayor altura y mejor respiracion vertical.
36. HUD suma doble profundidad de sombra.
37. HUD recibe doble linea de borde/registro inferior.
38. Titulo tecnico pasa a JOBS / SHIFT CONTROL.
39. Estado de Suspension se distingue del estado normal y de transicion.
40. Estado de nivel conserva localizacion real del mod.
41. Cuatro LEDs muestran rotacion, ambiente, musica y sonidos UI.
42. LEDs encendidos y apagados se leen sin depender solo de texto.
43. HUD muestra el codigo del perfil actual.
44. Barra de progreso representa avance real de la estancia del nivel.
45. Barra llega a completo durante la transicion.
46. Barra funciona tambien cuando la rotacion esta desactivada.
47. Escala de progreso recibe marcas 0/25/50/75/100 visuales.
48. Tecla F tiene capsula propia dentro del HUD.
49. Tecla M tiene capsula propia dentro del HUD.
50. TAB tiene capsula propia dentro del HUD.
51. ENTER tiene capsula propia dentro del HUD.
52. Medidor inferior pasa de cinco a seis barras con alturas variadas.
53. Registro central inferior refuerza la sensacion de panel tecnico.
54. Alto contraste tambien afecta correctamente el HUD principal.

### Atmosfera global de interfaz

55. Cuatro esquinas de registro ahora forman un marco coherente entre pantallas.
56. Marca superior central es mas legible.
57. Marca inferior central recibe una segunda referencia.
58. Laterales reciben referencias de calibracion adicionales.
59. Pantallas amplias incorporan ticks superiores estaticos.
60. Laterales incorporan ticks verticales estaticos.
61. Papel limpio elimina los ticks decorativos secundarios.
62. Movimiento reducido conserva identidad mediante marcas fijas.
63. Bajo consumo conserva identidad mediante marcas fijas.
64. Barrido superior suma un cursor central sutil.
65. Barrido inferior suma una marca de seguimiento secundaria.
66. Barrido vertical izquierdo incorpora referencia transversal.
67. Barrido vertical derecho incorpora referencia transversal.
68. Pantallas grandes reciben un pulso de sistema exclusivamente en el borde.
69. El pulso de borde nunca modifica escala, posicion ni contenido del background.

### Transiciones

70. Duracion pasa a 470 ms para una lectura fisica mas clara sin sentirse lenta.
71. Transicion reducida suma registro central superior e inferior.
72. Transicion completa incorpora doble borde de papel, triple registro vertical, perforaciones de archivador, ficha interna, rails direccionales, marcas de tercios y una salida sombreada de dos fases.

## Compatibilidad y limites

- No cambia hitboxes de Minecraft/Forge.
- No reconstruye pantallas externas complejas.
- Embeddium conserva su UI real.
- Gameplay sigue siendo frontera dura para audio de menu.
- Los overlays son posteriores al contenido y no capturan input.
- Movimiento reducido y Bajo consumo siguen teniendo prioridad.
- CI certifica recursos y compilacion; la estetica final requiere prueba manual dentro de Minecraft.

## Prueba manual prioritaria

1. Main screen a 1280x720 y 1920x1080.
2. Main screen con perfiles Equilibrado, Inmersivo, Rendimiento, Accesible y Minimo.
3. Pause screen en mundo local y servidor.
4. Options y Config Jobs para revisar rail inferior y foco.
5. Mods, Resource Packs, Mundos, Multiplayer e Idioma para confirmar que la capa no pisa listas ni buscadores.
6. Movimiento reducido y Bajo consumo.
7. Alto contraste.
8. Niveles PNG 10-17 durante cambios de pantalla y cambios de nivel: el PNG debe seguir inmovil.
