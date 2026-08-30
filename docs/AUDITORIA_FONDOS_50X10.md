# Auditoría de backgrounds — 50 criterios por escenario

Fecha: 2026-08-29
Rama: `arena/01a04ff1-jobs-menu`
Base: `0.10.0`
Estado: matriz de aceptación; la Evolución 6 marcó las filas AD-15, DE-17,
SE-11, NA-22, SA-11, BI-12, IN-14, CA-13, CI-11 y TR-09/10/11/16/17 como
implementadas (ver `docs/EVOLUCION_6.md`)

## Lectura honesta

Esta matriz convierte el requisito de cincuenta mejoras por escenario en criterios verificables y no en una afirmación de que quinientas mejoras ya estén implementadas. Cada fila tiene un objetivo concreto, un punto de intervención y una prueba de aceptación. Las filas pendientes no se cuentan como código entregado hasta que exista un diff y una comprobación visual. La pasada actual conserva la arquitectura procedural, corrige lifecycle y prepara el backup específico previo a cualquier nueva reescritura de fondos.

## Reglas comunes de aceptación

- No se aceptan cambios de color sin cambio de lectura, geometría, material o composición.
- Cada criterio debe probarse desnudo, con hoja, con pausa cuando corresponda y durante transición.
- Cada criterio visual debe revisarse en 320×240, 854×480, 1920×1080, 4:3 y ultrawide.
- Movimiento reducido congela animaciones; destellos reducidos elimina variación de luminancia.
- El render debe mantener el presupuesto de fills y no crear listas/objetos temporales por frame.
- La matriz no autoriza añadir overlays a pantallas de otros mods ni usar rojo fuera de Ejecutores.

## Resumen de estado

| Escenario | Criterios auditados | Implementados en esta pasada | Validación Minecraft |
|---|---:|---:|---|
| Nivel 0 — Administración | 50 | 4 | Estático parcial; Minecraft pendiente |
| Nivel 1 — Depósito | 50 | 5 | Estático parcial; Minecraft pendiente |
| Nivel 2 — Servicio | 50 | 5 | Estático parcial; Minecraft pendiente |
| Nivel 3 — Natatorio | 50 | 4 | Estático parcial; Minecraft pendiente |
| Nivel 4 — Sala de piedra | 50 | 4 | Estático parcial; Minecraft pendiente |
| Nivel 5 — Biblioteca | 50 | 4 | Estático parcial; Minecraft pendiente |
| Nivel 6 — Invernadero | 50 | 4 | Estático parcial; Minecraft pendiente |
| Nivel 7 — Catacumbas | 50 | 4 | Estático parcial; Minecraft pendiente |
| Nivel 8 — Cisterna | 50 | 4 | Estático parcial; Minecraft pendiente |
| Nivel 9 — Trono | 50 | 11 | Estático parcial; Minecraft pendiente |

## Nivel 0 — Administración

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **AD-01 · Cámara** desplazar la fuga ligeramente a la derecha para que el vestíbulo no parezca simétrico. **Target:** `Marco de nivel0`. **Prueba:** el eje no coincide con el centro del lienzo.
- **AD-02 · Cámara** elevar el horizonte para dar escala institucional al techo. **Target:** `fugaY de nivel0`. **Prueba:** el techo ocupa más campo que el suelo.
- **AD-03 · Cámara** reservar un margen oscuro de entrada a la izquierda. **Target:** `Planta/Sala`. **Prueba:** la hoja no tapa el margen de profundidad.
- **AD-04 · Composición** colocar el mostrador como segundo foco, no como centro. **Target:** `PrimerPlano`. **Prueba:** el ojo llega primero al vano y luego al mostrador.
- **AD-05 · Composición** abrir un corredor lateral visible detrás de la señalética. **Target:** `Sala`. **Prueba:** existe una dirección secundaria legible sin texto.
- **AD-06 · Composición** separar tres planos de oficinas con solapes distintos. **Target:** `Sala y Marco`. **Prueba:** se distinguen fondo, medio y primer plano.
- **AD-07 · Composición** reducir la simetría de las lámparas del vestíbulo. **Target:** `Sala`. **Prueba:** las luminarias no forman una fila perfectamente repetida. **Estado:** Implementado con un tubo fuera de servicio en el sexto tramo; validación dentro de Minecraft pendiente.
- **AD-08 · Composición** dejar una zona de silencio visual junto al título. **Target:** `EscenaNivel`. **Prueba:** la UI conserva contraste sin prop debajo.
- **AD-09 · Escala** hacer que el sello central sea menor que una puerta lejana. **Target:** `DireccionArte.administracion`. **Prueba:** el sello no roba perspectiva.
- **AD-10 · Escala** variar la anchura de archivadores por profundidad. **Target:** `PrimerPlano`. **Prueba:** los objetos cercanos superan a los lejanos.
- **AD-11 · Arquitectura** añadir un dintel pesado sobre el vano principal. **Target:** `Sala`. **Prueba:** la entrada tiene peso estructural. **Estado:** Implementado con dintel pesado integrado al vano principal; validación dentro de Minecraft pendiente.
- **AD-12 · Arquitectura** dibujar una junta vertical desplazada en el muro de fondo. **Target:** `MaterialesEscena.papelMural`. **Prueba:** no coincide con el eje.
- **AD-13 · Arquitectura** incluir una moldura de servicio debajo del techo. **Target:** `DireccionArte`. **Prueba:** conecta paredes y techo.
- **AD-14 · Arquitectura** quebrar una esquina con una columna parcial. **Target:** `PrimerPlano`. **Prueba:** la columna corta el borde sin tapar botones.
- **AD-15 · Arquitectura** marcar una abertura de mantenimiento en un lateral. **Target:** `Sala`. **Estado:** Implementado como abertura de mantenimiento con marco, bisagras e interior que no termina en la pared; validación dentro de Minecraft pendiente. **Prueba:** la abertura tiene marco y profundidad.
- **AD-16 · Silueta** hacer reconocibles las dos torres de luz como tótems administrativos. **Target:** `DireccionArte.torreLuz`. **Prueba:** se leen apagadas y encendidas.
- **AD-17 · Silueta** separar la señalética de la silueta del vano. **Target:** `DireccionArte.runas`. **Prueba:** no se funden los contornos.
- **AD-18 · Silueta** dar un borde irregular a la cinta del documento. **Target:** `HojaPapel/escena`. **Prueba:** la cinta no parece rectángulo perfecto.
- **AD-19 · Profundidad** ocultar parcialmente un archivador con el primer plano. **Target:** `PrimerPlano`. **Prueba:** hay oclusión real entre capas.
- **AD-20 · Profundidad** graduar la niebla del testero sin lavar la hoja. **Target:** `TratamientoEscena`. **Prueba:** la hoja supera al fondo en contraste.
- **AD-21 · Material** introducir vetas de papel mural en dos orientaciones. **Target:** `MaterialesEscena`. **Prueba:** no son líneas repetidas con una sola fase.
- **AD-22 · Material** diferenciar metal pintado de hormigón en zócalo. **Target:** `Sala/MaterialesEscena`. **Prueba:** ambos devuelven luz distinto.
- **AD-23 · Material** añadir grapas y remaches en la placa de administración. **Target:** `DireccionArte`. **Prueba:** cada remache queda anclado a una junta. **Estado:** Implementado con placa lateral, cuatro remaches y ranuras grabadas; validación dentro de Minecraft pendiente.
- **AD-24 · Material** reservar desgaste oscuro para las esquinas de paso. **Target:** `MaterialesEscena`. **Prueba:** el desgaste sigue geometría de contacto.
- **AD-25 · Material** añadir una gotera mínima solo donde el zócalo se humedece. **Target:** `humedad`. **Prueba:** no aparece en todo el lienzo.
- **AD-26 · Luz principal** usar fluorescente verdoso desde arriba, no un velo uniforme. **Target:** `brilloFluorescente`. **Prueba:** techo y suelo reciben valores distintos.
- **AD-27 · Luz secundaria** dar rebote cálido muy bajo al mostrador. **Target:** `TratamientoEscena.reboteSuelo`. **Prueba:** el acento no invade el título.
- **AD-28 · Luz secundaria** hacer que las torres tengan pulsos desfasados. **Target:** `DireccionArte.pulso`. **Prueba:** nunca respiran juntas.
- **AD-29 · Sombras** proyectar una sombra estrecha bajo archivadores cercanos. **Target:** `PrimerPlano`. **Prueba:** la sombra sigue la base del objeto.
- **AD-30 · Sombras** bajar el testero antes de bajar los laterales durante transición. **Target:** `EscenaNivel`. **Prueba:** el foco sobrevive al corte.
- **AD-31 · Reflejo** reflejar una luminaria en el suelo encerado. **Target:** `TratamientoEscena`. **Prueba:** reflejo más corto y menos intenso que la fuente.
- **AD-32 · Color** mantener verde institucional como acento, no como relleno. **Target:** `Paleta/DireccionArte`. **Prueba:** la mayor parte sigue siendo papel, hormigón y vano.
- **AD-33 · Contraste** medir contraste de runas contra muro oscuro. **Target:** `herramienta de preview`. **Prueba:** supera el umbral documentado.
- **AD-34 · Densidad** limitar props administrativos a tres grupos legibles. **Target:** `presupuesto de fills`. **Prueba:** no hay ruido detrás de texto.
- **AD-35 · Forma extraña** inclinar una placa de procedimiento en el plano medio. **Target:** `PrimerPlano`. **Prueba:** comunica abandono sin parecer un error.
- **AD-36 · Movimiento** animar solo el zumbido de lámparas y polvo lejano. **Target:** `DireccionArte`. **Prueba:** movimiento reducido lo congela.
- **AD-37 · Evento propio** hacer que una torre pierda intensidad cuando ocurre el tubo. **Target:** `sincronía visual/audio`. **Prueba:** el evento no cambia la geometría.
- **AD-38 · Presencia** reservar el vano central para la figura sin competir con torres. **Target:** `Presencia/piso`. **Prueba:** la silueta sigue identificable.
- **AD-39 · Transición** apagar primero señalética, después fluorescentes. **Target:** `RotacionNiveles`. **Prueba:** orden consistente en 2,6 s.
- **AD-40 · Legibilidad UI** excluir el área de la hoja del ruido de runas. **Target:** `EscenaNivel`. **Prueba:** el papel se lee con y sin interfaz mínima.
- **AD-41 · Resolución** preservar dintel y mostrador a 320x240 lógico. **Target:** `preview`. **Prueba:** el foco no colapsa en una franja.
- **AD-42 · Rendimiento** cachear las posiciones de archivadores por tamaño de lienzo. **Target:** `renderer`. **Prueba:** no hay listas nuevas por frame.
- **AD-43 · Rendimiento** recortar torres y runas fuera del lienzo. **Target:** `culling`. **Prueba:** fills no aumentan con props invisibles.
- **AD-44 · Accesibilidad** congelar pulso y polvo con movimiento reducido. **Target:** `EscenaNivel`. **Prueba:** dos capturas son idénticas salvo la luz permitida.
- **AD-45 · Accesibilidad** eliminar titileo con destellos reducidos. **Target:** `RotacionNiveles`. **Prueba:** no hay saltos de luminancia.
- **AD-46 · Identidad** incluir un número físico de nivel en la arquitectura, no solo en la hoja. **Target:** `señalética`. **Prueba:** se reconoce sin leer el menú.
- **AD-47 · Identidad** dar al mostrador un sello de “turnos” propio. **Target:** `PrimerPlano`. **Prueba:** no se confunde con una recepción genérica.
- **AD-48 · Verificación** comparar render desnudo, con hoja y en suspensión. **Target:** `vista_previa`. **Prueba:** los tres mantienen el punto focal.
- **AD-49 · Verificación** contar fills por capa de administración. **Target:** `perfil visual`. **Prueba:** queda bajo el presupuesto P0.
- **AD-50 · Verificación** probar ultrawide y 4:3 con fuga desplazada. **Target:** `Marco`. **Prueba:** ninguna superficie sale por un borde inesperado.

