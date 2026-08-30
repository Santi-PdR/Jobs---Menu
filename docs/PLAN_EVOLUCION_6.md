# Plan de evolución 6 — propuestas evaluadas (problema → decisión)

Cumple el requisito del plan obligatorio: **13 mejoras a sistemas existentes**,
**8 funciones nuevas** y **10 mejoras artísticas** (una por fondo) — 31
propuestas, cada una con su análisis interno. Todas las implementadas están en
`docs/CATALOGO_MEJORAS_Y_FUNCIONES.md`; aquí queda el análisis que no cabe en
el catálogo. Convención de decisión: **Implementar** / **Pospuesta** /
**Rechazada**.

## A. Mejoras a sistemas existentes

| # | Propuesta | Problema / oportunidad | Solución | Beneficio | Riesgo | Coste técnico | Coste render/audio | Impacto compatibilidad | Decisión |
|---|---|---|---|---|---|---|---|---|---|
| A1 | Duración de estancia configurable | La estancia era fija (24 s), sin control del jugador | Slider 15–90 s; `RotacionNiveles` usa `duracionEstancia()*1000L`; calma = ×2 | El ritmo del menú se adapta al jugador | Valor corrupto rompía el temporizador | Bajo: 1 opción + lectura | Ninguno | Ninguno | **Implementar** |
| A2 | Modo de bajo consumo | GPUs modestas sufrían polvo/grano/presencia por frame | Opción que suprime motas, grano, presencia y respiración de cámara | Menú completo en GTX 1050/UHD 630 | Escena "vacía" si se quita de más | Bajo: guardas en 2 clases | Bajo: menos fills | Ninguno | **Implementar** |
| A3 | Perfil accesible | Cuatro opciones de accesibilidad había que marcarlas una a una | Un interruptor que las enciende juntas; tocar una a mano lo desactiva | Configuración en un clic, coherente | Confusión entre perfil y opción individual | Medio: setters coordinados | Ninguno | Ninguno | **Implementar** |
| A4 | Accesos a config sin SPEC cargado | `ConfigTurno` podía leerse antes de cargar y romper | `leer(valor, defecto)` en todos los accesos | Nunca NPE por config ausente/corrupta | Ninguno | Bajo | Ninguno | Ninguno | **Implementar** |
| A5 | Continuidad del ambiente por visita | El ambiente moría al abrir Opciones/Mods | `SesionMenu` idempotente + `mantenerCamas()` en el tick | El recinto nunca queda mudo al navegar | Doble apertura de camas | Medio: guard de visita | Ninguno | Ninguno | **Implementar** |
| A6 | Volumen de ambiente en vivo | El slider no se oía hasta salir | Objetivo recalculado por tick con factor de luz y respiración | Control inmediato | Tirones de volumen | Bajo | Ninguno | Ninguno | **Implementar** |
| A7 | Vigilancia de instancia fantasma | Tras recargas podía quedar música congelada duplicada | Detección con edad congelada; vigía desarmado sin foco/pausa | Nunca dos instancias, sin falsas alarmas | Falso negativo | Medio | Ninguno | Ninguno | **Implementar** |
| A8 | Diagnóstico interno oculto | Sin forma de ver el estado de audio/escena | Ctrl+D vuelca al LOG (no documentado) | Depuración sin UI nueva | Exposición accidental | Bajo | Ninguno | Ninguno | **Implementar** |
| A9 | Salto manual de nivel | Había que esperar el apagón para cambiar de fondo | Tecla F con antirrepetición | Navegación inmediata | Choque con teclas de mods | Bajo | Ninguno | Bajo: solo en pantallas propias | **Implementar** |
| A10 | `addTitle` inexistente en 1.20.1 | `PantallaAjustesAviso` usaba una API que no existe | `OptionsList` con `addBig`/`addSmall`/`addAll` | Pantalla que realmente compila | Ninguno | Bajo | Ninguno | Mejora con mods de UI | **Implementar** |
| A11 | Bajo consumo enganchado al render | El modo solo tocaba config, no la escena | Guardas en `EscenaNivel` y `TratamientoEscena` | Efecto real por frame | Nada | Bajo | Bajo | Ninguno | **Implementar** |
| A12 | Import muerto y matrices por frame | Basura de memoria en 3 plantas y un import sin uso | `static final` en haz/páginas/marcas; import eliminado | Sin asignaciones por frame | Nada | Bajo | Bajo | Ninguno | **Implementar** |
| A13 | Textos ES/EN desincronizados | Opciones nuevas sin traducción | Claves nuevas en ambos idiomas | UI bilingüe completa | Nada | Bajo | Ninguno | Ninguno | **Implementar** |

## B. Mejoras artísticas (una por fondo)

