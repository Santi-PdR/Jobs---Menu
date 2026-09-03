# CONTEXTO - Jobs - Aviso a los ocupantes

Documento maestro del estado vigente. El historial vive en `CHANGELOG.md` y auditorias de `docs/`; este archivo define lo que debe seguir siendo verdad al modificar el proyecto.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Version actual | **0.26.0** |
| Artefacto esperado | **`jobsmenu-0.26.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0-17)** |
| Alcance | Menus, interfaces, escena, audio, lore y accesibilidad. Sin gameplay. |

## 1. Reglas duras

1. `main` es la unica rama entregable.
2. Todo JAR lleva version: `jobsmenu-<mod_version>.jar`; nunca `jobsmenu-latest.jar`.
3. `gradle.properties` es la fuente de verdad de version.
4. CI obligatorio: Java 17 -> version -> fondos -> auditoria estatica -> contratos UI/musica -> Forge build -> publicacion.
5. `dev-latest` contiene un unico JAR versionado y solo se actualiza desde `main` con pipeline verde.
6. Java visible permanece ASCII; cadenas para usuario viven en idiomas. Codigos tecnicos y nombres de teclas pueden ser universales.
7. ES/EN conservan paridad de claves.
8. El rojo es exclusivo de Executores.
9. Accesibilidad, Movimiento reducido y Bajo consumo tienen prioridad sobre decoracion.
10. Ningun control visible puede tener un hitbox vanilla invisible superpuesto.
11. Pantallas de logica compleja conservan comportamiento vanilla/Forge cuando eso protege compatibilidad.
12. PNG 10-17 no reciben movimiento propio ni deformacion; fades/apagones/transiciones y overlays globales del menu si estan permitidos.
13. Audio de menu no puede sobrevivir dentro de gameplay.
14. Pistas musicales solo se empaquetan con archivo autorizado y redistribuible.
15. Nuevas pistas se integran desde OGG subido al repo; el build no descarga audio de terceros.
16. Cambios visuales importantes se validan con CI y despues requieren prueba manual dentro de Minecraft.
17. Despliegue normal siempre apunta a `test-1`.
18. Capas globales de UI no capturan input ni sustituyen controles reales.
19. Una ayuda visual de teclado solo puede anunciar una tecla implementada de verdad.
20. Atajos numericos no actuan mientras un campo de texto tiene foco.

## 2. Identidad visual

Jobs es un backrooms administrativo con peaje. El ocupante trabaja, junta dinero y paga para pasar al siguiente Nivel. La interfaz no es futurista: usa archivo, formulario, instalacion vieja, marcas de inventario, calibracion y amenaza sugerida.

La escena y la UI no comparten paleta por comodidad:

- escena: materiales y luz propios del Nivel;
- UI: papel frio, grafito, gris verdoso y tinta neutra.

Familias de superficie:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Recursos.

## 3. Estado 0.26.0

0.26.0 suma un catalogo musical real de tres pistas y conserva como base todo el pase visual 0.24.0.

### Musica de sesion 0.25

- Absurdism, REQUIEM y Upon the Hill V2 son pistas independientes;
- cada una tiene SoundEvent y OGG de runtime propio;
- OGG largos usan streaming;
- inicio aleatorio por visita y siguiente pista sin repeticion inmediata;
- crossfade automatico cada 2-4 minutos;
- `N` solicita siguiente pista desde el main;
- credito y HUD siguen a la pista dominante durante crossfade;
- `M` conserva mute y gameplay conserva hard-stop.

### Base visual 0.24

0.24.0 profundiza navegacion, lectura de controles y feedback contextual. La auditoria visible completa esta en `docs/AUDITORIA_0.24.0_84_MEJORAS.md`.

### Instrumentacion contextual

- codigo tecnico por familia de pantalla;
- titulo real de la Screen como contexto secundario cuando cabe;
- contador de controles activos/totales con barra de proporcion;
- reloj de visita `T+MM:SS`;
- contador de pantallas visitadas durante la visita actual;
- volumen maestro Jobs y estado `MUTE`;
- breadcrumb de las ultimas tres familias de pantalla;
- etiqueta del control enfocado o bajo el puntero;
- modo de entrada `KEY` / `PTR`;
- tipo de control `TOG` / `SLD` / `TXT` / `ROW` / `BTN` / `CTL`;
- indice del control actual dentro de los controles activos;
- rail contextual por pantalla;
- comportamiento responsive y simplificacion con Interfaz minima;
- Movimiento reducido/Bajo consumo cambian actividad por referencias estaticas;
- Alto contraste tambien gobierna esta instrumentacion.

### Atajos visibles y reales

- Main: `1-4` activa los cuatro renglones de arriba hacia abajo;
- Main: teclado numerico `1-4` hace lo mismo;
- Pausa: `1-2` / keypad `1-2` activan Reanudar y Condiciones;
- Pausa: el renglón de salida no recibe numero rapido para evitar desconexiones accidentales;
- los atajos numericos se ignoran con EditBox enfocado o modificadores;
- `F`, `M`, `CTRL+F`, `F5`, `F1-F5`, TAB, ENTER y ESC solo se anuncian donde su comportamiento ya existe.

### Main screen

- placa lateral `JOBS / SHIFT CONTROL` retirada por completo;
- lectura de Nivel y estado normal/transicion/Suspension;
- LEDs rotulados R/A/M/U;
- perfil actual o `CUSTOM`;
- progreso real de estancia con cursor y marcas de cuartos;
- `NXT MM:SS`, `NXT HOLD` o `NXT MOVE` segun estado;
- tiempo de visita;
- volumen Jobs numerico o `MUTE`;
- medidor de volumen con escala;
- capsulas `1-4`, F, M, TAB y ENTER;
- toda la placa respeta luz, Alto contraste y espacio disponible.

### Controles vanilla/Forge preservados

`PielVanillaJobs` sigue dibujandose despues del control real y no cambia su logica.

- botones con sombra, doble borde e highlight superior;
- hover y foco de teclado se distinguen;
- foco de teclado obtiene marcadores externos;
- recorte de texto tiene indicador propio;
- disabled queda mas legible;
- sliders con doble borde, escala de diez pasos y notch de teclado;
- campos de texto con doble borde, notch de foco y estado no editable;
- archivo oscuro y formulario claro conservan paletas separadas.

### Scrollbars

`ListasExpediente` mantiene rueda/click/drag reales y agrega:

- tramo recorrido en el canal;
- escala 0/25/50/75/100;
- topes y chevrons;
- cursor de posicion a ambos lados;
- doble sombra del tirador;
- grip central ampliado;
- marcas de extremo del tirador.

### Sistema heredado

Se conserva todo lo ya certificado en 0.23.0:

- transicion de expediente de 470 ms;
- atmosfera de bordes sin tocar el background;
- botones NORMAL / PRINCIPAL / JOBS / TERMINAL;
- toggles y sliders Jobs;
- perfiles Equilibrado, Inmersivo, Rendimiento, Accesible y Minimo;
- Mods, Resources, Mundos, Multiplayer, Idioma, Sonido y Video tematizados conservando logica real.

## 4. Fondos 10-17

Los PNG 10-17 permanecen exactamente como archivos de imagen. Se usa filtrado lineal para evitar pixelado al ajustar la imagen a la ventana.

No se agrega al PNG:

- zoom;
- paneo;
- parallax;
- motas o particulas propias;
- foreground dinamico;
- flicker propio;
- deformacion;
- alteracion del encuadre fuente.

Si estan permitidos fade, apagon de traslado, overlays de interfaz y transicion de expediente porque pertenecen a la navegacion global y no animan la geometria interna de la imagen. Si se agregan niveles 18-19 como PNG, heredan este contrato.

## 5. Navegacion y ciclo de vida

`SesionMenu` representa una visita completa.

- `TitleScreen` vanilla se redirige a `PantallaNivel` con menu propio;
- pausa real se redirige a `PantallaEstancia`;
- Options, Multiplayer, Mundos y Mods se tematizan solo dentro del flujo Jobs;
- entrar a mundo/servidor cierra sesion y corta audio inmediatamente;
- salir de mundo/servidor/kick recupera `PantallaNivel`;
- reloj y contador de pantallas son temporales, locales y se reinician con una visita nueva;
- no existe telemetria de red ni persistencia de esa instrumentacion.

## 6. Multiplayer

Servidor fijado:

`JobsDosh.exaroton.me:56477`

Contrato:

- una sola entrada;
- nombre localizado;
- primera posicion;
- protegida frente a edicion/borrado desde Jobs;
- IP deduplicada;
- `Ghoul Outbreak` eliminado y no recreado.

## 7. Musica y ambiente

Catalogo vigente:

- **Absurdism** -> `assets/jobsmenu/sounds/musica/defecto.ogg`;
- **REQUIEM** -> `assets/jobsmenu/sounds/musica/requiem.ogg`; fuente `music/REQUIEM-Forsaken-OST.ogg`;
- **Upon the Hill V2** -> `assets/jobsmenu/sounds/musica/upon_the_hill_v2.ogg`; fuente `music/upon_the_hill_v2_q4.ogg`.

Comportamiento obligatorio:

- fade-in/fade-out;
- crossfade automatico entre las 3 pistas y skip manual con `N`;
- ducking en transiciones/Suspension/presencia;
- continuidad por subpantallas;
- watchdog de instancias fantasma;
- recuperacion tras recarga;
- hard stop al entrar a gameplay.

La musica usa `SoundSource.MASTER`: Maestro + volumen Jobs + volumen del aviso; no slider Musica vanilla.

## 8. Compatibilidad

- Redirecciones principales por clase exacta.
- Listas complejas conservan logica real.
- `ListasExpediente` modifica presentacion y conserva rueda/click/drag.
- `CapaProfesionalJobs` es solo visual y no captura input.
- `AtajosInterfazJobs` solo actua en Main/Pausa y protege EditBox/modificadores.
- `PielVanillaJobs` no se aplica indiscriminadamente a pantallas externas.
- Embeddium conserva su UI de video.
- Mods que sustituyan totalmente pantallas pueden requerir integracion especifica.

## 9. Prueba y entrega

CI certifica:

- Java 17;
- politica de version;
- PNG/CRC/IDAT;
- recursos, idiomas y ASCII Java;
- contratos UI/musica;
- Forge build 1.20.1;
- artefacto `jobsmenu-0.26.0.jar`;
- publicacion a `dev-latest` desde `main`.

CI no certifica estetica dentro de Minecraft. La prueba manual vigente esta en `docs/checklist-manual.md`.

Destino unico de despliegue:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

El PowerShell se entrega solo despues de: docs actualizados -> CI de PR verde -> merge -> CI de main verde -> `dev-latest` actualizado.
