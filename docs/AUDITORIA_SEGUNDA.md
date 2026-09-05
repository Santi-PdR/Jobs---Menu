# Segunda auditoría integral — HISTÓRICA (Jobs Menu 0.10.0)

> **Documento histórico, no contrato vigente.** Esta auditoría corresponde al 29 de agosto de 2026, rama `arena/01a04e24-jobs-menu`, commit `8e5c0ef`. Para el comportamiento actual consultar `CONTEXTO.md`, `KNOWN_ISSUES.md`, `docs/compatibilidad.md`, `docs/musica.md` y la auditoría de la versión vigente.

## Por qué se conserva

Este archivo deja constancia de una etapa antigua del proyecto en la que se auditaron de forma estática el lifecycle del menú, recursos, idiomas, audio, accesibilidad y compatibilidad con Forge 1.20.1. En ese momento el build posterior a `8e5c0ef` y la prueba dentro de Minecraft todavía estaban pendientes.

No debe usarse para decidir cómo funciona el runtime actual: desde 0.10.0 cambiaron la arquitectura de pantallas, el catálogo de fondos, la música, los verificadores, la navegación Multiplayer y el contrato de sonidos de interfaz.

## Corrección histórica importante

En 0.10.0 los gestos de UI todavía podían caer en `UI_BUTTON_CLICK` vanilla. **Eso dejó de ser válido desde 0.31.0.** El contrato actual prohíbe ese fallback: los gestos Jobs se resuelven con sonidos Jobs o fallan en silencio, y los clicks vanilla de controles preservados se sustituyen dentro de superficies Jobs. El verificador `tools/verificar_ui_musica.py` falla si `MezclaAudio` vuelve a usar `SoundEvents.UI_BUTTON_CLICK` como fallback.

La música sí conserva un fallback defensivo independiente a `MUSIC_MENU` cuando el registro de una pista no puede resolverse; no debe confundirse con el contrato de click/hover.

## Estado que describía esta auditoría

- Plataforma objetivo: Minecraft 1.20.1, Forge 47.4.0, Java 17.
- `SesionMenu` ya separaba la visita del menú de una `Screen` concreta.
- `ScreenEvent.Opening` ya se utilizaba para sustituciones conservadoras.
- `RecargaRecursosCliente` ya invalidaba audio después de recargas.
- Había una matriz inicial de mejoras para diez escenarios procedurales.
- Los controles de movimiento reducido y destellos reducidos ya formaban parte del contrato de accesibilidad.
- El build y la prueba real de Minecraft de aquella versión seguían pendientes cuando se escribió el documento original.

## Qué reemplaza sus conclusiones hoy

- `CONTEXTO.md`: fuente de verdad del contrato vigente.
- `README.md`: versión, artefacto y resumen actual.
- `KNOWN_ISSUES.md`: riesgos que todavía necesitan prueba real.
- `docs/compatibilidad.md`: fronteras entre Jobs, vanilla, Forge y gameplay.
- `docs/musica.md`: catálogo y lifecycle de música/audio actual.
- `docs/checklist-manual.md`: aceptación dentro de Minecraft.
- `docs/AUDITORIA_0.35.0_AUDIO_Y_RETORNO.md`: revisión específica de sonidos y retorno después de gameplay en 0.35.0.

La información detallada de 0.10.0 sigue disponible en el historial Git de este archivo. Mantener aquí afirmaciones técnicas ya superadas sólo podía inducir regresiones, por lo que este documento queda reducido a índice histórico y advertencia de compatibilidad.
