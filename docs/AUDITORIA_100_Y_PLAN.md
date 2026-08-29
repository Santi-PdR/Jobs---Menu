# Auditoria integral y plan de mejoras

Fecha: 2026-08-29
Rama: `arena/01a04e24-jobs-menu`
Version base: `0.10.0`

## Alcance real de esta auditoria

Se revisaron todas las rutas versionadas del checkout, las ramas locales/remotas
visibles y las etiquetas de seguridad existentes antes de cambiar codigo. El
inventario actual contiene 43 clases Java, 74 archivos OGG del mod, 33 WAV de
materia prima, 230 claves por idioma y cinco laminas PNG documentales.

La auditoria estatica cubre Java, recursos, configuracion, `sounds.json`,
idiomas, generador de audio, vista previa, Gradle, ciclo de vida y la matriz de
pantallas vanilla. No reemplaza compilacion con Java 17 ni prueba dentro de
Minecraft; tampoco puede demostrar que una llamada nativa de NVIDIA sea segura.

## Limpieza de archivos

No se encontro un archivo versionado que pueda eliminarse con seguridad:

- Los 33 WAV de `tools/crudo/` son usados por `tools/sonidos.py` y tienen
  licencia archivada en `tools/crudo/LICENCIA.txt`.
- `tools/muestras.py` es importado por el generador y forma parte de la cadena
  reproducible.
- `docs/presencia.png` sigue siendo la salida predeterminada de la herramienta y
  aparece en las instrucciones; no es un duplicado abandonado.
- `docs/contacto-actual.png`, `docs/eventos-actual.png` y
  `docs/presencia-actual.png` son evidencias de la auditoria anterior.
- `music/REQUIEM-Forsaken-OST.ogg` es una referencia local documentada del
  owner; no se copia al JAR.
- El `gradle-wrapper.jar` es parte del despliegue reproducible.
- `src/main/resources/assets/jobsmenu/musica_creditada.txt` ya habia sido
  eliminado cuando se comprobo que no correspondia a la pista empaquetada.
- `tools/desplegar-test-1.ps1` ya habia sido eliminado y no se recrea.
- Se retiro solamente `tools/__pycache__/`, que era basura generada e ignorada,
  no un archivo del repositorio.

Borrar cualquiera de los elementos anteriores reduciria reproducibilidad o
romperia una ruta documentada. Por eso esta pasada no hace una eliminacion
cosmetica de assets validos.

## Correcciones hechas durante esta pasada

- El auditor de duraciones de `tools/verificar.py` ahora comprueba los diez
  niveles, no solo los cuatro originales.
- Se corrigieron los periodos efectivos de las camas de niveles 6, 8 y 9. La
  salida del crossfade era de 20/30/51 s, 21/35/57 s y 20/33/54 s; ahora los
  periodos efectivos son 23/31/53 s, 23/37/59 s y 23/37/59 s.
- Se regeneraron las nueve camas afectadas y el verificador queda con cero
  fallos y cero avisos.
- El boton de ajustes de `OptionsScreen` no se agrega en ventanas donde no hay
  una esquina segura y comprueba que no tape el titulo vanilla.
- La pasada anterior ya habia protegido el reload de audio, el credito de pista
  local, la fila de `OptionsList` y cinco detalles arquitectonicos de fondos.

## Pantallas vanilla: matriz de compatibilidad

| Pantalla | Tratamiento permitido | Tratamiento que se evita |
|---|---|---|
| `TitleScreen` exacta | Sustitucion discreta en `ScreenEvent.Opening` cuando el menu propio esta activo | `instanceof` amplio, wrappers permanentes o tocar titulos de terceros |
| `PauseScreen` exacta con titulo `menu.game` | Sustitucion por pausa propia para conservar guardado y rutas vanilla | Tocar la pausa F3+Esc o subclases de otros mods |
| `OptionsScreen` exacta | Boton calculado en `ScreenEvent.Init.Post`, con fallback de anchura segura | Rehacer la grilla de vanilla o envolver `OptionsScreen` |
| `OptionsSubScreen` propia | `OptionsList`, tooltips, foco, scroll y boton Listo nativos | Registrar listeners globales para todas las subpantallas |
| Idioma | Dejar la pantalla de idioma vanilla y escuchar la recarga | Reemplazar idioma o interferir con traducciones de otros mods |
| Sonido | Mantener canales vanilla; usar `MASTER` para la música propia y controles del mod | Mover REQUIEM al slider `Music` o parar audio de terceros indiscriminadamente |
| Controles | No tocar; conservar key mappings, foco y narración vanilla | Registrar atajos globales salvo el gesto administrativo oculto ya existente |
| Recursos | No envolver; el listener solo invalida instancias propias | Interceptar paquetes ajenos o modificar el repositorio sin necesidad |
| Mods / multiplayer / singleplayer | No reemplazar | Inyectar botones globales o clonar pantallas de otros mods |

