# Música del menú

## Estado vigente — 0.18.0

Jobs usa un reproductor de **sesión**, no música ligada a una pantalla concreta. La visita al menú conserva la mezcla al abrir Opciones, Mods, Mundos, Multijugador, Idioma o Resource Packs; entrar a un mundo/servidor corta el audio inmediatamente.

### Catálogo

1. **Absurdism** — pista incluida actualmente en `assets/jobsmenu/sounds/musica/defecto.ogg` y registrada mediante `musica.tema`.
2. **upon_the_hill_v2** — segunda ranura preparada. Su archivo de entrada será `music/menu_nueva.ogg` y el recurso final generado será `assets/jobsmenu/sounds/musica/tema_nuevo.ogg`.

La segunda pista no depende de una descarga remota. El flujo oficial consiste en subir el OGG al repositorio y dejar que GitHub Actions haga validación, normalización, registro y build.

## Reproductor de sesión

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

## Integración de la próxima pista

La única acción manual prevista es subir:

`music/menu_nueva.ogg`

El workflow `.github/workflows/integrar_ogg_subido.yml`:

1. verifica que el archivo exista y sea Vorbis real;
2. comprueba duración mínima;
3. normaliza loudness a -18 LUFS integrados y limita true peak;
4. genera Vorbis 48 kHz estéreo;
5. copia el recurso final al árbol `assets/jobsmenu`;
6. registra `musica.tema_nuevo` en `sounds.json`;
7. añade `MUSICA_TEMA_NUEVO` a `SonidosNivel`;
8. añade `upon_the_hill_v2` al catálogo de `GestorMusica`;
9. ejecuta verificadores del proyecto;
10. compila con Java 17;
11. publica los cambios generados sólo si todo lo anterior termina bien.

No hace falta editar Java, JSON o Gradle manualmente para esa pista.

## Recursos y licencias

El JAR no genera ni activa resource packs musicales auxiliares. Los archivos empaquetados viven dentro de `assets/jobsmenu`.

Nunca se debe:

- descargar una pista desde YouTube como parte del build;
- publicar un OGG sin autorización/licencia de redistribución correspondiente;
- inventar créditos;
- registrar en `sounds.json` una pista cuyo archivo no exista realmente en la integración final.

## Pruebas de aceptación

1. Abrir Jobs desde arranque limpio: Absurdism entra progresivamente, sin golpe inicial.
2. Navegar Title → Options → Mods → Resource Packs → volver: no reinicia ni duplica la pista.
3. Cambiar volumen Jobs durante reproducción: transición suave, sin clicks.
4. Desactivar/reactivar música del aviso: la instancia anterior se retira y puede reconstruirse sin duplicados.
5. F3+T: no queda una instancia huérfana y la música vuelve de forma segura.
6. Alt+Tab/pausa de cliente: no confundir pausa del motor con instancia fantasma.
7. Entrar a mundo local/servidor: desde el primer tick jugable no se oye música del menú.
8. Salir del mundo/servidor: una visita nueva vuelve a iniciar su fade desde cero.
9. Cuando exista `music/menu_nueva.ogg`: verificar integración automática y crossfade sin hueco ni superposición excesiva.
