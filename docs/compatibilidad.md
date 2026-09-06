# Compatibilidad — Jobs Menu 0.39.0

## Perfil soportado

| Componente | Estado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente |
| Artefacto | `jobsmenu-0.39.0.jar` |

Jobs distingue entre pantallas que controla, pantallas vanilla/Forge cuya lógica conserva y pantallas de terceros que debe respetar.

## Frontera de gameplay

Con un mundo o servidor cargado:

- no se crea ni dibuja `TransicionInterfazJobs`;
- chat, inventario, contenedores y otras pantallas no Jobs quedan fuera de skin/banda/reemplazo global de click;
- música y ambiente del menú se detienen inmediatamente;
- Pausa/Config Jobs pueden mantener tema y feedback breve sin reactivar la sesión musical.

Video Settings queda fuera de Jobs incluso durante una visita de menú.

## Video Settings y mods gráficos

`PantallaOpcionesJobs` abre la pantalla de vídeo real. Jobs no reconstruye páginas de Embeddium/Sodium, no recoloca su lista y no dibuja capas posteriores encima.

Si un mod reemplaza completamente Video Settings por otra clase, Jobs la deja en manos de ese mod. La compatibilidad final debe probarse en el modpack real.

## Pantallas preservadas

Se conserva la lógica real de Minecraft/Forge en:

- Sonido;
- Video;
- Chat;
- Accesibilidad;
- Mouse/Teclas;
- Online;
- Resource Packs;
- Seleccionar mundo;
- Mods de Forge;
- Multiplayer y sus diálogos auxiliares.

Las capas Jobs nunca deben sustituir hitboxes, callbacks, datos de servidor, selección real o validación de estas pantallas.

## Resource reload — 0.39

`RecargaRecursosCliente` está registrado como listener de recursos del cliente. El callback puede llegar desde el executor de reload, por lo que no manipula `SoundInstance` directamente allí.

Flujo actual:

1. cada callback incrementa `GENERACION`;
2. se intenta programar una sola tarea en el hilo cliente;
3. la tarea invalida música/ambiente ligados al SoundEngine anterior;
4. si apareció una generación nueva mientras se procesaba la anterior, se agenda otra pasada.

Esto evita que una ráfaga idioma → F3+T → resource pack pierda la última invalidación.

## Sesión y navegación

`SesionMenu` representa una visita completa al flujo Jobs. Desde 0.39.0, `abrir()` retorna inmediatamente si la visita ya estaba activa: pasar Main → Options → Mods → Recursos no reinicializa el ambiente.

El mantenimiento de camas continúa por tick y puede recrearlas después de un resource reload.

## Créditos musicales

`GestorMusica` sólo muestra el bloque de créditos cuando existe `assets/jobsmenu/musica_creditada.txt`. En 0.39.0 ese marcador vuelve a existir y enumera las tres pistas acreditadas actuales.

El marcador no contiene audio; sólo habilita la presentación del catálogo ya empaquetado.

## Multiplayer

`PantallaMultijugadorJobs` conserva `ServerSelectionList`, pinger, favicons, MOTD y detector LAN reales.

- ESC/Cancelar usan el padre Jobs directo y un guard idempotente.
- F5/Actualizar reconstruye Jobs directamente.
- la selección online se conserva por IP y se busca una Entry nueva en la lista reconstruida;
- una Entry vieja nunca se reutiliza;
- LAN continúa siendo efímero y depende del detector nuevo;
- conectar usa la propia pantalla Jobs como padre de `ConnectScreen`;
- cancelar/error antes del login vuelve a la lista Jobs;
- después de una sesión remota, logout/kick/pérdida de conexión vuelve a Multiplayer Jobs.

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

## Scrollbars y listas

`ListasExpediente` sólo tematiza la presentación. La lista real conserva wheel, drag, click, foco y tamaño de contenido.

Desde 0.38.0:

- fields reflection se cachean por clase;
- listas se cachean por Screen viva;
- scrollbar Jobs se deduplica por frame;
- cachés se liberan al cerrar la Screen.

Si reflection no puede resolver una lista modificada por otro mod, Jobs debe degradar visualmente antes que romper input.

## Fondos

- niveles 10–17: PNG estrictamente estáticos;
- niveles 18–31: JPG 1920×1080 con cover y respiración opcional mínima;
- Movimiento reducido/Bajo consumo/escena quieta congelan 18–31;
- F3+T puede reconstruir objetos de textura y Jobs reaplica el filtrado sólo a la nueva instancia.

## Idiomas

`es_ar`, `es_cl`, `es_ec`, `es_mx`, `es_uy` y `es_ve` reutilizan la traducción neutral `es_es` durante `processResources` para evitar pantallas mitad español/mitad inglés.

Cambiar idioma puede disparar resource reload; 0.39.0 protege ese camino con generación.

## Compatibilidad que sigue siendo manual

Probar específicamente:

- Embeddium/Oculus y addons de vídeo;
- mods que sustituyan por completo `JoinMultiplayerScreen`;
- resource packs de GUI agresivos;
- mods de audio que alteren `SoundEngine`;
- múltiples F3+T/reloads consecutivos;
- LAN/ping/favicons tras varias reconstrucciones;
- GUI Scale extremos y Texto grande;
- salida/kick de servidor con mods que sustituyan `DisconnectedScreen`.

La regla general es: **si tematizar exige duplicar la lógica de Minecraft/Forge, se conserva la lógica real y se reduce la intervención visual**.
