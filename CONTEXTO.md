# CONTEXTO - Jobs - Aviso a los ocupantes

Documento maestro del estado vigente. El historial detallado vive en `CHANGELOG.md` y `docs/`.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Version actual | **0.38.0** |
| Artefacto esperado | **`jobsmenu-0.38.0.jar`** |
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
28. F5/Actualizar conserva la IP del servidor seleccionado y restaura una Entry fresca; una Entry vieja nunca se reutiliza.
29. `docs/README.md` separa documentacion vigente de auditorias historicas; un documento viejo no redefine el contrato actual.
30. Las listas Jobs no vuelven a hacer descubrimiento reflection completo por frame; cachean fields por clase y listas por Screen viva.
31. Una scrollbar Jobs se dibuja como maximo una vez por frame aunque una Screen y `Render.Post` la soliciten.
32. El filtrado lineal de fondos se configura por instancia de textura, no por frame; una recarga de recursos vuelve a aplicarlo al nuevo objeto.
33. Bajo consumo debe reducir draw calls ademas de apagar movimiento costoso.
34. El JAR debe usar orden reproducible y no incluir timestamps variables generados por el build.

## 2. Identidad visual

Jobs es una instalacion administrativa/industrial hostil: archivo, formularios, sectores, expedientes, peaje y mantenimiento. No debe sentirse como dashboard futurista.

Familias:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Resource Packs.

La escena usa la paleta material/luz del nivel; la UI usa papel frio, grafito, gris verdoso y tinta neutra.

## 3. Estado 0.38.0

0.38.0 conserva los contratos funcionales de 0.35-0.37 y optimiza caminos calientes de render, listas, escena, audio, texto y build.

### Listas y lifecycle

- `ListasExpediente` descubre los fields `AbstractSelectionList` una vez por clase y conserva solamente las listas de la Screen activa;
- `estilizar()` invalida la cache de instancia porque `init()`/resize pueden reconstruir widgets;
- `ScreenEvent.Render.Pre` abre la deduplicacion del frame y evita que una scrollbar pedida por la Screen y por `Render.Post` se pinte dos veces;
- `ScreenEvent.Closing` libera la referencia a la Screen y sus listas;
- el hover vanilla usa un `WeakHashMap` como set y conserva solo botones actualmente enfocados/hover, no un booleano por cada boton visitado.

### Texturas y fondos

- `PlantaImagen` sigue validando cada archivo una vez con `NativeImage` y conserva fallback procedural si el recurso falla;
- el filtro lineal se aplica una vez por objeto `AbstractTexture`;
- F3+T/resource reload reemplaza el objeto y la comprobacion de identidad reaplica el filtro automaticamente;
- 10-17 siguen completamente estaticos y 18-31 mantienen su respiracion sutil no destructiva.

### UI y texto

- `PulidoInterfazJobs` captura una sola hora por pasada y recorre `children()` una sola vez para jerarquia + foco;
- el aviso de cambio guardado reutiliza su `Component`;
- `NotaAviso` reutiliza el `Component` mientras la clave no cambia y cachea el calendario especial por minuto;
- `PielVanillaJobs` lee Alto contraste una vez por pasada;
- Multiplayer prepara rotulos dependientes del ancho al hacer `init()` y reutiliza tooltips edit/protegido/eliminar en vez de crearlos cada frame.

### Escena, audio y Bajo consumo

- `RotacionNiveles.capturar()` comparte el mismo record entre consumidores que consultan exactamente el mismo milisegundo; al siguiente milisegundo se recalcula normalmente;
- el salto manual invalida ese cache de inmediato;
- Bajo consumo duplica el ancho de las bandas de vignette y reduce capas de profundidad, rebote y humedad;
- el modo visual normal mantiene la cantidad y progresion anterior de esas capas;
- eventos, presencia, motas, movimiento de imagen y grano siguen respetando sus guardas existentes de accesibilidad/ahorro.

### Build reproducible

- `jar` usa `preserveFileTimestamps = false` y `reproducibleFileOrder = true`;
- se elimina `Implementation-Timestamp`, que hacia variar el hash por hora de compilacion aunque el contenido no cambiara;
- `tools/verificar_optimizacion.py` fija estos contratos junto a los verificadores historicos.

