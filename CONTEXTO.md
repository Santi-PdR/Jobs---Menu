# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado **vigente** del mod. El historial de implementaciones anteriores vive en `CHANGELOG.md` y `docs/`; este archivo describe lo que debe ser verdad hoy.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama de entrega | `main` |
| Mod id | `jobsmenu` |
| Nombre visible | Jobs · Aviso a los ocupantes |
| Versión actual | **0.12.0** |
| Artefacto esperado | **`jobsmenu-0.12.0.jar`** |
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
7. Todo Java permanece ASCII; acentos y texto visible pertenecen a `lang/es_es.json` y `lang/en_us.json`.
8. ES/EN tienen paridad estricta de claves.
9. El rojo es exclusivo de los Executores. No se usa como color genérico de botones peligrosos.
10. Accesibilidad y bajo consumo tienen prioridad sobre efectos decorativos.

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

- 0–9: recintos procedurales.
- 10–17: fondos de imagen validados y animados en runtime.

Todos usan la misma infraestructura de rotación, apagón, luz, música, camas ambientales, avisos, ronda, accesibilidad y estado de instalación.

Los fondos de imagen no son wallpapers estáticos: `PlantaImagen` aplica zoom/paneo lento, overscan y tratamiento por escena. Si un PNG falla, se usa fallback procedural; nunca se considera aceptable dejar la textura morado/negro.

## 4. Interfaz 0.12.0

La 0.12.0 extiende la identidad del mod a las interfaces de Minecraft que antes seguían pareciendo vanilla.

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

Estas pantallas usan `ChromeExpediente`, recinto vivo, papel, marco y pie de formulario; sus opciones reales siguen siendo las de Minecraft.

### 4.4 Widgets compartidos

- `ChromeExpediente`: superficie y contexto común.
- `BotonExpediente`: reemplazo visual/sonoro de botones del flujo propio.
- `SliderExpediente`: slider propio para controles simples como FOV.
- `ToggleExpediente`: interruptor con getter/setter reales.
- `ListasExpediente`: quita fondos vanilla de listas mediante reflexión defensiva; si falla, la pantalla sigue funcionando.
- `TransicionInterfazJobs`: gesto corto entre pantallas; con movimiento reducido se convierte en fade simple.

Las pantallas externas o auxiliares que no se sustituyen reciben una banda contextual durante la visita al menú.

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

## 7. Pruebas mínimas antes de una entrega

Además del CI:

- GUI scale 2, 3 y 4;
- ES y EN;
- Title → Opciones → todas las subpantallas → volver;
- Title → Multijugador → seleccionar/directo/agregar/editar/borrar/refrescar;
- cambio de idioma + recarga de recursos;
- resource packs;
- pause in-world → Opciones → volver;
- movimiento reducido / destellos reducidos / bajo consumo;
- Embeddium presente y ausente;
- navegación con mouse y teclado;
- 18 niveles y transición de fondos.

## 8. Documentación vigente

- `README.md`: resumen de la versión actual.
- `CHANGELOG.md`: historial.
- `KNOWN_ISSUES.md`: pruebas/riesgos pendientes.
- `docs/AUDITORIA_0.12.0_INTERFACES.md`: matriz de esta evolución.
- `docs/DESPLIEGUE.md`: instalación.
- `docs/checklist-manual.md`: verificación dentro de Minecraft.

Si un documento histórico contradice este archivo en versión, rama, cantidad de niveles o procedimiento de entrega, **manda CONTEXTO**.
