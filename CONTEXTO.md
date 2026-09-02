# CONTEXTO - Jobs - Aviso a los ocupantes

Documento maestro del estado vigente. El historial vive en `CHANGELOG.md` y auditorias de `docs/`; este archivo define lo que debe seguir siendo verdad al modificar el proyecto.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Version actual | **0.22.1** |
| Artefacto esperado | **`jobsmenu-0.22.1.jar`** |
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
6. Java visible permanece ASCII; cadenas para usuario viven en idiomas.
7. ES/EN conservan paridad de claves.
8. El rojo es exclusivo de Executores.
9. Accesibilidad, movimiento reducido y Bajo consumo tienen prioridad sobre decoracion.
10. Ningun control visible puede tener un hitbox vanilla invisible superpuesto.
11. Pantallas de logica compleja conservan comportamiento vanilla/Forge cuando eso protege compatibilidad.
12. PNG 10-17 no reciben movimiento propio ni deformacion; fades/apagones/transiciones globales del menu si estan permitidos.
13. Audio de menu no puede sobrevivir dentro de gameplay.
14. Pistas musicales solo se empaquetan con archivo autorizado y redistribuible.
15. Nuevas pistas se integran desde OGG subido al repo; el build no descarga audio de terceros.
16. Cambios visuales importantes se validan con CI y despues requieren prueba manual dentro de Minecraft.
17. Despliegue normal siempre apunta a `test-1`.

## 2. Identidad visual

Jobs es un backrooms administrativo con peaje. El ocupante trabaja, junta dinero y paga para pasar al siguiente Nivel. La interfaz no es futurista: usa archivo, formulario, instalacion vieja, marcas de inventario y amenaza sugerida.

La escena y la UI no comparten paleta por comodidad:

- escena: materiales y luz propios del Nivel;
- UI: papel frio, grafito, gris verdoso y tinta neutra.

Familias de superficie:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Recursos.

## 3. Estado 0.22.1

0.22.1 concentra el salto visible en menu principal, pausa, transiciones globales y secretos de sesion.

### Menu principal

- expediente con rails y marcas de registro;
- HUD lateral contextual en viewports amplios;
- lectura de turno, Nivel y estado de instalacion;
- pista de atajos integrada sin convertir la pantalla en dashboard;
- zona tecnica lateral con codigo de expediente;
- reglas secundarias y jerarquia de profundidad alrededor de la hoja;
- easter eggs de sesion discretos, sin red, recompensas ni efecto sobre gameplay.

### Pausa

- conserva el mundo real detras;
- doble profundidad de sombra;
- rails laterales y marcas de suspension;
- panel contextual adicional en viewports amplios;
- contexto visible `LOCAL/SERVER`;
- codigo de expediente de sesion;
- pista `M=MUTE`;
- Escape reanuda;
- Condiciones abre Options Jobs;
- salir conserva la secuencia real de desconexion/guardado.

### Atmosfera compartida

- rails globales muy tenues en pantallas Jobs;
- registros superiores, inferiores y laterales;
- barridos horizontales y verticales ultra sutiles;
- nunca mueve, escala ni deforma el fondo;
- se desactiva con Movimiento reducido o Bajo consumo.

### Transiciones

- expediente transversal de 430 ms;
- mayor profundidad de cola y sombra;
- marcas de registro internas;
- modo reducido conserva fade simple;
- no bloquea input ni cambia la Screen.

### Sistema visual heredado

- botones NORMAL / PRINCIPAL / JOBS / TERMINAL;
- toggles con transicion ON/OFF;
- sliders con tirador interpolado, escala y capsula de valor;
- renglones con placa de orden y tratamiento terminal;
- foco global reforzado;
- scrollbars Jobs;
- perfiles Equilibrado, Inmersivo, Rendimiento, Accesible y Minimo.

## 4. Estado de pantallas grandes

### Mundos

- superficie central propia;
- busqueda integrada;
- scrollbar Jobs;
- logica vanilla de seleccion, creacion, edicion y borrado intacta.

### Multijugador

- tablero de servidores propio;
- tarjeta reforzada del servidor oficial;
- estado visible de seleccion/proteccion;
- scrollbar Jobs;
- acciones reales de Minecraft conservadas.

### Mods / Forge

