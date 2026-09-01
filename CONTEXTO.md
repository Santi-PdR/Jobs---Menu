# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado **vigente** del mod. El historial de implementaciones anteriores vive en `CHANGELOG.md` y `docs/`; este archivo describe lo que debe ser verdad hoy.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama de entrega | `main` |
| Mod id | `jobsmenu` |
| Nombre visible | Jobs · Aviso a los ocupantes |
| Versión actual | **0.13.0** |
| Artefacto esperado | **`jobsmenu-0.13.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |
| Alcance | Menús, interfaces, escena, audio, lore y accesibilidad. Sin gameplay. |

## 1. Reglas duras de entrega

1. **El JAR siempre lleva versión en el nombre.** Nunca volver a publicar `jobsmenu-latest.jar`. La forma es `jobsmenu-<mod_version>.jar`.
2. `gradle.properties` es la fuente de verdad de la versión. README, CONTEXTO y changelog se actualizan en la misma entrega.
3. `main` es la rama entregable. Trabajo estructural en rama aparte; merge sólo después de CI verde.
4. CI obligatorio: Java 17 → política de versión → fondos → verificador estático → Forge build → JAR versionado.
5. `dev-latest` puede seguir siendo una release rodante, pero debe contener **un solo JAR y ese JAR debe estar versionado**. El workflow elimina assets obsoletos antes de publicar.
6. Los fondos 10–17 deben superar validación PNG/CRC/IDAT. Runtime vuelve a probarlos con `NativeImage`.
7. **Los PNG 10–17 son estáticos por requisito del proyecto.** No se les agrega zoom, paneo, parallax, flicker, niebla móvil, scanlines, motas, presencia ni otra capa animada.
8. Todo Java permanece ASCII; acentos y texto visible pertenecen a `lang/es_es.json` y `lang/en_us.json`.
9. ES/EN tienen paridad estricta de claves.
10. El rojo es exclusivo de los Executores. No se usa como color genérico de botones peligrosos.
11. Accesibilidad y bajo consumo tienen prioridad sobre efectos decorativos.
12. Ningún control visible puede solaparse con otro control o conservar un hitbox vanilla invisible debajo.

## 2. Identidad

Jobs es un **backrooms con peaje**. El ocupante trabaja, junta dinero y paga para pasar al siguiente Nivel. Los Executores son cíclicos, inevitables y no se presentan como enemigos derrotables.

La interfaz habla con voz administrativa: seca, breve y burocrática. No hay UI futurista ni HUD de combate. El lenguaje visual base es:

- papel fotocopiado y archivado;
- tinta oscura;
- fluorescentes y luz del recinto;
- bordes, sellos y marcas de formulario;
- instalación vieja pero operativa;
- amenaza sugerida, no jumpscares.

Grafía canónica: **Executor / Executores**.

## 3. Niveles y escena

Hay 18 niveles:

- 0–9: recintos procedurales vivos.
- 10–17: fondos PNG suministrados, validados y **estáticos**.

Todos comparten rotación de Nivel, apagón, música, camas ambientales, avisos, ronda, accesibilidad y estado de instalación. Esto no significa que las imágenes 10–17 reciban animación interna.

`PlantaImagen` realiza sólo un cover centrado estable, valida el PNG con `NativeImage`, lee dimensiones reales y aplica una integración estática mínima. Si un PNG falla se usa fallback procedural; nunca es aceptable dejar la textura morado/negro.

Sobre niveles 10–17 no se ejecutan las capas animadas de materiales, dirección artística, tratamiento, presencia, motas, eventos ni pulido de cámara. Los apagones y transiciones entre Niveles se conservan porque pertenecen al estado general del menú, no a la animación de la imagen.

## 4. Interfaz 0.13.0

La base de la familia de interfaces nació en 0.12.0 y se consolida en 0.13.0 con controles más robustos, scrollbar propia y reglas de layout.

### 4.1 Principio de compatibilidad

Se tomó `Santi-PdR/GripeVerde` únicamente como **referencia de arquitectura de UI**: una familia coherente de pantallas, wrappers visuales sobre lógica vanilla y redirecciones por clase exacta. Su tema victoriano/cuarentena **no se copia**.

Regla:

- cuando la pantalla vanilla contiene lógica compleja o hooks de otros mods, se preserva y se tematiza alrededor;
- cuando la pantalla es principalmente navegación/jerarquía, Jobs puede reimplementarla con widgets propios;
- nunca se sustituye por `instanceof` una subclase de otro mod: las redirecciones automáticas usan clase exacta.

### 4.2 Familia propia Jobs

- `PantallaOpcionesJobs`: hub de Condiciones de estancia.
- `PantallaMultijugadorJobs`: registro de cuadrillas, conservando lista/ping/MOTD/LAN vanilla.
- `PantallaControlesJobs`: hub de mouse, teclas y hábitos.
- `PantallaIdiomaJobs`: selección/aplicación de idioma.
- `PantallaPielJobs`: ficha visible del ocupante.

### 4.3 Vanilla preservado con presentación Jobs

- Sonido.
- Video.
- Chat.
- Accesibilidad.
- Mouse.
- Teclas.
- Online.
- Resource packs.
- Ajustes del aviso.

Estas pantallas usan `ChromeExpediente`, recinto, papel, marco y pie de formulario; sus opciones reales siguen siendo las de Minecraft.

### 4.4 Widgets y comportamiento compartido

- `ChromeExpediente`: superficie y contexto común; el pie reserva el centro para navegación y divide formulario/Nivel a la izquierda y versión a la derecha.
- `BotonExpediente`: reemplazo visual/sonoro de botones del flujo propio.
- `SliderExpediente`: slider propio para controles simples como FOV.
- `ToggleExpediente`: interruptor con getter/setter reales.
- `ListasExpediente`: quita fondos vanilla de listas y dibuja una **scrollbar Jobs** sobre el mismo hitbox sin sustituir la lógica de scroll.
- `TransicionInterfazJobs`: gesto corto entre pantallas; con movimiento reducido se convierte en fade simple.

`EscuchaCliente` desactiva cualquier `Done` vanilla duplicado que sobreviva dentro de una pantalla Jobs. Un botón invisible no puede conservar un hitbox activo detrás de otro control.

### 4.5 Acceso a configuración

Hay dos entradas válidas a la misma pantalla de configuración:

- hub Jobs → ajustes del aviso;
- Forge → **Mods → Jobs Menu → Config**.

`JobsMenu` registra `ConfigScreenHandler.ConfigScreenFactory`; no se mantiene una segunda implementación de ajustes.

### 4.6 Reglas de layout

- Cabecera, contenido y footer deben tener zonas verticales separadas.
- El pie nunca ocupa el centro reservado a `Cerrar expediente`.
- Accesibilidad deja margen extra al final de la lista.
- El hub de Opciones sólo muestra el slider FOV duplicado si existe espacio real; en escalas extremas se omite antes de superponer controles.
- Las listas deben seguir siendo utilizables con rueda, click y drag aun cuando su presentación visual cambie.

Las pantallas externas o auxiliares que no se sustituyen reciben como máximo una banda contextual durante la visita al menú.

## 5. Sonido

El mod usa sus propios gestos de interfaz y no debe mezclar el click vanilla sobre widgets propios.

- UI: pasar, elegir, confirmar, volver, alternar, abrir, cerrar y negado.
- Ambiente: BASE + CARÁCTER + ACTIVIDAD por nivel.
- Eventos: ocasionales, ponderados y con silencios deliberados.
- Música: independiente de las camas del recinto.

Todo audio empaquetado debe ser mono. Los nuevos sonidos se agregan sólo si tienen función identificable; cantidad no equivale a calidad.

## 6. Accesibilidad / rendimiento

Controles vigentes incluyen:

- movimiento reducido;
- destellos reducidos;
- alto contraste;
- texto grande;
- papel limpio;
- guía de lectura;
- bajo consumo;
- perfil accesible.

Una interfaz nueva no puede saltarse estas preferencias. En particular, transiciones nuevas no pueden introducir flashes blancos ni movimiento obligatorio.

Los PNG 10–17 permanecen estáticos independientemente de estas opciones; `movimiento_reducido` sigue gobernando las escenas procedurales 0–9 y las transiciones compatibles.

## 7. Pruebas mínimas antes de una entrega

Además del CI:

- GUI scale 2, 3 y 4;
- ES y EN;
- Title → Opciones → todas las subpantallas → volver;
- comprobar que **Mods → Jobs Menu → Config** existe y abre los ajustes correctos;
- revisar que ningún `Done` vanilla invisible capture clicks;
- Accesibilidad: primera/última fila, scrollbar y Volver sin solapes;
- scrollbar Jobs en Sonido, Video, Chat, Accesibilidad, Mouse, Teclas, Online, Resource Packs y ajustes cuando haya contenido suficiente;
- rueda, click y drag de scrollbar;
- Title → Multijugador → seleccionar/directo/agregar/editar/borrar/refrescar;
- cambio de idioma + recarga de recursos;
- resource packs;
- pause in-world → Opciones → volver;
- movimiento reducido / destellos reducidos / bajo consumo;
- Embeddium presente y ausente;
- navegación con mouse y teclado;
- niveles 10–17: **ningún zoom, paneo, niebla móvil, flicker, scanline, motas o presencia visual**;
- 18 niveles y transición entre fondos.

## 8. Documentación vigente

- `README.md`: resumen de la versión actual.
- `CHANGELOG.md`: historial.
- `KNOWN_ISSUES.md`: pruebas/riesgos pendientes.
- `docs/AUDITORIA_0.12.0_INTERFACES.md`: arquitectura de la familia de interfaces.
- `docs/DESPLIEGUE.md`: instalación.
- `docs/checklist-manual.md`: verificación dentro de Minecraft.

Si un documento histórico contradice este archivo en versión, rama, cantidad de niveles, carácter estático de los PNG 10–17 o procedimiento de entrega, **manda CONTEXTO**.
