# CONTEXTO — Jobs · Aviso a los ocupantes

> Documento maestro vigente de Jobs Menu. Los documentos históricos de `docs/` conservan las decisiones de versiones anteriores; este archivo describe el estado actual y las reglas que no se pueden romper.

## 1. Identidad del proyecto

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama de entrega | `main` |
| Mod id | `jobsmenu` |
| Nombre visible | Jobs · Aviso a los ocupantes |
| Paquete Java | `com.santipdr.jobsmenu` |
| Versión actual | **0.11.0** |
| Plataforma | Minecraft **1.20.1** · Forge **47.x** · Java **17** |
| Lado | **Cliente** |
| Alcance | Menús, escena viva, audio, música, lore y presentación. **Sin gameplay ni cambios de servidor.** |
| Niveles | **18**, del 0 al 17 |

### Regla obligatoria de versión

**Jobs Menu siempre debe tener una versión en el nombre del JAR entregado, publicado e instalado.**

Correcto:

```text
jobsmenu-0.11.0.jar
```

Prohibido:

```text
jobsmenu-latest.jar
```

`gradle.properties` (`mod_version`) es la fuente de verdad. La regla no depende de memoria humana: `tools/verificar_version.py` y GitHub Actions deben fallar si la entrega vuelve a perder la versión en el nombre.

La release de desarrollo conserva el tag rodante `dev-latest`, pero el **asset** siempre es versionado.

---

## 2. Canon del servidor

Jobs es un **backrooms con peaje**. El ocupante está atrapado en un Nivel y la salida al siguiente cuesta dinero. Trabaja, junta la tarifa, encuentra otros supervivientes y sobrevive a los **Executores**.

Los cuatro ejes que el menú debe transmitir:

1. **Encierro.** Se está dentro de algo que continúa más allá de la pantalla.
2. **Trabajo y dinero.** El ocupante no es un héroe: es mano de obra ahorrando para salir.
3. **La salida existe y cuesta.** Es el motor narrativo de la interfaz.
4. **Executores.** Son inmortales, cíclicos e inevitables. No se derrotan; se sobreviven.

Grafía canónica: **Executor / Executores**. En inglés: **Executor / Executors**.

La voz de la interfaz es la de **la administración del Nivel**: breve, fría, burocrática, con humor negro seco y amenaza implícita. No usar jerga de desarrollo, chistes meta ni épica heroica en textos que ve el jugador.

Todo texto visible debe vivir en `assets/jobsmenu/lang/*.json`; no se dejan literales de UI repartidos por Java.

---

## 3. Dirección visual

La interfaz principal es un **aviso fotocopiado pegado a la pared del recinto**. El jugador no debería sentir que mira una capa web encima de Minecraft, sino que está frente a un documento administrativo dentro del lugar.

Reglas:

- El papel tiene volumen, desgaste y sombra, pero nunca debe parecer pergamino fantástico.
- El rojo se reserva a **Executores / peligro relacionado con Executores**.
- La iluminación pertenece al recinto. La hoja y la tinta también se apagan cuando se corta la luz.
- Los efectos nuevos deben reforzar material, escala, profundidad, lectura, identidad o interacción. Si no cumplen ninguna función, sobran.
- No usar postprocesado pesado sólo para impresionar; el menú debe seguir siendo compatible con modpacks y hardware modesto.
- Accesibilidad tiene prioridad sobre animación: movimiento reducido, destellos reducidos, papel limpio, alto contraste y bajo consumo nunca pueden dejar una pantalla rota.

### Papel

`client/ui/HojaPapel.java` es la fuente común para la hoja principal y pantallas relacionadas. En 0.11.0 incluye:

- sombra en dos planos;
- degradado mínimo de luz;
- marcas deterministas de fotocopia;
- roce de borde;
- cinta con volumen visual;
- variante `papel_limpio` sin ruido decorativo.

### Interacción

Los botones principales son `RenglonTablon`: filas de formulario, no cápsulas vanilla. Conservan hitbox real, navegación por teclado, narración y respuesta sonora propia. La salida irreversible exige confirmación y queda visualmente separada.

---

## 4. Los 18 niveles

### Procedurales: 0–9

1. Nivel 0 — Sección administrativa.
2. Nivel 1 — Depósito.
3. Nivel 2 — Pasillos de servicio.
4. Nivel 3 — Las piscinas.
5. Nivel 4 — La sala.
6. Nivel 5 — La biblioteca.
7. Nivel 6 — El invernadero.
8. Nivel 7 — Las catacumbas.
9. Nivel 8 — La cisterna.
10. Nivel 9 — El salón del trono.