## Nivel 1 — Depósito

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **DE-01 · Cámara** bajar la fuga para que el techo del depósito parezca inalcanzable. **Target:** `Marco de nivel1`. **Prueba:** la escala vertical domina sin cortar la hoja.
- **DE-02 · Cámara** inclinar el eje hacia la grúa y no hacia el centro. **Target:** `Planta/Nave`. **Prueba:** la grúa guía la lectura.
- **DE-03 · Cámara** dejar un borde de carga visible en primer plano. **Target:** `PrimerPlano`. **Prueba:** la escena tiene cercanía material.
- **DE-04 · Composición** usar la grúa como foco alto y una carga como foco bajo. **Target:** `DireccionArte.gruaDeposito`. **Prueba:** dos alturas con jerarquía. **Estado:** Implementado con carga suspendida visible y acento contenido; validación dentro de Minecraft pendiente.
- **DE-05 · Composición** abrir una línea de estanterías al fondo. **Target:** `Nave`. **Prueba:** se entiende profundidad de almacén.
- **DE-06 · Composición** romper la repetición de pilares con una ausencia. **Target:** `DireccionArte.deposito`. **Prueba:** una bahía queda vacía. **Estado:** Implementado con ausencia puntual del pilar derecho en la octava bahía; validación dentro de Minecraft pendiente.
- **DE-07 · Composición** escalonar cargas por tamaño y profundidad. **Target:** `PrimerPlano`. **Prueba:** la carga cercana no parece un píxel suelto.
- **DE-08 · Composición** mantener el área del aviso limpia mediante oclusión lateral. **Target:** `EscenaNivel`. **Prueba:** props no cruzan el papel.
- **DE-09 · Escala** hacer que el cable de la grúa sea fino y su carga pesada. **Target:** `gruaDeposito`. **Prueba:** tensión visual creíble.
- **DE-10 · Escala** reducir señalética lejana a dos píxeles de alto. **Target:** `Nave`. **Prueba:** no compite con el título.
- **DE-11 · Arquitectura** añadir cerchas diagonales a dos vanos. **Target:** `Nave`. **Prueba:** las diagonales no son un filtro plano.
- **DE-12 · Arquitectura** mostrar una puerta de muelle lateral entre pilares. **Target:** `Nave`. **Prueba:** tiene jambas, dintel y umbral. **Estado:** Implementado con puerta lateral de muelle, jambas, dintel, umbral y bisagras; validación dentro de Minecraft pendiente.
- **DE-13 · Arquitectura** variar altura de las bahías del techo. **Target:** `Marco/Planta`. **Prueba:** el techo no es una cuadrícula plana.
- **DE-14 · Arquitectura** introducir un pasillo de servicio detrás de la carga. **Target:** `Nave`. **Prueba:** hay segundo recorrido visual.
- **DE-15 · Arquitectura** apoyar el pilar roto en una placa de base. **Target:** `PrimerPlano`. **Prueba:** no flota.
- **DE-16 · Silueta** diferenciar grúa, estantería y carga contra el vano. **Target:** `DireccionArte`. **Prueba:** tres siluetas se reconocen a contraluz.
- **DE-17 · Silueta** dejar una lona caída como forma irregular. **Target:** `PrimerPlano`. **Estado:** Implementado como lona de carga caida con cuatro bandas plegadas, costuras y borde de luz; validación dentro de Minecraft pendiente. **Prueba:** no es un rectángulo de color.
- **DE-18 · Silueta** quebrar el horizonte con una carga suspendida. **Target:** `gruaDeposito`. **Prueba:** el vacío del techo conserva respiración.
- **DE-19 · Profundidad** variar longitud de cables según dx real. **Target:** `Marco`. **Prueba:** cada cable converge a la fuga.
- **DE-20 · Profundidad** usar un pilar cercano para ocluir una estantería. **Target:** `Nave`. **Prueba:** la oclusión confirma escala.
- **DE-21 · Material** separar acero negro, acero pintado y hormigón. **Target:** `MaterialesEscena.metalHormigon`. **Prueba:** tres respuestas de luz.
- **DE-22 · Material** añadir soldaduras en las uniones de la grúa. **Target:** `gruaDeposito`. **Prueba:** siguen nodos estructurales. **Estado:** Implementado con dos soldaduras localizadas en nodos de la grúa; validación dentro de Minecraft pendiente.
- **DE-23 · Material** incluir etiquetas de carga gastadas en dos cajas. **Target:** `PrimerPlano`. **Prueba:** se leen como marcas, no como UI.
- **DE-24 · Material** añadir óxido concentrado bajo remaches. **Target:** `MaterialesEscena`. **Prueba:** gotea hacia abajo.
- **DE-25 · Material** crear polvo acumulado en el zócalo, no en el aire entero. **Target:** `MaterialesEscena`. **Prueba:** densidad localizada.
- **DE-26 · Luz principal** hacer que un fluorescente falle sobre la bahía vacía. **Target:** `brilloFluorescente`. **Prueba:** el fallo tiene foco espacial.
- **DE-27 · Luz secundaria** iluminar la carga desde una lámpara de trabajo cálida. **Target:** `gruaDeposito`. **Prueba:** acento puntual separado del verde.
- **DE-28 · Luz secundaria** apagar la lámpara de trabajo durante transición. **Target:** `estado de luz`. **Prueba:** la grúa permanece como silueta.
- **DE-29 · Sombras** proyectar carga sobre el suelo y no sobre el papel. **Target:** `PrimerPlano`. **Prueba:** sombra contenida por geometría.
- **DE-30 · Sombras** hacer más largas las sombras de pilares cercanos. **Target:** `Nave`. **Prueba:** escala por profundidad.
- **DE-31 · Reflejo** dar brillo horizontal a una placa metálica. **Target:** `MaterialesEscena`. **Prueba:** el brillo es menor que la luz fuente.
- **DE-32 · Color** contener naranja de seguridad a etiquetas y conos. **Target:** `Paleta`. **Prueba:** no tiñe toda la nave.
- **DE-33 · Contraste** distinguir carga suspendida en el apagón al 4 %. **Target:** `preview`. **Prueba:** silueta queda legible sin destellos.
- **DE-34 · Densidad** reservar el centro para vacío industrial. **Target:** `presupuesto`. **Prueba:** menos props que en laterales.
- **DE-35 · Forma extraña** incluir una tarima ladeada que no sigue la retícula. **Target:** `PrimerPlano`. **Prueba:** abandono físico reconocible.
- **DE-36 · Movimiento** desplazar lentamente la carga, no la cámara. **Target:** `gruaDeposito`. **Prueba:** movimiento reducido congela la carga.
- **DE-37 · Evento propio** sincronizar crujido con una microoscilación de cable. **Target:** `audio/eventos`. **Prueba:** uno por evento, sin bucle visible.
- **DE-38 · Presencia** colocar la figura detrás de la carga para que se descubra por oclusión. **Target:** `Presencia`. **Prueba:** no aparece encima de la UI.
- **DE-39 · Transición** desenergizar bahías en sentido alternado. **Target:** `RotacionNiveles`. **Prueba:** el depósito no se apaga como bloque uniforme.
- **DE-40 · Legibilidad UI** recortar etiquetas y polvo bajo el rectángulo de hoja. **Target:** `EscenaNivel`. **Prueba:** 100 % de texto limpio.
- **DE-41 · Resolución** conservar grúa como línea continua en 4:3 y ultrawide. **Target:** `preview`. **Prueba:** no se rompe en extremos.
- **DE-42 · Rendimiento** emitir solo bahías que intersecan viewport. **Target:** `culling`. **Prueba:** fills estables en 4K.
- **DE-43 · Rendimiento** precalcular anclajes de remaches por ancho/alto. **Target:** `renderer`. **Prueba:** no hay colecciones temporales.
- **DE-44 · Accesibilidad** no usar oscilación de grúa si movimiento reducido. **Target:** `escena viva`. **Prueba:** captura estática.
- **DE-45 · Accesibilidad** mantener cargas visibles sin flashes. **Target:** `destellos reducidos`. **Prueba:** contraste constante.
- **DE-46 · Identidad** mostrar una numeración de bahía no coincidente con el nivel. **Target:** `Nave`. **Prueba:** parece sistema de almacén real.
- **DE-47 · Identidad** hacer que el cable roto sugiera abandono, no accidente gráfico. **Target:** `gruaDeposito`. **Prueba:** remate irregular anclado.
- **DE-48 · Verificación** reconocimiento ciego depósito frente a nave genérica. **Target:** `checklist`. **Prueba:** 8/10 reconocen grúa/carga.
- **DE-49 · Verificación** contar objetos grandes y comprobar jerarquía. **Target:** `perfil`. **Prueba:** ningún prop tapa botones.
- **DE-50 · Verificación** comparar con y sin hoja en ventana 320x240. **Target:** `preview`. **Prueba:** punto grúa sigue presente.

