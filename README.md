# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Sustituye el flujo de menús por la interfaz de Jobs: expedientes administrativos, recintos, audio continuo y navegación propia sin reimplementar la lógica sensible de Minecraft cuando conservarla mejora compatibilidad.

| | |
|---|---|
| Versión | **0.26.0** |
| Artefacto | **`jobsmenu-0.26.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **18 (0–17)** |

## 0.26.0 · Correcciones de capturas y nuevo Depósito

- Retirado por completo el panel `SHIFT CONTROL`; tampoco queda el `JOBS / LEVEL` técnico flotando sobre el fondo.
- La barra inferior del main incorpora **N** junto a `1-4`, `F`, `M`, `TAB` y `ENTER`; N cambia realmente de pista.
- Mods vuelve a usar la geometría real de Forge, por lo que la lista de mods no se redimensiona ni se tapa.
- Resource Packs conserva las dos listas vanilla con sus posiciones originales, evitando la superposición vista en captura.
- Mundos y Multiplayer vuelven al padre Jobs con una sola pulsación de ESC/Volver.
- Corregido el texto literal `%s` en la fecha del turno.
- Reescritos los 20 avisos rotativos ES/EN para mantener reglas coherentes de la instalación.
- Absurdism de runtime usa ahora el nuevo OGG subido en `music/`; REQUIEM y Upon the Hill V2 siguen siendo pistas separadas.
- Nivel 1 · Depósito usa un renderer procedural nuevo con muelles, racks, pallets, vigas, luminarias y montacargas. El anterior queda respaldado.

## 0.25.0 · Catálogo musical real y control de sesión

- Catálogo real de **3 pistas**: Absurdism, REQUIEM y Upon the Hill V2.
- REQUIEM y Upon the Hill se empaquetan desde los OGG autorizados ya presentes en `music/`; el build no descarga audio externo.
- Cada pista tiene evento propio, `stream: true`, identidad independiente y crédito contextual.
- El crédito del main ya no muestra REQUIEM mientras suena otra canción.
- La visita comienza en una pista aleatoria y los cambios evitan repetir inmediatamente la pista actual.
- Rotación musical aproximada de 2–4 minutos con crossfade.
- Tecla **N** en el main para pasar manualmente a otra pista; durante un crossfade se rechaza el doble salto.
- El HUD muestra la pista dominante actual y la ayuda `N>NEXT`.
- Mute, ducking, recarga de recursos, continuidad por subpantallas y hard-stop en gameplay siguen vigentes.

## 0.24.0 · Navegación contextual y controles de tercera generación

0.24.0 es un pase transversal centrado en hacer que el mod sea más fácil de leer, navegar y entender sin convertir Jobs en un dashboard futurista.

Cambios principales:

- **Instrumentación contextual 2.0**: ruta de las últimas pantallas, título contextual, tiempo de visita, número de pantallas visitadas, volumen Jobs, modo de entrada KEY/PTR, tipo de control y posición dentro del conjunto de controles activos.
- **Barra inferior contextual** que muestra el control enfocado/hover, atajos reales de la pantalla y estado de navegación sin capturar input.
- **Atajos numéricos reales**: 1–4 activan los cuatro renglones del main; en pausa 1–2 permiten Reanudar/Condiciones. No actúan mientras se escribe en un campo de texto.
- **Main screen** con HUD ampliado: tiempo hasta traslado, tiempo de sesión, volumen maestro Jobs, estado MUTE, LEDs rotulados y progreso de estancia más preciso.
- **Controles vanilla/Forge** con doble registro, sombras, foco de teclado distinto del hover, estados disabled más claros, sliders con escala 0/50/100 y campos de texto con marco de foco reforzado.
- **Scrollbars Jobs** con canal de progreso, escala 0/25/50/75/100, topes, chevrons, cursor de posición externo y tirador de mayor profundidad.
- **84 mejoras visibles/perceptibles** documentadas en `docs/AUDITORIA_0.24.0_84_MEJORAS.md`.
- **Fondos PNG 10–17 intactos**: no se reemplazan, no se deforman y no reciben movimiento propio. Fades, apagones y overlays globales siguen permitidos si no mueven ni alteran la imagen.

## Fondos 10–17

Los PNG **no fueron reemplazados ni editados**. Mantienen filtrado lineal al escalarse para evitar pixelado. No reciben zoom, paneo, parallax, motas, foreground dinámico, flicker ni deformación. Los fades, apagones y overlays de navegación sí están permitidos porque no modifican la geometría o el contenido del PNG.

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
- las pantallas externas complejas no se reconstruyen por estética;
- ayudas visuales sólo anuncian atajos que existen realmente.

## Servidor oficial

La única entrada fijada por el mod es:

`JobsDosh.exaroton.me:56477`

Se localiza como `Jobs Official Server` / `Servidor oficial de Jobs`, queda primera, se deduplica y se protege frente a edición/borrado desde Jobs. El servidor legado `Ghoul Outbreak` no debe reaparecer.

## Ciclo de sesión y audio

`SesionMenu` representa una visita completa al menú. Abrir Options, Mods, Recursos u otra subpantalla no crea otra sesión ni reinicia música/ambiente. En 0.24.0 también mantiene un reloj y un contador local de pantallas para la instrumentación visual; esos datos no se guardan ni salen por red.

Al entrar a gameplay:

1. se cierra la sesión Jobs;
2. música y camas ambientales se detienen inmediatamente;
3. gameplay no recibe audio del menú.

Al salir de mundo, servidor, kick o desconexión, el flujo vuelve a `PantallaNivel` cuando el menú propio está habilitado.

La música Jobs usa Maestro + volumen Jobs; no depende del slider Música vanilla.

## Música

El catálogo empaquetado es **Absurdism + REQUIEM + Upon the Hill V2**. Las fuentes autorizadas de REQUIEM y Upon the Hill se conservan en `music/`; los recursos de runtime viven en `assets/jobsmenu/sounds/musica/`. No existe descarga de audio durante build o ejecución.

## Build y entrega

GitHub Actions ejecuta:

1. Java 17;
2. política de versión/JAR;
3. validación PNG 10–17;
4. auditoría estática;
5. contratos UI/música;
6. `./gradlew build --stacktrace --no-daemon`;
7. publicación de **`jobsmenu-0.26.0.jar`** en `dev-latest` sólo desde `main`.

La release rodante conserva un único JAR versionado. `jobsmenu-latest.jar` está prohibido.

## Despliegue

La prueba normal no compila localmente. El PowerShell canónico de `docs/DESPLIEGUE.md` descarga el único JAR certificado de `dev-latest`, lo valida y reemplaza la versión previa exclusivamente en:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

## Documentación vigente

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro actual.
- [`CHANGELOG.md`](CHANGELOG.md): cambios por versión.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y pruebas manuales pendientes.
- [`docs/AUDITORIA_AUDIO_0.26.0.md`](docs/AUDITORIA_AUDIO_0.26.0.md): catálogo musical y mejoras de sesión.
- [`docs/AUDITORIA_0.24.0_84_MEJORAS.md`](docs/AUDITORIA_0.24.0_84_MEJORAS.md): pase transversal anterior.
- [`docs/AUDITORIA_0.23.0_72_MEJORAS.md`](docs/AUDITORIA_0.23.0_72_MEJORAS.md): pase transversal anterior.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalación certificada en `test-1`.
- [`docs/musica.md`](docs/musica.md): catálogo y lifecycle musical.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): convivencia con otros mods.
