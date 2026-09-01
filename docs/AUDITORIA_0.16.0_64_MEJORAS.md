# Auditoría UI 0.16.0 — 64 mejoras verificables

Este inventario describe cambios implementados, no ideas pendientes. El pase conserva Forge/Minecraft 1.20.1, Java 17, lógica vanilla sensible, 18 niveles, fondos 10–17 estáticos, continuidad de audio y paridad ES/EN.

## Sistema visual compartido

1. Los paneles propios usan ancho y alto máximos en lugar de cubrir toda la pantalla.
2. Los cuatro bordes conservan margen visible del recinto incluso en ventanas estrechas.
3. Nuevas marcas de margen seguro hacen legible la separación pantalla/documento.
4. La entrada de cada pantalla tiene un asentamiento corto de bordes.
5. Movimiento reducido sustituye esa entrada por un fundido mínimo.
6. Bajo consumo también evita la entrada decorativa completa.
7. El foco de ratón recibe esquinas de lectura comunes.
8. El foco de teclado se distingue del hover por color e intensidad.
9. El foco de teclado añade una guía inferior sin cambiar el hitbox.
10. Los cambios en toggles muestran confirmación no modal localizada.
11. Los cambios en sliders muestran la misma confirmación coherente.
12. La confirmación desaparece sola y nunca bloquea interacción.
13. La piel Jobs se aplica globalmente a widgets vanilla dentro de pantallas propias.
14. Los widgets Jobs se excluyen de esa segunda piel para evitar doble render.
15. Los botones vanilla conservados reciben borde, tinta y foco Jobs.
16. Los sliders vanilla conservan su lógica y reciben pista/ticks Jobs.
17. Los campos de texto conservan cursor y selección, con marco de ficha.
18. Los controles inactivos conservan lectura diferenciada.

## Papel, archivo y listas

19. Las pantallas simples mantienen papel administrativo compacto.
20. Las listas extensas usan archivo oscuro y dejan de abusar del papel.
21. El papel incorpora profundidad en dos capas de sombra.
22. El papel incorpora perforaciones laterales de expediente.
23. El papel incorpora reglas tenues sólo cuando hay espacio suficiente.
24. El modo papel limpio elimina textura estructural no esencial.
25. El archivo oscuro incorpora borde exterior e interior.
26. El archivo oscuro incorpora marcas laterales de inventario.
27. Las cabeceras largas se recortan de forma segura.
28. Los pies reservan espacio para overlays de terceros.
29. Mods recibe un rótulo compacto que no tapa búsqueda ni lista.
30. Resource Packs recibe un rótulo compacto que no tapa sus dos columnas.
31. El scrollbar Jobs cubre la barra gris vanilla después del render.
32. Rueda, click y arrastre siguen perteneciendo a Minecraft.
33. El thumb se calcula con el máximo de scroll real.
34. La barra incorpora canaleta, topes y marcas de posición.
35. Un fallo de reflexión visual no impide utilizar la pantalla.

## Controles y respuesta

36. Los botones tienen estado de presión con desplazamiento físico breve.
37. Cada jerarquía de botón tiene marca visual propia.
38. El hover de botón entra suavemente y respeta movimiento reducido.
39. El texto largo de botón se elide dentro del ancho disponible.
40. Los toggles separan etiqueta y valor en una cápsula legible.
41. Los toggles tienen marca física de activación.
42. Los sliders muestran escala con marcas mayores y menores.
43. Los sliders muestran tramo recorrido y tirador de alto contraste.
44. Los sliders emiten sonido de entrada al hover sin repetición continua.
45. Los sonidos de arrastre se limitan para evitar saturación.
46. Cada pestaña de Ajustes de aviso explica su función con tooltip.
47. La pestaña activa tiene subrayado persistente.
48. Ajustes de aviso muestra índice `01 / 05` de categoría.
49. Las teclas `1`–`5` abren directamente sus categorías.
50. Las flechas izquierda/derecha recorren las categorías de forma circular.

## Idioma y opciones

51. Idioma renderiza una sola instancia de la lista y elimina la doble scrollbar.
52. La lista queda limitada entre búsqueda y footer, sin atravesar el panel.
53. Idioma permite buscar por nombre localizado.
54. Idioma permite buscar también por código, por ejemplo `es_es`.
55. Cada fila amplia muestra el código de idioma.
56. Un contador muestra cuántos idiomas coinciden.
57. `Ctrl+F` lleva el foco al buscador.
58. `Esc` limpia primero una búsqueda activa antes de cerrar.
59. Doble click sigue aplicando el idioma sin romper la recarga de recursos.
60. Forzar fuente Unicode es un toggle real con estado separado.
61. Cada destino del centro de opciones tiene tooltip localizado.
62. FOV y Volver también explican su efecto.

## Mundos, servidores, Mods y recursos

63. Singleplayer usa archivo oscuro, conserva previews/acciones vanilla y añade `Ctrl+F` más limpieza con `Esc`.
64. Multiplayer usa título Jobs, fija primero `JobsDosh.exaroton.me:56477`, traduce su nombre, lo protege de edición/borrado, muestra distintivo fijado, añade tooltips y refresco `F5`; Mods cambia blanco puro por tinta sepia y Resource Packs elimina dirt/papel gigante y limpia el pack musical legado redundante.

## Validación automática

- `tools/verificar.py`: claves, paridad, Java ASCII, assets y contratos estáticos.
- `tools/verificar_fondos.py`: PNG/IDAT/CRC de niveles 10–17.
- `tools/verificar_version.py`: versión y nombre exacto del artefacto.
- `git diff --check`: espacios y conflictos de parche.
- GitHub Actions: compilación Forge real antes de integrar en `main`.

La prueba manual dentro de Minecraft sigue siendo obligatoria para resolución, hover, teclado, drag del scroll, sonidos y convivencia con Embeddium/Forge.