## Nivel 2 — Servicio

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **SE-01 · Cámara** colocar la fuga cerca del conducto principal, fuera del centro. **Target:** `Marco de nivel2`. **Prueba:** tuberías convergen a un punto lateral.
- **SE-02 · Cámara** elevar la vista para mostrar bandejas superiores. **Target:** `Planta/Servicio`. **Prueba:** el techo tiene infraestructura.
- **SE-03 · Cámara** mantener un hueco oscuro bajo las máquinas. **Target:** `Servicio`. **Prueba:** suelo no es una banda uniforme.
- **SE-04 · Composición** separar tubería, panel de control y válvula en tres distancias. **Target:** `DireccionArte.panelServicio`. **Prueba:** jerarquía de trabajo.
- **SE-05 · Composición** orientar el panel hacia el punto de fuga. **Target:** `Servicio`. **Prueba:** sus reglas no son horizontales arbitrarias.
- **SE-06 · Composición** dejar un conducto cruzado como marco superior. **Target:** `DireccionArte`. **Prueba:** enmarca sin invadir título.
- **SE-07 · Composición** romper una repetición con una unión abierta. **Target:** `Servicio`. **Prueba:** se ve profundidad y mantenimiento.
- **SE-08 · Composición** reservar el lado de lectura para pared más tranquila. **Target:** `EscenaNivel`. **Prueba:** UI limpia.
- **SE-09 · Escala** hacer el manómetro suficientemente grande para leerse como máquina. **Target:** `panelServicio`. **Prueba:** no parece píxel decorativo.
- **SE-10 · Escala** reducir indicadores lejanos según profundidad. **Target:** `Servicio`. **Prueba:** perspectiva consistente.
- **SE-11 · Arquitectura** añadir bandeja de cables con soportes. **Target:** `Servicio`. **Estado:** Implementado como bandeja de cables colgada del techo con tres soportes y bucle de cable suelto; validación dentro de Minecraft pendiente. **Prueba:** cables tienen anclaje.
- **SE-12 · Arquitectura** mostrar un nicho de válvulas detrás del panel. **Target:** `Servicio`. **Prueba:** segundo plano real.
- **SE-13 · Arquitectura** usar una compuerta de inspección abierta. **Target:** `Servicio`. **Prueba:** interior oscuro y bisagras. **Estado:** Implementado con hoja entreabierta, jambas, bisagras y manija; validación dentro de Minecraft pendiente.
- **SE-14 · Arquitectura** diferenciar piso técnico de pared. **Target:** `MaterialesEscena`. **Prueba:** junta horizontal cambia de material.
- **SE-15 · Arquitectura** incluir una escalera corta hacia una pasarela. **Target:** `Servicio`. **Prueba:** no es pasillo plano.
- **SE-16 · Silueta** hacer tubos gruesos, finos y flexibles reconocibles. **Target:** `Servicio`. **Prueba:** tres perfiles materiales.
- **SE-17 · Silueta** convertir la válvula en círculo con eje y manija. **Target:** `PrimerPlano`. **Prueba:** se identifica sin texto. **Estado:** Implementado con aro, eje, manija y placa de anclaje; validación dentro de Minecraft pendiente.
- **SE-18 · Silueta** dejar una manguera caída en curva irregular. **Target:** `Servicio`. **Prueba:** no es línea geométrica repetida. **Estado:** Implementado con curva segmentada y brida terminal; validación dentro de Minecraft pendiente.
- **SE-19 · Profundidad** escalar abrazaderas con dx. **Target:** `Marco/Servicio`. **Prueba:** todas convergen.
- **SE-20 · Profundidad** ocluir una tubería con el marco del panel. **Target:** `PrimerPlano`. **Prueba:** panel está delante.
- **SE-21 · Material** metal galvanizado, cobre y goma deben tener valores distintos. **Target:** `MaterialesEscena`. **Prueba:** tres lecturas al mismo brillo.
- **SE-22 · Material** añadir manchas de condensación bajo codos. **Target:** `humedad`. **Prueba:** solo en cambios de dirección.
- **SE-23 · Material** dibujar remaches en la puerta de inspección. **Target:** `MaterialesEscena`. **Prueba:** remaches forman perímetro.
- **SE-24 · Material** añadir cal mineral alrededor de la válvula. **Target:** `Servicio`. **Prueba:** desgaste nace de humedad. **Estado:** Implementado como cal mineral localizada bajo la válvula; validación dentro de Minecraft pendiente.
- **SE-25 · Material** reservar juntas oscuras para zonas pisadas. **Target:** `MaterialesEscena`. **Prueba:** desgaste no es textura global.
- **SE-26 · Luz principal** usar fluorescente frío arriba con caída hacia suelo. **Target:** `EscenaNivel`. **Prueba:** valores por altura.
- **SE-27 · Luz secundaria** indicador ámbar ilumina solo borde del panel. **Target:** `panelServicio`. **Prueba:** acento controlado.
- **SE-28 · Luz secundaria** una luz de mantenimiento verde late con periodo distinto. **Target:** `pulso`. **Prueba:** no sincroniza con fluorescente.
- **SE-29 · Sombras** proyectar debajo de tuberías altas. **Target:** `Servicio`. **Prueba:** la sombra sigue eje.
- **SE-30 · Sombras** oscurecer interior de compuerta de forma estable. **Target:** `Servicio`. **Prueba:** no desaparece con animación.
- **SE-31 · Reflejo** especular mínimo en cobre húmedo. **Target:** `MaterialesEscena`. **Prueba:** reflejo no cubre texto.
- **SE-32 · Color** verde solo en señal de sistema; ámbar en advertencia. **Target:** `Paleta`. **Prueba:** no se vuelve semáforo.
- **SE-33 · Contraste** panel sigue legible sobre sombra de tubería. **Target:** `preview`. **Prueba:** texto de hoja no compite.
- **SE-34 · Densidad** limitar cables en el tercio visible para no crear moiré. **Target:** `presupuesto`. **Prueba:** 4K no produce rayado.
- **SE-35 · Forma extraña** una tubería debe entrar en pared y no terminar flotando. **Target:** `Servicio`. **Prueba:** termina con brida.
- **SE-36 · Movimiento** variar la presión del panel, no toda la escena. **Target:** `panelServicio`. **Prueba:** movimiento reducido congela.
- **SE-37 · Evento propio** válvula responde al sonido con giro corto. **Target:** `eventos/visual`. **Prueba:** sin animación permanente.
- **SE-38 · Presencia** ocultar figura entre conductos, nunca delante del papel. **Target:** `Presencia`. **Prueba:** revelación parcial.
- **SE-39 · Transición** perder primero indicadores, luego flujo, luego fluorescente. **Target:** `RotacionNiveles`. **Prueba:** orden narrativo.
- **SE-40 · Legibilidad UI** mantener fondo del panel fuera de la caja de hoja. **Target:** `EscenaNivel`. **Prueba:** texto sobre fondo estable.
- **SE-41 · Resolución** válvula no colapsa a línea en 480p lógico. **Target:** `preview`. **Prueba:** silueta circular conserva eje.
- **SE-42 · Rendimiento** compartir geometría de tubos sin crear listas por frame. **Target:** `renderer`. **Prueba:** perfil sin allocations.
- **SE-43 · Rendimiento** saltar remaches menores en viewport pequeño. **Target:** `culling adaptativo`. **Prueba:** identidad principal intacta.
- **SE-44 · Accesibilidad** presión no parpadea con destellos reducidos. **Target:** `ConfigTurno`. **Prueba:** luminancia monotónica.
- **SE-45 · Accesibilidad** animación de indicador se congela con movimiento reducido. **Target:** `escena`. **Prueba:** valores estables.
- **SE-46 · Identidad** numerar una válvula con placa física del edificio. **Target:** `PrimerPlano`. **Prueba:** servicio reconocible sin nombre.
- **SE-47 · Identidad** mostrar una flecha de mantenimiento gastada en pared. **Target:** `Servicio`. **Prueba:** no es tooltip ni overlay.
- **SE-48 · Verificación** prueba ciega contra un túnel industrial genérico. **Target:** `checklist`. **Prueba:** se identifica por válvula/panel.
- **SE-49 · Verificación** medir fills de tubos, panel y humedad por resolución. **Target:** `perfil`. **Prueba:** presupuesto P0.
- **SE-50 · Verificación** transición y F3+T sin audio huérfano. **Target:** `manual`. **Prueba:** recursos válidos.

