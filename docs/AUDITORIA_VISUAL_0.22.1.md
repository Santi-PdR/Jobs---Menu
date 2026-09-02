# Auditoria visual 0.22.1

## Objetivo

Pase centrado en impacto visible inmediato: menu principal, pausa, transiciones, atmosfera global y secretos de sesion. No modifica gameplay ni sustituye los PNG 10-17.

## Contrato PNG

Los PNG 10-17 siguen intactos como archivos y como geometria. No reciben zoom, paneo, parallax, respiracion, motas, foreground dinamico, flicker, deformacion ni animacion propia. Si se agregan 18-19 como PNG, heredan este contrato.

Si se permiten efectos globales que pertenecen al menu: fades, apagones de cambio de nivel, transicion de expediente, rails y overlays de interfaz que no mueven ni deforman la imagen.

## Mejoras visibles/perceptibles

1. HUD contextual nuevo en el menu principal.
2. Panel lateral con lectura de turno.
3. Nivel actual visible en el HUD.
4. Estado de instalacion visible en el HUD.
5. Pista de atajos integrada en el HUD.
6. Barras tecnicas decorativas del HUD.
7. Sombra propia del panel contextual.
8. Rail de acento del HUD.
9. Separador interno del HUD.
10. HUD se oculta en interfaz minima.
11. HUD se oculta en viewports pequenos.
12. Composicion principal agrega marca media izquierda.
13. Composicion principal agrega rail derecho.
14. Zona tecnica lateral ahora admite mas ancho.
15. Codigo de expediente visible de forma discreta en zona tecnica.
16. Mejor jerarquia entre hoja principal y datos laterales.
17. Main screen conserva luz real del nivel en adornos.
18. Pausa agrega panel de contexto superior izquierdo.
19. Panel de pausa identifica LOCAL/SERVER.
20. Panel de pausa muestra codigo de expediente de sesion.
21. Panel de pausa agrega regla interna.
22. Pausa mantiene doble sombra del papel.
23. Pausa mantiene rails de suspension.
24. Pausa mantiene pista M=MUTE.
25. Easter egg de archivo negro de sesion.
26. Easter egg de turno fantasma preparado para composiciones futuras.
27. Easter egg minuto 13 preparado como trigger discreto.
28. Codigo ARCH-xxx determinista durante la sesion.
29. Secretos siguen sin red.
30. Secretos siguen sin recompensa ni efecto de gameplay.
31. Atmosfera global agrega registro superior central.
32. Atmosfera global agrega registro inferior central.
33. Atmosfera global agrega marcas laterales centrales.
34. Barrido superior refinado y mas lento.
35. Barrido inferior complementario refinado.
36. Nuevo barrido vertical ultra sutil izquierdo.
37. Nuevo barrido vertical ultra sutil derecho.
38. Barridos verticales solo en viewports amplios.
39. Movimiento ambiental respeta Movimiento reducido.
40. Movimiento ambiental respeta Bajo consumo.
41. Ningun barrido mueve el background.
42. Transicion de expediente ampliada a 430 ms para lectura mas suave.
43. Cola de transicion mas profunda.
44. Sombra de transicion mas profunda.
45. Marcas internas de expediente conservadas.
46. Nuevo doble registro intermedio en transicion.
47. Rails superior e inferior conservados.
48. Transicion reducida sigue siendo fade simple.
49. Transicion no bloquea input.
50. Transicion no cambia Screen.
51. Main screen sigue conservando cuatro acciones administrativas.
52. Renunciar mantiene segunda confirmacion.
53. Pausa conserva Resume real.
54. Pausa conserva Options real.
55. Pausa conserva Leave real.
56. Pausa conserva mundo real detras.
57. Gameplay sigue siendo frontera dura del audio de menu.
58. No se agregan dependencias.
59. No se agregan mixins.
60. No se editan PNG 10-17.

## Prueba manual prioritaria

- main screen en 16:9 y ventana pequena;
- niveles 0-17, especialmente 10-17;
- confirmar que el HUD lateral no pisa reloj, credito o rotulo de nivel;
- abrir/cerrar varias pantallas para revisar la nueva transicion;
- abrir pausa en singleplayer y multiplayer;
- probar Movimiento reducido y Bajo consumo;
- verificar que ningun PNG se mueve aunque los overlays globales sigan funcionando.
