# Puntos de recuperación de la evolución

Esta carpeta no contiene copias del árbol: los backups completos son **tags de
git**, que son copias exactas e inmutables de un estado y viajan con la rama a
GitHub. Un tag es verificable (`git checkout <tag>`), no ocupa espacio en el
árbol y no puede desincronizarse.

| Tag | Cuándo se creó | Qué contiene |
|---|---|---|
| `backup-A-inicial-0.10.0` | Antes de tocar nada | Estado inicial 0.10.0 (commit `811586e`) |
| `backup-B-tecnica-ui-sonido-config` | Al terminar la etapa técnica/UI/sonido/configuración | Mejoras de lifecycle, robustez, UI/UX, audio y configuración, antes de la etapa artística |
| `backup-C-final` | Punto C registrado por una evolución anterior | Versión 0.11.0 completa (registro histórico; no se reescribe) |
| `backup-C-final-evolucion6` | Estado final de la Evolución 6: backgrounds, segunda auditoría y documentación | Evolución 6 completa sobre 0.10.0; build con Java 17 y prueba en Minecraft pendientes (ver `KNOWN_ISSUES.md`) |

Reglas de la evolución respetadas:

1. Ninguna copia de backups dentro de `mods/`.
2. Los backups existentes no se borran ni se reescriben.
3. Los tres puntos de recuperación se suben a GitHub junto con la rama.
4. Backup B se crea solo después de un commit estable y del verificador en verde.