## Nivel 3 — Natatorio

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **NA-01 · Cámara** bajar la fuga hasta la línea del agua para que el vacío tenga peso. **Target:** `Marco de nivel3`. **Prueba:** horizonte acuático claro.
- **NA-02 · Cámara** abrir un lateral de pasarela y no centrar todos los carriles. **Target:** `Natatorio`. **Prueba:** perspectiva asimétrica.
- **NA-03 · Cámara** conservar techo alto fuera del área de texto. **Target:** `Natatorio`. **Prueba:** la bóveda se intuye sin ensuciar hoja.
- **NA-04 · Composición** usar borde de piscina como diagonal de entrada. **Target:** `Natatorio`. **Prueba:** el ojo sigue el borde al fondo.
- **NA-05 · Composición** diferenciar cuatro carriles por dirección, no por color. **Target:** `Planta/Natatorio`. **Prueba:** líneas convergen.
- **NA-06 · Composición** dejar una piscina parcial fuera de cuadro. **Target:** `PrimerPlano`. **Prueba:** el espacio parece mayor.
- **NA-07 · Composición** reservar una escalera acuática como foco secundario. **Target:** `Natatorio`. **Prueba:** objeto no tapa UI.
- **NA-08 · Composición** mantener reflejo y agua en planos separados. **Target:** `DireccionArte.natatorio`. **Prueba:** reflejo no parece rayos.
- **NA-09 · Escala** reducir carriles lejanos de forma proyectiva. **Target:** `Marco`. **Prueba:** ancho decrece hacia fuga.
- **NA-10 · Escala** hacer borde mojado cercano más grueso. **Target:** `PrimerPlano`. **Prueba:** proximidad legible.
- **NA-11 · Arquitectura** añadir juntas de azulejo con desfase por hilada. **Target:** `MaterialesEscena.azulejo`. **Prueba:** no hay rejilla rígida.
- **NA-12 · Arquitectura** dibujar pasarela suspendida en el plano medio. **Target:** `Natatorio`. **Prueba:** tiene soportes.
- **NA-13 · Arquitectura** integrar desagüe lateral con rejilla. **Target:** `Natatorio`. **Prueba:** punto físico reconocible. **Estado:** Implementado como rejilla lateral anclada al borde; validación dentro de Minecraft pendiente.
- **NA-14 · Arquitectura** dejar una ventana alta rota en el fondo. **Target:** `Natatorio`. **Prueba:** luz y borde se alinean. **Estado:** Implementado con marco, paños de vidrio y rotura localizada; validación dentro de Minecraft pendiente.
- **NA-15 · Arquitectura** cambiar altura de borde entre piscina cercana y lejana. **Target:** `Marco`. **Prueba:** dos niveles de agua.
- **NA-16 · Silueta** distinguir trampolín, escalera y flotador abandonado. **Target:** `PrimerPlano`. **Prueba:** tres perfiles inequívocos.
- **NA-17 · Silueta** quebrar la línea de azulejo con una zona reparada. **Target:** `MaterialesEscena`. **Prueba:** reparación tiene contorno.
- **NA-18 · Silueta** dejar un hueco negro bajo pasarela. **Target:** `Natatorio`. **Prueba:** profundidad antes que decoración.
- **NA-19 · Profundidad** proyectar ondas hacia la fuga. **Target:** `Ondas`. **Prueba:** intervalos se comprimen al fondo.
- **NA-20 · Profundidad** hacer que la escalera oculte parte del reflejo. **Target:** `PrimerPlano`. **Prueba:** oclusión real.
- **NA-21 · Material** separar azulejo seco, borde pulido y agua. **Target:** `MaterialesEscena`. **Prueba:** tres valores.
- **NA-22 · Material** añadir sarro bajo rebosaderos. **Target:** `Natatorio`. **Estado:** Implementado como lenguetas de sarro bajo el rebosadero, sembradas por la clave del nivel; validación dentro de Minecraft pendiente. **Prueba:** desgaste vertical localizado.
- **NA-23 · Material** introducir metal oxidado en escalera. **Target:** `Natatorio`. **Prueba:** óxido en uniones.
- **NA-24 · Material** reflejar azulejo solo en franja húmeda. **Target:** `TratamientoEscena`. **Prueba:** reflejo corto.
- **NA-25 · Material** dibujar pequeñas juntas abiertas sin llenar toda la pared. **Target:** `azulejo`. **Prueba:** irregularidad controlada.
- **NA-26 · Luz principal** usar luz fluorescente arriba y rebote azul verdoso del agua. **Target:** `EscenaNivel`. **Prueba:** dos fuentes distinguibles.
- **NA-27 · Luz secundaria** un lucernario cálido mínimo en ventana rota. **Target:** `Natatorio`. **Prueba:** no blanquea el agua.
- **NA-28 · Luz secundaria** reflejo de agua con una sola fase por columna. **Target:** `DireccionArte`. **Prueba:** no relampaguea.
- **NA-29 · Sombras** proyectar sombra de pasarela sobre azulejo. **Target:** `Natatorio`. **Prueba:** sombra con perspectiva.
- **NA-30 · Sombras** oscurecer fondo bajo el agua sin negro plano. **Target:** `TratamientoEscena`. **Prueba:** mantiene textura.
- **NA-31 · Reflejo** limitar caústicas a bandas discontinuas. **Target:** `causticas`. **Prueba:** no hay filtro de rayos.
- **NA-32 · Color** reservar cian para agua y ocre para óxido. **Target:** `Paleta`. **Prueba:** paleta no es arcoíris.
- **NA-33 · Contraste** texto sigue siendo más oscuro que la reflexión acuática. **Target:** `UI/preview`. **Prueba:** lectura en 4% de luz.
- **NA-34 · Densidad** reducir ondas cuando hay hoja encima. **Target:** `culling UI`. **Prueba:** agua no vibra detrás del texto.
- **NA-35 · Forma extraña** un carril termina en una compuerta que no debería estar allí. **Target:** `Natatorio`. **Prueba:** anomalía propia del nivel.
- **NA-36 · Movimiento** ondas suaves con fase lenta. **Target:** `DireccionArte`. **Prueba:** movimiento reducido congela.
- **NA-37 · Evento propio** gota produce un anillo local, no toda la piscina. **Target:** `eventos`. **Prueba:** radio limitado.
- **NA-38 · Presencia** figura solo como reflejo roto en una calle. **Target:** `Presencia`. **Prueba:** silueta no flota.
- **NA-39 · Transición** apagar luces sobre el agua antes que el fondo. **Target:** `RotacionNiveles`. **Prueba:** agua conserva rebote breve.
- **NA-40 · Legibilidad UI** mantener carriles fuera del papel y bajo contraste medio. **Target:** `EscenaNivel`. **Prueba:** UI no pierde borde.
- **NA-41 · Resolución** conservar línea de agua en 4:3 y ultrawide. **Target:** `preview`. **Prueba:** no se corta el borde.
- **NA-42 · Rendimiento** reducir caústicas por área visible, no por número fijo. **Target:** `causticas`. **Prueba:** coste proporcional.
- **NA-43 · Rendimiento** reutilizar fases de columnas en vez de recalcular por tramo. **Target:** `renderer`. **Prueba:** menos trigonometría.
- **NA-44 · Accesibilidad** reflejos sin flashes cuando destellos reducidos. **Target:** `RotacionNiveles`. **Prueba:** luminancia continua.
- **NA-45 · Accesibilidad** agua queda quieta con movimiento reducido. **Target:** `escena`. **Prueba:** captura repetida igual.
- **NA-46 · Identidad** añadir marcas de profundidad de piscina como placas físicas. **Target:** `Natatorio`. **Prueba:** reconocimiento sin título. **Estado:** Implementado con tres placas de profundidad ancladas al borde; validación dentro de Minecraft pendiente.
- **NA-47 · Identidad** hacer que el trampolín esté clausurado, no simplemente vacío. **Target:** `PrimerPlano`. **Prueba:** cinta/placa integrada.
- **NA-48 · Verificación** reconocer natatorio sin depender del color azul. **Target:** `checklist`. **Prueba:** agua, carriles y pasarela bastan.
- **NA-49 · Verificación** comparar sin hoja y con hoja en 320x240. **Target:** `preview`. **Prueba:** agua no tapa widgets.
- **NA-50 · Verificación** medir fills de caústicas y ondas en 4K. **Target:** `perfil`. **Prueba:** bajo presupuesto.

## Nivel 4 — Sala de piedra

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **SA-01 · Cámara** desplazar la fuga hacia el candil principal. **Target:** `Marco de nivel4`. **Prueba:** piedra converge al calor.
- **SA-02 · Cámara** bajar el horizonte para dar masa a los sillares. **Target:** `SalaPiedra`. **Prueba:** suelo pesa más que techo.
- **SA-03 · Cámara** abrir una esquina de muro como primer plano. **Target:** `Planta/Cripta`. **Prueba:** sensación de cámara lateral.
- **SA-04 · Composición** usar candil, arco y cadena como triángulo focal. **Target:** `DireccionArte.salaPiedra`. **Prueba:** tres escalas ordenadas.
- **SA-05 · Composición** dejar corredor de piedra lateral sin simetría. **Target:** `Cripta`. **Prueba:** segundo recorrido.
- **SA-06 · Composición** separar fuego cálido de piedra azul fría. **Target:** `Paleta`. **Prueba:** temperaturas distintas.
- **SA-07 · Composición** evitar que la cadena quede detrás de la hoja. **Target:** `EscenaNivel`. **Prueba:** oclusión limpia.
- **SA-08 · Composición** insertar vacío entre arcos para que respire silencio. **Target:** `Cripta`. **Prueba:** masa y vacío alternan.
- **SA-09 · Escala** candil cercano tiene soporte y copa, no solo llama. **Target:** `antorcha`. **Prueba:** escala física.
- **SA-10 · Escala** sillares lejanos se vuelven bandas, no ladrillos iguales. **Target:** `MaterialesEscena`. **Prueba:** detalle decrece.
- **SA-11 · Arquitectura** añadir dovelas visibles en un arco. **Target:** `Cripta`. **Estado:** Implementado como dovelas visibles en el arco mas cercano, con junta perpendicular y tono propio; validación dentro de Minecraft pendiente. **Prueba:** curva construida por piezas.
- **SA-12 · Arquitectura** marcar una bóveda con costillas desplazadas. **Target:** `Cripta`. **Prueba:** techo tiene volumen.
- **SA-13 · Arquitectura** quebrar un sillar del zócalo. **Target:** `piedra`. **Prueba:** grieta termina en borde.
- **SA-14 · Arquitectura** añadir nicho ciego con profundidad. **Target:** `Cripta`. **Prueba:** interior no es rectángulo plano. **Estado:** Implementado como nicho lateral con marco, intrados y fondo oscuro; validación dentro de Minecraft pendiente.
- **SA-15 · Arquitectura** anclar cadenas a ménsulas, no al cielo. **Target:** `DireccionArte.cadena`. **Prueba:** punto de soporte visible.
- **SA-16 · Silueta** la cadena debe leerse como cadena, con eslabones alternos. **Target:** `cadena`. **Prueba:** no es línea vertical.
- **SA-17 · Silueta** separar candil de halo mediante soporte oscuro. **Target:** `antorcha`. **Prueba:** fuego tiene objeto.
- **SA-18 · Silueta** dar borde dentado a una ruina. **Target:** `PrimerPlano`. **Prueba:** piedra partida reconocible.
- **SA-19 · Profundidad** alternar nichos y columnas en planos distintos. **Target:** `Cripta`. **Prueba:** oclusión escalonada.
- **SA-20 · Profundidad** hacer que la luz cálida muera antes del fondo. **Target:** `TratamientoEscena`. **Prueba:** no baña toda la sala.
- **SA-21 · Material** juntas profundas en primer plano y suaves al fondo. **Target:** `piedra`. **Prueba:** escala por dx.
- **SA-22 · Material** añadir humedad en juntas inferiores. **Target:** `humedad`. **Prueba:** no aparece en techo seco.
- **SA-23 · Material** diferenciar caliza, hierro y hollín. **Target:** `MaterialesEscena`. **Prueba:** materiales al tacto visual.
- **SA-24 · Material** reservar grietas grandes para columnas con carga. **Target:** `piedra`. **Prueba:** daño tiene lógica estructural.
- **SA-25 · Material** añadir cera acumulada bajo candil. **Target:** `PrimerPlano`. **Prueba:** residuo junto a fuente. **Estado:** Implementado como residuo cálido localizado bajo el candil; validación dentro de Minecraft pendiente.
- **SA-26 · Luz principal** fuego lateral cálido con sombra dura corta. **Target:** `antorcha`. **Prueba:** dirección clara.
- **SA-27 · Luz secundaria** azul de piedra entra por un hueco superior. **Target:** `SalaPiedra`. **Prueba:** rebote frío bajo arco.
- **SA-28 · Luz secundaria** candil derecho más bajo que el izquierdo. **Target:** `antorcha`. **Prueba:** asimetría mantenida.
- **SA-29 · Sombras** eslabones proyectan pequeñas interrupciones. **Target:** `cadena`. **Prueba:** sombra sigue luz.
- **SA-30 · Sombras** arco proyecta sombra sobre nicho y no sobre hoja. **Target:** `Cripta`. **Prueba:** UI protegida.
- **SA-31 · Reflejo** humedad devuelve un brillo mínimo en zócalo. **Target:** `TratamientoEscena`. **Prueba:** no parece agua.
- **SA-32 · Color** mantener ocre solo en fuego y bronce envejecido. **Target:** `Paleta`. **Prueba:** piedra domina.
- **SA-33 · Contraste** recuperar juntas durante suspensión sin flashes. **Target:** `EscenaNivel`. **Prueba:** lectura en penumbra.
- **SA-34 · Densidad** reservar grietas fuertes para 18–26 marcas estructurales. **Target:** `presupuesto`. **Prueba:** no ruido de mármol.
- **SA-35 · Forma extraña** un arco tiene una desviación casi humana. **Target:** `Cripta`. **Prueba:** anomalía sutil, no error de pixel.
- **SA-36 · Movimiento** llama oscila en una sola envolvente. **Target:** `antorcha`. **Prueba:** no parpadeo caótico.
- **SA-37 · Evento propio** cadena responde con una sola elongación audible/visual. **Target:** `eventos`. **Prueba:** no se anima siempre.
- **SA-38 · Presencia** figura se integra como sombra al fondo del arco. **Target:** `Presencia`. **Prueba:** no tiene brillo propio.
- **SA-39 · Transición** fuego sobrevive al primer corte y muere después. **Target:** `RotacionNiveles`. **Prueba:** material tiene inercia narrativa.
- **SA-40 · Legibilidad UI** fondo detrás de hoja usa juntas de bajo contraste. **Target:** `EscenaNivel`. **Prueba:** texto supera piedra.
- **SA-41 · Resolución** arco y candil sobreviven en ventana baja. **Target:** `preview`. **Prueba:** silueta principal continua.
- **SA-42 · Rendimiento** saltar grietas fuera de la zona visible. **Target:** `culling`. **Prueba:** fills estables.
- **SA-43 · Rendimiento** precalcular sillares por nivel/tamaño, no por frame. **Target:** `MaterialesEscena`. **Prueba:** sin asignaciones.
- **SA-44 · Accesibilidad** llama sin titileo con destellos reducidos. **Target:** `RotacionNiveles`. **Prueba:** transición suave.
- **SA-45 · Accesibilidad** congelar cadena/llama con movimiento reducido. **Target:** `escena`. **Prueba:** no hay deriva.
- **SA-46 · Identidad** incluir marca de peregrinación gastada en un sillar. **Target:** `Cripta`. **Prueba:** sala tiene historia. **Estado:** Implementado como marca cruzada erosionada fuera de la hoja; validación dentro de Minecraft pendiente.
- **SA-47 · Identidad** candil y arco se reconocen sin leer “nivel4”. **Target:** `checklist`. **Prueba:** reconocimiento ciego.
- **SA-48 · Verificación** probar luz al 1.0, 0.04 y transición. **Target:** `preview`. **Prueba:** piedra no se aplasta.
- **SA-49 · Verificación** comprobar que el rojo no se use como tapiz. **Target:** `Paleta`. **Prueba:** identidad de Ejecutores intacta.
- **SA-50 · Verificación** medir contraste, fills y presencia a 720p–4K. **Target:** `perfil`. **Prueba:** checklist completo.

