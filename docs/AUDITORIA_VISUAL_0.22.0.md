# Auditoria visual 0.22.0

## Objetivo

Pase centrado en cambios que se perciben inmediatamente al usar Jobs Menu: menu principal, pausa, transiciones globales, contexto de sesion, microanimacion ambiental y easter eggs discretos.

## Contrato de fondos PNG

Los fondos PNG 10-17 no se reemplazan, no se editan y no reciben movimiento propio. Permanecen sin zoom, paneo, parallax, motas, foreground dinamico, flicker ni deformacion. Si se agregan 18-19 como PNG, heredan esta regla.

Si se permiten efectos globales del menu: transicion de expediente, fade/apagon entre niveles, rails de interfaz y capas que no cambian la geometria ni el contenido del PNG.

## Mejoras visibles de esta tanda

1. Menu principal con rail vertical junto a la hoja.
2. Marca transversal de registro junto a la hoja.
3. Zona tecnica lateral cuando hay espacio libre.
4. Identificador LEVEL dinamico en esa zona.
5. Regla secundaria bajo la cabecera.
6. Regla adicional bajo el reloj de ronda.
7. Regla sobre el rotulo de nivel.
8. Composicion lateral que adapta su ancho al viewport.
9. Composicion omitida automaticamente en modo compacto.
10. Contexto visual que sigue la luz real del nivel.
11. Easter egg de expediente raro por sesion.
12. Codigo de expediente determinista durante la sesion.
13. Easter egg especial a las 03:33.
14. Los secretos no alteran gameplay.
15. Los secretos no requieren red ni datos externos.
16. Pausa con doble profundidad de sombra.
17. Pausa con rails laterales de suspension.
18. Pausa con marcas centrales laterales.
19. Pausa con estado LOCAL/SERVER.
20. Pausa muestra atajo M=MUTE.
21. Pausa puede mostrar el codigo raro de sesion.
22. Nueva capa AtmosferaMenuJobs compartida.
23. Rail izquierdo global discreto.
24. Rail derecho global discreto.
25. Barrido superior muy tenue.
26. Barrido inferior complementario.
27. Movimiento ambiental desactivado con Movimiento reducido.
28. Movimiento ambiental desactivado con Bajo consumo.
29. La atmosfera no mueve ni reescala fondos.
30. Las transiciones siguen siendo globales y compatibles con PNG.
31. La interfaz conserva hitboxes y logica existentes.
32. No se agregan dependencias.
33. No se agregan mixins.
34. Gameplay sigue siendo frontera dura para audio del menu.
35. El menu principal conserva sus cuatro acciones administrativas.
36. Renunciar sigue requiriendo segunda confirmacion.
37. La pausa conserva Resume/Options/Leave reales.
38. La pausa sigue devolviendo correctamente a mundo o servidor.
39. La composicion nueva respeta Interfaz minima.
40. La composicion nueva respeta resoluciones compactas.

## Prueba manual prioritaria

- abrir el menu en 16:9 y ventana pequena;
- recorrer niveles 0-17;
- comprobar que PNG 10-17 no se mueven;
- cambiar de nivel sobre PNG y observar que solo actua la transicion global;
- abrir/cerrar pausa varias veces;
- comprobar LOCAL y SERVER;
- probar M en pausa;
- probar Movimiento reducido y Bajo consumo;
- verificar que la nueva composicion no pisa reloj, credito ni rotulo de nivel.
