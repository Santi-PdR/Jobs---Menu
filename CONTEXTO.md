# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado **vigente**. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`; este archivo define lo que debe seguir siendo verdad al modificar el proyecto.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.19.0** |
| Artefacto esperado | **`jobsmenu-0.19.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |
| Alcance | Menús, interfaces, escena, audio, lore y accesibilidad. Sin gameplay. |

## 1. Reglas duras

1. `main` es la única rama entregable. No dejar ramas de trabajo huérfanas después de integrar cambios.
2. Todo JAR lleva versión: `jobsmenu-<mod_version>.jar`. Nunca `jobsmenu-latest.jar`.
3. `gradle.properties` es la fuente de verdad de la versión.
4. CI obligatorio: Java 17 → versión → fondos → auditoría estática → contratos UI/música → Forge build → publicación.
5. `dev-latest` contiene un único JAR versionado y sólo se actualiza desde `main` cuando el pipeline llega a build/publicación.
6. Java visible permanece ASCII; cadenas de usuario viven en idiomas.
7. ES/EN conservan paridad de claves.
8. El rojo es exclusivo de los Executores.
9. Accesibilidad, movimiento reducido y bajo consumo tienen prioridad sobre efectos decorativos.
10. Ningún control visible puede tener un hitbox vanilla invisible superpuesto.
11. Pantallas de lógica compleja conservan comportamiento vanilla cuando eso protege compatibilidad.
12. Los PNG 10–17 son estáticos: no zoom, paneo, parallax, flicker, niebla móvil, scanlines animadas, motas ni presencia sobre la imagen.
13. El audio del menú no puede sobrevivir dentro de gameplay ni un tick audible.
14. Una pista musical sólo se empaqueta si existe el archivo y su redistribución está autorizada/documentada.
15. La integración de nuevas pistas se hace desde un OGG subido al repositorio; el build no descarga audio de terceros.
16. El perfil **Bajo consumo** debe evitar interpolaciones/pulsos decorativos continuos también en widgets compartidos, no sólo en fondos y transiciones.

## 2. Identidad

Jobs es un **backrooms con peaje**: el ocupante trabaja, junta dinero y paga para pasar al siguiente Nivel. Los Executores son cíclicos, inevitables y no se presentan como enemigos derrotables.

La interfaz usa voz administrativa seca y breve. No es un HUD futurista. Su lenguaje visual se basa en archivo, formulario, instalación vieja, marcas de inventario y amenaza sugerida.

Grafía canónica: **Executor / Executores**.

## 3. Separación escena / interfaz

La escena y la UI no comparten paleta por comodidad.

### Escena

Puede usar paredes amarillas, fluorescente cálido, humedad, alfombra y materiales propios del Nivel. Esos colores describen el lugar físico.

### Interfaz

Usa una familia neutral independiente:

- `UI_PAPEL` / `UI_PAPEL_FOCO`;
- `UI_TINTA` / `UI_TINTA_TENUE`;
- `UI_ACENTO` / `UI_ACENTO_FUERTE`;
- `ARCHIVO_FONDO`;
- `ARCHIVO_SUPERFICIE` / `ARCHIVO_SUPERFICIE_FOCO`;
- `ARCHIVO_ACENTO` / textos de archivo.

Los widgets y marcos administrativos no deben volver a usar `PARED`, `PARED_ALTA` o `FLUOR` como color de foco/superficie. `tools/verificar_ui_musica.py` protege este contrato en CI para los componentes compartidos principales.

Desde 0.18.0 el pulido global añade foco/hover y transiciones más sobrias sin modificar hitboxes. En 0.19.0 botones, toggles y sliders compartidos también respetan **Bajo consumo** sin tween de foco frame a frame.

### Superficies

- Formularios compactos: papel frío, tinta oscura.
- Archivos grandes (Mundos, Multiplayer, Mods, Recursos): grafito y gris verdoso.
- Una pantalla externa no se recolorea a ciegas si eso puede romper su render.

## 4. Navegación y ciclo de vida

`SesionMenu` representa una visita completa. Cambiar de `PantallaNivel` a Options, Mods, Resource Packs u otra hija no inicia otra sesión.

Reglas:

- `TitleScreen` vanilla se redirige a `PantallaNivel` cuando el menú propio está activo;
- pausa real se redirige a `PantallaEstancia`;
- Options, Multiplayer, Mundos y Mods se tematizan sólo dentro del flujo Jobs;
- redirecciones sensibles usan clase exacta para no capturar subclases de mods externos;
- al entrar a mundo/servidor se cierra la sesión y se corta audio;
- al salir de mundo/servidor/kick se recupera `PantallaNivel`, no el título vanilla.

## 5. Multiplayer

Servidor fijado:

`JobsDosh.exaroton.me:56477`

Contrato:

- una sola entrada;
- nombre localizado;
- primera posición;
- protegida frente a edición/borrado desde Jobs;
- IP deduplicada;
- `Ghoul Outbreak` legado eliminado y no recreado.

La lista, ping, LAN, MOTD y conexión siguen siendo de Minecraft.

## 6. Música

`GestorMusica` es un reproductor de sesión con catálogo.

Pista incluida actual:

- **Absurdism** → `assets/jobsmenu/sounds/musica/defecto.ogg` mediante `musica.tema`.

Segunda pista preparada:

- identificador interno `upon_the_hill_v2`;
- archivo de entrada esperado: `music/menu_nueva.ogg`;
- recurso final generado: `assets/jobsmenu/sounds/musica/tema_nuevo.ogg`;
- integración automática mediante `.github/workflows/integrar_ogg_subido.yml`.

La única acción manual para completar esa integración es subir `music/menu_nueva.ogg` a `main`. El workflow valida Vorbis, normaliza loudness/true peak, registra el evento, añade la pista al catálogo, ejecuta las verificaciones y compila Java 17. Si falla cualquier etapa, no publica los cambios generados.

Comportamiento obligatorio:

- fade-in desde silencio;
- fade-out al retirar una pista dentro del menú;
- crossfade automático cuando el catálogo tenga 2+ pistas;
- ducking en transición de Nivel, La Suspensión y presencia;
- una sola sesión musical aunque se reconstruyan pantallas;
- continuidad por Options/Mods/Recursos;
- watchdog de instancias fantasma;
- recuperación tras F3+T/recarga;
- hard stop al entrar a gameplay.

La música usa `SoundSource.MASTER`: depende de Maestro de Minecraft, volumen de música Jobs y volumen del aviso, no del slider Música vanilla.

## 7. Ambiente

Cada Nivel procedural mantiene BASE + CARÁCTER + ACTIVIDAD y eventos ocasionales. La música no sustituye el ambiente ni viceversa.

Los niveles 10–17 usan PNG suministrados y estáticos. Sus apagones/cambios de Nivel siguen existiendo porque pertenecen al estado del menú, no a animación interna del PNG.

## 8. Pantallas cubiertas

Familia Jobs propia o tematizada:

- Title / `PantallaNivel`;
- Pausa / `PantallaEstancia`;
- Options;
- Config Jobs;
- Multijugador;
- Mundos;
- Mods / Forge;
- Controles;
- Mouse;
- Teclas;
- Idioma;
- Piel;
- Sonido;
- Video;
- Chat;
- Accesibilidad;
- Online;
- Resource Packs.

Embeddium conserva su pantalla propia cuando está presente.

## 9. Pruebas mínimas

Además del CI:

- GUI scale 2, 3 y 4;
- 854×480 y ventanas estrechas;
- ES, EN y Español (Uruguay);
- Title → Options → Config → todas las categorías → volver;
- Mods → Jobs Menu → Config;
- teclado Tab/Enter/Espacio/Escape y Ctrl+F donde corresponde;
- Direct Connect / Add Server / confirmaciones;
- scrollbar: rueda, click, drag y límites reales;
- Multiplayer: oficial único + acciones vanilla;
- Mundos: previews + crear/editar/borrar/recrear;
- Mods: búsqueda, orden, Config, detalles y carpeta;
- Resource Packs: dos listas y aplicar;
- F3+T y Alt+Tab con audio;
- entrada/salida de mundo y servidor sin audio Jobs dentro de gameplay;
- Absurdism con fade-in sin duplicación;
- cuando exista `menu_nueva.ogg`, comprobar crossfade y nivel percibido entre pistas;
- niveles 10–17 inmóviles;
- Bajo consumo: sliders, toggles y botones cambian foco sin interpolación decorativa.

## 10. Documentación vigente

- `README.md`: resumen de **0.19.0**.
- `CONTEXTO.md`: este contrato.
- `docs/AUDITORIA_0.18.0_PROFESIONAL.md`: auditoría base del pase profesional anterior.
- `docs/musica.md`: sistema musical y política de pistas.
- `music/LEEME.txt`: regla de una sola subida para la próxima pista.
- `KNOWN_ISSUES.md`: riesgos que requieren Minecraft real.
- `CHANGELOG.md`: historial.
- `docs/DESPLIEGUE.md`: instalación.
- `docs/compatibilidad.md`: convivencia con otros mods.
- `docs/checklist-manual.md`: aceptación manual.