### Multiplayer

- `PantallaMultijugadorJobs` guarda explicitamente su `pantallaPadre`;
- ESC y Cancelar convergen en `cerrarAlPadre()` con guard idempotente;
- F5/Actualizar captura solo la IP del servidor online seleccionado y crea una lista nueva;
- la lista nueva restaura una Entry fresca y ejecuta `onSelectedChange()`;
- F5 conserva `UI_ALTERNAR` Jobs y el indicador localizado de Refresh/Actualizar;
- conectar sigue usando esta pantalla como padre de `ConnectScreen`.

### Gameplay sin transiciones

- `usaTransicionJobs()` devuelve false siempre que `Minecraft.level != null`;
- cualquier transicion pendiente se cancela en login, logout y tick de gameplay;
- `TransicionInterfazJobs.dibujar()` solo se llama cuando no existe nivel cargado;
- `PulidoInterfazJobs.notificarApertura()` tampoco se registra durante gameplay;
- Pausa/Config Jobs pueden conservar tema y feedback corto, pero aparecen sin transicion;
- chat, inventario, contenedores y Video Settings mantienen sus exclusiones.

### Audio de interfaz y retorno

- `PlaySoundEvent` sustituye `minecraft:ui.button.click` solo en superficies Jobs validas;
- el feedback corto no reactiva `SesionMenu`, musica ni camas;
- abandonar servidor remoto vuelve a `PantallaMultijugadorJobs`; abandonar mundo local vuelve a `PantallaNivel`;
- Video Settings, chat, inventario y pantallas no Jobs quedan fuera.

## 4. Fondos y ambiente

- 0-9 conservan sus escenas procedurales y tres camas;
- 10-17 mantienen sus PNG y combinaciones de ambiente, sin movimiento de imagen;
- 18-31 mantienen JPG directos 1920x1080, punto de interes, respiracion opcional y perfiles de base/caracter/actividad;
- no se incorporan muestras nuevas ni audio de terceros;
- Movimiento reducido, Bajo consumo o escena quieta dejan los fondos de imagen fijos.

Ruta de fondos:

`src/main/resources/assets/jobsmenu/textures/backgrounds/`

## 5. Musica y sesion

Catalogo real:

1. Absurdism
2. REQUIEM - Forsaken OST
3. Upon the Hill V2

Reglas:

- inicio aleatorio por visita o pista fija elegida;
- sin repeticion inmediata en Aleatoria;
- crossfade y rotacion automatica;
- `N` solicita siguiente pista solo cuando corresponde;
- `M` controla mute Jobs;
- F3+T/recarga no debe duplicar audio;
- cambiar entre subpantallas Jobs no reinicia la sesion;
- gameplay ejecuta hard-stop de musica y ambiente Jobs.

## 6. Navegacion e interfaz

Atajos funcionales del main: `1-4`, `F`, `M`, `N`, `TAB`, `ENTER` y `ESC`.

Desde 0.28.0 no se listan en una barra inferior sobre `PantallaNivel`; su funcionalidad permanece. Los atajos numericos no actuan mientras se escribe en un EditBox ni con modificadores.

Pantallas Forge/vanilla sensibles se tematizan alrededor de su logica real. Video Settings sigue vanilla por contrato.

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
4. auditoria estatica y paridad de idiomas;
5. contratos UI/musica y frontera de gameplay;
6. continuidad de Multiplayer/documentacion;
7. contratos de optimizacion de caminos calientes;
8. Forge build;
9. JAR versionado;
10. publicacion a `dev-latest` solo desde `main`.

CI no mide FPS ni reemplaza prueba visual. Despues del deploy revisar GUI Scale 2/3/4, fondos, Bajo consumo, audio, F3+T, Multiplayer con ESC/Cancelar/F5 y ausencia de transiciones durante gameplay.

## 9. Despliegue

Destino unico:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

Flujo:

`GitHub -> Actions -> dev-latest -> PowerShell -> test-1`