## 100 mejoras candidatas

Las siguientes son mejoras nuevas para priorizar, no una afirmacion de que ya
estén todas implementadas. Cada una indica el resultado perceptible y el riesgo
principal. Las P0/P1 deben hacerse antes que las ideas de alto impacto visual.

### A. Estabilidad, reload y ciclo de vida

1. **P0 — Estado explícito de visita:** modelar `CERRADA`, `NIVEL`, `PANTALLA_HIJA` y
   `MUNDO` para que música y ambiente no dependan de varios booleanos estáticos.
2. **P0 — Generación de reload:** asociar cada recarga a un contador y descartar
   tareas antiguas que lleguen tarde.
3. **P0 — Prueba de doble reload:** automatizar idioma + F3+T consecutivos y exigir
   cero instancias duplicadas.
4. **P0 — Cierre por desconexión:** cerrar capas al limpiar nivel aunque no se
   dispare `ScreenEvent.Opening`.
5. **P0 — Guarda contra SoundEngine ausente:** no recrear audio hasta que el
   `SoundManager` esté listo después del reload.
6. **P0 — Reintento con backoff:** reemplazar el reintento fijo de 20 ticks por
   backoff limitado y log una sola vez.
7. **P0 — Listener único:** proteger el registro del listener ante inicializaciones
   repetidas del bus de cliente.
8. **P0 — Hilo documentado:** verificar con una aserción de desarrollo que el
   cierre de instancias ocurre en el hilo cliente.
9. **P0 — Cierre directo de canal propio:** evaluar `SoundManager.stop(instance)`
   en el hilo correcto además de marcar `stop()`.
10. **P0 — Sonido rechazado:** detectar y registrar recursos que el motor descarta
    sin convertir un fallo de audio en crash.
11. **P1 — Snapshot común de audio:** compartir el mismo `Estado` entre las tres
    capas en vez de capturarlo una vez por instancia.
12. **P1 — Snapshot común de pantalla:** centralizar el estado de visita para
    evitar carreras entre `SesionMenu` y `EscuchaCliente`.
13. **P1 — Recarga de configuración:** escuchar cambios externos del TOML y aplicar
    sonido/escena sin reiniciar una pantalla.
14. **P1 — Guardado agrupado seguro:** sustituir el booleano de pendiente por una
    cola de valores o un dirty-set de opciones.
15. **P1 — Restauración tras error:** si falla la activación de música local,
    restaurar la selección previa del repositorio.
16. **P1 — Paquete local atómico:** copiar pista a un temporal y moverla después,
    evitando leer un OGG incompleto.
17. **P1 — Cambio de pista en caliente:** botón discreto para solicitar reload solo
    cuando el hash de la pista cambie.
18. **P1 — Hash persistente:** guardar tamaño, fecha y hash corto de la pista para
    no usar `Files.mismatch` en cada arranque.
19. **P2 — Diagnóstico opcional:** pantalla de depuración no pública con estado de
    instancia, recurso y canal, sin registrar el atajo administrativo oculto en documentación.
20. **P2 — Métrica de duración:** registrar una vez los tiempos de creación y
    cierre de capas para encontrar lentitud real en modpacks grandes.

### B. Integración vanilla y compatibilidad

21. **Implementado esta pasada — Idempotencia del widget:** el botón de ajustes se
    detecta por mensaje antes de insertarse para no duplicarse si otro listener
    reconstruye la pantalla.
