# Compatibilidad — Jobs Menu 0.40.0

## Perfil soportado

| Componente | Estado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente |
| Artefacto | `jobsmenu-0.40.0.jar` |

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

## Música — identidad 0.40

El catálogo musical contiene únicamente las tres pistas Jobs empaquetadas. Ya no existe fallback a `SoundEvents.MUSIC_MENU`.

Si una pista propia no puede resolverse:

1. no se crea la instancia entrante;
2. la pista actual no se retira prematuramente;
3. se programa reintento;
4. el log emite una advertencia única por visita/reload;
5. nunca se reproduce `minecraft:music.menu` como sustitución.

`CATALOGO` se construye una sola vez por JVM. Consultas de título/autor/cantidad y decisiones de crossfade reutilizan esa misma estructura.

El hard-stop musical pone volumen/ganancias a cero, marca la instancia detenida y llama también a `SoundManager.stop(instance)`.

## Resource reload — 0.39+

`RecargaRecursosCliente` usa generación atómica. El callback de recursos nunca manipula `SoundInstance` desde el executor de reload; el cierre se agenda en el hilo cliente y una generación nueva fuerza otra pasada si llegó durante el procesamiento anterior.

## Sesión y créditos

`SesionMenu` representa una visita completa y no se reinicializa al pasar entre pantallas Jobs. `musica_creditada.txt` habilita créditos del catálogo actual y se reevalúa tras reload.

## Multiplayer

`PantallaMultijugadorJobs` conserva `ServerSelectionList`, pinger, favicons, MOTD y detector LAN reales.

- ESC/Cancelar usan el padre Jobs directo y un guard idempotente.
- F5/Actualizar reconstruye Jobs directamente.
- la selección online se conserva por IP buscando una Entry nueva;
- conectar usa la propia pantalla Jobs como padre de `ConnectScreen`;
- cancelar/error antes del login vuelve a la lista Jobs;
- logout/kick/pérdida de conexión remota vuelve a Multiplayer Jobs.

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

## Scrollbars y listas

`ListasExpediente` sólo tematiza presentación. Wheel, drag, click, foco y tamaño de contenido pertenecen a la lista real. Reflection/listas se cachean y una scrollbar Jobs no se dibuja dos veces por frame.

## Fondos

- niveles 10–17: PNG estrictamente estáticos;
- niveles 18–31: JPG 1920×1080 con cover y respiración opcional mínima;
- Movimiento reducido/Bajo consumo/escena quieta congelan 18–31;
- F3+T reaplica filtrado sólo si Minecraft crea un objeto de textura nuevo.

## Idiomas

`es_ar`, `es_cl`, `es_ec`, `es_mx`, `es_uy` y `es_ve` reutilizan la traducción neutral `es_es` durante `processResources`. Cambiar idioma puede disparar resource reload y queda cubierto por el sistema de generaciones.

## Compatibilidad manual

Probar especialmente Embeddium/Oculus, mods que sustituyan `JoinMultiplayerScreen`, resource packs de GUI, mods de audio/SoundEngine, múltiples F3+T consecutivos, LAN/ping/favicons, GUI Scale extremos, Texto grande y salida/kick con reemplazos de `DisconnectedScreen`.

Regla general: **si tematizar exige duplicar la lógica de Minecraft/Forge, se conserva la lógica real y se reduce la intervención visual**.
