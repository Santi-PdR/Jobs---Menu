# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menus con una interfaz administrativa/industrial propia, audio de sesion y fondos de nivel, conservando la logica vanilla/Forge cuando reconstruirla perjudicaria compatibilidad.

| Campo | Valor |
|---|---|
| Version | **0.38.0** |
| Artefacto | **`jobsmenu-0.38.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0-31)** |

## 0.38.0 · Optimización global

0.38.0 es una pasada de rendimiento y mantenimiento sobre el mod completo sin ampliar su alcance a gameplay.

- Las listas vanilla/Forge tematizadas ya no recorren por reflection todos los fields de la Screen en cada frame: los fields se descubren una vez por clase y las listas se cachean sólo mientras la pantalla está viva.
- Las scrollbars Jobs quedan deduplicadas por frame aunque una Screen propia y `Render.Post` pidan dibujarlas en la misma pasada.
- El lifecycle libera la caché de listas al cerrar una pantalla y el seguimiento de hover vanilla conserva sólo los botones actualmente enfocados.
- Los fondos de imagen aplican filtrado lineal una vez por objeto de textura; F3+T vuelve a aplicarlo automáticamente sólo si Minecraft crea otra textura.
- La nota administrativa reutiliza el `Component` mientras no cambia de aviso y resuelve fechas especiales como máximo una vez por minuto.
- `PulidoInterfazJobs` comparte un único reloj por pasada y combina conteo de jerarquía + foco en un solo recorrido de widgets.
- Los consumidores de escena/audio comparten el mismo snapshot de `RotacionNiveles` cuando lo solicitan dentro del mismo milisegundo.
- Multiplayer precalcula rótulos y reutiliza tooltips en vez de reconstruirlos continuamente durante render.
- `PielVanillaJobs` resuelve Alto contraste una sola vez por pasada de piel.
- **Bajo consumo** reduce de verdad draw calls en viñeta, profundidad, rebote y humedad; el modo normal mantiene la misma composición.
- El JAR se genera con orden reproducible y sin timestamps variables en sus entradas/manifest.
- `tools/verificar_optimizacion.py` impide que estos caminos calientes vuelvan silenciosamente al comportamiento anterior.

Los contratos de 0.35–0.37 permanecen: retorno contextual de servidor, F5 con selección por IP, sonido Jobs, cero transiciones durante gameplay y Video Settings completamente vanilla.

## Fondos 18-31

- Los archivos entregados por el usuario viven directamente en `src/main/resources/assets/jobsmenu/textures/backgrounds/` como `nivel18.jpg` ... `nivel31.jpg`.
- No se usa ZIP, Base64 ni reconstruccion durante Gradle: las imagenes del repositorio son los recursos reales del JAR.
- Los JPG se conservan en **1920x1080, 16:9**.
- `Nivel.CATALOGO` contiene 32 entradas.
- Los niveles 10-17 mantienen su contrato historico y siguen totalmente estaticos.
- Los niveles 18-31 pueden recibir una respiracion de camara muy leve y no destructiva. Movimiento reducido, Bajo consumo o escena quieta la desactivan.
- El verificador de fondos valida PNG 10-17 y JPEG 18-31 directamente desde recursos.

Asignacion tecnica: [`docs/FONDOS_18_31.md`](docs/FONDOS_18_31.md).

## Musica

Catalogo actual:

1. Absurdism
2. REQUIEM - Forsaken OST
3. Upon the Hill V2

La musica Jobs mantiene continuidad entre subpantallas y ejecuta hard-stop al entrar a gameplay. `M` controla mute y `N` solicita la siguiente pista. El build no descarga audio de terceros.

## Contratos de fondos

### Niveles 10-17

- PNG existentes intactos.
- Sin zoom, paneo, parallax, flicker, deformacion ni movimiento interno.
- Se permiten fades, apagones y overlays globales que no alteren la imagen.

### Niveles 18-31

- JPG originales del repositorio, 1920x1080.
- Cover centrado y filtrado lineal.
- Movimiento opcional de camara de intensidad muy baja, aplicado en render y sin reescribir el archivo.
- Movimiento reducido y Bajo consumo fuerzan imagen quieta.
- No se agregan objetos falsos ni se modifica la composicion del JPG.

## Interfaz

Jobs usa dos familias de superficie:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Resource Packs.

Reglas permanentes:

- ningun titulo vanilla debe sangrar bajo una cabecera Jobs;
- ningun control visible puede tener hitbox invisible superpuesto;
- listas complejas conservan logica real de Minecraft/Forge;
- rojo reservado a Executores;
- accesibilidad y compatibilidad tienen prioridad sobre decoracion;
- ayudas de teclado solo anuncian atajos realmente implementados;
- el main reserva su pie para el nombre y la nota del nivel, no para una barra global de atajos;
- gameplay nunca recibe animaciones de transicion Jobs, aunque la pantalla abierta sea Pausa o Config Jobs.

## Servidor oficial

Entrada fijada unica:

`JobsDosh.exaroton.me:56477`

Nombre localizado: `Jobs Official Server` / `Servidor oficial de Jobs`.

## Build y entrega

GitHub Actions ejecuta Java 17, politica de version, validacion de fondos, verificadores estaticos, contratos UI/musica, continuidad de Multiplayer/documentacion, contratos de optimizacion y Forge build. Solo `main` verde publica `dev-latest`.

El test normal instala el JAR certificado en:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

El pipeline no sustituye una prueba visual dentro de Minecraft; GUI Scale, audio, navegacion, fondos y Bajo consumo deben revisarse manualmente despues del deploy.

## Documentacion

- [`docs/README.md`](docs/README.md): índice de documentación vigente e histórica.
- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro vigente.
- [`CHANGELOG.md`](CHANGELOG.md): historial de versiones.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y pruebas pendientes.
- [`docs/AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md`](docs/AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md): alcance técnico de esta pasada.
- [`docs/FONDOS_18_31.md`](docs/FONDOS_18_31.md): catalogo de recursos.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalacion certificada.
- [`docs/musica.md`](docs/musica.md): catalogo y lifecycle musical.
