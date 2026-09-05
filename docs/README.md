# Índice de documentación — Jobs Menu

Este directorio contiene tanto el **contrato vigente** como auditorías de versiones anteriores. Una auditoría histórica explica cómo era el mod en ese momento; no debe usarse para revertir decisiones posteriores.

## Documentación vigente

Para trabajar sobre `main`, leer en este orden:

1. [`../CONTEXTO.md`](../CONTEXTO.md) — fuente maestra de alcance, reglas duras y estado actual.
2. [`../README.md`](../README.md) — resumen operativo de la entrega vigente.
3. [`../KNOWN_ISSUES.md`](../KNOWN_ISSUES.md) — riesgos reales y límites que CI no puede certificar.
4. [`../CHANGELOG.md`](../CHANGELOG.md) — evolución por versión.
5. [`checklist-manual.md`](checklist-manual.md) — aceptación dentro de Minecraft.
6. [`compatibilidad.md`](compatibilidad.md) — fronteras con vanilla/Forge, gameplay, otros mods y caminos calientes.
7. [`DESPLIEGUE.md`](DESPLIEGUE.md) — flujo de build/release/instalación.
8. [`musica.md`](musica.md) — catálogo y lifecycle de audio musical.
9. [`FONDOS_18_31.md`](FONDOS_18_31.md) — asignación de los JPG directos 18–31.

## Contratos que no deben regredir

- Video Settings permanece vanilla y sin capas Jobs.
- Chat, inventario, contenedores y UI normal de gameplay no reciben transición, piel, banda ni reemplazo global de clicks.
- Pausa/Config Jobs pueden mantener tema y feedback breve, pero no música/ambiente ni transiciones durante gameplay.
- Multiplayer conserva padre Jobs para ESC/Cancelar, conexión, error y retorno tras servidor.
- F5/Actualizar reconstruye Multiplayer Jobs directamente y conserva la selección de un servidor guardado por IP sin reutilizar una Entry vieja.
- El main no muestra la antigua barra inferior de atajos sobre el nombre del nivel.
- PNG 10–17 son estáticos; JPG 18–31 sólo admiten respiración de cámara sutil y desactivable.
- Las listas Jobs cachean reflection/instancias y una scrollbar no se dibuja dos veces en el mismo frame.
- El filtrado de fondos de imagen se aplica por instancia de textura, no por frame.
- Bajo consumo reduce trabajo real de render sin modificar el modo normal.
- El JAR usa orden reproducible y no introduce timestamps variables de build.
- `gradle.properties` es la fuente de verdad de versión y `main` es la única rama entregable.

## Auditoría vigente de la entrega

- [`AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md`](AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md) — optimización de listas/reflection, UI, texturas, escena/audio, Bajo consumo y build reproducible.
- [`AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md`](AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md) — continuidad F5, feedback de recarga, documentación y CI.
- [`AUDITORIA_0.36.0_MULTIPLAYER_Y_GAMEPLAY.md`](AUDITORIA_0.36.0_MULTIPLAYER_Y_GAMEPLAY.md) — cierre directo de Multiplayer y cero transiciones durante gameplay.
- [`AUDITORIA_0.35.0_AUDIO_Y_RETORNO.md`](AUDITORIA_0.35.0_AUDIO_Y_RETORNO.md) — clicks/hover Jobs y retorno contextual después de servidor.

Estas auditorías se complementan: 0.38.0 optimiza los caminos calientes sin revertir los contratos funcionales de 0.35–0.37.

## Histórico

Los demás archivos `AUDITORIA_*.md`, revisiones, catálogos y planes permanecen como evidencia del desarrollo. Pueden mencionar versiones, diseños o restricciones ya reemplazados.

Regla de interpretación: si un documento histórico contradice `CONTEXTO.md`, `KNOWN_ISSUES.md`, `compatibilidad.md` o una auditoría más reciente, **manda el documento vigente más nuevo**.

`AUDITORIA_SEGUNDA.md` está conservado específicamente como referencia histórica y ya advierte que el antiguo fallback de gestos de UI a `minecraft:ui.button.click` dejó de ser válido desde 0.31.0.
