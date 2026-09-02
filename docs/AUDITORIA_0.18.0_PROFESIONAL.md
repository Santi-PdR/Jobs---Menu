# Jobs Menu 0.18.0 — Pase profesional

Fecha: 2026-09-02

## Objetivo

Este pase no intenta cambiar la identidad de Jobs Menu. Refuerza lo que ya funciona: expediente frio, instalacion inquietante, interfaces compactas, audio continuo y una separacion estricta entre menu y gameplay.

## Cambios aplicados

### 1. Microinteracciones globales

`PulidoInterfazJobs` pasa a una respuesta mas sobria y consistente:

- foco de teclado con respiracion minima, desactivada por movimiento reducido o bajo consumo;
- hover con marca de lectura en lugar de marcos gruesos;
- esquinas y rail superior con menos contraste para no ensuciar pantallas pequenas;
- entrada de pantalla con curva mas suave;
- confirmacion de cambios con entrada y salida progresivas;
- ninguna mejora cambia hitboxes ni logica vanilla.

### 2. Transiciones entre expedientes

`TransicionInterfazJobs` deja de usar un barrido demasiado agresivo:

- smoothstep en lugar de desplazamiento cubico;
- ancho maximo controlado en resoluciones grandes;
- papel frio/gris, sin volver al amarillo vanilla;
- sombra y velo mas contenidos;
- movimiento reducido y bajo consumo usan solo una atenuacion breve.

### 3. Pipeline nuevo para la siguiente cancion

El flujo antiguo dependia de descargar una fuente externa y reconstruir scripts por chunks. Se reemplaza por una regla unica:

> Subir `music/menu_nueva.ogg` a `main`.

El workflow `integrar_ogg_subido.yml` valida, normaliza, registra, verifica y compila la pista automaticamente.

La pista queda preparada con el identificador interno:

`upon_the_hill_v2`

El recurso final generado sera:

`src/main/resources/assets/jobsmenu/sounds/musica/tema_nuevo.ogg`

Cuando exista, el workflow amplia el catalogo de `GestorMusica` a dos pistas y aprovecha el crossfade que ya estaba implementado.

### 4. Limpieza tecnica

Se eliminaron piezas del integrador remoto anterior:

- workflow de descarga externa;
- trigger manual legado;
- transportador remoto;
- chunks temporales del integrador generado.

La nueva infraestructura es mas pequena, visible y mantenible.

### 5. Version

El proyecto pasa a `0.18.0`.

## Contratos que no se deben romper

- Nunca debe sonar musica o ambiente del menu dentro de un mundo o servidor.
- Salir de un mundo/servidor debe devolver al ecosistema Jobs, no al title screen vanilla.
- La musica pertenece a la visita completa al menu, no a una Screen concreta.
- Opciones, Mods, Mundos, Multiplayer y pantallas hijas no deben reiniciar la cama sonora al navegar entre ellas.
- Movimiento reducido y bajo consumo siempre tienen prioridad sobre efectos decorativos.
- La interfaz no debe volver a sepia/amarillo como lenguaje global.
- No se alteran hitboxes vanilla para conseguir decoracion.

## Integracion pendiente del OGG

No hay trabajo manual de codigo pendiente. Cuando el archivo este disponible, subirlo como:

`music/menu_nueva.ogg`

El pipeline hace el resto y aborta sin publicar si falla validacion, verificaciones o build Java 17.
