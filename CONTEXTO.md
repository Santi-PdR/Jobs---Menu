# CONTEXTO — Jobs · Menú de Turno

> Documento maestro del mod de menús del servidor **Jobs**.
> Todo cambio de identidad, alcance o proceso se decide **aquí primero** y después se programa.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama de trabajo | `arena/01a03962-jobs-menu` |
| Mod id | `jobsmenu` |
| Nombre visible | Jobs · Menú de Turno |
| Paquete Java | `com.santipdr.jobsmenu` |
| Versión actual | **0.1.0** |
| Plataforma | Minecraft **1.20.1** · Forge **47.x** · Java **17** |
| Alcance | Menús (Title / Pause / Options), escena viva, audio, lore. **Sin gameplay.** |
| Lado | **Cliente**. El mod no toca el servidor ni exige instalarse en él. |

---

## 1. De qué va el servidor (canon)

> *"La temática de Jobs es trabajar y encontrarte con supervivientes, hacerte tu negocio y sobrevivir a los
> **Executores**, entidades inmortales que aparecen cada cierto tiempo para perseguirte y matarte."*
> — definición del owner, canon de partida.

De ahí salen los cuatro ejes que el menú tiene que respirar:

1. **Trabajo y oficio.** No eres un héroe: eres mano de obra. Hay turnos, hay tareas, hay un tablón.
2. **Supervivientes.** Hay otros. A veces ayudan, a veces son competencia. El multijugador *es* eso.
3. **Negocio propio.** Se acumula, se comercia, se pierde. El menú huele a inventario, a libro de cuentas.
4. **Executores.** Inmortales. **Periódicos.** No se derrotan: se sobreviven. El menú debe recordarte que
   el reloj corre y que la próxima aparición ya está agendada.

**Grafía canónica:** *Executor* / *Executores* (mayúscula inicial siempre, en cursiva nunca). Es el término
del servidor y no se "corrige" a Ejecutor. En inglés: *Executor* / *Executors*.

---

## 2. Identidad del menú

### 2.1 Concepto visual

**Un tablón de turnos atornillado a la pared de un depósito, de noche, bajo una lámpara de sodio que
parpadea.** El jugador no está "en un menú": está fichando antes de salir a trabajar.

- Nada de pergamino, nada de sepia, nada de tipografía de época. Esto es **hormigón, chapa, tinta de
  fotocopiadora y luz sucia de lámpara de sodio**.
- El fondo está **vivo pero quieto**: lluvia lejana, polvo suspendido en el cono de luz, el zumbido de la
  lámpara, y muy de vez en cuando **algo que cruza el fondo del pasillo y no vuelve a pasar**.
- El terror es **administrativo**, no gore. El horror de Jobs es que la muerte está *programada* y alguien
  la anotó en una planilla.

### 2.2 Voz narrativa

La voz del menú es la de **la empresa/el capataz que sigue contratando gente aunque sepa lo que hay
afuera**. Fría, breve, burocrática, con un fondo de amenaza que nunca se explica.

**Sí:**
- "Turno asignado. Preséntese en el punto de entrada."
- "La empresa no cubre desapariciones ocurridas fuera del horario."
- "Próxima aparición estimada: 07:42. Estimada."
- "Se ruega no describir lo que se vio."

**No:**
- Jerga de taller ni de software ("build", "commit", "config", "render", "bug").
- Chistes que rompan el tono. Se admite **humor negro seco**, nunca guiño meta.
- Épica heroica ("¡derrota a los Executores!"). A los Executores no se los derrota.
- Cualquier resto de la identidad anterior (crónica victoriana, gripe, cuarentena, ghouls).

**Regla dura:** todo texto que vea el jugador vive en `lang/*.json` y pasa por esta voz. Ningún literal
suelto en `.java`.

### 2.3 Paleta

Definida en `client/ui/Paleta.java`. ARGB, con alfa explícito.

