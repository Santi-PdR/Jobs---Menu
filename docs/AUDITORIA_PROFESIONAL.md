# Auditoría profesional — Jobs Menu

Fecha: 2026-08-28

## Estado general

Jobs Menu ya no es un simple reemplazo de pantalla: tiene identidad visual, rotación de recintos, transición diegética, mezcla de audio por capas, música propia, accesibilidad y pantallas temáticas. La arquitectura es suficientemente modular para seguir creciendo sin convertir el render principal en una clase monolítica.

## Fortalezas que se mantienen

- Diez recintos con `Planta` independiente.
- Cámara, paleta, reflectividad y humedad por nivel.
- Tres camas de ambiente por nivel: base, carácter y actividad.
- Eventos sonoros ocasionales ponderados con variación de tono y volumen.
- Transiciones de luz y audio sincronizadas.
- Presencia rara sin jumpscare.
- Configuración cliente y accesibilidad.
- Pantalla principal, pausa y ajustes con lenguaje visual común.
- Música separada del ambiente y con mezcla dinámica.
- Render procedural: no depende de texturas externas para existir.

## Problemas detectados

### 1. Dirección artística desigual

Los recintos evolucionaron en momentos distintos y algunos tenían mucho más detalle que otros. Había buenas geometrías, pero no un lenguaje de material e iluminación común. Esto hacía que ciertos niveles parecieran concept art terminado y otros prototipos funcionales.

### 2. Superficies demasiado limpias

La geometría procedural resolvía arquitectura, pero paredes y suelos grandes podían leerse como bloques de color. Faltaban señales pequeñas de material: juntas, grietas, remaches, veta, humedad, depósitos y desgaste.

### 3. Jerarquía de iluminación insuficiente

La mayoría de los niveles tenía una fuente principal y viñeta, pero faltaban fuentes secundarias, rebote, manchas de luz y separación clara entre primer plano, medio y fondo.

### 4. Música ligada al canal vanilla de música

La pista del mod usaba `SoundSource.MUSIC`, por lo que el slider Música de Minecraft podía silenciarla. El mod ya tiene su propio control de volumen, por lo que esa dependencia era redundante.

### 5. Loop musical demasiado mecánico

El loop no dejaba aire entre una reproducción y la siguiente. Se cambió a una repetición con aproximadamente dos segundos de silencio.

### 6. Residuos locales de desarrollo

Los crash logs de JVM podían aparecer como archivos sin trackear y bloquear scripts de actualización conservadores. Se añadieron a `.gitignore`.

## Cambios de esta pasada

### Música

- La pista del menú usa una categoría que no depende del slider vanilla Música.
- Sigue respetando el volumen maestro y el volumen propio de Jobs Menu.
- Repetición con ~2 s de separación.
- Se mantiene el ducking durante transición y presencia.

### Fondos

Se añadieron tres capas complementarias:

1. `TratamientoEscena`: profundidad, rebote de luz, humedad y grano sutil.
2. `MaterialesEscena`: detalle físico por familia de material.
3. `DireccionArte`: motivos y luces específicos de cada uno de los diez recintos.

La dirección visual toma como referencia arquitectura monumental oscura, piedra antigua, metal industrial, verde tóxico/energético usado con moderación, fuego ámbar, cadenas y espacios de gran escala. No se copia una escena concreta y no se usa rojo ambiental: el rojo continúa reservado a los Executores.

## Lenguaje visual por nivel

- Nivel 0: administración monumental con acentos verdes y señalética luminosa.
- Nivel 1: nave industrial de gran escala con pilares oscuros y luces de mantenimiento.
- Nivel 2: servicio caliente, tuberías, abrazaderas y reflejos metálicos.
- Nivel 3: natatorio con cáusticas, reflejos fragmentados y azulejo húmedo.
- Nivel 4: piedra cálida, cadenas y piscinas de luz ámbar.
- Nivel 5: biblioteca oscura, madera, vetas, lomos y lámparas verdes discretas.
- Nivel 6: vidrio húmedo, vegetación en silueta y rayos naturales superiores.
- Nivel 7: piedra fría, nichos, cadenas y luz cálida aislada.
- Nivel 8: cisterna industrial, agua negra, cáusticas y luces de mantenimiento sumergidas.
- Nivel 9: sala ceremonial arruinada, cadenas, luz vertical y antorchas laterales.

## Siguientes mejoras recomendadas

1. Revisar cada escena en juego a GUI scale 2, 3 y 4 y ajustar densidad de detalle por resolución.
2. Crear dos o tres variantes geométricas por recinto seleccionadas por sesión, sin cambiar su identidad.
3. Añadir paneo estéreo muy leve a eventos puntuales según dónde ocurren visualmente.
4. Dar a cada nivel una firma de transición de audio pequeña además del apagón común.
5. Medir coste de render en hardware modesto y reducir capas si el frame time del menú supera el objetivo.
6. Limpiar documentación histórica que todavía describe comportamientos antiguos.
7. Mantener cualquier acceso administrativo oculto fuera de documentación orientada al jugador.

## Regla de calidad

No añadir efectos solo porque son posibles. Todo elemento nuevo debe cumplir al menos una de estas funciones: explicar material, reforzar escala, guiar la mirada, dar identidad a un recinto, mejorar interacción o enriquecer sonido. Si no cumple ninguna, sobra.
