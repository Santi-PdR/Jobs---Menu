# Checklist manual de aceptación — 0.16.0

Este checklist se ejecuta dentro de una instancia Forge 1.20.1 real. CI certifica código/recursos/build; no certifica estética, hitboxes, audio ni compatibilidad visual.

## Preparación

- [ ] Java 17, Forge 47.x y Minecraft 1.20.1.
- [ ] Instancia `test-1` cerrada antes de sustituir el JAR.
- [ ] Sólo un `jobsmenu-0.16.0.jar` activo en `mods`.
- [ ] Guardar `latest.log` ante crash, pantalla vacía, textura morado/negro o audio huérfano.

## Título y pausa

- [ ] El título Jobs sustituye únicamente el `TitleScreen` previsto.
- [ ] Los cuatro renglones principales responden dentro de su hitbox real.
- [ ] Renunciar conserva su confirmación.
- [ ] Escape sigue bloqueado en el título propio.
- [ ] Escape reanuda desde la pausa propia.
- [ ] F3+Esc conserva pausa vanilla sin menú.
- [ ] Pausa → Condiciones de estancia abre `PantallaOpcionesJobs` directamente.
- [ ] Volver desde Options regresa a la pausa cuando el padre era la pausa.

## Centro de control / Options

- [ ] **Config Jobs** aparece como acción principal de ancho completo.
- [ ] La sección Jobs se distingue claramente de las opciones Minecraft.
- [ ] Piel, Sonido, Video, Controles, Idioma, Chat, Resource Packs, Accesibilidad y Online abren la pantalla correcta.
- [ ] FOV aparece cuando hay espacio y modifica el valor real.
- [ ] En ventana pequeña el layout reduce contenido antes de superponer hitboxes.
- [ ] Volver regresa al padre correcto.
- [ ] No quedan botones `Done` invisibles capturando foco/click.

## Config Jobs

- [ ] Options → Config abre `PantallaAjustesAviso`.
- [ ] Mods → Jobs Menu → Config abre la misma implementación.
- [ ] Existen cinco categorías visibles: Visual, Nivel, Audio, Accesibilidad y Sistema.
- [ ] La categoría seleccionada tiene jerarquía JOBS propia.
- [ ] Cambiar de categoría no pierde ajustes.
- [ ] Cerrar/reabrir conserva los valores.
- [ ] No se ven filas, sliders o botones con skin vanilla.
- [ ] Ningún toggle, slider, tab, footer o Volver se solapa.
- [ ] Mantener el puntero sobre cada tab/toggle/slider muestra una explicación localizada y legible.

### Visual

- [ ] Escena viva.
- [ ] Estado de instalación.
- [ ] Respiración de cámara.
- [ ] Presencia de fondo.
- [ ] Eventos ambientales.
- [ ] Papel limpio.
- [ ] Guía de lectura.
- [ ] Interfaz mínima.
- [ ] Alto contraste.
- [ ] Texto grande.

### Nivel

- [ ] Rotación de niveles.
- [ ] Cuenta regresiva.
- [ ] Nivel fijo 0–17.
- [ ] Duración de estancia.
- [ ] Rotación calma.
- [ ] Avisos rotativos.
- [ ] Duración de avisos.
- [ ] Fecha.

### Audio

- [ ] Volumen del aviso.
- [ ] Volumen de música.
- [ ] Volumen de ambiente.
- [ ] Música de menú.
- [ ] Sonido ambiente.
- [ ] Sonidos de botones.
- [ ] Crédito de música.

### Accesibilidad

- [ ] Perfil accesible.
- [ ] Movimiento reducido.
- [ ] Destellos reducidos.
- [ ] Bajo consumo.
- [ ] Alto contraste.
- [ ] Texto grande.
- [ ] Papel limpio.
- [ ] Guía de lectura.

### Sistema

- [ ] La Suspensión.
- [ ] Menú propio.
- [ ] Pausa propia.
- [ ] Rotación calma.

## Widgets Jobs