22. **P0 — Prueba de resoluciones:** comprobar 320x240, 480x270, 854x480 y 1920x1080.
23. **P0 — Traducciones largas:** probar alemán, francés y cadenas artificialmente
    largas sin desbordar el botón exacto de opciones.
24. **Implementado esta pasada — Título narrado:** el botón discreto de
    `OptionsScreen` tiene tooltip traducido, además de la narración nativa del
    widget, sin modificar los tooltips vanilla.
25. **P1 — Orden de foco:** documentar y comprobar que Tab entra al botón propio en
    un orden razonable sin secuestrar el foco inicial.
26. **P1 — Tooltip de compatibilidad:** mostrar tooltip del botón propio sin
    modificar tooltips de vanilla.
27. **P1 — Pausa de servidor:** probar pausa local, servidor dedicado y Realms con
    sus tres rutas vanilla de salida.
28. **P1 — F3+Esc:** comprobar que la pausa técnica nunca se reemplaza por la hoja.
29. **P1 — Pantalla de opciones hija:** conservar `lastScreen` al volver desde la
    subpantalla propia después de cambiar idioma.
30. **P1 — Recursos y Mods:** comprobar navegación desde Options a Resource Packs,
    Mods, idioma y regreso sin duplicar gestos.
31. **P1 — Mouse capturado:** probar reanudar, cerrar pausa y volver al mundo con
    captura de mouse intacta.
32. **P2 — Narración de filas:** diferenciar en narración entre acción normal,
    acción terminal y acción deshabilitada.
33. **P2 — Indicador de foco teclado:** usar un borde visible que no dependa del
    cursor ni del sonido.
34. **P2 — Soporte de escalado GUI:** revisar texto grande con GUI scale alto y
    ancho efectivo reducido.
35. **P2 — Mod menu opcional:** si se integra, hacerlo mediante API opcional y sin
    dependencia obligatoria de otro mod.

### C. Interfaz propia, botones y lectura

36. **Implementado esta pasada — Estado presionado visible:** marcar durante unos
    milisegundos el renglón que acaba de aceptar una acción sin retrasar la acción.
37. **Implementado esta pasada — Estado terminal diferenciado:** separar
    visualmente salir/renunciar sin usar rojo fuera de la identidad permitida de
    Executores.
38. **P0 — Estado bloqueado explicado:** mostrar por qué una opción está inactiva,
    además del sonido `negado`.
39. **P1 — Barra de progreso de aviso:** una línea fina y opcional que muestre la
    duración del aviso rotativo sin competir con el texto.
40. **P1 — Separadores semánticos:** agrupar visualmente escena, sonido y
    accesibilidad dentro de la lista nativa sin inventar otra pantalla.
41. **P1 — Restablecer sección:** botón vanilla para devolver solo una sección a
    valores por defecto, con confirmación accesible.
42. **P1 — Aplicar sin saltos:** evitar que cambiar una opción de escena desplace
    el scroll o robe foco.
43. **P1 — Contraste medido:** comprobar luminancia de texto/papel con una prueba
    numérica y no solo inspección visual.
44. **P1 — Lectura nocturna:** opción separada para reducir brillo del papel sin
    cambiar el fondo ni invalidar alto contraste.
45. **P1 — Ancho adaptable:** calcular sangrías y puntos de relleno con el mismo
    ancho lógico que el texto grande.
46. **P1 — Texto de tarifa:** truncar o partir tarifa y nivel en una métrica común,
    también con nombres traducidos largos.
47. **P2 — Sello de estado:** icono textual discreto para “rotando”, “fijo” o
    “suspensión”, siempre narrable.
48. **P2 — Fecha localizada:** usar formato regional localizado en vez de una forma
    única y fija.
49. **P2 — Aviso de cambio guardado:** feedback no modal al guardar configuración.
50. **P2 — Tecla de silencio visible:** mostrar en tooltip o ayuda contextual la
    tecla M, sin documentar el atajo administrativo oculto.

### D. Fondos, arquitectura y composición

51. **P0 — Culling por rectángulo:** recortar props fuera del lienzo antes de emitir
    `fill` para reducir costo en resoluciones grandes.
52. **P0 — Presupuesto de fills:** medir fills por nivel y fijar un máximo para que
    una escena no degrade FPS.
