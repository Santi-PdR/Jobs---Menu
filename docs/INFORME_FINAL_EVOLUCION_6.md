# Informe final — Evolución 6 (Jobs Menu, Forge 1.20.1)

**Rama:** `arena/01a04ff1-jobs-menu` · **Base:** `811586e` · **Fecha:** 2026-08-29

Este informe diferencia siempre entre **revisión estática** (ejecutada, verde),
**compilación** (pendiente: entorno sin JDK 17 ni wrapper) y **prueba real en
Minecraft** (pendiente). Ninguna afirmación de runtime se da por verificada.

---

1. **Estado inicial.** Mod cliente Forge 1.20.1 `jobsmenu` v0.10.0 (Forge
   47.4.0, mapeos official 1.20.1). Diez fondos procedurales, menú de aviso que
   sustituye TitleScreen y PauseScreen, música propia, ~74 sonidos ambientales
   sintetizados, config Forge completa, espejo Python de composición y
   verificador estático.
2. **Ramas revisadas.** `main` (solo el merge `811586e`) y la rama de trabajo
   previa `arena/01a04e24-jobs-menu` integrada en ella. No había otras ramas ni
   backups. No se hicieron merges de ramas antiguas.
3. **Backups creados.** `backup-A-inicial-0.10.0` (`14efd06`), `backup-B-
   tecnica-ui-sonido-config` (tras la etapa técnica), `backup-C-final` (estado
   final de esta evolución). Tags de git subidos a GitHub; registro en
   `backups/README.md`; ninguna copia dentro de `mods/`.
4. **Archivos y sistemas auditados.** 28 archivos con diff en esta rama
   (+1230/−99): config, ciclo de vida (`SesionMenu`, `EscuchaCliente`), música
   (`GestorMusica`, `CapaAmbiente`, `GestorAmbiente`), escena (`EscenaNivel`,
   `RotacionNiveles`, `TratamientoEscena`), las 10 plantas, pantallas
   (`PantallaNivel`, `PantallaAjustesAviso`), idiomas ES/EN, espejo Python y
   matriz de fondos. Además se releyó el resto del árbol (UI, sonidos, docs,
   herramientas).
5. **Bugs encontrados.** (a) `OptionsList.addTitle` no existe en 1.20.1 y se
   usaba en `PantallaAjustesAviso`; (b) rotar de nivel con Opciones/Mods
   abiertos dejaba el recinto nuevo en silencio (ambiente atado a la pantalla);
   (c) la vigilancia de instancia fantasma podía dar falsas alarmas con el
   juego pausado o sin foco; (d) `vista_previa.py` ignoraba `--nivel N` con
   espacio; (e) `DOVELAS` de `Cripta` era constante muerta (el arco no se
   construía por piezas).
6. **Riesgos potenciales.** Build y runtime no verificables en este entorno;
   integración con el modpack real sin probar; `MusicaPropia` solo acepta OGG
   Vorbis (cabecera inválida informada al log); pista REQUIEM con permiso de
   redistribución pendiente.
7. **Bugs corregidos.** (a) `PantallaAjustesAviso` sin `addTitle`
   (`OptionsList` solo ofrece `addBig`/`addSmall`/`addAll`); (b)
   `GestorAmbiente.mantenerCamas()` en el tick del cliente con guard de visita;
   (c) vigía de fantasma desarmado cuando el cliente no tickea; (d) `--nivel N`
   normalizado a `--nivel=N`; (e) las dovelas del arco más cercano de la bóveda
   ahora se dibujan (SA-11).
8. **Código eliminado.** No se eliminó código vivo: se retiró el uso inválido
   de `addTitle`, se evitó la duplicación de `DireccionArte` en
   `PantallaEstancia` (no aplica) y se aprovechó la constante `DOVELAS` que
   estaba muerta. Los comentarios obsoletos de la matriz de fondos se
   actualizaron.
9. **Las 30 mejoras evaluadas.** Catálogo completo (implementadas,
   rechazadas y pospuestas) en `docs/CATALOGO_MEJORAS_Y_FUNCIONES.md`.
   Se evaluaron más de 30; las implementadas suman 13 en la sección A y 10 en
   la sección B.
