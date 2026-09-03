# Auditoría de correcciones 0.26.0

Pase motivado por capturas reales dentro de Minecraft.

- SHIFT CONTROL: retirado, no renombrado.
- JOBS / LEVEL duplicado: retirado.
- Fecha `%s`: corregida a tres argumentos posicionales.
- Mods: se conserva el layout real de Forge y ya no se fuerza `updateSize`.
- Resource Packs: se conservan las dos listas reales y ya no se fuerza `updateSize`.
- Worlds y Multiplayer: ESC/Volver apuntan directamente al padre Jobs.
- Atajo N: llama a `GestorMusica.adelantarPista()` y se anuncia en MAIN.
- Absurdism: runtime reemplazado con la nueva fuente subida en `music/`.
- Avisos: 20 textos ES/EN reescritos para mantener lógica administrativa.
- Nivel 1: `DepositoNuevo` sustituye a `Nave`; `Nave` queda intacta y copiada en `backups/nivel1/Nave_0.25.0.java.txt`.
- PNG 10–17: sin cambios.
