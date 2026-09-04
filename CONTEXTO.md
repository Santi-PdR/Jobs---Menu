# CONTEXTO - Jobs - Aviso a los ocupantes

Documento maestro del estado vigente. El historial detallado vive en `CHANGELOG.md` y `docs/`.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Version actual | **0.27.0** |
| Artefacto esperado | **`jobsmenu-0.27.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **32 (0-31)** |
| Alcance | Menus, interfaces, escena, audio, lore y accesibilidad. Sin gameplay. |

## 1. Reglas duras

1. `main` es la unica rama entregable.
2. Todo JAR lleva version: `jobsmenu-<mod_version>.jar`; nunca `jobsmenu-latest.jar`.
3. `gradle.properties` es la fuente de verdad de version.
4. CI obligatorio antes de publicar.
5. `dev-latest` conserva un unico JAR versionado y solo se actualiza desde `main` verde.
6. Java visible permanece ASCII; textos de usuario viven en lang.
7. ES/EN conservan paridad de claves.
8. El rojo queda reservado a Executores.
9. Accesibilidad, Movimiento reducido y Bajo consumo tienen prioridad sobre decoracion.
10. Ningun control visible puede tener un hitbox vanilla invisible superpuesto.
11. Pantallas complejas conservan comportamiento vanilla/Forge cuando eso protege compatibilidad.
12. PNG 10-17 no reciben movimiento propio ni deformacion.
13. Los JPG 18-31 pueden usar solo movimiento de camara sutil, no destructivo y desactivable.
14. Audio de menu no puede sobrevivir dentro de gameplay.
15. Pistas musicales solo se empaquetan con archivo autorizado y redistribuible.
16. El build no descarga audio ni fondos externos.
17. Cambios visuales importantes requieren CI y luego prueba manual dentro de Minecraft.
18. Despliegue normal siempre apunta a `test-1`.
19. Capas globales de UI no capturan input ni sustituyen controles reales.
20. Una ayuda visual de teclado solo puede anunciar una tecla implementada de verdad.

## 2. Identidad visual

Jobs es una instalacion administrativa/industrial hostil: archivo, formularios, sectores, expedientes, peaje y mantenimiento. No debe sentirse como dashboard futurista.

Familias:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Resource Packs.

La escena usa la paleta material/luz del nivel; la UI usa papel frio, grafito, gris verdoso y tinta neutra.

## 3. Estado 0.27.0

0.27.0 amplia el catalogo con 14 fondos entregados directamente al repositorio y elimina la solucion temporal basada en ZIP/Base64.

### Fondos directos

Ruta de runtime y fuente versionada:

`src/main/resources/assets/jobsmenu/textures/backgrounds/`

Archivos nuevos:

- `nivel18.jpg` - cool_glitchy_null_by_autumn
- `nivel19.jpg` - dark_moon_1
- `nivel20.jpg` - heavenlytegrity
- `nivel21.jpg` - circuit_frolic
- `nivel22.jpg` - caveman
- `nivel23.jpg` - caveboy
- `nivel24.jpg` - bad_posture
- `nivel25.jpg` - a_very_null_night
- `nivel26.jpg` - moonboy
- `nivel27.jpg` - void_castle
- `nivel28.jpg` - tbread
- `nivel29.jpg` - scarlet_king
- `nivel30.jpg` - new_super_circuit_bros_3d
- `nivel31.jpg` - world_domination

Los 14 JPG son 1920x1080 y forman niveles nuevos 18-31. No se generan ni extraen durante Gradle.

### Movimiento de imagen

- niveles 10-17: siempre estaticos;
- niveles 18-31: respiracion de camara muy leve segun el fondo;
- el movimiento se realiza solo en render: el JPG del repositorio no se modifica;
- Movimiento reducido, Bajo consumo o escena quieta dejan la imagen fija;
- no se agregan objetos, foreground falso, flicker agresivo ni deformacion.

### Catalogo y lore

`Nivel.CATALOGO` tiene 32 entradas. ES/EN tienen nombre y tres notas para cada nivel 18-31. El mapeo completo vive en `docs/FONDOS_18_31.md`.

## 4. Estado heredado 0.26.0

- `SHIFT CONTROL` fue retirado por completo.
- el `JOBS / LEVEL` duplicado del fondo fue eliminado.
- `N` cambia de pista y aparece en la barra inferior contextual.
- Mods conserva la geometria real de `ModListScreen`.
- Resource Packs conserva las listas reales de Minecraft.
- Mundos y Multiplayer vuelven al padre Jobs con una sola accion.
- la fecha ya no muestra `%s` literal.
- avisos rotativos ES/EN fueron reescritos.
- Nivel 1 usa `DepositoNuevo`; el anterior tiene backup.

## 5. Musica y sesion

Catalogo real:

1. Absurdism
2. REQUIEM - Forsaken OST
3. Upon the Hill V2

Reglas:

- inicio aleatorio por visita;
- sin repeticion inmediata;
- crossfade y rotacion automatica;
- `N` solicita siguiente pista;
- `M` controla mute Jobs;
- F3+T/recarga no debe duplicar audio;
- cambiar entre subpantallas Jobs no reinicia la sesion;
- gameplay ejecuta hard-stop de musica y ambiente Jobs.

## 6. Navegacion e interfaz

Atajos principales del main:

- `1-4`: renglones principales;
- `F`: siguiente nivel cuando corresponde;
- `M`: mute Jobs;
- `N`: siguiente pista;
- `TAB`: navegacion;
- `ENTER`: activar;
- `ESC`: volver.

Los atajos numericos no actuan mientras se escribe en un EditBox ni con modificadores.

Pantallas Forge/vanilla sensibles se tematizan alrededor de su logica real. No se redimensionan listas internas por estetica si eso rompe compatibilidad.

## 7. Servidor oficial

Unica entrada fijada:

`JobsDosh.exaroton.me:56477`

Nombre localizado: `Jobs Official Server` / `Servidor oficial de Jobs`.

`Ghoul Outbreak` no debe reaparecer.

## 8. Verificacion

GitHub Actions verifica:

1. Java 17;
2. politica de version;
3. PNG 10-17 y JPEG 18-31;
4. auditoria estatica;
5. contratos UI/musica;
6. Forge build;
7. JAR versionado;
8. publicacion a `dev-latest` solo desde `main`.

CI no sustituye prueba visual. Despues del deploy revisar GUI Scale 2/3/4, fondos 18-31, movimiento reducido, Bajo consumo, audio y navegacion ESC.

## 9. Despliegue

Destino unico:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

Flujo:

`GitHub -> Actions -> dev-latest -> PowerShell -> test-1`