- [ ] Botones NORMAL, PRINCIPAL, JOBS y TERMINAL se distinguen sin depender de rojo.
- [ ] Hover/foco de teclado es visible.
- [ ] Press tiene respuesta clara sin desplazar el hitbox.
- [ ] Toggle separa etiqueta y estado y la narración sigue leyendo el valor completo.
- [ ] Slider muestra escala/tirador y conserva drag/teclado.
- [ ] Movimiento reducido elimina/simplifica microanimaciones de foco.
- [ ] No hay doble sonido vanilla + Jobs sobre widgets propios.

## Diálogos vanilla auxiliares

- [ ] Direct Connect conserva validación y conexión real.
- [ ] Add Server conserva campos, validación y guardado.
- [ ] Edit Server modifica el registro correcto.
- [ ] Confirmaciones vanilla conservan acciones correctas.
- [ ] Sus botones/campos se perciben integrados mediante `PielVanillaJobs`.
- [ ] La capa visual coincide con los hitboxes reales.
- [ ] Pantallas de otros mods NO reciben esta skin de controles.

## Sonido, Chat, Mouse, Teclas, Online y Accesibilidad

- [ ] Todas las opciones vanilla esperadas continúan presentes.
- [ ] Las listas hacen scroll hasta el final.
- [ ] Cabecera/footer no cubren contenido.
- [ ] `Cerrar expediente` vuelve al padre correcto.
- [ ] Accesibilidad mantiene las ayudas Jobs añadidas al final de la lista.
- [ ] La **Guía de accesibilidad** vanilla no aparece como botón inferior superpuesto.
- [ ] Agacharse/Correr conservan Mantener/Alternar.
- [ ] Reasignación de teclas y conflictos funcionan.

## Scrollbar Jobs

- [ ] Se ve canaleta, topes, marcas de recorrido y tirador cuando la lista permite resolver sus datos.
- [ ] El tamaño del tirador representa aproximadamente el contenido visible.
- [ ] La rueda funciona.
- [ ] Click en la barra funciona.
- [ ] Drag funciona.
- [ ] La posición visual coincide con la posición real de scroll.
- [ ] Una lista incompatible conserva una scrollbar utilizable/fallback en vez de romperse o dibujar una barra negra gigante.

## Video / Embeddium

- [ ] Sin Embeddium se abre video vanilla tematizado.
- [ ] Con Embeddium se abre su pantalla real.
- [ ] Jobs no reconstruye ni rompe sus tabs/opciones.
- [ ] La banda contextual no tapa controles críticos.

## Idioma y Resource Packs

- [ ] La lista de idiomas hace scroll completo.
- [ ] ES ↔ EN aplica correctamente.
- [ ] **Español (Uruguay)** mantiene títulos, subtítulos y botones Jobs en español.
- [ ] No aparecen cadenas mezcladas como `Close file` o `Notice settings` al usar variantes españolas.
- [ ] La recarga de recursos vuelve al flujo correcto.
- [ ] Resource Packs mantiene selección, orden, aplicar y abrir carpeta.
- [ ] Resource Packs no deja dirt ni una hoja gigante; se ve el archivo oscuro con margen.
- [ ] No aparece `jobsmenu-musica-activa`; si existía de una versión anterior, se retira sin tocar otros packs.
- [ ] F3+T no duplica música/ambiente.

## Multijugador

- [ ] Servidores guardados, iconos, ping y MOTD.
- [ ] LAN.
- [ ] Seleccionar/entrar.
- [ ] Direct Connect.
- [ ] Add/Edit/Delete.
- [ ] Refresh.
- [ ] Cancel vuelve al padre.
- [ ] Los estados activos de botones Jobs coinciden con las acciones reales.
- [ ] La cabecera **Puestos de acceso** no se solapa con la lista.
- [ ] `JobsDosh.exaroton.me:56477` aparece primero como servidor oficial en el idioma seleccionado.
- [ ] Con el servidor oficial seleccionado, Edit/Delete quedan desactivados y Seleccionar sigue funcionando.

