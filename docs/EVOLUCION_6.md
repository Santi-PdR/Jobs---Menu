# Evolución 6 — robustez, continuidad, bajo consumo y dirección artística

**Fecha:** 2026-08-29
**Proyecto:** Jobs · Aviso a los ocupantes
**Versión de código:** 0.10.0 (sin bump: el build sigue pendiente, ver KNOWN_ISSUES)
**Minecraft:** 1.20.1 · Forge 47.x · Java 17 objetivo
**Rama fijada de esta sesión:** `arena/01a04ff1-jobs-menu`
**Base:** `811586e` (merge de `arena/01a04e24-jobs-menu`)
**Backups:** `backup-A-inicial-0.10.0`, `backup-B-tecnica-ui-sonido-config`, `backup-C-final-evolucion6` (ver `backups/README.md`)

## Estado real

Esta evolución continúa sobre un snapshot estable y **no se presenta como
terminada solo porque la auditoría estática pase**. La verificación estática
está en verde (`python3 tools/verificar.py` → 0 avisos, 0 fallos). El
`gradle-wrapper.jar` (Gradle 8.1.1) quedó versionado en la rama. El `clean build`
con Java 17 y la
prueba dentro de Minecraft siguen pendientes: en este entorno no hay JDK 17 ni
wrapper y la red hacia gradle.org / maven.minecraftforge.net está bloqueada
(detalles y procedimiento en `KNOWN_ISSUES.md` y en el informe final).

## Etapa 0 — Seguridad y puntos de recuperación

- Revisión de ramas/commits/backups: solo `main` y la rama de trabajo; sin
  backups previos; sin merges de ramas antiguas.
- `14efd06` crea `backups/README.md` (reglas de los tres puntos) y el tag
  `backup-A-inicial-0.10.0` sobre el estado inicial `811586e`.
- Todo se sube a GitHub junto con la rama.

## Etapa 1 — Configuración, continuidad del ambiente y bajo consumo

Dos commits (`5702eb2`, `ae48a07`) con tres frentes:

1. **Configuración ampliada y protegida.** `ConfigTurno` gana
   `duracion_estancia` (15–90 s), `bajo_consumo` y `perfil_accesible`, y todos
   sus accesos quedan blindados contra el SPEC sin cargar. El perfil accesible
   enciende juntas las cuatro opciones de accesibilidad y se desactiva solo
   cuando se tocan a mano, con persistencia inmediata.
2. **Continuidad por visita.** El ambiente ya no pertenece a una pantalla:
   `SesionMenu` lo abre y cierra de forma idempotente, y `mantenerCamas()` del
   tick del cliente mantiene vivas las camas del nivel actual aunque el
   jugador esté en Opciones o Mods. El salto manual de nivel (F) con
   antirrepetición usa el nuevo `RotacionNiveles.adelantar()`.
3. **Bajo consumo real en el render.** `EscenaNivel` y `TratamientoEscena`
   suprimen polvo, grano, presencia y respiración de cámara cuando
   `bajo_consumo` está activo; el recinto y su audio quedan intactos.

Junto a esto, la vigilancia de instancia fantasma de `GestorMusica` quedó
blindada contra pausa y falta de foco (no se desarma con falsas alarmas), y se
añadió el diagnóstico oculto Ctrl+D para volcar el estado de audio al LOG.

## Etapa 2 — Dirección artística: una mejora nueva por fondo

Antes de tocar fondos se creó el **Backup B** (`backup-B-tecnica-ui-sonido-config`).
Después, un commit por escenario, cada uno con su espejo en
`tools/vista_previa.py` y su fila marcada en `docs/AUDITORIA_FONDOS_50X10.md`:

1. **Trono (nivel 9)** — `08ed9bf`. Rediseño desde cero: tarima 1.18, estrado
   de seis escalones monumentales, ábside con tres dovelas concéntricas (oro
   reservado al arco interno), hueco de corona ausente grande y oscuro, y
   estandartes torcidos con sesgo determinista. El trono es el foco
   inequívoco.
2. **Administración (nivel 0)** — `52eea3f`. Abertura de mantenimiento lateral
   con marco, bisagras e interior que no termina en la pared.
