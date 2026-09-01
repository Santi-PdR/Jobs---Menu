# Auditoría visual 0.16.1 — correcciones por captura

Este pase nace de la prueba real de `jobsmenu-0.16.0.jar` en `test-1`. La compilación anterior era válida, pero las capturas demostraron errores de render y geometría que sólo podían detectarse dentro del juego.

| Pantalla | Evidencia 0.16.0 | Corrección 0.16.1 |
|---|---|---|
| Idioma | contador correcto, lista invisible y buscador negro con texto blanco | lista añadida como renderizable; buscador de papel y tinta; filas con nombre y código reservados |
| Resource Packs | tres líneas de cabecera superpuestas y `jobsmenu-musica` visible | título vanilla vacío, borrado opaco de instrucción/cabecera, rótulo único y limpieza de ambos nombres legados |
| Mundos | `Select World` blanco encima de `SHIFT ARCHIVE`; pie bajo botones | cabecera vanilla cubierta de forma opaca; texto cálido; pie retirado |
| Mods | rótulo Jobs encima del nombre del mod, búsqueda blanca y tinte marrón saturado | rótulo superpuesto retirado; búsqueda localizada en tinta cálida; paleta neutral más clara |
| Ajustes | pestañas truncadas, `01/05` y detalle sobre la misma línea, resumen incompleto sobre controles | nombres cortos, indicador aislado en cabecera, sección breve y resumen redundante eliminado |
| Multiplayer | cabecera vanilla visible debajo de la Jobs | borrado opaco antes de la cabecera Jobs, con lista y tarjeta oficial fuera de la máscara |

## Contratos de aceptación

1. Idioma debe mostrar filas desde el primer frame y conservar una sola scrollbar.
2. Buscar `es`, `en` o un código debe filtrar sin vaciar incorrectamente la lista.
3. Ninguna pantalla puede mostrar simultáneamente el título vanilla y el título Jobs.
4. No debe quedar texto blanco puro en búsquedas, cabeceras o listados oscurecidos por Jobs.
5. Las pestañas de Ajustes deben leerse completas sin puntos suspensivos a escala GUI normal.
6. `01 / 05` no puede tocar pestañas, títulos ni descripciones.
7. La categoría Nivel no puede dibujar notas encima de `Shift date` ni del botón de cierre.
8. `jobsmenu-musica` y `jobsmenu-musica-activa`, como carpeta o ZIP, deben desaparecer tras entrar al menú.
9. Los botones, previews, ping, listas y acciones vanilla deben conservar sus hitboxes y lógica.
10. Sólo `main` permanece al cerrar la entrega y el despliegue sigue limitado a `test-1`.
