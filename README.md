# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Sustituye el flujo de menús por la interfaz de Jobs: expedientes administrativos, recintos, audio continuo y navegación propia sin reimplementar la lógica sensible de Minecraft cuando conservarla mejora compatibilidad.

| | |
|---|---|
| Versión | **0.22.1** |
| Artefacto | **`jobsmenu-0.22.1.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **18 (0–17)** |

## 0.22.1 · Main screen, pausa, transiciones y secretos

0.22.1 refuerza el impacto visual inmediato del mod sin reemplazar ni animar los PNG de niveles altos.

Cambios principales:

- **Main screen** con HUD lateral contextual, lectura de turno/nivel/estado, atajos y mejor jerarquía alrededor del expediente.
- **Pausa Jobs** con panel de contexto adicional, lectura LOCAL/SERVER y más profundidad visual sin ocultar el mundo real.
- **Atmósfera compartida** con registros y barridos globales muy sutiles que nunca mueven el background.
- **Transiciones** más suaves y con mayor lectura física de expediente.
- **Easter eggs** ampliados con variantes de sesión muy raras, sin red, gameplay ni recompensas.
- **Fondos PNG 10–17 intactos**: no se reemplazan, no se deforman y no reciben movimiento propio. Sí pueden participar de fades/apagones/transiciones globales que pertenecen al menú, no a la imagen.
- Auditoría específica en `docs/AUDITORIA_VISUAL_0.22.1.md`.

## Fondos 10–17

Los PNG **no fueron reemplazados ni editados**. Mantienen filtrado lineal al escalarse para evitar pixelado. No reciben zoom, paneo, parallax, motas, foreground dinámico, flicker ni deformación. Los fades y apagones globales de transición sí están permitidos porque no modifican la geometría o el contenido del PNG.

Si en el futuro se agregan niveles 18–19 como PNG, deben heredar este mismo contrato.

## Estado de prueba

GitHub Actions certifica código, recursos, políticas, Java 17 y Forge build. **No sustituye una prueba visual dentro de Minecraft**. La entrega debe probarse manualmente en `test-1` siguiendo `docs/checklist-manual.md` cuando sea posible.

## Reglas de interfaz

Jobs usa dos familias de superficie:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Resource Packs.

Contratos permanentes:

- ningún título vanilla puede sangrar debajo de una cabecera Jobs;
- ningún control visible puede tener un hitbox vanilla invisible superpuesto;
- búsqueda, foco, portapapeles, rueda, click y drag deben seguir usando la lógica real de Minecraft/Forge;
- rojo reservado a Executores;
- escena y UI usan paletas separadas;
- movimiento reducido y Bajo consumo tienen prioridad sobre decoración;
- los PNG 10–17 no reciben movimiento propio;
- las pantallas externas complejas no se reconstruyen por estética.

## Servidor oficial

La única entrada fijada por el mod es:

`JobsDosh.exaroton.me:56477`

Se localiza como `Jobs Official Server` / `Servidor oficial de Jobs`, queda primera, se deduplica y se protege frente a edición/borrado desde Jobs. El servidor legado `Ghoul Outbreak` no debe reaparecer.

## Ciclo de sesión y audio

`SesionMenu` representa una visita completa al menú. Abrir Options, Mods, Recursos u otra subpantalla no crea otra sesión ni reinicia música/ambiente.

Al entrar a gameplay:

1. se cierra la sesión Jobs;
2. música y camas ambientales se detienen inmediatamente;
3. gameplay no recibe audio del menú.

Al salir de mundo, servidor, kick o desconexión, el flujo vuelve a `PantallaNivel` cuando el menú propio está habilitado.

La música Jobs usa Maestro + volumen Jobs; no depende del slider Música vanilla.

## Música futura

La integración de una segunda pista sigue preparada mediante una sola subida a:

`music/menu_nueva.ogg`

El workflow `Integrar OGG subido` valida Vorbis, normaliza loudness/true peak, genera el recurso final, registra el tema, ejecuta verificaciones y compila con Java 17. Si algo falla, no publica la integración.

## Build y entrega

GitHub Actions ejecuta:

1. Java 17;
2. política de versión/JAR;
3. validación PNG 10–17;
4. auditoría estática;
5. contratos UI/música;
6. `./gradlew build --stacktrace --no-daemon`;
7. publicación de **`jobsmenu-0.22.1.jar`** en `dev-latest` sólo desde `main`.

La release rodante conserva un único JAR versionado. `jobsmenu-latest.jar` está prohibido.

## Despliegue

La prueba normal no compila localmente. El PowerShell canónico de `docs/DESPLIEGUE.md` descarga el único JAR certificado de `dev-latest`, lo valida y reemplaza la versión previa exclusivamente en:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

## Documentación vigente

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro actual.
- [`CHANGELOG.md`](CHANGELOG.md): cambios por versión.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y pruebas manuales pendientes.
- [`docs/AUDITORIA_VISUAL_0.22.1.md`](docs/AUDITORIA_VISUAL_0.22.1.md): pase visual de esta entrega.
- [`docs/AUDITORIA_VISUAL_0.22.0.md`](docs/AUDITORIA_VISUAL_0.22.0.md): pase visual anterior.
- [`docs/AUDITORIA_0.21.0_90_MEJORAS.md`](docs/AUDITORIA_0.21.0_90_MEJORAS.md): pase transversal anterior.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalación certificada en `test-1`.
- [`docs/musica.md`](docs/musica.md): catálogo y lifecycle musical.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): convivencia con otros mods.
