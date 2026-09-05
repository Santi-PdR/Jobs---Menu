# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menus con una interfaz administrativa/industrial propia, audio de sesion y fondos de nivel, conservando la logica vanilla/Forge cuando reconstruirla perjudicaria compatibilidad.

| Campo | Valor |
|---|---|
| Version | **0.32.0** |
| Artefacto | **`jobsmenu-0.32.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0-31)** |

## 0.32.0 · Identidad ambiental completa y controles precisos

- Los niveles 18-31 dejan de caer en el ambiente genérico del nivel 0: cada uno tiene base, carácter, actividad, repertorio, frecuencia, balance y afinación propios.
- Las mezclas reutilizan únicamente los sonidos Jobs ya autorizados; no se añaden muestras externas ni se modifican los fondos.
- Los sliders muestran la unidad real de cada ajuste: porcentaje para volumen, segundos para tiempos y posición del catálogo para nivel/pista.
- Reactivar los sonidos de interfaz confirma el cambio con `UI_ALTERNAR`; antes el propio estado apagado impedía oírlo.
- `N` mantiene la pista fija intacta y responde con el gesto Jobs de acción negada cuando el salto no está disponible.
- Los verificadores protegen automáticamente la identidad de los 32 ambientes y evitan que regresen porcentajes falsos.

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
- el main reserva su pie para el nombre y la nota del nivel, no para una barra global de atajos.

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
- [`docs/FONDOS_18_31.md`](docs/FONDOS_18_31.md): catalogo de recursos.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalacion certificada.
- [`docs/musica.md`](docs/musica.md): catalogo y lifecycle musical.
