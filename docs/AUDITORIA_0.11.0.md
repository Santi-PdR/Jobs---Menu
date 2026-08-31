# Auditoría profesional — Jobs Menu 0.11.0

Fecha: 2026-08-31

## Objetivo

Revisar el estado completo del mod después de integrar correctamente los 18 backgrounds y aplicar mejoras que aumenten sensación de producto terminado sin añadir ruido visual gratuito ni romper accesibilidad, rendimiento o compatibilidad.

## Regla nueva e innegociable

**Toda entrega de Jobs Menu debe llevar versión en el nombre del JAR.**

Correcto: `jobsmenu-0.11.0.jar`  
Incorrecto: `jobsmenu-latest.jar`

`gradle.properties` es la fuente de verdad. `tools/verificar_version.py` y GitHub Actions hacen cumplir la regla para que no dependa de memoria humana.

## Diagnóstico general

El mod ya tenía una base sólida: arquitectura por capas, 18 niveles, fondos animados, audio por camas, eventos, música, accesibilidad y ciclo de vida de menú. Los puntos débiles de esta revisión no eran grandes funciones ausentes sino acabado transversal y proceso de entrega.

### Lo que ya estaba bien

- Los 18 niveles están integrados y los fondos 10–17 pasan validación PNG fuerte.
- `PlantaImagen` valida con `NativeImage` y evita el cubo morado/negro mediante fallback.
- La transición de nivel ya sincroniza luz y audio desde un único snapshot.
- Movimiento reducido, destellos reducidos y bajo consumo son opciones reales, no decorativas.
- La mezcla ambiental tiene BASE, CARÁCTER, ACTIVIDAD y eventos puntuales.
- El menú principal tiene jerarquía, navegación de teclado, confirmación de salida y sonido propio.
- La música y el ambiente conservan lifecycle al entrar en pantallas hijas.

## Problemas encontrados en esta pasada

### 1. La release ocultaba la versión

Gradle producía un JAR versionado, pero el workflow copiaba el artefacto a `jobsmenu-latest.jar`. Para el usuario final el mod parecía no tener versión aunque internamente sí la tuviera.

**Corrección:** 0.11.0 publica `jobsmenu-0.11.0.jar`. El tag `dev-latest` sigue siendo rodante, pero el archivo no pierde identidad.

### 2. El acabado visual estaba dividido entre dos mundos

Los fondos 10–17 ya tenían movimiento propio y los niveles 0–9 tenían dirección procedural, pero faltaba una capa de cámara/instalación que unificara ambos grupos.

**Corrección:** `PulidoEscena` añade un lenguaje transversal muy sutil:

- halo residual de fluorescente;
- barrido de exposición lento;
- masa visual durante el apagado;
- recuperación eléctrica con banda de luz;
- borde opresivo específico durante La Suspensión.

No sustituye efectos de cada nivel ni usa rojo global. Respeta bajo consumo, movimiento reducido y estado de Suspensión.

### 3. Las camas largas podían delatar el loop por afinación perfecta

El volumen ya respiraba, pero BASE y CARÁCTER conservaban exactamente el mismo tono cada repetición.

**Corrección:** microderiva tonal de ciclo largo, inferior a unas décimas de porcentaje. ACTIVIDAD queda fija porque contiene objetos reconocibles y no debe cambiar su material acústico.

### 4. El papel seguía leyéndose demasiado plano en pantallas secundarias

La hoja tenía una sombra y cinta, pero visualmente podía parecer un rectángulo superpuesto, especialmente con fondos fotográficos.

**Corrección:** `HojaPapel` recibe:

- sombra en dos planos;
- degradado vertical mínimo según luz;
- marcas de fotocopia deterministas;
- roce de borde muy pequeño;
- cinta con highlight/sombra propios.

`papel_limpio` elimina estas marcas y conserva el perfil accesible.

### 5. Documentación de entrega desactualizada

README/DESPLIEGUE todavía hablaban de 15 niveles, `jobsmenu-latest.jar` y procedimientos históricos.

**Corrección:** documentos vigentes reescritos para 0.11.0, 18 niveles y release versionada.

## Cambios aplicados

### Código

- `PulidoEscena.java` nuevo.
- `EscenaNivel` integra acabado global en los 18 niveles.
- `CapaAmbiente` incorpora microderiva tonal larga en BASE/CARÁCTER.
- `HojaPapel` mejora profundidad, luz y textura del documento.

### Pipeline

- versión elevada a **0.11.0**;
- resolución automática de `mod_version` en Actions;
- artefacto y release con nombre versionado;
- `tools/verificar_version.py` evita regresiones;
- se conservan validación fuerte de PNG, auditoría Python y build Forge Java 17.

### Documentación

- README actualizado al estado real;
- despliegue actualizado para descubrir dinámicamente el asset versionado de `dev-latest`;
- esta auditoría pasa a ser la referencia de la revisión 0.11.0.

## Decisiones de no hacer

Una revisión profesional también decide qué no agregar.

- No se añadieron shaders ni postprocesado pesado: coste y compatibilidad no justifican el beneficio en un menú.
- No se añadieron flashes nuevos durante La Suspensión.
- No se duplicaron pantallas de opciones: los ajustes propios siguen integrados con Opciones de Minecraft.
- No se añadieron sonidos nuevos sólo por cantidad; primero se mejoró variación y mezcla de los existentes.
- No se tocaron hitboxes de vanilla/mods ajenos.

## Pruebas que todavía requieren Minecraft real

Aunque CI valide recursos y compile, deben comprobarse en `test-1`:

1. recorrer los 18 niveles y observar que el pulido global no tape puntos focales;
2. comparar movimiento normal vs movimiento reducido;
3. comparar normal vs bajo consumo;
4. escuchar BASE/CARÁCTER durante varios minutos para confirmar que la microderiva no sea perceptible como cambio de pitch;
5. verificar transición automática y salto manual F;
6. verificar La Suspensión;
7. probar 854×480, 1280×720, GUI Scale extremos y ventana estrecha;
8. recorrer UI con mouse, Tab, Shift+Tab, Enter y Espacio;
9. comprobar música/ambiente al entrar en Opciones, Mods y al volver;
10. desplegar el JAR versionado de `dev-latest` y confirmar que no quede una versión anterior junto a él.

## Criterio para la siguiente evolución

No sumar partículas, animaciones o sonidos por cantidad. Las próximas mejoras deben resolver al menos una de estas funciones:

- mejorar lectura;
- mejorar material/profundidad;
- reforzar identidad de un nivel;
- mejorar continuidad entre pantallas;
- enriquecer sonido sin volverlo predecible;
- reducir errores de entrega;
- mejorar accesibilidad o rendimiento.

Si una idea no cumple ninguna, no entra.
