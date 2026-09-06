# Índice de documentación — Jobs Menu

Este directorio contiene tanto el **contrato vigente** como auditorías de versiones anteriores. Una auditoría histórica explica cómo era el mod en ese momento; no debe usarse para revertir decisiones posteriores.

## Documentación vigente

Para trabajar sobre `main`, leer en este orden:

1. [`../CONTEXTO.md`](../CONTEXTO.md) — fuente maestra de alcance, reglas duras y estado actual.
2. [`../README.md`](../README.md) — resumen operativo de la entrega vigente.
3. [`../KNOWN_ISSUES.md`](../KNOWN_ISSUES.md) — riesgos reales y límites que CI no puede certificar.
4. [`../CHANGELOG.md`](../CHANGELOG.md) — evolución por versión.
5. [`checklist-manual.md`](checklist-manual.md) — aceptación dentro de Minecraft.
6. [`compatibilidad.md`](compatibilidad.md) — fronteras con vanilla/Forge, gameplay y otros mods.
7. [`DESPLIEGUE.md`](DESPLIEGUE.md) — flujo de build/release/instalación.
8. [`musica.md`](musica.md) — catálogo, créditos e identidad/lifecycle musical.
9. [`FONDOS_18_31.md`](FONDOS_18_31.md) — asignación de los JPG directos 18–31.

## Contratos que no deben regredir

- Video Settings permanece vanilla y sin capas Jobs.
- Chat, inventario, contenedores y UI normal de gameplay quedan fuera de Jobs.
- Pausa/Config Jobs pueden mantener tema y feedback breve, pero no música/ambiente ni transiciones durante gameplay.
- Multiplayer conserva padre Jobs para ESC/Cancelar, conexión, error y retorno tras servidor.
- F5/Actualizar conserva selección por IP sin reutilizar Entries viejas.
- El main no muestra la antigua barra inferior de atajos.
- PNG 10–17 son estáticos; JPG 18–31 sólo admiten respiración sutil/desactivable.
- Listas/scrollbars mantienen los contratos de optimización 0.38.
- `musica_creditada.txt` representa las tres pistas acreditadas actuales.
- Resource reload usa generaciones y no pierde una recarga posterior.
- Navegar entre pantallas Jobs de una visita no reinicializa la sesión.
- **La música Jobs no usa fallback a `minecraft:music.menu`.**
- El catálogo musical se construye una sola vez por JVM.
- El hard-stop musical ordena también el corte directo al `SoundManager`.
- El JAR usa orden reproducible y no introduce timestamps variables de build.
- `gradle.properties` es la fuente de verdad de versión y `main` es la única rama entregable.

## Auditoría vigente de la entrega

- [`AUDITORIA_0.40.0_IDENTIDAD_MUSICAL.md`](AUDITORIA_0.40.0_IDENTIDAD_MUSICAL.md) — eliminación del fallback vanilla, catálogo estático y hard-stop directo.
- [`AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md`](AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md) — créditos, generaciones de resource reload, sesión y diagnóstico.
- [`AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md`](AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md) — optimización global y build reproducible.
- [`AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md`](AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md) — continuidad F5 y documentación.
- [`AUDITORIA_0.36.0_MULTIPLAYER_Y_GAMEPLAY.md`](AUDITORIA_0.36.0_MULTIPLAYER_Y_GAMEPLAY.md) — cierre de Multiplayer y cero transiciones en gameplay.
- [`AUDITORIA_0.35.0_AUDIO_Y_RETORNO.md`](AUDITORIA_0.35.0_AUDIO_Y_RETORNO.md) — clicks/hover Jobs y retorno contextual.

## Histórico

Los demás archivos `AUDITORIA_*.md`, revisiones, catálogos y planes son evidencia histórica. Si contradicen `CONTEXTO.md`, `KNOWN_ISSUES.md`, `compatibilidad.md`, `musica.md` o una auditoría más reciente, manda el documento vigente más nuevo.

En particular, cualquier mención histórica a fallback musical `MUSIC_MENU` queda reemplazada desde 0.40.0 por el contrato **registro Jobs o silencio/reintento**.