## Nivel 5 — Biblioteca

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **BI-01 · Cámara** colocar la fuga entre dos estanterías desiguales. **Target:** `Marco de nivel5`. **Prueba:** pasillo no es simétrico.
- **BI-02 · Cámara** elevarla apenas para ver galería superior. **Target:** `Biblioteca`. **Prueba:** escala vertical de libros.
- **BI-03 · Cámara** dejar mesa de lectura como foreground parcial. **Target:** `PrimerPlano`. **Prueba:** cercanía sin caja flotante.
- **BI-04 · Composición** usar reloj, escalera y vacío como focos distintos. **Target:** `DireccionArte.biblioteca`. **Prueba:** jerarquía silenciosa.
- **BI-05 · Composición** cortar una estantería en el borde para sugerir extensión. **Target:** `Biblioteca`. **Prueba:** espacio continúa fuera de cuadro.
- **BI-06 · Composición** reservar la zona bajo hoja para lomos oscuros uniformes. **Target:** `EscenaNivel`. **Prueba:** texto limpio.
- **BI-07 · Composición** separar mesa cálida de pasillo verde apagado. **Target:** `Paleta`. **Prueba:** foco cálido no domina.
- **BI-08 · Composición** mantener un hueco sin libros como anomalía. **Target:** `Biblioteca`. **Prueba:** vacío tiene lectura.
- **BI-09 · Escala** lomos cercanos tienen alturas distintas. **Target:** `PrimerPlano`. **Prueba:** no son dientes de sierra uniformes.
- **BI-10 · Escala** escalera reduce peldaños hacia galería. **Target:** `escaleraBiblioteca`. **Prueba:** perspectiva visible.
- **BI-11 · Arquitectura** añadir viga de galería con baranda. **Target:** `Biblioteca`. **Prueba:** segundo piso creíble.
- **BI-12 · Arquitectura** mostrar un arco de acceso entre estantes. **Target:** `Biblioteca`. **Estado:** Implementado como arco de acceso entre estantes con capucha de arco y pilares de marco; validación dentro de Minecraft pendiente. **Prueba:** límite espacial.
- **BI-13 · Arquitectura** dejar techo con panel acústico desprendido. **Target:** `Biblioteca`. **Prueba:** abandono material.
- **BI-14 · Arquitectura** incluir una mesa desplazada y una pata rota. **Target:** `PrimerPlano`. **Prueba:** objeto apoya en suelo.
- **BI-15 · Arquitectura** conectar escalera con la baranda superior. **Target:** `escaleraBiblioteca`. **Prueba:** no flota.
- **BI-16 · Silueta** reloj debe tener marco, esfera y sombra. **Target:** `Biblioteca`. **Prueba:** no es círculo abstracto.
- **BI-17 · Silueta** escalera diagonal rompe las líneas horizontales. **Target:** `DireccionArte`. **Prueba:** foco de lectura.
- **BI-18 · Silueta** libros sueltos sobresalen irregularmente. **Target:** `PrimerPlano`. **Prueba:** abandono reconocible.
- **BI-19 · Profundidad** variar densidad de lomos por plano. **Target:** `Biblioteca`. **Prueba:** fondo se simplifica.
- **BI-20 · Profundidad** ocluir la escalera parcialmente con mesa. **Target:** `PrimerPlano`. **Prueba:** capas físicas.
- **BI-21 · Material** separar madera, papel, latón y polvo. **Target:** `MaterialesEscena.madera`. **Prueba:** distintos brillos.
- **BI-22 · Material** añadir páginas dobladas solo en primeros estantes. **Target:** `PrimerPlano`. **Prueba:** escala plausible. **Estado:** Implementado con dos páginas dobladas localizadas en estantes cercanos; validación dentro de Minecraft pendiente.
- **BI-23 · Material** marcar huellas de manos en baranda. **Target:** `Biblioteca`. **Prueba:** desgaste de contacto.
- **BI-24 · Material** condensación mínima en ventana, no sobre libros. **Target:** `humedad`. **Prueba:** humedad localizada. **Estado:** Implementado como cuatro trazas verticales localizadas en el ventanal; validación dentro de Minecraft pendiente.
- **BI-25 · Material** añadir polvo acumulado en huecos de estantes. **Target:** `Biblioteca`. **Prueba:** gravedad y recoveco. **Estado:** Implementado como cinco depósitos sutiles en recovecos alternos; validación dentro de Minecraft pendiente.
- **BI-26 · Luz principal** lámparas verdes muy bajas de lectura. **Target:** `DireccionArte.pulso`. **Prueba:** no bañan el fondo.
- **BI-27 · Luz secundaria** una franja cálida cae sobre mesa vacía. **Target:** `Biblioteca`. **Prueba:** foco humano ausente.
- **BI-28 · Luz secundaria** reloj devuelve reflejo metálico puntual. **Target:** `MaterialesEscena`. **Prueba:** reflejo inferior a fuente.
- **BI-29 · Sombras** estantes proyectan bandas no uniformes. **Target:** `Biblioteca`. **Prueba:** profundidad lateral.
- **BI-30 · Sombras** escalera deja sombra diagonal sobre pared. **Target:** `escalera`. **Prueba:** refuerza orientación.
- **BI-31 · Reflejo** pulir solo bordes de madera gastada. **Target:** `MaterialesEscena`. **Prueba:** brillo no es filtro.
- **BI-32 · Color** verde biblioteca apagado y ámbar de mesa. **Target:** `Paleta`. **Prueba:** sin saturación excesiva.
- **BI-33 · Contraste** páginas claras no compiten con título. **Target:** `preview`. **Prueba:** UI siempre primer plano.
- **BI-34 · Densidad** limitar marcas de lomos para que no parezcan texto ilegible. **Target:** `presupuesto`. **Prueba:** textura legible a distancia.
- **BI-35 · Forma extraña** un libro abierto en un lugar imposible del estante. **Target:** `PrimerPlano`. **Prueba:** anomalía propia.
- **BI-36 · Movimiento** polvo cae lentamente entre dos haces. **Target:** `eventos/escena`. **Prueba:** se congela con reducción.
- **BI-37 · Evento propio** una página cambia una vez por evento libro. **Target:** `GestorAmbiente`. **Prueba:** no ocurre cada frame.
- **BI-38 · Presencia** sugerir figura entre dos filas sin iluminarla. **Target:** `Presencia`. **Prueba:** lectura periférica.
- **BI-39 · Transición** detener luces de lectura antes que el reloj. **Target:** `RotacionNiveles`. **Prueba:** reloj queda como último testigo.
- **BI-40 · Legibilidad UI** evitar líneas horizontales de estante detrás de labels. **Target:** `EscenaNivel`. **Prueba:** papel domina.
- **BI-41 · Resolución** reloj sigue siendo círculo y no punto en 320x240. **Target:** `preview`. **Prueba:** identidad mínima.
- **BI-42 · Rendimiento** agrupar lomos en tramos y omitir microdetalles lejanos. **Target:** `Biblioteca`. **Prueba:** menos fills en 4K.
- **BI-43 · Rendimiento** cachear escalera y baranda por dimensiones. **Target:** `renderer`. **Prueba:** sin listas por frame.
- **BI-44 · Accesibilidad** lámparas no pulsan si destellos reducidos. **Target:** `ConfigTurno`. **Prueba:** luz estable.
- **BI-45 · Accesibilidad** polvo congelado con movimiento reducido. **Target:** `EscenaNivel`. **Prueba:** captura idéntica.
- **BI-46 · Identidad** reloj y escalera forman firma visual incluso sin texto. **Target:** `checklist`. **Prueba:** reconocimiento ciego.
- **BI-47 · Identidad** mesa debe parecer de archivo laboral, no biblioteca fantástica. **Target:** `PrimerPlano`. **Prueba:** material institucional.
- **BI-48 · Verificación** probar idioma largo y texto grande sobre la hoja. **Target:** `PantallaNivel`. **Prueba:** estantes no interfieren.
- **BI-49 · Verificación** medir relación cálido/frío y contraste de página. **Target:** `preview`. **Prueba:** papel sigue foco.
- **BI-50 · Verificación** revisar audio de libro/reloj con silencios amplios. **Target:** `GestorAmbiente`. **Prueba:** no metrónomo.