| # | Fondo | Propuesta (fila matriz) | Problema | Solución | Beneficio | Riesgo | Coste técnico | Coste render | Compatibilidad | Decisión |
|---|---|---|---|---|---|---|---|---|---|---|
| B1 | Trono (TR-09/10/11/16/17) | Rediseño monumental | El trono no era el foco | Tarima 1.18, estrado de 6 escalones, ábside con 3 dovelas, hueco de corona, estandartes torcidos | Foco inequívoco | Cobertura de la hoja | Medio | Bajo | Ninguna | **Implementar** |
| B2 | Administración (AD-15) | Abertura de mantenimiento | El mantenimiento vivía solo del lado izquierdo | Hueco con marco, bisagras e interior oscuro a la derecha | Equilibrio sin simetría | Nada | Bajo | Bajo | Ninguna | **Implementar** |
| B3 | Depósito (DE-17) | Lona caída | Nave toda recta, sin forma blanda | Cuatro bandas plegadas con costura | Ruptura de rigidez | Nada | Bajo | Bajo | Ninguna | **Implementar** |
| B4 | Servicio (SE-11) | Bandeja de cables | Solo tuberías, sin instalación eléctrica | Bandeja colgada con 3 colgadores y bucle suelto | Segundo sistema visible | Nada | Bajo | Bajo | Ninguna | **Implementar** |
| B5 | Natatorio (NA-22) | Sarro bajo rebosadero | Orilla impecable, sin rastro de agua | Lenguetas minerales sembradas por clave | Desgaste con historia | Nada | Bajo | Bajo | Ninguna | **Implementar** |
| B6 | Sala de piedra (SA-11) | Dovelas en el arco | `DOVELAS` muerta; arco era una V | Bezier por piezas con junta perpendicular | Cantería real | Nada | Medio | Bajo | Ninguna | **Implementar** |
| B7 | Biblioteca (BI-12) | Arco de acceso | Pasillo cerrado de lomos | Capucha de arco con pilares | Límite espacial | Nada | Bajo | Bajo | Ninguna | **Implementar** |
| B8 | Invernadero (IN-14) | Pasarela oxidada | Solo bancos a ras de suelo | Tablón con soportes y barandilla | Vía de trabajo abandonada | Nada | Bajo | Bajo | Ninguna | **Implementar** |
| B9 | Catacumbas (CA-13) | Pasadizo estrecho | El túnel terminaba en pared negra | Segundo umbral con dovelas a media luz | El túnel sigue | Nada | Bajo | Bajo | Ninguna | **Implementar** |
| B10 | Cisterna (CI-11) | Galería de mantenimiento | Columnas sobre agua sola | Pasillo entre hileras con reflejo partido | Escala de servicio | Nada | Medio | Bajo | Ninguna | **Implementar** |

## C. Funciones nuevas

| # | Función | Problema / oportunidad | Solución | Beneficio | Riesgo | Coste técnico | Coste render/audio | Compatibilidad | Decisión |
|---|---|---|---|---|---|---|---|---|---|
| F1 | Duración de estancia (nueva opción) | — | Ver A1 | — | — | — | — | — | **Implementar** |
| F2 | Salto manual (nueva tecla) | — | Ver A9 | — | — | — | — | — | **Implementar** |
| F3 | Modo bajo consumo (nueva opción) | — | Ver A2/A11 | — | — | — | — | — | **Implementar** |
| F4 | Perfil accesible (nueva opción) | — | Ver A3 | — | — | — | — | — | **Implementar** |
| F5 | Continuidad de ambiente por visita | — | Ver A5 | — | — | — | — | — | **Implementar** |
| F6 | Vigilancia de fantasma | — | Ver A7 | — | — | — | — | — | **Implementar** |
| F7 | Diagnóstico interno oculto (Ctrl+D) | — | Ver A8 | — | — | — | — | — | **Implementar** |
| F8 | `--nivel N` en el espejo Python | La forma con espacio se ignoraba | Normalización a `--nivel=N` | Herramienta sin sorpresas | Ninguno | Bajo | Ninguno | Ninguno | **Implementar** |

## D. Evaluadas y no implementadas

| Propuesta | Análisis | Decisión |
|---|---|---|
| Historial/selector visual de escenarios | Sobrecarga la UI; el salto manual (F) ya navega sin superficie nueva | **Pospuesta** |
| Sistema de favoritos / nivel preferido | Duplica `nivel_fijo` | **Pospuesta** |
| Transiciones contextuales por pantalla de origen | La transición por frame ya es coherente; variantes por origen no añaden lectura | **Pospuesta** |
| Duplicar `DireccionArte` en `PantallaEstancia` | Esa pantalla no dibuja escena viva; no hay respiración que suprimir | **Rechazada** |
| Sonidos nuevos sin función concreta | Regla del diseño sonoro: el silencio es parte del diseño | **Rechazada** |
| Mixin o dependencia nueva para cualquier mejora | Todas se resolvieron con APIs existentes | **Rechazada** |
| Bump a 0.11.0 antes del build | No se puede compilar en el entorno; la versión no se declara hasta build | **Pospuesta** |