10. **Mejoras implementadas.** Ver catálogo: configuración ampliada y blindada,
    continuidad por visita, camas vivas en pantallas hijas, volumen en vivo,
    vigía de fantasma, diagnóstico oculto, salto manual, bajo consumo en
    render, textos ES/EN, y las 10 mejoras artísticas de la etapa 2.
11. **Mejoras rechazadas y motivo.** Duplicar `DireccionArte` en
    `PantallaEstancia` (esa pantalla no dibuja escena viva); overlay de
    historial visual en la UI (sobrecarga). Ver sección D del catálogo.
12. **Las 8 funciones nuevas evaluadas.** Ver catálogo sección C: las ocho
    propuestas se evaluaron y se implementaron (F1–F8).
13. **Funciones implementadas.** Duración de estancia, salto manual, bajo
    consumo, perfil accesible, continuidad de ambiente por visita, vigilancia
    de fantasma, diagnóstico oculto y forma `--nivel N` del espejo.
14. **Funciones pospuestas y motivo.** Historial/selector visual de escenarios
    y favoritos/nivel preferido (duplican `nivel_fijo` y el salto manual);
    transiciones contextuales por pantalla de origen (la transición por frame
    ya es coherente). Quedan registradas para una evolución futura sin cargar
    la interfaz.
15. **Arquitectura.** Sin mixins ni dependencias nuevas. Escena procedural por
    planta (`planta/*.java`) + `DireccionArte` + `Trazo`; audio por
    `GestorMusica`/`GestorAmbiente` con ciclo de vida por visita; configuración
    Forge con accesos protegidos; espejo Python para validar composición.
16. **UI/UX.** La superficie de la UI no cambió (no se añadieron controles
    vanilla duplicados); sí cambió el comportamiento: salto manual con
    antirrepetición, perfil accesible desde Opciones nativa y continuidad del
    ambiente al navegar pantallas hijas. `PantallaEstancia` sigue sin escena
    viva para proteger la legibilidad del papel.
17. **Botones e hitboxes.** No se tocaron hitboxes nativas en esta rama; los
    cambios de pantalla (`PantallaNivel`, `PantallaAjustesAviso`) conservan los
    widgets de 1.20.1 con narración y Tab.
18. **Sliders.** Se añadió `duracion_estancia` (15–90, defecto 24) con
    aplicación en vivo; el volumen de ambiente se recalcula por tick
    (aplicación inmediata); todos los rangos validan 0/1/50/99/100 y los
    valores fuera de rango quedan protegidos por `defineInRange`.
19. **Configuración.** `perfil_accesible` y `bajo_consumo` nuevos; setters
    manuales desactivan el perfil; guardado diferido con vuelco al cerrar;
    accesos seguros cuando el SPEC no está cargado (config corrupta o ausente →
    defectos).
20. **Accesibilidad.** Movimiento reducido, destellos reducidos, alto
    contraste y texto grande combinables en un perfil; bajo consumo como
    palanca adicional para equipos modestos; narración vanilla intacta.
21. **Interfaces tematizadas.** TitleScreen y PauseScreen (sustituidas por el
    aviso), Opciones (botón "Condiciones de estancia") y pantallas hijas del
    aviso. Sin overlays sobre pantallas de otros mods.
22. **Interfaces no modificadas.** OptionsList vanilla, Mods, SelectWorld,
    Singleplayer/Multiplayer, Resource Packs, Language, controles de Cloth
    Config/Controlling/Searchables y demás pantallas de mods.
23. **Música.** REQUIEM con una única instancia, dependiente de Master + slider
    propio (`volumen_musica`); Music vanilla 0 % no la silencia (usa su propia
    categoría de reproducción), Master 0 % sí; el volumen se aplica en vivo; se
    invalida al entrar a mundo y se restaura al volver; sobrevive a Opciones y
    a F3+T mediante la limpieza de `RecargaRecursosCliente`.
24. **Resultado de Music vanilla.** No se modifica la opción global de Music
    del jugador; la pista del menú es independiente de ella por diseño.
25. **Regreso desde mundo y servidor.** La continuidad pertenece a la visita
    (`SesionMenu.activa()`); al volver al título la estancia se reanuda y las
    camas se reaseguran sin duplicar instancias.