## Nivel 6 — Invernadero

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **IN-01 · Cámara** colocar la fuga entre dos hojas invasivas, no en el centro. **Target:** `Marco de nivel6`. **Prueba:** vegetación enmarca.
- **IN-02 · Cámara** elevarla para mostrar estructura de vidrio superior. **Target:** `Invernadero`. **Prueba:** techo transmite escala.
- **IN-03 · Cámara** dejar una mesa de cultivo como primer plano parcial. **Target:** `PrimerPlano`. **Prueba:** cercanía material.
- **IN-04 · Composición** usar lucernario, árbol roto y charco como triángulo. **Target:** `Invernadero/DireccionArte`. **Prueba:** foco distribuido.
- **IN-05 · Composición** dejar un corredor sin vegetación para contraste. **Target:** `Invernadero`. **Prueba:** la invasión no cubre todo.
- **IN-06 · Composición** reservar una placa de mantenimiento entre hojas. **Target:** `EscenaNivel`. **Prueba:** señalética legible.
- **IN-07 · Composición** distinguir silueta de macetas del vano. **Target:** `PrimerPlano`. **Prueba:** formas físicas.
- **IN-08 · Composición** hacer que el vidrio dirija la mirada y no sea una capa blanca. **Target:** `techoInvernadero`. **Prueba:** líneas convergentes.
- **IN-09 · Escala** hojas cercanas grandes, brotes lejanos agrupados. **Target:** `hojas`. **Prueba:** profundidad botánica.
- **IN-10 · Escala** macetas con bordes y suelo interior. **Target:** `PrimerPlano`. **Prueba:** no son manchas.
- **IN-11 · Arquitectura** añadir nervios de vidrio con uniones metálicas. **Target:** `techoInvernadero`. **Prueba:** módulos creíbles.
- **IN-12 · Arquitectura** mostrar panel roto con borde serrado. **Target:** `Invernadero`. **Prueba:** ruptura integrada. **Estado:** Implementado con dos huecos y borde de vidrio irregular en la cristalera; validación dentro de Minecraft pendiente.
- **IN-13 · Arquitectura** incluir canaleta de lluvia que entra en depósito. **Target:** `Invernadero`. **Prueba:** agua tiene ruta. **Estado:** Implementado con bajante lateral, codo y depósito de recogida; validación dentro de Minecraft pendiente.
- **IN-14 · Arquitectura** añadir pasarela oxidada sobre cultivo. **Target:** `Invernadero`. **Estado:** Implementado como pasarela oxidada sobre los bancos con soportes al suelo y barandilla; validación dentro de Minecraft pendiente. **Prueba:** soportes visibles.
- **IN-15 · Arquitectura** dejar una puerta de vidrio entreabierta. **Target:** `Planta/Invernadero`. **Prueba:** umbral y bisagras. **Estado:** Implementado con hoja lateral desplazada, umbral, tirador y dos bisagras; validación dentro de Minecraft pendiente.
- **IN-16 · Silueta** una planta trepa por la estructura, no por toda la pantalla. **Target:** `hojas`. **Prueba:** crecimiento direccional.
- **IN-17 · Silueta** distinguir hojas anchas, tallos finos y helechos. **Target:** `hojas/PrimerPlano`. **Prueba:** tres perfiles.
- **IN-18 · Silueta** condensación corta siluetas del fondo. **Target:** `vidrioHumedo`. **Prueba:** niebla selectiva.
- **IN-19 · Profundidad** cruzar tallos por delante de un haz, con alfa controlado. **Target:** `Invernadero`. **Prueba:** oclusión y contraluz.
- **IN-20 · Profundidad** dejar agua reflejada debajo de pasarela. **Target:** `TratamientoEscena`. **Prueba:** reflejo no plano.
- **IN-21 · Material** vidrio, hierro pintado, tierra y hoja tienen tonos diferentes. **Target:** `MaterialesEscena`. **Prueba:** lectura táctil.
- **IN-22 · Material** gotas siguen juntas del vidrio en vertical. **Target:** `vidrioHumedo`. **Prueba:** no flotan.
- **IN-23 · Material** tierra se acumula en esquinas de maceta. **Target:** `PrimerPlano`. **Prueba:** gravedad.
- **IN-24 · Material** óxido bajo tornillos de techo. **Target:** `techoInvernadero`. **Prueba:** desgaste localizado.
- **IN-25 · Material** musgo solo sobre superficies húmedas. **Target:** `Invernadero`. **Prueba:** no aparece en metal seco.
- **IN-26 · Luz principal** filtrar luz fría por paneles, no dibujar rayos sólidos. **Target:** `Invernadero`. **Prueba:** profundidad atmosférica.
- **IN-27 · Luz secundaria** rebote verde de hojas en laterales. **Target:** `Paleta/Tratamiento`. **Prueba:** acento bajo.
- **IN-28 · Luz secundaria** una abertura cálida pequeña en la puerta. **Target:** `Invernadero`. **Prueba:** contraste narrativo.
- **IN-29 · Sombras** hojas proyectan formas blandas sobre pared. **Target:** `hojas`. **Prueba:** sombra sigue grupo, no ruido.
- **IN-30 · Sombras** pasarela corta un haz con borde duro. **Target:** `Invernadero`. **Prueba:** estructura pesa.
- **IN-31 · Reflejo** vidrio devuelve líneas rotas y no espejo completo. **Target:** `vidrioHumedo`. **Prueba:** reflejo discreto.
- **IN-32 · Color** verdes diferenciados por humedad, no por saturación arbitraria. **Target:** `Paleta`. **Prueba:** verde no aplasta papel.
- **IN-33 · Contraste** señal de mantenimiento supera hojas en tinta. **Target:** `preview`. **Prueba:** legible con densidad.
- **IN-34 · Densidad** limitar hojas al perímetro y foco del fondo. **Target:** `presupuesto`. **Prueba:** centro respira.
- **IN-35 · Forma extraña** una raíz invade una junta estructural. **Target:** `PrimerPlano`. **Prueba:** anomalía botánica anclada.
- **IN-36 · Movimiento** gotas descienden lento por paneles seleccionados. **Target:** `vidrioHumedo`. **Prueba:** movimiento reducido congela.
- **IN-37 · Evento propio** una hoja cae al activarse evento de follaje. **Target:** `eventos`. **Prueba:** caída única y localizada.
- **IN-38 · Presencia** figura se oculta tras condensación, sin brillo propio. **Target:** `Presencia`. **Prueba:** silueta parcial.
- **IN-39 · Transición** apagar ventilación pero conservar goteo breve. **Target:** `RotacionNiveles/GestorAmbiente`. **Prueba:** materiales sobreviven al corte.
- **IN-40 · Legibilidad UI** no dibujar gotas sobre el rectángulo del aviso. **Target:** `EscenaNivel`. **Prueba:** hoja seca/legible.
- **IN-41 · Resolución** conservar techo y planta focal en 4:3. **Target:** `preview`. **Prueba:** no se vuelve masa verde.
- **IN-42 · Rendimiento** emitir gotas solo sobre paneles visibles. **Target:** `vidrioHumedo`. **Prueba:** coste no escala con canvas vacío.
- **IN-43 · Rendimiento** usar semilla estable para hojas, sin objetos temporales. **Target:** `hojas`. **Prueba:** perfil sin allocations.
- **IN-44 · Accesibilidad** congelar gotas/hojas con movimiento reducido. **Target:** `EscenaNivel`. **Prueba:** frame estable.
- **IN-45 · Accesibilidad** retirar cualquier pulso de lucernario con destellos reducidos. **Target:** `luz`. **Prueba:** sin flicker.
- **IN-46 · Identidad** puerta y canaleta cuentan que era un lugar de trabajo. **Target:** `checklist`. **Prueba:** no parece bosque genérico.
- **IN-47 · Identidad** placa de cultivo con numeración física. **Target:** `PrimerPlano`. **Prueba:** conexión Jobs.
- **IN-48 · Verificación** reconocer invernadero sin depender del verde. **Target:** `checklist`. **Prueba:** vidrio, macetas y canaleta bastan.
- **IN-49 · Verificación** probar humedad con papel limpio y UI mínima. **Target:** `preview`. **Prueba:** atmósfera controlada.
- **IN-50 · Verificación** medir fills de hojas/gotas en ventana 4K. **Target:** `perfil`. **Prueba:** bajo presupuesto.

## Nivel 7 — Catacumbas

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **CA-01 · Cámara** bajar la fuga para que el suelo de nichos gane peso. **Target:** `Marco de nivel7`. **Prueba:** techo desaparece en penumbra.
- **CA-02 · Cámara** desplazar el eje hacia el túnel de viento. **Target:** `Catacumba`. **Prueba:** dirección lateral clara.
- **CA-03 · Cámara** reservar un primer plano de muro con nicho cortado. **Target:** `PrimerPlano`. **Prueba:** cámara dentro de la cripta.
- **CA-04 · Composición** alternar nichos, columna y túnel como ritmos de lectura. **Target:** `Catacumba`. **Prueba:** no es corredor uniforme.
- **CA-05 · Composición** dejar el centro vacío salvo una gota distante. **Target:** `Catacumba`. **Prueba:** silencio visual.
- **CA-06 · Composición** usar antorcha baja como único calor. **Target:** `DireccionArte.catacumbas`. **Prueba:** foco pequeño.
- **CA-07 · Composición** dar al fondo una salida que no coincide con fuga exacta. **Target:** `Marco`. **Prueba:** inquietud espacial.
- **CA-08 · Composición** proteger la hoja de nichos de alto contraste. **Target:** `EscenaNivel`. **Prueba:** texto no se mezcla con tumbas.
- **CA-09 · Escala** nicho cercano tiene borde y profundidad, lejano solo hueco. **Target:** `Catacumba`. **Prueba:** detalle por dx.
- **CA-10 · Escala** cadenas/antorchas no dominan el techo. **Target:** `DireccionArte`. **Prueba:** escala soterrada.
- **CA-11 · Arquitectura** variar anchura de nichos por muro. **Target:** `Catacumba`. **Prueba:** construcción irregular.
- **CA-12 · Arquitectura** añadir dinteles de piedra sobre nichos. **Target:** `Catacumba`. **Prueba:** peso estructural.
- **CA-13 · Arquitectura** mostrar pasadizo estrecho detrás de arcos. **Target:** `Catacumba`. **Estado:** Implementado como pasadizo estrecho detras del arco del fondo con segundo umbral y dovelas; validación dentro de Minecraft pendiente. **Prueba:** segundo plano.
- **CA-14 · Arquitectura** romper una columna en la base, no a mitad flotante. **Target:** `PrimerPlano`. **Prueba:** apoyo visible.
- **CA-15 · Arquitectura** incluir drenaje o canal de humedad en suelo. **Target:** `Catacumba`. **Prueba:** agua tiene recorrido. **Estado:** Implementado como canal estrecho con reflejos discontinuos siguiendo el suelo; validación dentro de Minecraft pendiente.
- **CA-16 · Silueta** nichos deben ser cavidades con alfeizar, no cuadros. **Target:** `Catacumba`. **Prueba:** profundidad negra interior.
- **CA-17 · Silueta** viento se sugiere con tela/partícula, no línea blanca. **Target:** `eventos`. **Prueba:** gesto mínimo.
- **CA-18 · Silueta** cadena central anclada a bóveda. **Target:** `cadena`. **Prueba:** continuidad física.
- **CA-19 · Profundidad** hacer que el túnel se pierda por contraste, no por tamaño. **Target:** `TratamientoEscena`. **Prueba:** vacío creíble.
- **CA-20 · Profundidad** ocluir una antorcha con pilar cercano. **Target:** `Catacumba`. **Prueba:** descubrimiento parcial.
- **CA-21 · Material** diferenciar piedra húmeda, mortero y hierro. **Target:** `MaterialesEscena.piedra`. **Prueba:** tres valores.
- **CA-22 · Material** musgo solo en juntas inferiores. **Target:** `humedad`. **Prueba:** gravedad y agua.
- **CA-23 · Material** polvo no cubre el interior oscuro de nichos. **Target:** `Catacumba`. **Prueba:** material conserva vacío.
- **CA-24 · Material** añadir arañazos en umbrales de paso. **Target:** `PrimerPlano`. **Prueba:** desgaste de uso. **Estado:** Implementado con tres arañazos cortos anclados a un umbral; validación dentro de Minecraft pendiente.
- **CA-25 · Material** marcar piedra reciente junto a piedra antigua. **Target:** `Catacumba`. **Prueba:** reparación visible. **Estado:** Implementado como sillar de reparación con juntas y valor diferenciado; validación dentro de Minecraft pendiente.
- **CA-26 · Luz principal** antorcha cálida lateral con caída corta. **Target:** `antorcha`. **Prueba:** solo un sector respira.
- **CA-27 · Luz secundaria** azul gris del túnel opuesto. **Target:** `TratamientoEscena`. **Prueba:** contraste frío/caliente.
- **CA-28 · Luz secundaria** reflejo húmedo apenas debajo de la antorcha. **Target:** `reboteSuelo`. **Prueba:** no parece charco brillante.
- **CA-29 · Sombras** nichos conservan sombra aunque suba luz. **Target:** `Catacumba`. **Prueba:** profundidad de cavidad.
- **CA-30 · Sombras** cadena deja interrupciones estrechas sobre pared. **Target:** `cadena`. **Prueba:** sombras coherentes.
- **CA-31 · Reflejo** agua del drenaje refleja un hilo, no todo el suelo. **Target:** `Catacumba`. **Prueba:** foco localizado.
- **CA-32 · Color** piedra azul/verde apagada y fuego ocre contenido. **Target:** `Paleta`. **Prueba:** no fantasía saturada.
- **CA-33 · Contraste** conservar lectura de nichos en suspensión sin flashing. **Target:** `preview`. **Prueba:** luz mínima estable.
- **CA-34 · Densidad** máximo de nichos visibles que permita distinguir huecos. **Target:** `presupuesto`. **Prueba:** no mosaico repetitivo.
- **CA-35 · Forma extraña** un nicho demasiado profundo sin contenido. **Target:** `Catacumba`. **Prueba:** anomalía silenciosa.
- **CA-36 · Movimiento** solo humo o viento en ventana corta. **Target:** `EventosAmbientales`. **Prueba:** no movimiento constante.
- **CA-37 · Evento propio** gota cae en drenaje y produce respuesta sonora puntual. **Target:** `GestorAmbiente`. **Prueba:** localización visual/audio.
- **CA-38 · Presencia** sombra lejana detrás de nichos, no cuerpo superpuesto. **Target:** `Presencia`. **Prueba:** ambigüedad.
- **CA-39 · Transición** apagar antorcha antes de revelar el siguiente muro. **Target:** `RotacionNiveles`. **Prueba:** corte tiene expectativa.
- **CA-40 · Legibilidad UI** bajar contraste de juntas detrás de hoja. **Target:** `EscenaNivel`. **Prueba:** texto sobresale.
- **CA-41 · Resolución** conservar un nicho y la antorcha en ventana baja. **Target:** `preview`. **Prueba:** identidad mínima.
- **CA-42 · Rendimiento** omitir juntas detrás de huecos negros. **Target:** `culling`. **Prueba:** no fills inútiles.
- **CA-43 · Rendimiento** cachear patrón de nichos por escala. **Target:** `Catacumba`. **Prueba:** sin listas frame.
- **CA-44 · Accesibilidad** humo quieto con movimiento reducido. **Target:** `EscenaNivel`. **Prueba:** captura estable.
- **CA-45 · Accesibilidad** antorcha no titila con destellos reducidos. **Target:** `ConfigTurno`. **Prueba:** caída suave.
- **CA-46 · Identidad** nichos y drenaje reconocibles sin leer nivel. **Target:** `checklist`. **Prueba:** 8/10.
- **CA-47 · Identidad** evitar iconografía funeraria explícita fuera del canon. **Target:** `Catacumba`. **Prueba:** misterio institucional.
- **CA-48 · Verificación** comparar silencio visual con el de Biblioteca. **Target:** `matriz`. **Prueba:** materiales diferencian.
- **CA-49 · Verificación** probar la figura con luz 0.04 y UI mínima. **Target:** `Presencia`. **Prueba:** no invade.
- **CA-50 · Verificación** medir fills de juntas/nichos en 4K. **Target:** `perfil`. **Prueba:** presupuesto.