Cada uno usa una `Planta` propia y comparte materiales, tratamiento, dirección artística, presencia, eventos y pulido global.

### Fondos suministrados: 10–17

Los niveles 10–17 usan `PlantaImagen` y están integrados como escenas reales, no como posters estáticos.

- Todos los PNG pasan validación fuerte de firma, chunks, CRC, IDAT, descompresión, dimensiones e IEND antes del build.
- `PlantaImagen` valida nuevamente con `NativeImage` en runtime y obtiene las dimensiones del archivo real.
- Si un recurso falta o no decodifica, se usa fallback procedural y se registra el error; no debe aparecer el cubo morado/negro.
- Tienen zoom/paneo cinematográfico lento, vignette, respiración de luz y efectos específicos por escena.
- Los efectos respetan movimiento reducido y bajo consumo.

Los 18 niveles reciben `PulidoEscena`, que unifica el lenguaje de cámara/instalación con halo de luz, barrido de exposición sutil y transición física.

---

## 5. Transiciones y eventos

`RotacionNiveles` captura un único estado por frame para que nivel, luz, render y audio no se contradigan.

### Cambio de Nivel

El Nivel no se funde con el siguiente como una presentación de diapositivas: **falla la instalación eléctrica**.

- preaviso con titileos;
- apagado;
- cambio de recinto en oscuridad;
- encendido irregular;
- audio sincronizado con los mismos instantes visuales.

0.11.0 añade masa visual: la oscuridad entra desde bordes y la recuperación deja una banda de luz breve antes de estabilizarse.

### La Suspensión

Evento raro de apagón largo. No es jumpscare ni gameplay. Reduce luz y mezcla sin introducir flashes nuevos. Su tratamiento tiene prioridad sobre la transición normal cuando coinciden.

### Ronda de Executores

La cuenta regresiva es ambiental y no controla mecánicas reales. Bajo el umbral de inminencia usa el lenguaje de alerta de Executores. Con destellos reducidos permanece legible sin parpadeos.

### Presencia

La presencia de fondo es ambigua, lejana y rara. No corre hacia cámara ni hace jumpscares. Se desactiva con movimiento reducido.

---

## 6. Audio

El audio se diseña como un lugar, no como una lista de efectos.

Cada Nivel mantiene tres papeles:

- **BASE:** volumen/aire estable del sitio.
- **CARÁCTER:** lo que está funcionando o moviéndose.
- **ACTIVIDAD:** sucesos lejanos que rompen la repetición.

`GestorAmbiente` añade eventos ocasionales con ventanas largas y silencios deliberados. Los niveles 10–17 reutilizan material sonoro compatible de 0–9, pero con mezcla, densidad y timing propios.

En 0.11.0 BASE y CARÁCTER reciben una **microderiva tonal de ciclo largo** para que los bucles no vuelvan idénticos. ACTIVIDAD conserva tono estable porque representa objetos reconocibles.

Reglas duras:

- el ambiente no debe sonar como metrónomo;
- los eventos no se repiten inmediatamente;
- el apagón modifica las capas de forma distinta, no baja todo por igual;
- la música y el ambiente tienen lifecycle por visita, no por reconstrucción de pantalla;
- no añadir sonidos por cantidad: cada pieza debe tener una función acústica clara.

La música de terceros sólo se redistribuye con permiso explícito. La pista de fábrica debe ser legalmente redistribuible.

---

## 7. Pantallas e interfaces

### Menú principal

`PantallaNivel` contiene:

- cabecera del aviso;
- Nivel actual y tarifa narrativa;
- cuatro filas de acción;
- avisos rotativos;
- reloj de ronda;
- rótulo del Nivel;
- estado de instalación opcional;
- crédito de música cuando corresponde.

Atajos vigentes del menú:

- **M:** silenciar/restaurar el audio del aviso.
- **F:** adelantar un Nivel cuando la rotación está activa, respetando la transición.

Las herramientas internas de diagnóstico no se muestran como funciones públicas del aviso.

### Opciones

Los ajustes propios viven dentro del flujo de Opciones de Minecraft mediante `PantallaAjustesAviso`; no existe un segundo ecosistema de configuración aislado.

`nivel_fijo` debe aceptar **0–17**.

### Pausa