26. **Sonido ambiental.** 3 camas por nivel (BASE/CARÁCTER/ACTIVIDAD) con
    volúmenes por capa; el slider de ambiente se aplica en vivo; respiración,
    agachado, apagón y suspensión modulan el objetivo por tick; silencio como
    parte del diseño.
27. **Eventos raros.** La Suspensión (apagón de 22 s cada 45–52 min)
    conserva su comportamiento; el bajo consumo no la rompe (solo apaga
    efectos visuales). Validación en sesión larga pendiente.
28. **Optimización.** Sin bucles por píxel nuevos; las adiciones artísticas
    usan fills deterministas con culling por profundidad; sin objetos por
    frame; presupuesto de fills respetado en el espejo. Bajo consumo elimina
    polvo/grano/presencia/motas.
29. **Cambios de cada background.** Tabla completa en el catálogo (sección B):
    AD-15, DE-17, SE-11, NA-22, SA-11, BI-12, IN-14, CA-13, CI-11 y TR.
30. **Rediseño del Trono.** Tarima 1.18, estrado de seis escalones con
    proporciones monumentales, ábside con tres dovelas concéntricas (oro solo
    en el arco interno), hueco de corona ausente grande y oscuro, estandartes
    torcidos con sesgo determinista. El trono es el foco inequívoco.
31. **Código antiguo eliminado.** Llamadas a `addTitle`; esquema de `--nivel`
    con espacio en el espejo; comentarios de filas de la matriz sin estado.
32. **Documentación actualizada.** README, CONTEXTO (referencias), CHANGELOG,
    KNOWN_ISSUES, `docs/EVOLUCION_6.md`, `docs/CATALOGO_MEJORAS_Y_FUNCIONES.md`,
    `docs/INFORME_FINAL_EVOLUCION_6.md`, `docs/AUDITORIA_FONDOS_50X10.md`,
    `backups/README.md`. Ctrl+S no aparece en ninguna documentación pública.
33. **Compatibilidad.** Sin mixins ni dependencias nuevas; sin sustituir
    subclases de pantallas ajenas; `PantallaAjustesAviso` compatible con
    `OptionsList` de 1.20.1; los fondos no taponan botones ni overlays de
    otros mods.
34. **Riesgos restantes.** Build y runtime sin verificar; modpack real sin
    probar; OGG con cabecera inválida; pista REQUIEM sin permiso de
    redistribución; validación visual dentro de Minecraft pendiente para las
    10 mejoras artísticas.
35. **Rama de trabajo.** `arena/01a04ff1-jobs-menu` (única rama de esta
    sesión; todo el trabajo se pusheó a ella).
36. **Commits importantes.** `14efd06` (Backup A), `5702eb2` y `ae48a07`
    (Etapa 1), `08ed9bf` (Trono), `52eea3f`…`dd2ff9f` (fondos 0–8),
    `bee642a` (matriz).
37. **Commit final.** El último commit de la rama tras esta pasada de
    documentación (ver `git log` en la rama).
38. **Resultado del build.** Pendiente: este entorno no tiene JDK 17 ni
    `gradle-wrapper.jar`, y la red hacia gradle.org / maven.minecraftforge.net
    está bloqueada. No se declara ningún build como exitoso.
39. **JAR generado.** Ninguno en esta sesión. El JAR `jobsmenu-0.10.0.jar` debe
    generarse localmente con JDK 17 según el procedimiento del README y
    probarse dentro de Minecraft antes de declarar la versión validada.
40. **Pruebas pendientes dentro de Minecraft.** Checklist completa en
    `docs/checklist-manual.md` + `KNOWN_ISSUES.md`: resoluciones y GUI scale,
    hitboxes, ciclo mundo/servidor/Realms, F3+Esc, la pista con Master/Music en
    0/100, F3+T, Alt+Tab, resource packs, sesión de 52 min para La Suspensión,
    modpack real (Embeddium, Oculus, ImmediatelyFast, Sophisticated,
    Cloth Config, Controlling, etc.), y revisión visual de las 10 mejoras
    artísticas (filas marcadas en `AUDITORIA_FONDOS_50X10.md`).