## Nivel 8 — Cisterna

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **CI-01 · Cámara** colocar la fuga bajo la línea de galerías para que el agua domine. **Target:** `Marco de nivel8`. **Prueba:** vacío superior y depósito inferior.
- **CI-02 · Cámara** desplazarla hacia columna sumergida. **Target:** `Cisterna`. **Prueba:** eje no central.
- **CI-03 · Cámara** dejar borde de pasarela en primer plano. **Target:** `PrimerPlano`. **Prueba:** cámara tiene altura.
- **CI-04 · Composición** usar columna, compuerta y onda como tres focos. **Target:** `DireccionArte.cisterna`. **Prueba:** jerarquía acuática.
- **CI-05 · Composición** reservar gran masa oscura sin evento en el fondo. **Target:** `Cisterna`. **Prueba:** escala y silencio.
- **CI-06 · Composición** romper la simetría de luces de mantenimiento. **Target:** `pulso`. **Prueba:** puntos desfasados.
- **CI-07 · Composición** mantener canal de agua libre de texto. **Target:** `EscenaNivel`. **Prueba:** UI no pelea con ondas.
- **CI-08 · Composición** hacer que la pasarela corte el cuadro lateralmente. **Target:** `Cisterna`. **Prueba:** segundo plano.
- **CI-09 · Escala** gotas cercanas tienen impacto ancho, lejanas son puntos. **Target:** `Ondas`. **Prueba:** escala por profundidad.
- **CI-10 · Escala** columna pierde detalle bajo el agua. **Target:** `Cisterna`. **Prueba:** atenuación vertical.
- **CI-11 · Arquitectura** añadir galería con baranda y puntos de anclaje. **Target:** `Cisterna`. **Estado:** Implementado como galeria de mantenimiento sobre el agua con barandilla, anclajes y reflejo partido; validación dentro de Minecraft pendiente. **Prueba:** estructura sobre agua.
- **CI-12 · Arquitectura** dibujar compuerta de inspección en muro. **Target:** `Cisterna`. **Prueba:** borde y bisagras. **Estado:** Implementado con placa de inspección, perímetro, manija y bisagras; validación dentro de Minecraft pendiente.
- **CI-13 · Arquitectura** introducir tubería de entrada con goteo real. **Target:** `Cisterna`. **Prueba:** agua tiene origen. **Estado:** Implementado con tubería lateral, codos y gota descendente; validación dentro de Minecraft pendiente.
- **CI-14 · Arquitectura** mostrar marcas de nivel en la columna. **Target:** `Cisterna`. **Prueba:** escala de altura. **Estado:** Implementado con tres marcas horizontales ancladas a una columna; validación dentro de Minecraft pendiente.
- **CI-15 · Arquitectura** dejar una pasarela rota que termina en sombra. **Target:** `PrimerPlano`. **Prueba:** abandono anclado.
- **CI-16 · Silueta** columna sumergida, compuerta y galería no se funden. **Target:** `Cisterna`. **Prueba:** tres perfiles.
- **CI-17 · Silueta** ondas son líneas rotas con intervalos, no rayos. **Target:** `ondasCisterna`. **Prueba:** lectura de agua.
- **CI-18 · Silueta** puntos verdes son pilotos de mantenimiento, no partículas. **Target:** `pulso`. **Prueba:** soporte físico.
- **CI-19 · Profundidad** escalonar ondas según dx y no por filas planas. **Target:** `Marco`. **Prueba:** convergen.
- **CI-20 · Profundidad** hacer que columna oculte parte del fondo. **Target:** `Cisterna`. **Prueba:** oclusión.
- **CI-21 · Material** metal húmedo, hormigón y agua tienen contraste separado. **Target:** `MaterialesEscena`. **Prueba:** materiales táctiles.
- **CI-22 · Material** añadir sarro bajo entrada de agua. **Target:** `Cisterna`. **Prueba:** desgaste vertical.
- **CI-23 · Material** reflejo roto en borde de pasarela. **Target:** `TratamientoEscena`. **Prueba:** no espejo total.
- **CI-24 · Material** marcar remaches grandes en compuerta. **Target:** `MaterialesEscena`. **Prueba:** perímetro físico.
- **CI-25 · Material** condensación sobre la zona fría, no sobre todo. **Target:** `humedad`. **Prueba:** localizada.
- **CI-26 · Luz principal** luz fría sumergida que se pierde con profundidad. **Target:** `Cisterna`. **Prueba:** gradiente.
- **CI-27 · Luz secundaria** pilotos verdes bajos y espaciados. **Target:** `DireccionArte.pulso`. **Prueba:** no iluminan escena.
- **CI-28 · Luz secundaria** reflejo cálido accidental en pasarela seca. **Target:** `TratamientoEscena`. **Prueba:** acento pequeño.
- **CI-29 · Sombras** galería proyecta una banda sobre agua. **Target:** `Cisterna`. **Prueba:** sombra corta.
- **CI-30 · Sombras** columna tiene lado sumergido más oscuro. **Target:** `Cisterna`. **Prueba:** volumen.
- **CI-31 · Reflejo** ondas devuelven luz con intensidad decreciente. **Target:** `ondasCisterna`. **Prueba:** no suben a UI.
- **CI-32 · Color** verde técnico, azul agua, gris hormigón. **Target:** `Paleta`. **Prueba:** tres familias contenidas.
- **CI-33 · Contraste** compuerta visible durante suspensión sin aumentar exposición. **Target:** `preview`. **Prueba:** valores estables.
- **CI-34 · Densidad** limitar caústicas a agua visible y no a cielo completo. **Target:** `causticas`. **Prueba:** sin patrón repetitivo.
- **CI-35 · Forma extraña** una marca de nivel queda por encima de la compuerta. **Target:** `Cisterna`. **Prueba:** historia extraña.
- **CI-36 · Movimiento** ondas lentas con distinta fase por fuente. **Target:** `ondasCisterna`. **Prueba:** movimiento reducido congela.
- **CI-37 · Evento propio** chapoteo solo deforma una onda local. **Target:** `GestorAmbiente`. **Prueba:** evento no cubre cuenca.
- **CI-38 · Presencia** figura cruzando detrás de columna reflejada en agua. **Target:** `Presencia`. **Prueba:** oclusión y reflejo.
- **CI-39 · Transición** apagar pilotos por tramos, dejar una compuerta audible. **Target:** `RotacionNiveles`. **Prueba:** no bloque uniforme.
- **CI-40 · Legibilidad UI** bajar caústicas detrás de hoja sin quitar agua del resto. **Target:** `EscenaNivel`. **Prueba:** texto claro.
- **CI-41 · Resolución** conservar línea de agua y columna en 320x240. **Target:** `preview`. **Prueba:** identidad.
- **CI-42 · Rendimiento** limitar ondas al rectángulo de agua y viewport. **Target:** `culling`. **Prueba:** coste proporcional.
- **CI-43 · Rendimiento** reutilizar fases de caústicas por nivel. **Target:** `renderer`. **Prueba:** menos trigonometría.
- **CI-44 · Accesibilidad** eliminar variación de luz acuática con destellos reducidos. **Target:** `ConfigTurno`. **Prueba:** no flicker.
- **CI-45 · Accesibilidad** congelar ondas/pilotos con movimiento reducido. **Target:** `EscenaNivel`. **Prueba:** estabilidad.
- **CI-46 · Identidad** marcas de nivel y compuerta conectan agua con mantenimiento. **Target:** `checklist`. **Prueba:** no piscina genérica.
- **CI-47 · Identidad** columna debe parecer soporte real, no pilar decorativo. **Target:** `Cisterna`. **Prueba:** junta y base.
- **CI-48 · Verificación** reconocimiento ciego sin color azul. **Target:** `checklist`. **Prueba:** agua, galerías y compuerta.
- **CI-49 · Verificación** probar 4:3, ultrawide y GUI scale máximo. **Target:** `preview`. **Prueba:** agua no corta UI.
- **CI-50 · Verificación** contar fills de ondas/caústicas a 4K. **Target:** `perfil`. **Prueba:** P0.