- catalogo y panel de detalle separados;
- buscador integrado;
- scrollbar Jobs;
- cabecera reservada para que no pise entradas;
- dirt del panel derecho eliminado en el flujo Jobs;
- busqueda, orden, Config, logos, panel y carpeta siguen siendo de Forge.

### Resource Packs

- doble archivador visual alineado a la geometria real de las listas vanilla;
- scrollbar Jobs;
- seleccion, orden, aplicar y carpeta siguen siendo de Minecraft.

### Idioma

- layout responsive;
- hover, seleccionado e idioma aplicado diferenciados;
- codigos como badges;
- estado actual -> pendiente antes de aplicar;
- buscador propio sin perder foco/teclado.

### Sonido y Video

- Sonido usa bandeja interior de mezcla y scrollbar Jobs;
- Video vanilla usa ficha de calibracion y scrollbar Jobs;
- Embeddium conserva su propia pantalla.

## 5. Fondos 10-17

Los PNG 10-17 permanecen exactamente como archivos de imagen. Se usa filtrado lineal para evitar el aspecto pixelado al ajustar la imagen a la ventana.

No se agrega al PNG:

- zoom;
- paneo;
- parallax;
- motas o particulas propias;
- foreground dinamico;
- flicker propio;
- deformacion;
- alteracion del encuadre fuente.

Si estan permitidos los efectos que pertenecen a la navegacion completa del menu, como fade, apagon de traslado, overlays de interfaz y transicion de expediente, porque no animan la geometria interna de la imagen. Si se agregan niveles 18-19 como PNG, heredan este contrato.

## 6. Navegacion y ciclo de vida

`SesionMenu` representa una visita completa.

- `TitleScreen` vanilla se redirige a `PantallaNivel` cuando menu propio esta activo;
- pausa real se redirige a `PantallaEstancia`;
- Options, Multiplayer, Mundos y Mods se tematizan solo dentro del flujo Jobs;
- redirecciones sensibles usan clase exacta;
- entrar a mundo/servidor cierra sesion y corta audio inmediatamente;
- salir de mundo/servidor/kick recupera `PantallaNivel`.

## 7. Multiplayer

Servidor fijado:

`JobsDosh.exaroton.me:56477`

Contrato:

- una sola entrada;
- nombre localizado;
- primera posicion;
- protegida frente a edicion/borrado desde Jobs;
- IP deduplicada;
- `Ghoul Outbreak` eliminado y no recreado.

## 8. Musica y ambiente

Pista incluida actual:

- **Absurdism** -> `assets/jobsmenu/sounds/musica/defecto.ogg`.

Segunda pista preparada:

- entrada: `music/menu_nueva.ogg`;
- id interno: `upon_the_hill_v2`;
- salida: `assets/jobsmenu/sounds/musica/tema_nuevo.ogg`;
- workflow: `.github/workflows/integrar_ogg_subido.yml`.

Comportamiento obligatorio:

- fade-in;
- fade-out dentro del menu;
- crossfade cuando haya 2+ pistas;
- ducking en transiciones/Suspension/presencia;
- continuidad por subpantallas;
- watchdog de instancias fantasma;
- recuperacion tras recarga;
- hard stop al entrar a gameplay.

La musica usa `SoundSource.MASTER`: Maestro + volumen Jobs + volumen del aviso; no slider Musica vanilla.

## 9. Compatibilidad

- Redirecciones principales por clase exacta.
- Listas complejas conservan logica real.
- `ListasExpediente` modifica presentacion y conserva rueda/click/drag.
- `PielVanillaJobs` no se aplica indiscriminadamente a pantallas externas.
- Embeddium conserva su UI de video.
- Mods que sustituyan totalmente pantallas pueden requerir integracion especifica.

## 10. Prueba y entrega

CI certifica:

- Java 17;
- politica de version;
- PNG/CRC/IDAT;
- recursos, idiomas y ASCII Java;
- contratos UI/musica;
- Forge build 1.20.1;
- artefacto `jobsmenu-0.22.1.jar`;
- publicacion a `dev-latest` desde `main`.

CI no certifica estetica dentro de Minecraft. La prueba manual vigente esta en `docs/checklist-manual.md`.

Destino unico de despliegue:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

El PowerShell se entrega solo despues de: docs actualizados -> CI de PR verde -> merge -> CI de main verde -> `dev-latest` actualizado.
