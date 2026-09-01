# Auditoría visual — Jobs Menu 0.14.1

## Objetivo

Este pase nace de capturas reales de Minecraft 1.20.1 y no de una revisión puramente estática. El objetivo fue localizar solapes, jerarquías rotas, pantallas que todavía parecían vanilla/Forge aisladas, scrollbars anómalas, mezcla de idiomas y superficies demasiado vacías.

## Hallazgos de las capturas

### Accesibilidad

Se observó un botón vanilla de **Guía de accesibilidad** ocupando la misma zona inferior que `Cerrar expediente`. El problema no era sólo visual: un widget duplicado puede conservar foco/hitbox aunque otro elemento se dibuje encima.

Corrección: dentro de `PantallaAccesibilidadJobs` se oculta y desactiva el botón inferior vanilla que Jobs sustituye. La lista conserva las opciones reales de Minecraft y las ayudas Jobs añadidas.

### Idioma

Con `Español (Uruguay)` aparecían cadenas Jobs en inglés (`Close file`, `Notice settings`, etc.) porque el mod sólo aportaba `es_es` y Minecraft seleccionaba `es_uy`.

Corrección: `processResources` genera aliases de `es_es` para `es_ar`, `es_cl`, `es_ec`, `es_mx`, `es_uy` y `es_ve`. Se mantiene una única fuente de traducción española.

### Multijugador

Título, subtítulo y nota contextual estaban demasiado próximos y podían invadir la misma franja vertical.

Corrección: la lista comienza más abajo y cada capa textual tiene su espacio propio. Se conserva `ServerSelectionList` y todas las acciones reales de Minecraft.

### Seleccionar mundo

`SelectWorldScreen` mantenía una gran superficie de dirt y rompía completamente la continuidad con el expediente.

Corrección: `PantallaMundosJobs` conserva lista, previews y callbacks vanilla, pero sustituye el contexto visual por `ChromeExpediente` y estiliza la lista de forma defensiva.

### Mods de Forge

`ModListScreen` se veía como una pantalla independiente del mod, con grandes zonas oscuras/dirt.

Corrección: `PantallaModsJobs` conserva búsqueda, orden, logos, Config, panel de información y abrir carpeta; Jobs sólo cambia el marco/contexto.

### Resource Packs

La pantalla podía volver a dibujar bandas/fondo de dirt después del fondo Jobs.

Corrección: la envoltura Jobs intercepta la presentación de la lista/pantalla sin sustituir selección, orden, aplicar ni abrir carpeta.

### Scrollbars

En listas como Idioma se observó una barra negra de tamaño/posición incorrectos. La causa es que no todas las listas exponen los mismos campos internos de `AbstractSelectionList` de forma accesible.

Corrección: `ListasExpediente` mantiene reflection defensiva y usa fallback seguro cuando no puede resolver geometría real. Nunca debe inventar una barra Jobs si no conoce su rango.

### Composición vacía

Algunas pantallas ocupaban una hoja grande con muy pocos widgets y parecían prototipos sin terminar.

Corrección: `ChromeExpediente.panel` añade reglas horizontales, un pliegue vertical y marcas de archivo de opacidad mínima. Son estáticas, se eliminan con `papel_limpio` y no compiten con contenido.

### Footer y overlays

Un badge de otro mod podía tapar la versión en la esquina inferior derecha.

Corrección: el pie reserva una franja derecha. En paneles anchos usa la clave localizada `jobsmenu.interfaz.formulario`; en paneles pequeños mantiene el layout dividido y truncado seguro.

## Corrección adicional del pipeline

El verificador estático todavía avisaba que `jobsmenu.interfaz.formulario` estaba definida pero no usada. En lugar de borrar una clave útil, 0.14.1 la vuelve a utilizar de forma real en el footer cuando el ancho permite mostrar el registro completo.

Esto elimina el aviso y mejora localización sin introducir texto visible duro en Java.

## Versión

El pase cambia el artefacto a:

`jobsmenu-0.14.1.jar`

La versión se incrementa porque el comportamiento/render de varias pantallas cambia respecto al JAR 0.14.0 publicado anteriormente.

## Criterio de aceptación

No se considera cerrada la entrega sólo por compilar. Antes del PowerShell deben cumplirse:

1. `tools/verificar_version.py` verde;
2. `tools/verificar_fondos.py` verde;
3. `tools/verificar.py` verde;
4. build Forge Java 17 verde en el PR;
5. merge a `main`;
6. build verde de `main`;
7. `dev-latest` actualizado con un único `jobsmenu-0.14.1.jar`.

Después de instalar, la aceptación visual final se realiza con `docs/checklist-manual.md` en `test-1`.
