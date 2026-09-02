# Auditoría 0.16.2 — Interfaz y ciclo de sesión

Esta auditoría convierte las capturas de aceptación de `0.16.1` y los fallos de
sesión reportados después del despliegue en contratos verificables.

## Diagnóstico

| Área | Causa real | Corrección 0.16.2 |
|---|---|---|
| Tono amarillo | Cuatro pantallas aplicaban `RenderSystem.setShaderColor` con menos azul que rojo/verde sobre todo el render vanilla. | Atenuación RGB gris uniforme; la paleta Jobs usa hueso frío, grafito y gris verdoso. |
| Títulos dobles | El texto vanilla y el reemplazo Jobs compartían profundidad de render. | Banda opaca y cabecera Jobs trasladadas a un plano frontal. |
| Options residual | `options.title` se dibujaba como sección en la misma franja que `Notice settings`. | Eliminados ambos rótulos de sección redundantes. |
| Buscador de Idioma | `EditBox` dibujaba texto/hint con su estilo y sombra vanilla. | Render de campo centrado, sin sombra y con tinta secundaria. |
| Servidor Ghoul | Permanecía guardado en `servers.dat`; Jobs sólo añadía/movía su propio servidor. | Migración específica que elimina `Ghoul Outbreak`, nombres oficiales falsos y duplicados de la IP Jobs. |
| Retorno vanilla | `prepararSalidaAlTitulo()` autorizaba deliberadamente una visita al título vanilla. | Login/logout marcan el ciclo; el primer destino de retorno se reconduce a `PantallaNivel`. |
| Música dentro del juego | `SesionMenu.cerrar()` permitía que la música completara un fundido. | Corte inmediato de volumen/instancia en login, logout y tick con nivel activo. |

## Contratos de aceptación

1. No se usa ningún multiplicador RGB sepia/amarillo sobre pantallas completas.
2. Mundos y Multiplayer muestran una sola cabecera localizada.
3. Mods no muestra título vanilla ni agrega otro sobre el panel de información.
4. Options no dibuja `Options` ni `JOBS` detrás del botón de Config.
5. El buscador de Idioma está centrado y se dibuja una sola vez, sin sombra.
6. `JobsDosh.exaroton.me:56477` existe una sola vez y ocupa el primer renglón.
7. `Ghoul Outbreak` no permanece en `servers.dat` después de abrir Multiplayer.
8. Entrar en singleplayer o multiplayer detiene música y camas del menú sin cola audible.
9. Salir de mundo, servidor o kick recupera el menú Jobs; el mensaje de desconexión puede mostrarse antes.
10. ES/EN, fondos 10–17, Forge 1.20.1 y el artefacto versionado conservan sus validadores existentes.

## Prueba mínima dentro del juego

- Abrir Options, Idioma, Mundos, Multiplayer, Mods y Resource Packs en GUI Scale 2–4.
- Entrar a un mundo local, esperar cinco segundos y confirmar silencio total del menú.
- Volver, entrar al servidor Jobs y repetir la prueba.
- Forzar desconexión/kick y pulsar Volver: el destino final debe ser `PantallaNivel`.
- Reabrir Multiplayer dos veces y confirmar una sola IP Jobs y ausencia de Ghoul.

