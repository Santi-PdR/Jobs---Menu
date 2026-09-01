# Música del menú

## Estado en 0.15.0

Jobs incluye su pista de menú directamente en:

```text
src/main/resources/assets/jobsmenu/sounds/musica/defecto.ogg
```

El JAR registra el recurso mediante `sounds.json`. Instalar el mod es suficiente: no hace falta crear, activar ni mantener ningún resource pack musical.

La referencia **REQUIEM — Forsaken OST**, de Emmy Z, no se incorpora al JAR sin autorización escrita de redistribución. La carpeta `music/` documenta esa separación y no participa en `processResources`.

## Contrato de audio

- `GestorMusica` mantiene una sola instancia durante toda la visita al menú.
- La pista no se reinicia al cambiar de Nivel, abrir Opciones, Mods o Resource Packs.
- Usa `SoundSource.MASTER`: la gobiernan Maestro de Minecraft, volumen de música Jobs y volumen maestro del aviso.
- No depende del deslizador Música vanilla.
- La tecla `M` conserva el último volumen del aviso y alterna silencio.
- Al entrar a un mundo o cerrar la visita se retiran música y camas ambientales.
- F3+T y una reconstrucción de `SoundEngine` invalidan la instancia anterior antes de crear otra.

## Migración desde versiones anteriores

Versiones previas podían generar:

```text
resourcepacks/jobsmenu-musica-activa/
```

0.15.0 elimina ese sistema. `LimpiezaRecursosLegados` deselecciona y borra únicamente esa carpeta en el primer acceso al menú. No modifica otros resource packs ni la carpeta de origen que pudiera haber creado el usuario.

## Sustituir la pista durante el desarrollo

Sólo para una pista propia o con licencia de redistribución compatible:

1. reemplazar `defecto.ogg` por OGG Vorbis compatible con Minecraft;
2. conservar el evento `musica.tema` de `sounds.json`;
3. actualizar título, autor y licencia cuando corresponda;
4. ejecutar verificadores y build con Java 17.

Un archivo externo no se convierte automáticamente en resource pack. Esta decisión evita que Jobs vuelva a aparecer como paquete independiente en la pantalla de recursos.

## Pruebas

La validación estática comprueba presencia y registro. La aceptación manual debe confirmar reproducción, continuidad, volumen, F3+T, Alt+Tab, entrada a mundo y ausencia del pack legado.
