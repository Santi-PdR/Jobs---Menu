# CONTEXTO - Jobs - Aviso a los ocupantes

Documento maestro del estado vigente. El historial detallado vive en `CHANGELOG.md` y `docs/`.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Version actual | **0.37.0** |
| Artefacto esperado | **`jobsmenu-0.37.0.jar`** |
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
22. Chat, inventario y demas pantallas de gameplay no reciben piel, banda, transicion ni sustitucion de clicks Jobs.
23. Escape y Cancelar en Multiplayer terminan directamente en el padre Jobs; no dependen de `Screen.onClose()` ni de `popGuiLayer()`.
24. El hard-stop afecta musica y ambiente; el feedback breve de UI Jobs puede seguir activo en pausa/configuracion sin abrir una sesion musical.
25. Un retorno desde servidor remoto nunca debe caer en Multiplayer o Title vanilla: conserva `PantallaMultijugadorJobs` como superficie contextual.
26. Mientras exista un mundo o servidor cargado no se crea, registra ni dibuja ninguna animacion de transicion Jobs, incluida la entrada corta de `PulidoInterfazJobs`.
27. Actualizar Multiplayer no crea una `JoinMultiplayerScreen` vanilla intermedia: reconstruye directamente la superficie Jobs con el mismo padre.
28. F5/Actualizar conserva la IP del servidor seleccionado y restaura la selección después de reconstruir la lista; una Entry vieja nunca se reutiliza.
29. `docs/README.md` separa documentación vigente de auditorías históricas; un documento viejo no redefine el contrato actual.

## 2. Identidad visual

Jobs es una instalacion administrativa/industrial hostil: archivo, formularios, sectores, expedientes, peaje y mantenimiento. No debe sentirse como dashboard futurista.

Familias:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Resource Packs.

La escena usa la paleta material/luz del nivel; la UI usa papel frio, grafito, gris verdoso y tinta neutra.

## 3. Estado 0.37.0

0.37.0 conserva los contratos de 0.35/0.36 y pule continuidad de selección, feedback de recarga, CI y documentación.

### Multiplayer

- `PantallaMultijugadorJobs` guarda explicitamente su `pantallaPadre`;
- ESC llega a `onClose()` desde la logica normal de `Screen`, pero `onClose()` no delega en `super.onClose()`;
- Cancelar llama a la misma funcion `cerrarAlPadre()`;
- `cerrarAlPadre()` aplica guard idempotente y hace `minecraft.setScreen(padreDestino())` una sola vez;
- F5/Actualizar captura únicamente la IP del servidor online seleccionado, marca la pantalla saliente como cerrada y crea otra `PantallaMultijugadorJobs` con el mismo padre;
- la nueva pantalla busca una Entry fresca con esa IP, la selecciona y ejecuta `onSelectedChange()` para sincronizar acciones vanilla;
- nunca se conserva una referencia a una `ServerSelectionList.Entry` perteneciente a la lista anterior;
- el atajo F5 emite `UI_ALTERNAR` Jobs y su indicador usa la traduccion de `selectServer.refresh`, no literales duros `JOBS/SERVER`;
- conectar sigue usando esta pantalla como padre de `ConnectScreen`, por lo que Cancelar/error antes del login regresan a la lista Jobs.

### Gameplay sin transiciones

- `usaTransicionJobs()` devuelve false siempre que `Minecraft.level != null`;
- cualquier transicion pendiente se cancela en login, logout y tick de gameplay;
- `TransicionInterfazJobs.dibujar()` solo se llama cuando no existe nivel cargado;
- `PulidoInterfazJobs.notificarApertura()` tampoco se llama durante gameplay, eliminando su animacion corta de entrada;
- Pausa Jobs, Config Jobs y sus subpantallas pueden conservar tema, foco y sonidos breves, pero aparecen directamente, sin barrido/fundido de entrada;
- chat, inventario, contenedores y Video Settings mantienen sus exclusiones anteriores.

### Audio de interfaz

- `PlaySoundEvent` sustituye `minecraft:ui.button.click` en cualquier superficie propia Jobs, incluso con un mundo cargado;
- esa sustitucion no reactiva `SesionMenu`, por lo que pausa/configuracion pueden sonar Jobs sin devolver musica o ambiente al gameplay;
- botones y sliders vanilla conservados por compatibilidad reciben `UI_PASAR` una sola vez al entrar con raton o foco;
- widgets Jobs propios siguen gestionando su hover internamente y se excluyen del seguimiento global;
- Video Settings, chat, inventario y pantallas de gameplay no Jobs quedan fuera.

### Retorno tras juego

- mientras hay un nivel cargado se memoriza si `Minecraft#getCurrentServer()` identifica un servidor remoto;
- `LoggingOut` conserva ese contexto antes de que la limpieza vanilla pueda perderlo;
- abandonar un servidor remoto reconduce tanto `TitleScreen` como `JoinMultiplayerScreen` a `PantallaMultijugadorJobs` con `PantallaNivel` como padre;
- abandonar un mundo local o volver a Title/Realms sin contexto remoto reconduce a `PantallaNivel`.

### Ambiente de los 32 niveles

- 0-9 conservan sus tres camas y repertorios originales;
- 10-17 mantienen las combinaciones disenadas en sus versiones de imagen;
- 18-31 tienen seleccion explicita de base, caracter, actividad y eventos;
- cada fondo 18-31 define ademas frecuencia, balance y afinacion estables;
- no se incorporan muestras nuevas ni audio de terceros.

### Controles de ajustes

- volumen muestra porcentaje;
- estancia y duracion de avisos muestran segundos;
- nivel fijo muestra posicion 0-31 y pista muestra posicion 0-3;
- reactivar Sonidos de interfaz produce confirmacion Jobs;
- `N` no altera una pista fija y usa `UI_NEGADO` cuando no puede saltar.

### Pie del menu principal

- `CapaProfesionalJobs` no se renderiza sobre `PantallaNivel`.
- desaparecen del main los rotulos visibles `1-4`, `F`, `M`, `N`, `TAB` y `ENTER`;
- los atajos siguen activos y no cambian sus callbacks;
- las pantallas secundarias mantienen la instrumentacion contextual;
- nombre y nota del nivel dominan la zona inferior sin competir con una barra global.

### Catalogo visual 18-31

Los 14 JPG 1920x1080 se revisaron contra su contenido real. Los nombres y notas visibles viven en los `lang/*.json`, mientras `RotulosNivelesImagen` funciona solo como fachada de acceso. Asi no hay dos catalogos ES/EN que puedan desincronizarse.

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
- gameplay ejecuta hard-stop de musica y ambiente Jobs;
- sonidos breves de botones/hover no forman parte de la sesion musical y pueden responder en pausa/configuracion Jobs.

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
6. continuidad de Multiplayer y sincronizacion de documentacion vigente;
7. Forge build;
8. JAR versionado;
9. publicacion a `dev-latest` solo desde `main`.

CI no sustituye prueba visual. Despues del deploy revisar GUI Scale 2/3/4, fondos 18-31, movimiento reducido, Bajo consumo, audio, Multiplayer con ESC/Cancelar/F5, preservacion de seleccion al refrescar y ausencia total de transiciones mientras existe gameplay.

## 9. Despliegue

Destino unico:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

Flujo:

`GitHub -> Actions -> dev-latest -> PowerShell -> test-1`
