# Fondos 18-31 - Jobs Menu 0.27.0

Los 14 fondos fueron subidos directamente al repositorio y son la fuente real usada por el mod. No existe paso de ZIP/Base64 ni reconstruccion durante Gradle.

Ruta:

`src/main/resources/assets/jobsmenu/textures/backgrounds/`

Todos los nuevos archivos son JPG **1920x1080, 16:9**.

| Nivel | Archivo | Nombre de origen |
|---:|---|---|
| 18 | `nivel18.jpg` | `cool_glitchy_null_by_autumn` |
| 19 | `nivel19.jpg` | `dark_moon_1` |
| 20 | `nivel20.jpg` | `heavenlytegrity` |
| 21 | `nivel21.jpg` | `circuit_frolic` |
| 22 | `nivel22.jpg` | `caveman` |
| 23 | `nivel23.jpg` | `caveboy` |
| 24 | `nivel24.jpg` | `bad_posture` |
| 25 | `nivel25.jpg` | `a_very_null_night` |
| 26 | `nivel26.jpg` | `moonboy` |
| 27 | `nivel27.jpg` | `void_castle` |
| 28 | `nivel28.jpg` | `tbread` |
| 29 | `nivel29.jpg` | `scarlet_king` |
| 30 | `nivel30.jpg` | `new_super_circuit_bros_3d` |
| 31 | `nivel31.jpg` | `world_domination` |

## Render

`PlantaImagen` usa cover centrado y filtrado lineal. Los archivos no se reescriben ni se deforman.

- 10-17: contrato historico totalmente estatico.
- 18-31: se permite una respiracion de camara muy leve y no destructiva.
- Movimiento reducido, Bajo consumo o escena quieta desactivan ese movimiento.
- No se agregan objetos falsos, foreground dinamico o flicker agresivo.
- Fades, apagones y transiciones globales siguen funcionando por encima.

## Verificacion

`tools/verificar_fondos.py` valida directamente:

- PNG 10-17: firma, chunks, CRC, IDAT y dimensiones.
- JPEG 18-31: firma, cierre, SOF y dimensiones 1920x1080.

Los niveles 10-17 no fueron modificados durante 0.27.0.