| Nombre | Hex | Uso |
|---|---|---|
| `FONDO_PROFUNDO` | `#0B0C0E` | Cielo/fondo del pasillo, base de todo |
| `FONDO_ALTO` | `#15181C` | Degradado superior, aire frío |
| `HORMIGON` | `#232830` | Placas, base de botones, tablón |
| `HUMO` | `#3A414B` | Bordes, separadores, estructura |
| `SODIO` | `#D9922E` | Acento vivo: lámpara, barra activa, foco |
| `SODIO_TENUE` | `#8A5E1C` | Halo, subrayados, acento en reposo |
| `HUESO` | `#E8E4DA` | Texto principal |
| `HUESO_TENUE` | `#9A968E` | Texto secundario, avisos, sellos |
| `ALERTA` | `#B3261E` | Cuenta regresiva en zona roja |
| `ALERTA_BRILLO` | `#E8442F` | Pulso de "aparición inminente" |

Reglas de color:
- El **rojo es exclusivo de los Executores**. Nada más en la interfaz puede usarlo.
- El **ámbar es la única fuente de luz**. Si algo brilla, es porque la lámpara lo alcanza.
- Nunca blanco puro (`#FFFFFF`): siempre `HUESO`.

### 2.4 Tipografía y composición

- Fuente del juego, sin texturas de fuente propias (por ahora).
- **Alineado a la izquierda**, como un formulario. El centrado se reserva para el titular.
- Los botones son **renglones de una planilla**, no cápsulas: rectángulo bajo, borde de 1px, número de
  orden a la izquierda (`01`, `02`, …) y barra ámbar cuando el cursor está encima.
- Espacio negativo generoso abajo a la derecha: ahí vive el **sello de turno** (versión, build).

---

## 3. Elementos del menú principal

Implementados en `client/screen/PantallaTurno.java`.

| Zona | Contenido |
|---|---|
| Cabecera | `JOBS` en grande + subtítulo `REGISTRO DE TURNOS` en `HUESO_TENUE` |
| Columna central | Los cuatro renglones del tablón (ver abajo) |
| Esquina superior derecha | **Cuenta regresiva a la próxima aparición** |
| Pie izquierdo | **Avisos rotativos** del tablón (línea que cambia cada 7 s) |
| Pie derecho | Sello: `jobsmenu 0.1.0` |

Renglones del tablón (etiquetas in-fiction, no vanilla):

| # | Etiqueta | Acción real |
|---|---|---|
| 01 | Turno en solitario | `SelectWorldScreen` |
| 02 | Presentarse al complejo | `JoinMultiplayerScreen` |
| 03 | Condiciones del contrato | `OptionsScreen` |
| 04 | Abandonar el puesto | `Minecraft#stop()` |

### 3.1 La cuenta regresiva (pieza de identidad)

Ciclo fijo de **13 minutos**, anclado al reloj del sistema. No depende de la partida ni del servidor: es un
recordatorio ambiental, no una mecánica.

- Formato `MM:SS`, en `HUESO_TENUE`.
- Bajo 60 s: pasa a `ALERTA`.
- Bajo 8 s: pulsa entre `ALERTA` y `ALERTA_BRILLO` y el texto cambia a **INMINENTE**.
- Al llegar a cero: durante 4 s el rótulo dice **APARICIÓN** y la escena baja la luz un punto.
- Con *destellos reducidos* activo, el pulso no parpadea: se queda fijo en `ALERTA`.

Es la única cosa del menú que "sabe la hora" y es deliberadamente inútil: no puedes hacer nada al respecto.

### 3.2 Escena viva

`client/scene/EscenaDeposito.java`, todo procedural (cero texturas en 0.1.0):

1. Degradado vertical del pasillo.
2. Piso y placas de hormigón deterministas (semilla fija: la escena es *siempre el mismo depósito*).
3. Cono y halo de la lámpara de sodio con **parpadeo irregular**.
4. Lluvia lejana en diagonal.
5. Polvo suspendido dentro del cono de luz.
6. **Silueta**: cada ~47 s algo cruza el fondo del pasillo, tres segundos, sin ruido.
7. Viñeta de bordes.

Todas las capas 4–6 se apagan con *movimiento reducido*; la 3 se congela con *destellos reducidos*.

---

## 4. Configuración (cliente)

`config/jobsmenu-client.toml`, definida en `config/ConfigTurno.java`.