53. **P0 — Prueba de legibilidad:** generar cada nivel con hoja, sin hoja y en
    apagón para comprobar que el foco no queda atrás.
54. **P1 — Silueta de entrada:** reforzar el vano de cada nivel con una forma propia
    sin iluminarlo artificialmente.
55. **P1 — Profundidad por oclusión:** hacer que pilares, estantes y cañerías tengan
    una máscara de primer plano consistente.
56. **P1 — Puntos focales alternos:** variar lentamente el foco de lectura cuando
    vuelve el mismo nivel, sin mover la cámara si está desactivada.
57. **P1 — Administración:** añadir archivadores y numeración física muy tenue que
    relacionen hoja y pared.
58. **P1 — Depósito:** completar la grúa con carga suspendida y cable roto opcional.
59. **P1 — Servicio:** añadir válvula y manómetro geométricos con variación de
    presión sincronizada con sus eventos sonoros.
60. **P1 — Natatorio:** distinguir carriles, profundidad y borde mojado sin sumar
    tres capas de caústicas.
61. **P1 — Sala de piedra:** mejorar juntas de sillares y dirección de la luz del
    candil hacia el primer plano.
62. **P1 — Biblioteca:** añadir papeles sueltos o una lámpara focal sin convertir
    la escena en ruido repetido.
63. **P1 — Invernadero:** sumar condensación localizada y macetas reconocibles con
    siluetas asimétricas.
64. **P1 — Catacumbas:** reforzar nichos y alfeizares con profundidad de sombra,
    evitando cuadros flotantes.
65. **P1 — Cisterna:** separar pasarela, superficie de agua y reflejo con una línea
    de horizonte estable.
66. **P1 — Trono:** enriquecer ruina, corona ausente y estrado sin rojo; mantener
    el asiento como foco.
67. **P2 — Imperfección determinista:** crear daños distintos por nivel con semilla
    estable para que no cambien al redimensionar.
68. **P2 — Variación de materiales:** alternar metal, madera, piedra, vidrio y
    azulejo con paletas limitadas por recinto.
69. **P2 — Señalética diegética:** usar placas y flechas físicas, nunca texto HUD,
    para reforzar el trabajo y el peaje.
70. **P2 — Fondo por estado:** pequeños cambios en props durante transición,
    suspensión o ronda inminente.

### E. Efectos, animación, accesibilidad y rendimiento

71. **P0 — Congelación completa auditada:** verificar que movimiento reducido deja
    quietas también ondas, telas, haces, granos y props nuevos.
72. **P0 — Destellos independientes:** asegurar que ninguna animación de luz se
    confunde con un destello cuando el usuario los desactiva.
73. **P0 — Sin asignaciones por frame:** eliminar `new`, arrays y strings en rutas
    de render caliente o cachearlos por nivel/resolución.
74. **P1 — Cache geométrico:** precalcular coordenadas estáticas de cada planta al
    cambiar tamaño, no en cada frame.
75. **P1 — Cache de paleta:** precalcular colores iluminados por nivel y estado de
    luz cuando no cambien.
76. **P1 — Paso adaptativo:** aumentar el paso de barrido en ventanas pequeñas y
    mantener detalle solo cerca del foco.
77. **P1 — Frecuencia de eventos visuales:** limitar eventos a una ventana que no
    coincida con la entrada de widgets.
78. **P1 — Figura gradual:** evitar que la presencia aparezca al mismo tiempo que
    cambio de nivel o suspensión.
79. **P1 — Sombra de figura:** hacer que la sombra respete el suelo real de las
    diez plantas y no un valor aproximado.
80. **P1 — Movimiento de cámara:** establecer amplitud máxima por resolución para
    que el vaivén no maree en ventanas pequeñas.
81. **P1 — Reducción de partículas:** adaptar motas y humedad al área visible, no
    solo al ancho y alto nominales.
82. **P2 — Perfil visual:** añadir un comando local de vista previa que mida tiempo
    de render por capa sin tocar la UI del juego.
83. **P2 — Comparación visual:** generar diffs entre láminas antes/después para
    detectar props que desaparecen bajo la hoja.
84. **P2 — Paleta para daltonismo:** validar contraste sin depender de verde/rojo y
    reservar rojo solo para Executores.