La pausa propia conserva el lenguaje visual del aviso sin impedir las funciones esperadas de Minecraft.

---

## 8. Accesibilidad y rendimiento

Opciones principales:

- escena viva;
- movimiento reducido;
- destellos reducidos;
- alto contraste;
- texto grande;
- papel limpio;
- guía de lectura;
- interfaz mínima;
- bajo consumo;
- perfil accesible;
- presencia;
- eventos ambientales;
- La Suspensión;
- rotación y Nivel fijo 0–17;
- volumen maestro, música y ambiente.

`bajo_consumo` elimina primero capas de aire/coste visual, no identidad arquitectónica ni controles.

---

## 9. Build, CI y despliegue

### Rama de entrega

`main` es la única rama de entrega. Los cambios grandes se desarrollan en `arena/**` y sólo se integran después de pasar CI.

### Pipeline obligatorio

GitHub Actions ejecuta:

1. checkout;
2. Java 17;
3. resolución de `mod_version`;
4. `python3 tools/verificar_version.py`;
5. `python3 tools/verificar_fondos.py`;
6. `python3 tools/verificar.py`;
7. build Forge/Gradle;
8. preparación del JAR versionado;
9. artifact del workflow;
10. publicación en `dev-latest` sólo desde `main`.

Un CI rojo **no se entrega**.

### Destino de prueba

Único destino local documentado:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods
```

El usuario no necesita compilar localmente para una prueba normal. `docs/DESPLIEGUE.md` consulta `dev-latest`, descubre el asset versionado, lo descarga y valida antes de borrar una instalación anterior.

Nunca se elimina el JAR instalado antes de validar el reemplazo.

---

## 10. Reglas de trabajo vigentes

1. **Versión en el nombre, siempre.** Ningún JAR de entrega puede llamarse sólo `latest` o equivalente.
2. **Docs sincronizados.** Si cambia `mod_version`, se actualizan como mínimo `gradle.properties`, `README.md`, `CONTEXTO.md`, `CHANGELOG.md` y la documentación de despliegue cuando corresponda.
3. **CI antes de merge.** Rama de trabajo primero; `main` sólo recibe cambios que compilaron.
4. **Fondos raster validados de verdad.** No alcanza con firma PNG: se validan CRC e IDAT.
5. **Sin cubo morado/negro silencioso.** Runtime valida con `NativeImage` y usa fallback.
6. **Accesibilidad primero.** Movimiento/destellos reducidos deben ser respetados por cualquier efecto nuevo.
7. **No romper hitboxes por decoración.** El dibujo interactivo debe coincidir con la zona clicable.
8. **Un snapshot temporal por frame.** Render, luz y eventos que dependen de la rotación comparten estado.
9. **Audio con función.** No se agregan pistas, capas o gestos sin una razón perceptiva concreta.
10. **Rojo reservado a Executores.** No convertirlo en color general de interfaz.
11. **No tocar gameplay.** Items, entidades, economía, comandos y lógica de servidor siguen fuera de alcance.
12. **Prueba manual sigue siendo necesaria.** Compilar no demuestra que algo se vea o suene bien dentro de Minecraft.

---

## 11. Herramientas vigentes

| Archivo | Función |
|---|---|
| `tools/verificar_version.py` | Hace cumplir SemVer y el nombre versionado del JAR/release. |
| `tools/verificar_fondos.py` | Verifica PNG 10–17 incluyendo CRC e IDAT decodificable. |
| `tools/verificar.py` | Auditoría estática de recursos, idiomas, configuración y conexiones. |
| `tools/vista_previa.py` | Espejo visual para revisar composición sin abrir Minecraft. |
| `tools/sonidos.py` | Generador de piezas sintetizadas del mod. |

---

## 12. Referencias vigentes

- `README.md` — estado resumido de la versión actual.
- `CHANGELOG.md` — cambios por versión.
- `KNOWN_ISSUES.md` — pruebas reales aún pendientes.
- `docs/AUDITORIA_0.11.0.md` — auditoría profesional de esta evolución.
- `docs/DESPLIEGUE.md` — instalación en `test-1`.
- `docs/DIRECCION_ARTISTICA.md` — criterios visuales.
- `docs/compatibilidad.md` — convivencia con otros mods.
- `docs/checklist-manual.md` — pruebas dentro de Minecraft.

Los documentos históricos permanecen como registro, pero si contradicen este archivo sobre el estado vigente, **manda CONTEXTO.md**.