## Nivel 9 — Trono

Las 50 filas siguientes son específicas de este recinto. `Target` identifica la clase o capa que debe cambiar; `Prueba` evita contabilizar un retoque cosmético.

- **TR-01 · Cámara** desplazar la fuga por debajo del centro del trono para que el ábside tenga peso. **Target:** `Marco de nivel9`. **Prueba:** trono no flota. **Estado:** Implementado en Java y espejo de preview; validación dentro de Minecraft pendiente.
- **TR-02 · Cámara** bajar horizonte y aumentar altura aparente de la sala. **Target:** `Trono`. **Prueba:** ceremonial sin techo aplastado.
- **TR-03 · Cámara** usar columnas cercanas como marco desigual. **Target:** `columnas`. **Prueba:** no simetría de pasillo.
- **TR-04 · Composición** trono vacío debe ser foco único de alta jerarquía. **Target:** `Trono.trono`. **Prueba:** nada brillante lo compite.
- **TR-05 · Composición** separar tarima, alfombra y ábside en planos escalonados. **Target:** `Trono`. **Prueba:** profundidad ceremonial.
- **TR-06 · Composición** dejar un vacío oscuro detrás del respaldo. **Target:** `abside`. **Prueba:** asiento se recorta.
- **TR-07 · Composición** evitar que cadenas y estandartes creen un marco rectangular. **Target:** `DireccionArte.trono`. **Prueba:** asimetría.
- **TR-08 · Composición** proteger la hoja de la banda central del haz. **Target:** `EscenaNivel`. **Prueba:** UI conserva legibilidad.
- **TR-09 · Escala** trono debe ser grande pero menor que el ábside. **Target:** `Trono`. **Estado:** Implementado: escala revisada: trono grande, con la arquitectura del abside dominando el conjunto; validación dentro de Minecraft pendiente. **Prueba:** arquitectura domina.
- **TR-10 · Escala** escalones decrecen hacia arriba, con cantos legibles. **Target:** `Trono`. **Estado:** Implementado: estrado de seis escalones con proporciones decrecientes hacia arriba y cantos legibles; validación dentro de Minecraft pendiente. **Prueba:** tarima no es cajón.
- **TR-11 · Arquitectura** rehacer el ábside con dovelas concéntricas. **Target:** `abside`. **Estado:** Implementado: abside rehecho con tres dovelas concentricas por lado y oro solo en el arco interno; validación dentro de Minecraft pendiente. **Prueba:** curva construida.
- **TR-12 · Arquitectura** añadir boquetes de techo con bordes integrados. **Target:** `boquetes`. **Prueba:** no rectángulos flotantes.
- **TR-13 · Arquitectura** conectar columnas con capiteles o ruinas visibles. **Target:** `columnas`. **Prueba:** soporte vertical.
- **TR-14 · Arquitectura** hacer que alfombra termine en tarima, no en suelo infinito. **Target:** `alfombraRoja`. **Prueba:** perspectiva anclada.
- **TR-15 · Arquitectura** añadir una losa rota en primer plano. **Target:** `PrimerPlano.trono`. **Prueba:** ruina entra desde borde. **Estado:** Implementado en Java y espejo de preview; validación dentro de Minecraft pendiente.
- **TR-16 · Silueta** asiento vacío debe reconocerse por respaldo, brazos y corona ausente. **Target:** `Trono.trono`. **Estado:** Implementado: silueta del asiento vacio: respaldo, brazos y hueco de corona ausente grande y oscuro; validación dentro de Minecraft pendiente. **Prueba:** silueta inequívoca.
- **TR-17 · Silueta** estandartes deben ser tela irregular y asta. **Target:** `estandartes`. **Estado:** Implementado: estandartes como tela irregular: jirones con sesgo determinista y asta torcida; validación dentro de Minecraft pendiente. **Prueba:** no son bandas.
- **TR-18 · Silueta** columna rota necesita remate dentado. **Target:** `columnas`. **Prueba:** ruina visible.
- **TR-19 · Profundidad** ocultar base de una columna con losa cercana. **Target:** `PrimerPlano`. **Prueba:** oclusión real.
- **TR-20 · Profundidad** hacer que haz cenital se ensanche hacia la tarima. **Target:** `hazMayor`. **Prueba:** perspectiva de luz.
- **TR-21 · Material** diferenciar piedra azul, oro apagado, tela y polvo. **Target:** `MaterialesEscena/Trono`. **Prueba:** tactilidad.
- **TR-22 · Material** añadir desgaste en cantos de escalones. **Target:** `Trono`. **Prueba:** uso repetido. **Estado:** Implementado con marcas deterministas y un único brillo de canto; validación dentro de Minecraft pendiente.
- **TR-23 · Material** romper dorado en remates y no llenar toda la silla. **Target:** `Trono`. **Prueba:** oro reservado.
- **TR-24 · Material** añadir humedad oscura en juntas bajas del ábside. **Target:** `piedra/humedad`. **Prueba:** localizada. **Estado:** Implementado con humedad oscura localizada en las juntas bajas del ábside; validación dentro de Minecraft pendiente.
- **TR-25 · Material** estandarte tiene galón y parche, no textura uniforme. **Target:** `estandartes`. **Prueba:** material textil.
- **TR-26 · Luz principal** haz cenital debe definir vacío, no convertir silla en bloque claro. **Target:** `hazMayor`. **Prueba:** interior sigue en sombra.
- **TR-27 · Luz secundaria** antorchas laterales deben ser débiles y desiguales. **Target:** `DireccionArte.antorcha`. **Prueba:** el trono domina.
- **TR-28 · Luz secundaria** boquete exterior frío contrasta con oro. **Target:** `boquetes`. **Prueba:** dos temperaturas.
- **TR-29 · Sombras** trono proyecta sombra sobre ábside. **Target:** `Trono`. **Prueba:** anclaje físico.
- **TR-30 · Sombras** columnas cortan haces de forma gradual. **Target:** `columnas/haces`. **Prueba:** oclusión de luz.
- **TR-31 · Reflejo** oro recibe un brillo corto en un solo canto. **Target:** `Trono`. **Prueba:** no halo dorado global. **Estado:** Implementado en un único canto de la tarima; validación dentro de Minecraft pendiente.
- **TR-32 · Color** no usar rojo en alfombra, oro y azul de piedra mandan. **Target:** `Paleta`. **Prueba:** rojo ausente.
- **TR-33 · Contraste** asiento vacío queda visible en suspensión al 4 %. **Target:** `preview`. **Prueba:** silueta, no brillo.
- **TR-34 · Densidad** limitar haces, polvo y estandartes para que no roben foco. **Target:** `presupuesto`. **Prueba:** trono sigue primero.
- **TR-35 · Forma extraña** corona central ausente deja un hueco negro reconocible. **Target:** `Trono`. **Prueba:** vacío narrativo.
- **TR-36 · Movimiento** estandartes respiran con amplitud mínima. **Target:** `estandartes`. **Prueba:** movimiento reducido congela.
- **TR-37 · Evento propio** cascote cae fuera del eje y no golpea el trono. **Target:** `GestorAmbiente`. **Prueba:** evento lateral. **Estado:** Implementado en `EventosAmbientales` y espejo de preview; validación dentro de Minecraft pendiente.
- **TR-38 · Presencia** figura se oculta detrás de una columna y cruza una vez. **Target:** `Presencia`. **Prueba:** escala comparable.
- **TR-39 · Transición** apagar haces laterales, luego cenital, dejando silueta final. **Target:** `RotacionNiveles`. **Prueba:** orden ceremonial.
- **TR-40 · Legibilidad UI** trono y haz deben quedar fuera de la hoja o bajo contraste. **Target:** `EscenaNivel`. **Prueba:** aviso no pierde tinta.
- **TR-41 · Resolución** asiento, tarima y ábside siguen legibles en ventana baja. **Target:** `preview`. **Prueba:** composición no colapsa.
- **TR-42 · Rendimiento** culling por tramo de columnas, haces y boquetes. **Target:** `renderer`. **Prueba:** fills sin geometría invisible.
- **TR-43 · Rendimiento** reducir polvo del haz según área visible. **Target:** `hazMayor`. **Prueba:** coste proporcional.
- **TR-44 · Accesibilidad** haz no pulsa con destellos reducidos. **Target:** `EscenaNivel`. **Prueba:** luz monotónica.
- **TR-45 · Accesibilidad** telas/partículas se congelan con movimiento reducido. **Target:** `Trono/DireccionArte`. **Prueba:** cuadro estático.
- **TR-46 · Identidad** reconocimiento ciego debe depender de trono vacío y ábside, no de texto. **Target:** `checklist`. **Prueba:** 9/10.
- **TR-47 · Identidad** conservar rojo exclusivamente para Ejecutores, aunque haya imaginario real. **Target:** `Paleta`. **Prueba:** no contradicción.
- **TR-48 · Verificación** comparar trono actual con una versión sin maquillaje. **Target:** `diff visual`. **Prueba:** foco central mejora.
- **TR-49 · Verificación** probar papel, pausa, UI mínima y suspensión. **Target:** `preview/manual`. **Prueba:** legibilidad completa.
- **TR-50 · Verificación** medir fills y tiempo de render del trono en 4K. **Target:** `perfil`. **Prueba:** bajo presupuesto.

## Procedimiento de implementación

1. Mantener el commit estable y el tag `seguridad/2026-08-29/evolucion-5/backup-pre-backgrounds` como rollback.
2. Implementar un solo escenario por commit, empezando por Trono después de revisar la composición actual.
3. Generar lámina desnuda y lámina con UI; comparar diff y contar fills antes de aceptar.
4. Ejecutar `python3 tools/verificar.py`, `py_compile` y `git diff --check` en cada bloque.
5. Solo después de disponer de Java 17, hacer `clean build`; la compilación no sustituye la prueba manual en Minecraft.
6. Marcar cada fila como implementada únicamente con commit, captura/medición y checklist manual asociado.

## Backup y decisiones

- Backup específico previo a backgrounds: `seguridad/2026-08-29/evolucion-5/backup-pre-backgrounds`.
- Los backups son referencias Git y no se copian dentro de `mods`.
- No se elimina ninguna planta existente en esta auditoría: primero se necesita la comparación visual y la prueba de reconocimiento.
- El crash nativo de `nvapi64.dll` bloquea cualquier idea futura que requiera profiling GPU o integración agresiva con Oculus/Embeddium; esas ideas permanecen P2.
- REQUIEM permanece bajo `Master` y el slider propio del aviso; no se traslada a `Music` vanilla.
ic` vanilla.
la.

la.
a.
la.
