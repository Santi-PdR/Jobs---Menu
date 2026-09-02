# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado **vigente**. El historial vive en `CHANGELOG.md` y auditorías de `docs/`; este archivo define lo que debe seguir siendo verdad al modificar el proyecto.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.20.0** |
| Artefacto esperado | **`jobsmenu-0.20.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |
| Alcance | Menús, interfaces, escena, audio, lore y accesibilidad. Sin gameplay. |

## 1. Reglas duras

1. `main` es la única rama entregable.
2. Todo JAR lleva versión: `jobsmenu-<mod_version>.jar`; nunca `jobsmenu-latest.jar`.
3. `gradle.properties` es la fuente de verdad de versión.
4. CI obligatorio: Java 17 → versión → fondos → auditoría estática → contratos UI/música → Forge build → publicación.
5. `dev-latest` contiene un único JAR versionado y sólo se actualiza desde `main` con pipeline verde.
6. Java visible permanece ASCII; cadenas para usuario viven en idiomas.
7. ES/EN conservan paridad de claves.
8. El rojo es exclusivo de Executores.
9. Accesibilidad, movimiento reducido y Bajo consumo tienen prioridad sobre decoración.
10. Ningún control visible puede tener un hitbox vanilla invisible superpuesto.
11. Pantallas de lógica compleja conservan comportamiento vanilla/Forge cuando eso protege compatibilidad.
12. PNG 10–17 son estáticos.
13. Audio de menú no puede sobrevivir dentro de gameplay.
14. Pistas musicales sólo se empaquetan con archivo autorizado y redistribuible.
15. Nuevas pistas se integran desde OGG subido al repo; el build no descarga audio de terceros.
16. Cambios visuales importantes se validan con CI y después requieren prueba manual dentro de Minecraft.
17. Despliegue normal siempre apunta a `test-1`.

## 2. Identidad visual

Jobs es un backrooms administrativo con peaje. El ocupante trabaja, junta dinero y paga para pasar al siguiente Nivel. La interfaz no es futurista: usa archivo, formulario, instalación vieja, marcas de inventario y amenaza sugerida.

La escena y la UI no comparten paleta por comodidad:

- escena: materiales y luz propios del Nivel;
- UI: papel frío, grafito, gris verdoso y tinta neutra.

Familias de superficie:

- **Formulario claro:** Options, Config Jobs, Idioma, controles y pausa.
- **Archivo oscuro:** Mundos, Multiplayer, Mods y Recursos.

## 3. Estado 0.20.0

La versión 0.20.0 reúne los pases visuales 0.19.x y añade un cierre avanzado sobre pantallas que seguían demasiado cercanas a vanilla.

### Mundos

- superficie central propia;
- búsqueda integrada;
- scrollbar Jobs;
- jerarquía más clara entre cabecera, lista y acciones;
- lógica vanilla de selección, creación, edición y borrado intacta.

### Multijugador

- tablero de servidores propio;
- tarjeta reforzada del servidor oficial;
- estado visible de selección/protección;
- scrollbar Jobs;
- acciones reales de Minecraft conservadas.

### Mods / Forge

- catálogo y panel de detalle separados visualmente;
- buscador integrado;
- scrollbar Jobs;
- título Forge cubierto sin bloquear información;
- búsqueda, orden, Config, logos, panel y carpeta de mods siguen siendo de Forge.

### Resource Packs

- doble archivador visual;
- dos bandejas claramente separadas;
- scrollbar Jobs;
- selección, orden, aplicar y carpeta siguen siendo de Minecraft.

### Idioma

- layout responsive para ancho/alto reducido y GUI Scale alto;
- hover, seleccionado e idioma aplicado diferenciados;
- códigos como badges;
- estado actual → pendiente antes de aplicar;
- buscador propio sin perder foco/teclado;
- recarga de recursos sigue usando Minecraft.

### Sonido

- bandeja interior de mezcla;
- raíles laterales y marcas de canal;
- scrollbar Jobs;
- opciones reales de `SoundOptionsScreen` intactas.

### Video

- ficha de calibración con visor, esquinas y regla visual;
- scrollbar Jobs en video vanilla;
- Embeddium conserva su pantalla real cuando está presente.

### Pausa

- mundo real permanece detrás;
- oscurecido por capas y profundidad lateral;
- sombra de hoja reforzada;
- guías y marcas administrativas estáticas;
- Escape reanuda;
- Condiciones abre Options Jobs;
- salir conserva la secuencia real de desconexión/guardado.

## 4. Widgets compartidos

Botones, toggles, sliders y renglones mantienen el pase de 73 mejoras visibles:

- marcos internos/externos;
- jerarquía PRINCIPAL/JOBS/TERMINAL;
- foco de ratón y teclado diferenciados;
- señales de presión sin mover hitbox;
- estados deshabilitados legibles;
- sliders con escala/porcentaje;
- toggles con ON/OFF físico;
- renglones principales con selección y continuidad visual;
- Bajo consumo elimina interpolaciones decorativas innecesarias.

## 5. Navegación y ciclo de vida

`SesionMenu` representa una visita completa.

- `TitleScreen` vanilla se redirige a `PantallaNivel` cuando menú propio está activo;
- pausa real se redirige a `PantallaEstancia`;
- Options, Multiplayer, Mundos y Mods se tematizan sólo dentro del flujo Jobs;
- redirecciones sensibles usan clase exacta;
- entrar a mundo/servidor cierra sesión y corta audio inmediatamente;
- salir de mundo/servidor/kick recupera `PantallaNivel`.

## 6. Multiplayer

Servidor fijado:

`JobsDosh.exaroton.me:56477`

Contrato:

- una sola entrada;
- nombre localizado;
- primera posición;
- protegida frente a edición/borrado desde Jobs;
- IP deduplicada;
- `Ghoul Outbreak` eliminado y no recreado.

## 7. Música y ambiente

Pista incluida actual:

- **Absurdism** → `assets/jobsmenu/sounds/musica/defecto.ogg`.

Segunda pista preparada:

- entrada: `music/menu_nueva.ogg`;
- id interno: `upon_the_hill_v2`;
- salida: `assets/jobsmenu/sounds/musica/tema_nuevo.ogg`;
- workflow: `.github/workflows/integrar_ogg_subido.yml`.

Comportamiento obligatorio:

- fade-in;
- fade-out dentro del menú;
- crossfade cuando haya 2+ pistas;
- ducking en transiciones/Suspensión/presencia;
- continuidad por subpantallas;
- watchdog de instancias fantasma;
- recuperación tras recarga;
- hard stop al entrar a gameplay.

La música usa `SoundSource.MASTER`: Maestro + volumen Jobs + volumen del aviso; no slider Música vanilla.

Cada Nivel mantiene BASE + CARÁCTER + ACTIVIDAD y eventos ocasionales. PNG 10–17 no reciben animación interna.

## 8. Compatibilidad

- Redirecciones principales por clase exacta.
- Listas complejas conservan lógica real.
- `ListasExpediente` modifica presentación y conserva rueda/click/drag.
- `PielVanillaJobs` no se aplica indiscriminadamente a pantallas externas.
- Embeddium conserva su UI de video.
- Mods que sustituyan totalmente pantallas pueden requerir integración específica.

## 9. Prueba y entrega

CI certifica:

- Java 17;
- política de versión;
- PNG/CRC/IDAT;
- recursos, idiomas y ASCII Java;
- contratos UI/música;
- Forge build 1.20.1;
- artefacto `jobsmenu-0.20.0.jar`;
- publicación a `dev-latest` desde `main`.

CI **no certifica estética dentro de Minecraft**. La prueba manual vigente está en `docs/checklist-manual.md`.

Destino único de despliegue:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

El PowerShell se entrega sólo después de: docs actualizados → CI de PR verde → merge → CI de main verde → `dev-latest` actualizado.