3. **Depósito (nivel 1)** — `d03c347`. Lona de carga caída: la única forma
   blanda de la nave, plegada en bandas con costura y borde de luz.
4. **Servicio (nivel 2)** — `f50b8dc`. Bandeja de cables con colgadores del
   techo y un bucle de cable suelto.
5. **Natatorio (nivel 3)** — `174eb8a`. Sarro bajo el rebosadero, sembrado por
   la clave del nivel: el agua que se evaporó dejó su mineral.
6. **Sala de piedra (nivel 4)** — `003edbd`. Dovelas en el arco más cercano:
   la curva se construye por piezas con junta perpendicular.
7. **Biblioteca (nivel 5)** — `8acf949`. Arco de acceso entre estantes: la
   hilera se interrumpe y el pasillo tiene un límite espacial.
8. **Invernadero (nivel 6)** — `7b3875d`. Pasarela oxidada sobre los cultivos
   con soportes al suelo y barandilla de un lado.
9. **Catacumbas (nivel 7)** — `8dfe306`. Pasadizo estrecho detrás del arco del
   fondo: segundo umbral y un arco de dovelas a media luz.
10. **Cisterna (nivel 8)** — `dd2ff9f`. Galería de mantenimiento sobre el agua,
    entre las dos hileras de columnas, con anclajes y reflejo partido.

`bee642a` actualiza la matriz: las filas AD-15, DE-17, SE-11, NA-22, SA-11,
BI-12, IN-14, CA-13, CI-11 y TR-09/10/11/16/17 quedan registradas como
implementadas, con sus conteos reales por escenario.

## Etapa 3 — Segunda auditoría, documentación y Backup C

- Segunda pasada estática sobre todos los cambios propios: verde.
- `tools/vista_previa.py` acepta `--nivel N` (forma con espacio) además de
  `--nivel=N`, sin romper los posicionales.
- Documentación final: este documento, `docs/PLAN_EVOLUCION_6.md` (las 31
  propuestas evaluadas con problema/solución/beneficio/riesgo/coste/impacto/
  decisión), `docs/CATALOGO_MEJORAS_Y_FUNCIONES.md`,
  `docs/DIRECCION_ARTISTICA.md`, `docs/FONDOS_EXPLICADOS.md`,
  `docs/INFORME_FINAL_EVOLUCION_6.md`, `docs/musica.md`,
  `docs/compatibilidad.md`, `docs/checklist-manual.md` (con los checks de las
  opciones nuevas), CHANGELOG, KNOWN_ISSUES, CONTEXTO y README.
- Limpieza: sin basura versionada, sin OGG huérfanos, sin imports muertos;
  las matrices por frame de tres plantas pasaron a `static final`.
- Reintento de build el 29/08: `services.gradle.org` y
  `maven.minecraftforge.net` siguen bloqueados (HTTP 000) y no hay JDK en el
  entorno; el `clean build` con Java 17 queda para el equipo local del owner.
- Primer build real en el PC del owner (29/08, noche): las validaciones
  pasaron pero `clean build` falló con 2 errores de compilación en
  `GestorMusica.java` (`Window.isFocused()` inexistente en 1.20.1 y falta de
  import de `JobsMenu`); corregidos con GLFW directo + import. El bloque
  PowerShell pasó a ser un único `try/catch` con `git fetch` + chequeo de
  actualización; el `BUILD SUCCESSFUL` sigue pendiente.
- Backup C creado y subido.

## Lo que NO se hizo (y por qué)

- **Build y JAR:** sin JDK 17 y con la red bloqueada hacia gradle.org /
  maven.minecraftforge.net, el `clean build` no se puede ejecutar en este
  entorno. El wrapper 8.1.1 quedó versionado para que el build local funcione
  directo. El primer build local real (29/08) reveló y permitió corregir 2
  errores de compilación en `GestorMusica.java`; el pipeline completo (build
  `BUILD SUCCESSFUL` → JAR → `mods`) sigue pendiente. No se presenta ningún
  JAR como validado.
- **Prueba en Minecraft:** requiere la instancia con el modpack real; queda
  registrada como pendiente, no como verificada.
- **Duplicar `DireccionArte` en `PantallaEstancia`:** no aplica, esa pantalla
  no dibuja escena viva.
