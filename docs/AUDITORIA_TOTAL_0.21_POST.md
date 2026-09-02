# Auditoria total posterior a 0.21.0

Este pase parte del build 0.21.0 certificado y revisa el mod como producto completo, no como una pantalla aislada. El objetivo es aumentar profesionalidad, control del usuario, legibilidad, rendimiento y coherencia artistica sin convertir Jobs en una interfaz futurista ni reconstruir logica sensible de Minecraft/Forge.

## Regla de fondos PNG

El catalogo actual tiene 18 niveles, 0-17. Los fondos de imagen actuales son 10-17. La peticion de arte menciona tambien 18-19 para una ampliacion futura; si esos niveles se agregan, heredan exactamente este contrato.

Un fondo PNG de la familia alta:

- no recibe pan;
- no recibe zoom;
- no recibe parallax;
- no recibe respiracion de camara;
- no recibe flicker;
- no recibe destellos ambientales;
- no recibe motas;
- no recibe presencia;
- no recibe foreground animado;
- no recibe tratamiento/materiales procedurales;
- no recibe pulso de penumbra asociado al reloj de ronda;
- mantiene una vignette estatica y neutral para separar UI/fondo;
- puede usar filtrado lineal al escalar para evitar pixelado;
- puede participar en el apagado global de cambio de Nivel, porque esa es una transicion entre pantallas/estancias y no una animacion interna del PNG.

Los archivos PNG no fueron sustituidos ni editados en este pase.

## Centro de perfiles

`PantallaAjustesAviso` incorpora una sexta seccion de perfiles. No existe una segunda fuente de configuracion: `PerfilesJobs` aplica valores reales de `ConfigTurno` y luego el usuario puede seguir editandolos uno por uno.

Perfiles disponibles:

1. Equilibrado: experiencia completa con valores base.
2. Inmersivo: mas presencia sonora y ritmo de rotacion mas activo.
3. Rendimiento: movimiento/destellos reducidos, efectos costosos desactivados y bajo consumo.
4. Accesible: activa el perfil accesible, tiempos mas largos y reduce efectos distractores.
5. Minimo: interfaz minima, bajo consumo y ambientacion visual recortada.

La pagina permite aplicarlos por click y tambien mediante F1-F5. El perfil reconocido se distingue con jerarquia Jobs. Las flechas y las teclas 1-6 siguen recorriendo categorias.

## Mejoras de UX del centro de ajustes

- panel ampliado de forma contenida para alojar seis categorias sin perder margen de pantalla;
- ancho de pestanas calculado desde la cantidad real de categorias;
- margenes y gaps compactos adaptativos;
- seleccion de pestana con underline y rail lateral;
- indicador discreto del perfil reconocido;
- perfiles en cuadrilla de dos columnas;
- tooltip explicativo en cada perfil reutilizando descripciones localizadas existentes;
- confirmacion visual al aplicar preset;
- reapertura inmediata de la misma pagina para reflejar el estado nuevo;
- F1-F5 como acceso rapido dentro de perfiles;
- 1-6 para salto directo de categoria;
- flechas izquierda/derecha con wrap;
- guardado real antes de abandonar/cambiar pantalla;
- cada preset conserva menu y pausa Jobs activos;
- cada preset conserva controles de audio reales;
- no se crean hitboxes invisibles ni widgets vanilla superpuestos.

## Rendimiento

El perfil Rendimiento reduce simultaneamente las capas mas costosas: eventos ambientales, presencia, respiracion de camara y suspension rara; tambien activa bajo consumo, reduce movimiento/destellos y alarga la estancia para disminuir transiciones frecuentes. No desactiva la identidad del mod ni su audio por completo.

El perfil Minimo va un paso mas alla y oculta elementos secundarios del aviso, pero conserva navegacion y acciones.

## Accesibilidad

El perfil Accesible reutiliza el contrato ya existente de `perfilAccesible`: movimiento reducido, destellos reducidos, alto contraste y texto grande. Ademas evita presencia/eventos distractores y usa tiempos de lectura mas largos.

## Compatibilidad

- Minecraft 1.20.1 / Forge 47.x.
- Solo cliente.
- No se reemplaza logica de mundos, servidores, resource packs o Forge Mods.
- Los perfiles solo escriben preferencias Jobs.
- Las pantallas externas complejas siguen conservando su logica real.
- El audio de menu mantiene hard stop al entrar a gameplay.

## Prueba pendiente dentro de Minecraft

CI puede verificar Java 17, recursos, contratos, compilacion y JAR. No puede juzgar visualmente la interfaz. Cuando sea posible probar, revisar prioritariamente:

1. Ajustes Jobs en GUI Scale 2, 3 y 4.
2. Las seis pestanas sin solapes.
3. Aplicar los cinco perfiles y volver a modificar opciones individuales.
4. F1-F5 en la pestaña de perfiles.
5. 1-6 y flechas para navegar categorias.
6. PNG 10-17 totalmente inmoviles durante una estancia normal.
7. PNG 10-17 sin pulso de oscuridad al acercarse la ronda.
8. Transicion global de un PNG al siguiente sin zoom/pan/parallax.
9. Perfil Rendimiento con consumo visual reducido.
10. Perfil Accesible con texto y contraste legibles.
11. Entrada a mundo/servidor sin musica o ambiente de menu residual.
