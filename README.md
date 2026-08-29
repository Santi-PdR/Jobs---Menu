# Jobs — Aviso a los ocupantes

Mod visual y sonoro exclusivamente de cliente para Minecraft Forge 1.20.1.
Reemplaza el título y, opcionalmente, la pausa por una interfaz diegética del
servidor Jobs: un aviso administrativo pegado dentro de diez recintos vivos.

Versión actual: **1.0.0**

Minecraft: **1.20.1** · Forge: **47.x** · Java: **17**

## Qué incluye

- Pantalla principal por ratón y teclado, con hitboxes idénticas a sus regiones visuales.
- Hoja adaptable para ventanas estrechas, 720p, GUI Scale 1–4 y Auto.
- Ajustes integrados de forma poco invasiva en OptionsScreen.
- Selector de nivel fijo, movimiento y destellos reducidos, interfaz mínima,
  controles separados de música y ambiente.
- Música REQUIEM con volumen propio, gobernada por **Master + volumen del mod**;
  Music de vanilla no la controla.
- Tres camas ambientales por recinto, eventos propios, ducking y silencios deliberados.
- Diez fondos procedurales reconstruidos con composición, escala y silueta diferentes.
- Eventos visuales específicos de lugar y apariciones especiales poco frecuentes.
- Inglés y español completos.

## Los diez recintos

| Nivel | Identidad |
|---|---|
| 0 | Administración abandonada: recepción, archivo, accesos de personal |
| 1 | Nave de carga: cerchas, puente grúa, portón y escala industrial |
| 2 | Servicio: tuberías, válvula, paneles, vapor y un codo ciego |
| 3 | Natatorio: vaso dominante, calles, gradería, vidrio y reflejos |
| 4 | Cripta: nave pétrea, arcos, altar y braseros |
| 5 | Biblioteca: doble altura, balcón, estanterías, escalera y mesa |
| 6 | Invernadero: cubierta de vidrio, bancales, vegetación y condensación |
| 7 | Catacumbas: nichos, bifurcación, derrumbe y un solo farol |
| 8 | Cisterna: columnas verticales, pasarela y agua negra |
| 9 | Trono: abside ceremonial, columnata, graderío y foco cenital |

## Instalación

1. Instala Minecraft 1.20.1, Java 17 y Forge 47.x.
2. Ejecuta `gradlew clean build`.
3. Copia `build/libs/jobsmenu-1.0.0.jar` a la carpeta `mods` de la instancia.

El servidor no necesita instalarlo. `displayTest="IGNORE_ALL_VERSION"` y las
dependencias de cliente mantienen la conexión con servidores sin el mod.

## Configuración

El botón **Ajustes del aviso** aparece únicamente en la pantalla de opciones
vanilla exacta. Se conserva incluso con el menú propio desactivado para que
siempre exista una ruta de vuelta.

| Opción | Predeterminado | Efecto |
|---|---:|---|
| Menú propio | Sí | Sustituye la pantalla de título vanilla |
| Pausa propia | Sí | Sustituye solo la pausa vanilla real |
| Escena viva | Sí | Movimiento ambiental y transiciones |
| Rotar niveles | Sí | Rotación automática con apagón |
| Nivel fijo | 0 | Nivel usado cuando la rotación está apagada |
| Movimiento reducido | No | Detiene deriva, presencia y partículas |
| Destellos reducidos | No | Suaviza parpadeos y arranques |
| Interfaz mínima | No | Retira decoración secundaria |
| Música | Sí | Activa REQUIEM |
| Volumen música | 70 % | Volumen propio, además de Master |
| Ambiente | Sí | Camas y eventos del recinto |
| Volumen ambiente | 55 % | Volumen ambiental propio |

La configuración se guarda en `config/jobsmenu-client.toml`. `ForgeConfigSpec` y
los sliders nativos comparten los mismos límites.

## Música

Ruta: `sounds.json` → `SoundEvent` → `GestorMusica` → `SoundManager` → `MASTER`.

REQUIEM no depende de Music de vanilla. Master en 0 % la silencia. Cambiar
Master o el volumen del mod se aplica mientras suena. Solo puede existir una
instancia; F3+T y las recargas de packs invalidan la referencia anterior. Cada
vuelta deja 40 ticks de silencio y la pista usa streaming.

Detalles y sustitución legal en [docs/musica.md](docs/musica.md).

## Compatibilidad

El mod evita mixins, reemplazos globales y cambios de slots. Solo sustituye las
clases vanilla exactas de título y pausa. Consulta
[docs/compatibilidad.md](docs/compatibilidad.md).

## Verificación

    python tools/verificar.py
    python tools/vista_previa.py docs/vista_previa.png
    gradlew clean build

La vista previa es una hoja de dirección de arte; el renderer, audio, foco y
escalado deben probarse dentro de Minecraft. La matriz manual está en
[docs/PRUEBAS_MANUALES.md](docs/PRUEBAS_MANUALES.md).

## Licencia y créditos

Código y recursos: **All Rights Reserved**. Autor: **Santi-PdR**.

REQUIEM — Forsaken OST: Emmy Z. No redistribuyas una compilación fuera del
alcance autorizado sin confirmar los derechos de la música incluida.