## Seleccionar mundo

- [ ] La lista conserva previews de mundos.
- [ ] Seleccionar mundo activa las acciones correctas.
- [ ] Crear mundo nuevo funciona.
- [ ] Editar/borrar/recrear conservan los diálogos y callbacks vanilla.
- [ ] Cancelar vuelve al padre correcto.
- [ ] No aparece una banda central de dirt que rompa el chrome Jobs.
- [ ] El título visible es **Archivo de turnos** / **Shift archive** y el buscador conserva su hitbox.

## Mods / Forge

- [ ] La lista de mods carga completa.
- [ ] Orden normal, A–Z y Z–A funciona.
- [ ] Search filtra sin perder la selección válida.
- [ ] Config sólo se activa cuando el mod seleccionado expone configuración.
- [ ] Open mods folder conserva la acción real.
- [ ] Logos y panel de información siguen renderizando.
- [ ] El texto de Forge se ve en tinta sepia, no blanco puro.
- [ ] El marco Jobs no tapa el contenido de Forge.

## Layout y escalado

Probar como mínimo:

- [ ] 854×480.
- [ ] 1280×720.
- [ ] 1920×1080.
- [ ] ventana estrecha.
- [ ] poca altura.
- [ ] GUI Scale 2.
- [ ] GUI Scale 3.
- [ ] GUI Scale 4.
- [ ] español.
- [ ] inglés.
- [ ] Español (Uruguay).

En todos:

- [ ] no hay texto sobre botones;
- [ ] no hay footer sobre Volver;
- [ ] no hay controles fuera de pantalla;
- [ ] el foco de teclado se ve;
- [ ] las cadenas traducidas no muestran claves `jobsmenu.*`;
- [ ] las hojas grandes tienen jerarquía visual sin sentirse llenas de decoración arbitraria.

## Fondos 10–17

- [ ] Ninguno muestra textura morado/negro.
- [ ] Cada PNG permanece completamente inmóvil una vez estabilizado el Nivel.
- [ ] No hay zoom.
- [ ] No hay paneo/parallax.
- [ ] No hay flicker propio.
- [ ] No hay scanline animada.
- [ ] No hay niebla móvil.
- [ ] No hay motas/presencia superpuestas.
- [ ] El chrome de interfaz puede ser estático alrededor de la pantalla sin mover la imagen.
- [ ] Los apagones/cambios de Nivel siguen funcionando.

## Escena, transición y audio

- [ ] Niveles 0–9 conservan su vida procedural.
- [ ] Transición entre expedientes se percibe como papel/carpeta, no wipe digital genérico.
- [ ] Movimiento reducido la convierte en fade breve.
- [ ] Abrir interfaces no reinicia Nivel, música ni camas ambientales.
- [ ] Entrar a un mundo detiene el ambiente del menú.
- [ ] Alt+Tab, F3+T y recargas no duplican audio.
- [ ] La Suspensión no rompe una pantalla hija y recupera el estado correctamente.

## Build y entrega

- [ ] `tools/verificar_version.py` sin fallos.
- [ ] `tools/verificar_fondos.py` sin fallos.
- [ ] `tools/verificar.py` sin fallos ni avisos nuevos.
- [ ] GitHub Actions compila con Java 17.
- [ ] Artefacto exacto: `jobsmenu-0.16.0.jar`.
- [ ] PR 0.16.0 verde antes de mergear.
- [ ] `main` verde después del merge.
- [ ] `dev-latest` contiene únicamente el JAR versionado actual.
- [ ] El PowerShell se pasa sólo después de completar los puntos anteriores.

## Evidencia ante fallos

Registrar:

- versión exacta del JAR;
- pantalla/ruta;
- Nivel;
- resolución y GUI Scale;
- idioma;
- mods UI/video activos;
- opciones de accesibilidad/bajo consumo;
- captura/video si es visual;
- `latest.log` para recursos, audio, conexión o crash.

Un build verde certifica compilación y verificadores. La aceptación visual requiere probar el mod dentro de Minecraft.