| Clave | Def. | Qué hace |
|---|---|---|
| `menu_propio` | `true` | Sustituye el título vanilla. En `false` el mod queda invisible. |
| `escena_viva` | `true` | Fondo animado; en `false`, fondo estático con la misma composición. |
| `movimiento_reducido` | `false` | Apaga lluvia, polvo y silueta. |
| `destellos_reducidos` | `false` | Congela el parpadeo de la lámpara y el pulso rojo. |
| `interfaz_minima` | `false` | Deja sólo cabecera y renglones: sin avisos, sin cuenta regresiva, sin sello. |
| `mostrar_cuenta_regresiva` | `true` | Control fino del reloj de aparición. |
| `avisos_rotativos` | `true` | Control fino de la línea de avisos. |

Accesibilidad primero: **cualquiera de esos interruptores tiene que dejar un menú usable y legible**, nunca
uno roto.

---

## 5. Alcance por fases

| Fase | Contenido | Estado |
|---|---|---|
| **0.1.0** | Esqueleto Forge, config, paleta, escena procedural, pantalla de título propia, avisos, cuenta regresiva | **Entregado** |
| 0.2.0 | Pantalla de pausa ("Turno en suspenso") y de opciones con la misma piel | Pendiente |
| 0.3.0 | Audio: zumbido de lámpara, lluvia, golpe seco en la aparición; mezcla respetando el volumen del juego | Pendiente |
| 0.4.0 | Texturas propias (tablón, chapa, sellos) y viñeta en textura | Pendiente |
| 0.5.0 | Lore ampliado: expediente consultable, avisos con memoria, easter eggs por fecha/hora | Pendiente |
| 1.0.0 | Pulido, revisión de accesibilidad completa, empaquetado para repartir a los jugadores | Pendiente |

Fuera de alcance, explícitamente: entidades, ítems, mecánicas, comandos, datos de mundo, cualquier cosa que
toque el servidor.

---

## 6. Reglas de trabajo vigentes

Heredadas del proyecto anterior; siguen aplicando tal cual.

1. **Voz coherente**: ningún texto in-game con jerga de taller o de software.
2. **Docs sincronizados**: si sube la versión, suben `gradle.properties`, este documento y el `README`.
3. **Changelog in-game discrecional**: sólo si el cambio se *ve* o se *oye*.
4. **Revisión obligatoria antes de entregar**: GUI scale 2, 3 y 4, más `interfaz_minima`,
   `movimiento_reducido` y `destellos_reducidos`.
5. **Respaldo antes de cambios estructurales**: `git bundle` del repo.
6. **Sin firmas de memoria**: si una API de Forge/Minecraft no está verificada en el propio repo, no se usa.
   El sandbox no tiene JDK: la compilación real ocurre en el PC del owner.
7. **Verificación estática obligatoria**: `python3 tools/verificar.py` antes de cada entrega (ver §7).
8. **Cada entrega cierra con el bloque PowerShell** de actualizar + compilar + desplegar.

---

## 7. Sello de verificación

`tools/verificar.py` es el sustituto del compilador ausente. Comprueba:

- Versión idéntica en `gradle.properties`, `CONTEXTO.md` y `README.md`.
- Todo `${...}` de `mods.toml` tiene su clave en `gradle.properties`.
- Paridad de claves entre `es_es.json` y `en_us.json`, y JSON válido.
- Toda clave usada en `Component.translatable(...)` existe en ambos idiomas, y ninguna clave está huérfana.
- Los `.java` son **ASCII puro** (los acentos viven en los `lang`, nunca en el código).
- Llaves, paréntesis y comillas balanceadas en cada `.java`.
- `pack.mcmeta` válido y con el `pack_format` de 1.20.1 (`15`).

No sustituye a `gradlew build`, pero atrapa el 90 % de las erratas que costaban una vuelta entera.

---

## 8. Glosario in-fiction

| Término | Significado |
|---|---|
| **Turno** | Una sesión de juego. Empieza al entrar, termina cuando terminás o cuando te terminan. |
| **El complejo** | El servidor. Donde están los demás supervivientes. |
| **Contrato** | Las opciones del juego. Se "aceptan condiciones", no se "configura". |
| **Aparición** | El evento cíclico del Executor. Nunca "spawn", nunca "ataque". |
| **La empresa** | Quien escribe los avisos. Nunca se nombra ni se muestra. |
| **Registro** | La memoria del menú: avisos, sellos, expediente. |
