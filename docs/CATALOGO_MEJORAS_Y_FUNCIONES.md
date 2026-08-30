# Catálogo de mejoras y funciones nuevas — Evolución 6

Este catálogo registra **únicamente lo que existe en la rama**
`arena/01a04ff1-jobs-menu` (desde `811586e`). Cada entrada tiene su commit de
referencia. Nada de lo que aquí figura está "en teoría": o está en el diff, o
no está en este documento.

Convención de commits:

- `14efd06` — Backup A y registro de puntos de recuperación.
- `5702eb2` — Etapa 1: configuración ampliada, continuidad del ambiente, salto
  manual de nivel.
- `ae48a07` — Etapa 1: bajo consumo enganchado al render y camas continuas en
  pantallas hijas.
- `08ed9bf` — Etapa 2: Trono rediseñado.
- `52eea3f`…`dd2ff9f` — Etapa 2: una mejora nueva por nivel (0 a 8).
- `bee642a` — Matriz de auditoría de fondos actualizada.

---

## A. Mejoras a sistemas existentes

### Configuración y opciones

1. **Duración de estancia configurable** (`duracion_estancia`, 15–90 s,
   defecto 24). `RotacionNiveles` usa `duracionEstancia() * 1000L`; con la
   rotación en calma la estancia se duplica. Commit `5702eb2`.
2. **Modo de bajo consumo** (`bajo_consumo`). Apaga polvo, grano, presencia,
   eventos visuales y respiración de cámara; el recinto y su audio siguen
   intactos. Commit `ae48a07`.
3. **Perfil accesible** (`perfil_accesible`). Enciende juntas movimiento
   reducido, destellos reducidos, alto contraste y texto grande. Tocar
   cualquiera de las cuatro opciones a mano desactiva el perfil (los setters
   manuales lo apagan); `fijarPerfilAccesible(true)` persiste las cuatro en
   `true`. Commit `5702eb2`.
4. **Accesibilidad a prueba de config no cargada.** Todos los accesos de
   `ConfigTurno` pasan por `leer(...)` con valor por defecto cuando el SPEC
   todavía no está cargado (p. ej. `duracionEstancia()` devuelve 24 fuera de
   contexto). Commit `5702eb2`.

### Ciclo de vida y robustez

5. **La continuidad del audio pertenece a la visita, no a la pantalla.**
   `SesionMenu` gobierna el ciclo de vida del ambiente con apertura/cierre
   idempotentes; `PantallaNivel.removed()` ya no detiene el ambiente por
   sí solo. Solo entrar a un mundo, apagar el menú o salir al título detiene
   las camas. Commit `5702eb2`.
6. **Camas ambientales vivas en pantallas hijas.** `GestorAmbiente.
   mantenerCamas()` corre en el tick del cliente (`EscuchaCliente`): guarda
   `SesionMenu.activa()`, limpia capas agotadas, respeta `sonidoAmbiente()` y
   asegura las camas del nivel actual. Rotar de nivel con Opciones o Mods
   abiertos ya no deja el recinto nuevo en silencio. Commit `ae48a07`.
7. **Volumen de ambiente en vivo.** `CapaAmbiente.tick()` recalcula cada tick
   su objetivo: `volumenAmbiente() * AMBIENTE * papel.peso * volumenAviso()`
   por factor de luz (apagón/suspensión), respiración y agachado. Mover el
   slider se oye al instante. Commit `5702eb2`.
8. **Vigilancia de instancia fantasma blindada.** `GestorMusica` solo declara
   fantasma cuando el cliente está realmente tickeando
   (`!isPaused() && isFocused()`); con el juego pausado o sin foco el vigía se
   desarma y al reanudar el primer tick solo rearma, sin falsas alarmas.
   Commit `5702eb2`.
9. **Diagnóstico interno oculto.** Nueva clase `DiagnosticoOculto` (Ctrl+D,
   no documentada en la UI pública) vuelca el estado de música y ambiente al
   LOG para depuración; `GestorMusica.reintentoParaDiagnostico()` lo alimenta.
   Ctrl+S sigue reservado y oculto para debug/admin. Commit `5702eb2`.
10. **Compatibilidad con `OptionsList` de 1.20.1.** `PantallaAjustesAviso` ya
    no usa `addTitle` (no existe en 1.20.1; `OptionsList` solo ofrece
    `addBig`/`addSmall`/`addAll`). Commit `5702eb2`.

### Salto y rotación de niveles

11. **Salto manual de nivel.** `RotacionNiveles.adelantar()` con `ultimoSalto`
    evita repeticiones; `capturar()` fuerza el nivel dentro del recinto. La
    tecla F (solo con traslados activos) suena `UI_ALTERNAR`. Commit `5702eb2`.
12. **Bajo consumo en el render.** `EscenaNivel` suprime respiración,
    `Presencia.dibujar` y `motas(...)` en `atmosferaMovimiento`; `Tratamiento
    Escena` suprime el grano. El recinto se ve igual de completo sin el
    coste por frame. Commit `ae48a07`.

### Idiomas

13. **Textos ES/EN sincronizados** para estancia, salto manual y perfil
    accesible (`es_es.json`, `en_us.json`). Commit `5702eb2`.
