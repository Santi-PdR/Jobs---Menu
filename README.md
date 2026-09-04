# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menus con una interfaz administrativa/industrial propia, audio de sesion y fondos de nivel, conservando la logica vanilla/Forge cuando reconstruirla perjudicaria compatibilidad.

| Campo | Valor |
|---|---|
| Version | **0.27.0** |
| Artefacto | **`jobsmenu-0.27.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0-31)** |

## 0.27.0 · Fondos 18-31

- Se agregan **14 niveles nuevos**, del 18 al 31.
- Los archivos entregados por el usuario viven directamente en `src/main/resources/assets/jobsmenu/textures/backgrounds/` como `nivel18.jpg` ... `nivel31.jpg`.
- No se usa ZIP, Base64 ni reconstruccion durante Gradle: las imagenes del repositorio son los recursos reales del JAR.
- Los JPG se conservan en **1920x1080, 16:9**.
- `Nivel.CATALOGO` llega ahora a 32 entradas.
- ES/EN incluyen nombre y tres notas propias para cada nivel nuevo.
- Los niveles 10-17 mantienen su contrato historico y siguen totalmente estaticos.
- Los niveles 18-31 pueden recibir una respiracion de camara muy leve y no destructiva. Movimiento reducido, Bajo consumo o escena quieta la desactivan.
- El verificador de fondos valida PNG 10-17 y JPEG 18-31 directamente desde recursos.

Asignacion completa: [`docs/FONDOS_18_31.md`](docs/FONDOS_18_31.md).

## Estado heredado de 0.26.0

- `SHIFT CONTROL` y el `JOBS / LEVEL` duplicado fueron retirados del main.
- Mods y Resource Packs conservan geometria real Forge/vanilla.
- ESC/Volver en Mundos y Multiplayer vuelve directamente al padre Jobs.
- `N` cambia realmente de pista y aparece en la barra inferior contextual.
- El texto literal `%s` de la fecha fue corregido.
- El Nivel 1 usa `DepositoNuevo`; el renderer anterior sigue respaldado.

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
- ayudas de teclado solo anuncian atajos realmente implementados.

## Servidor oficial

Entrada fijada unica:

`JobsDosh.exaroton.me:56477`

Nombre localizado: `Jobs Official Server` / `Servidor oficial de Jobs`.

## Build y entrega

GitHub Actions ejecuta Java 17, politica de version, validacion de fondos, verificadores estaticos, contratos UI/musica y Forge build. Solo `main` verde publica `dev-latest`.

El test normal instala el JAR certificado en:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

El pipeline no sustituye una prueba visual dentro de Minecraft; GUI Scale, audio, navegacion y fondos deben revisarse manualmente despues del deploy.

## Documentacion

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro vigente.
- [`CHANGELOG.md`](CHANGELOG.md): historial de versiones.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y pruebas pendientes.
- [`docs/FONDOS_18_31.md`](docs/FONDOS_18_31.md): catalogo nuevo.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalacion certificada.
- [`docs/musica.md`](docs/musica.md): catalogo y lifecycle musical.
