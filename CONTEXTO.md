# CONTEXTO - Jobs - Aviso a los ocupantes

Documento maestro del estado vigente. El historial detallado vive en `CHANGELOG.md` y `docs/`.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Version actual | **0.32.0** |
| Artefacto esperado | **`jobsmenu-0.32.0.jar`** |
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
6. Java fuente permanece ASCII; cualquier caracter visible no ASCII en catalogos Java usa escapes Unicode. Los textos generales siguen viviendo en lang.
7. ES/EN conservan paridad funcional.
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
21. `PantallaNivel` reserva el pie para el nombre y la nota del nivel; la barra contextual generica no se dibuja en el main.

## 2. Identidad visual

Jobs es una instalacion administrativa/industrial hostil: archivo, formularios, sectores, expedientes, peaje y mantenimiento. No debe sentirse como dashboard futurista.

Familias:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Resource Packs.

La escena usa la paleta material/luz del nivel; la UI usa papel frio, grafito, gris verdoso y tinta neutra.

## 3. Estado 0.32.0

0.32.0 completa la identidad sonora de los fondos 18-31 y corrige la lectura de los controles sin tocar imágenes ni ampliar la superficie de configuración.

### Ambiente de los 32 niveles

- 0-9 conservan sus tres camas y repertorios originales;
- 10-17 mantienen las combinaciones diseñadas en sus versiones de imagen;
- 18-31 tienen selección explícita de base, carácter, actividad y eventos;
- cada fondo 18-31 define además frecuencia, balance y afinación estables;
- no se incorporan muestras nuevas ni audio de terceros.

### Controles de ajustes

- volumen muestra porcentaje;
- estancia y duración de avisos muestran segundos;
- nivel fijo muestra posición 0-31 y pista muestra posición 0-3;
- reactivar Sonidos de interfaz produce confirmación Jobs;
- `N` no altera una pista fija y usa `UI_NEGADO` cuando no puede saltar.

### Pie del menu principal

- `CapaProfesionalJobs` ya no se renderiza sobre `PantallaNivel`.
- desaparecen del main los rotulos visibles `1-4`, `F`, `M`, `N`, `TAB` y `ENTER`;
- los atajos siguen activos y no cambian sus callbacks;
- las pantallas secundarias mantienen la instrumentacion contextual;
- nombre y nota del nivel vuelven a dominar la zona inferior sin competir con una barra global.

### Catalogo visual 18-31

Los 14 JPG 1920x1080 se revisaron contra su contenido real. Los nombres y notas visibles viven en los `lang/*.json`, mientras `RotulosNivelesImagen` funciona solo como fachada de acceso. Así no hay dos catálogos ES/EN que puedan desincronizarse.

Nombres ES vigentes:

- 18: Interferencia carmesi
- 19: La estrella del vacio
- 20: El huesped de tinta
- 21: El claro de los centinelas
- 22: La caverna del vigia
- 23: La marana organica
- 24: El umbral escarlata
- 25: La senal sobre el bosque
- 26: El observador lunar
- 27: La fortaleza roja
- 28: El nucleo fragmentado
- 29: El soberano escarlata
- 30: La figura fragmentada
- 31: El coloso del vacio

La ortografia visible se conserva directamente en los JSON de idioma. Cada fondo tiene tres notas ES/EN ligadas a elementos observables de la escena. El HUD las envuelve por ancho y reinicia la secuencia al entrar al nivel.

### Fondos directos

Ruta de runtime y fuente versionada:

`src/main/resources/assets/jobsmenu/textures/backgrounds/`

Los archivos `nivel18.jpg` a `nivel31.jpg` siguen siendo recursos directos del repositorio. No se generan ni extraen durante Gradle.

### Movimiento de imagen

- niveles 10-17: siempre estaticos;
- niveles 18-31: respiracion de camara muy leve con intensidad y punto de interes propios por fondo;
- el movimiento se realiza solo en render: el JPG del repositorio no se modifica;
- Movimiento reducido, Bajo consumo o escena quieta dejan la imagen fija;
- no se agregan objetos, foreground falso, flicker agresivo ni deformacion.

## 4. Estado heredado

- `SHIFT CONTROL` fue retirado por completo.
- el `JOBS / LEVEL` duplicado del fondo fue eliminado.
- Mods conserva la geometria real de `ModListScreen`.
- Resource Packs conserva las listas reales de Minecraft.
- Mundos y Multiplayer vuelven al padre Jobs con una sola accion.
- la fecha ya no muestra `%s` literal.
- Nivel 1 usa `DepositoNuevo`; el anterior tiene backup.
- `nivel_fijo` admite 0-31.

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

Atajos funcionales del main:

- `1-4`: renglones principales;
- `F`: siguiente nivel cuando corresponde;
- `M`: mute Jobs;
- `N`: siguiente pista;
- `TAB`: navegacion;
- `ENTER`: activar;
- `ESC`: volver.

En 0.28.0 estos atajos ya no se listan en una barra inferior sobre `PantallaNivel`; su funcionalidad permanece. Los atajos numericos no actuan mientras se escribe en un EditBox ni con modificadores.

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