14. **Sin basura por frame en las plantas.** Las matrices locales de
    `Servicio.haz`, `Biblioteca.paginasDobladas` y `Natatorio.
    marcasProfundidad` pasan a `static final`, y el colgador central de la
    bandeja de cables de `Servicio` ya no crea un arreglo temporal. Segunda
    pasada de auditoría sobre los propios cambios (`d4f4e88`).

## B. Mejoras artísticas (una por fondo)

Cada una implementa una fila concreta de `docs/AUDITORIA_FONDOS_50X10.md`
(marcada como **Estado: Implementado** en la matriz) y su espejo en
`tools/vista_previa.py` produce la misma geometría.

| Nivel | Fila | Mejora | Commit |
|---|---|---|---|
| 0 — Administración | AD-15 | Abertura de mantenimiento en el lateral derecho: marco, dos bisagras e interior que no termina en la pared. Contrapeso de la placa de administración. | `52eea3f` |
| 1 — Depósito | DE-17 | Lona de carga caída al pie del lado derecho: cuatro bandas plegadas que se montan, con costura y borde de luz. Única forma blanda de la nave. | `d03c347` |
| 2 — Servicio | SE-11 | Bandeja de cables colgada del techo: canto inferior de pared a pared, tres colgadores y un bucle de cable suelto bajo el central. | `f50b8dc` |
| 3 — Natatorio | NA-22 | Sarro bajo el rebosadero: nueve lenguetas minerales sembradas por la clave del nivel; el agua que se evaporó dejó su marca. | `174eb8a` |
| 4 — Sala de piedra | SA-11 | Dovelas visibles en el arco más cercano de la bóveda: curva en bezier construida por piezas, con junta perpendicular y tono propio por dovela. | `003edbd` |
| 5 — Biblioteca | BI-12 | Arco de acceso entre estantes de la hilera derecha: capucha de arco, interior oscuro que no termina en la pared y pilares de marco. | `8acf949` |
| 6 — Invernadero | IN-14 | Pasarela oxidada sobre los cultivos: tablón con soportes al suelo, sombra de contacto y barandilla de un solo lado. | `7b3875d` |
| 7 — Catacumbas | CA-13 | Pasadizo estrecho detrás del arco del fondo: segundo umbral más alto, jambas a media luz y arco de siete dovelas; el túnel sigue. | `8dfe306` |
| 8 — Cisterna | CI-11 | Galería de mantenimiento sobre el agua entre las dos hileras de columnas: tablón, barandilla, anclajes y reflejo partido en la superficie. | `dd2ff9f` |
| 9 — Trono | TR-09/10/11/16/17 | Rediseño completo: tarima 1.18, estrado de seis escalones con proporciones monumentales, ábside con tres dovelas concéntricas (oro solo en el arco interno), hueco de corona ausente grande y oscuro, estandartes torcidos con sesgo determinista. | `08ed9bf` |

## C. Funciones nuevas (evaluadas e implementadas)

| Función | Descripción | Commit |
|---|---|---|
| F1 — Duración de estancia configurable | Slider de 15–90 s en Opciones; la rotación respeta el valor en vivo y en calma lo duplica. | `5702eb2` |
| F2 — Salto manual de nivel | Tecla F con antirrepetición; avanza el recinto sin esperar el apagón. | `5702eb2` |
| F3 — Modo de bajo consumo | Opción dedicada que apaga efectos visuales costosos y respiración de cámara en equipos modestos. | `ae48a07` |
| F4 — Perfil accesible | Un interruptor que combina las cuatro opciones de accesibilidad; los ajustes manuales lo desactivan de forma coherente. | `5702eb2` |
| F5 — Continuidad de ambiente por visita | Las camas de audio sobreviven a pantallas hijas y a la rotación con Opciones abiertas; se detienen solo al salir del menú. | `ae48a07` |
| F6 — Vigilancia de instancia fantasma | Detección de una segunda instancia de música congelada sin falsas alarmas por pausa o pérdida de foco. | `5702eb2` |
| F7 — Diagnóstico interno oculto | Ctrl+D vuelca el estado de audio/ambiente al LOG; reservado a debug/admin, sin presencia en la UI. | `5702eb2` |
| F8 — Herramienta de vista previa con `--nivel N` | El espejo Python acepta la forma con espacio además de `--nivel=N`, sin romper los argumentos posicionales. | `bee642a` (herramienta) |

## D. Rechazadas o pospuestas (con motivo)

| Propuesta | Decisión | Motivo |
|---|---|---|
| Historial/selector visual de escenarios en la UI | Pospuesta | Sobrecarga la interfaz; el salto manual (F) cubre la navegación sin añadir superficie. |
| Sistema de favoritos / nivel preferido | Pospuesta | Ya existe `nivel_fijo`; un favorito duplicaría el control. |
| Transiciones contextuales por pantalla de origen | Pospuesta | La transición por frame de `EscenaNivel` ya es coherente; añadir variantes por origen no aporta lectura. |
| Duplicar `DireccionArte` en `PantallaEstancia` | Descartada | `PantallaEstancia` no dibuja escena viva (fondo vanilla + papel); no hay respiración que suprimir. |
| Bump de versión a 0.11.0 | Pospuesta | El build no se puede ejecutar en este entorno (ver KNOWN_ISSUES); la versión se mantiene 0.10.0 hasta compilar. |

---

*Última actualización: 2026-08-29. Verificador estático en verde
(`tools/verificar.py`, 1 aviso, 0 fallos).*