85. **P2 — Vignette regulable:** separar intensidad de viñeta de escena y ronda,
    respetando papel limpio.

### F. Audio, mezcla y variedad

86. **P0 — Prueba de loops:** verificar automáticamente duración, firma OGG, mono,
    sample rate y ausencia de clipping en los 74 recursos.
87. **P0 — Mapa de capas:** validar que cada nivel tenga exactamente base,
    carácter, actividad y repertorio de eventos.
88. **P0 — Smoke test de reload:** reproducir recursos después de idioma, F3+T y
    activación del paquete local.
89. **P1 — Fundidos por nivel:** usar curvas de salida diferentes según tamaño del
    recinto para que depósitos y cisternas no corten igual.
90. **P1 — Arranques desfasados:** iniciar capas con fases controladas y no solo
    edades enteras para romper golpes simultáneos.
91. **P1 — Variación de tono acotada:** variar eventos dentro de rangos medidos,
    sin cambiar el material percibido.
92. **P1 — Ducking multibanda:** retirar graves o agudos concretos durante presencia
    en vez de bajar todo el ambiente por igual.
93. **P1 — Prioridad de transición:** reservar margen de pico medible para apagon,
    encendido y titileo.
94. **P1 — Repertorio por estado:** permitir actividad sutil en suspensión sin
    reintroducir eventos visuales prohibidos.
95. **P1 — Silencio intencional:** añadir ventanas de silencio verificables, sin
    convertir el menú en un metrónomo.
96. **P2 — Subtítulos opcionales:** subtitular eventos ambientales en una opción de
    accesibilidad, nunca por defecto.
97. **P2 — Test de fatiga:** medir RMS, LUFS aproximado y picos por familia para
    evitar que una cama tape la música.
98. **P2 — Preescucha de opciones:** reproducir un gesto de UI al enfocar controles
    de sonido, con cooldown y sin duplicar el click vanilla.
99. **P2 — Música local segura:** aceptar solo formatos que Minecraft decodifica y
    mostrar nombre del archivo sin atribuir autoría desconocida.
100. **P2 — Créditos legales:** separar marcador de crédito, licencia y pista
     local; impedir que una cadena por defecto atribuya otra obra.

## Registro de validación Windows y corrección del procedimiento

La prueba manual comunicada el 29/08/2026 no certifica esta versión. Se ejecutó
con `arena/01a04e0d-jobs-menu`, cuyo `mod_version` era 0.9.0, Java 21.0.12 y sin
Python instalado fuera del alias de Microsoft Store. Gradle sí terminó con
`BUILD SUCCESSFUL`, pero ese build no corresponde a esta rama ni a Java 17; el
JAR `jobsmenu-0.10.0.jar` no apareció.

La salida "BUILD SUCCESSFUL y despliegue terminado" tampoco era válida: se
continuó pegando comandos después de los `throw` y el intento de backup falló
antes de borrar el JAR anterior. El nuevo bloque de README valida rama, versión,
Java, Python, auditoría, build y artefacto en ese orden; usa `-LiteralPath` para
los JARs de `mods`, y solo imprime éxito al final. No se encontró evidencia de
pérdida del JAR anterior.

Esta corrección de procedimiento es de bajo riesgo y está implementada. El
build correcto de 0.10.0 con Java 17 y el arranque dentro de Minecraft siguen
siendo validaciones pendientes.

## Orden propuesto de ejecución

1. **Bloque P0:** reload, SoundEngine, resoluciones, culling, accesibilidad de
   animaciones y pruebas OGG.
2. **Bloque P1 de UI y fondos:** foco, estados presionado/terminal, cache de
   geometría, puntos focales y variación de composición.
3. **Bloque P1 de audio:** ducking, fases, transiciones y repertorio por estado.
4. **Bloque P2:** pulido visual, subtítulos, perfil y documentación.

Cada bloque debe ir en commits separados y pasar por `tools/verificar.py`,
`git diff --check`, compilacion con Java 17 y prueba manual en Forge 47.3.x.
No se debe afirmar que una mejora de pantalla vanilla es segura hasta probarla
con pantalla de idioma, recursos, controles, sonido, Mods, F3+Esc, servidor,
Realms, redimensionado y un modpack con Embeddium/Iris/BetterClouds.
