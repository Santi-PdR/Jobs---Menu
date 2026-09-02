# Música del menú

## Estado vigente — 0.17.0

Jobs usa un reproductor de **sesión**, no música ligada a una pantalla concreta. La visita al menú conserva la posición y la mezcla al abrir Opciones, Mods, Mundos, Multijugador, Idioma o Resource Packs; entrar a un mundo/servidor corta el audio inmediatamente.

### Catálogo

1. **Absurdism** — pista incluida actualmente en `assets/jobsmenu/sounds/musica/defecto.ogg` y registrada mediante `musica.tema`.
2. **Segunda pista solicitada** — fuente de referencia: `https://www.youtube.com/watch?v=t9KaSaGEwvI`.

La segunda pista **no se descarga ni se redistribuye desde YouTube**. El reproductor queda preparado para incorporarla como segunda entrada del catálogo cuando el proyecto disponga del archivo OGG autorizado. No se inventan título, autor ni licencia mientras esa información no esté archivada en el repositorio.

## Reproductor 0.17

`GestorMusica` mantiene hasta dos instancias durante un cambio de pista:

- una pista principal;
- una pista entrante a volumen cero durante el crossfade.

Cada pista tiene ganancia propia y una envolvente de volumen independiente. El sistema incorpora:

- **fade-in** real desde silencio al empezar la visita;
- **fade-out** al retirar una pista dentro del menú;
- **crossfade** preparado para catálogos de dos o más pistas;
- ducking durante apagones/transiciones de Nivel;
- ducking más fuerte durante La Suspensión;
- ducking contextual cuando aparece la presencia del fondo;
- continuidad entre subpantallas de una misma visita;
- vigilancia de instancias fantasma tras cambios de dispositivo/OpenAL;
- reconstrucción segura tras F3+T/recarga de recursos;
- corte inmediato al entrar en gameplay para impedir música de menú dentro de mundos/servidores.

El fade no sustituye al corte de seguridad: salir del menú hacia gameplay es una frontera de lifecycle y no puede dejar una cola audible varios ticks dentro del mundo.

## Mezcla

La música Jobs usa `SoundSource.MASTER` de forma deliberada. La controlan:

- volumen Maestro de Minecraft;
- volumen de música Jobs;
- volumen maestro del aviso / silencio con `M`.

No depende del slider Música vanilla. Mientras la música Jobs está habilitada durante la sesión, el `MusicManager` vanilla se detiene para evitar dos bandas sonoras simultáneas.

## Recursos y licencias

El JAR no genera ni activa resource packs musicales auxiliares. Los archivos empaquetados viven dentro de `assets/jobsmenu`.

La carpeta histórica `music/` no participa automáticamente en `processResources`: conservar un archivo de referencia allí no implica autorización para incluirlo en el JAR.

Nunca se debe:

- descargar una pista desde YouTube como parte del build;
- publicar un OGG sin autorización/licencia de redistribución archivada;
- inventar créditos;
- registrar en `sounds.json` una pista cuyo archivo no exista realmente.

## Pruebas de aceptación

1. Abrir Jobs desde arranque limpio: Absurdism entra progresivamente, sin golpe inicial.
2. Navegar Title → Options → Mods → Resource Packs → volver: no reinicia ni duplica la pista.
3. Cambiar volumen Jobs durante reproducción: transición suave, sin clicks.
4. Desactivar/reactivar música del aviso: la instancia anterior se retira y puede reconstruirse sin duplicados.
5. F3+T: no queda una instancia huérfana y la música vuelve de forma segura.
6. Alt+Tab/pausa de cliente: no confundir pausa del motor con instancia fantasma.
7. Entrar a mundo local/servidor: desde el primer tick jugable no se oye música del menú.
8. Salir del mundo/servidor: una visita nueva vuelve a iniciar su fade desde cero.
9. Cuando exista una segunda pista autorizada: verificar crossfade sin hueco ni superposición excesiva.
