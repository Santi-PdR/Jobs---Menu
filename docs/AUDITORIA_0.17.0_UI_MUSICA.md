# Auditoría 0.17.0 — UI neutra y música de sesión

Fecha: 2026-09-01

## Objetivo

Eliminar la contaminación de color entre escena e interfaz y convertir la música del menú en un subsistema de sesión preparado para más de una pista.

## Auditoría de ramas

Al iniciar el pase, GitHub devolvió únicamente `main` como rama activa. Los PR recientes útiles ya estaban fusionados, por lo que no existían ramas pendientes de merge ni ramas residuales que borrar.

`main` partía del commit `6024ee2eaca0bfe64df575a1e787d50283eb1502` (0.16.2), cuyo workflow `Build Jobs Menu` había finalizado correctamente.

## Hallazgo principal de UI

0.16.2 había neutralizado el tinte global de Mundos/Multiplayer/Mods/Recursos, pero varios componentes compartidos seguían usando colores físicos del recinto:

- `PARED_ALTA` como hover/foco;
- `FLUOR` como foco de teclado/confirmación;
- `PARED` como selección de Idioma;
- papel cálido dentro de transiciones y archivos oscuros.

Eso explicaba por qué algunas interfaces todavía podían verse amarillentas aunque el render vanilla ya usara un `shaderColor` RGB neutro.

## Corrección 0.17

`Paleta` queda dividida semánticamente:

### Escena

`PARED`, `PARED_ALTA`, `PARED_BAJA`, `MOHO`, `ALFOMBRA`, `TECHO`, `FLUOR`, `PAPEL` y `VANO` describen materiales/luz física.

### UI

Se agregan constantes exclusivas:

- `UI_PAPEL`;
- `UI_PAPEL_FOCO`;
- `UI_TINTA`;
- `UI_TINTA_TENUE`;
- `UI_ACENTO`;
- `UI_ACENTO_FUERTE`;
- `ARCHIVO_FONDO`;
- `ARCHIVO_SUPERFICIE`;
- `ARCHIVO_SUPERFICIE_FOCO`.

Componentes migrados:

- `ChromeExpediente`;
- `PielVanillaJobs`;
- `BotonExpediente`;
- `ToggleExpediente`;
- `SliderExpediente`;
- `PulidoInterfazJobs`;
- `TransicionInterfazJobs`;
- `PantallaIdiomaJobs`.

`PielVanillaJobs` ahora distingue formularios claros de archivos oscuros. Los botones vanilla conservados en Mundos/Multiplayer/Mods/Recursos dejan de ser hojas claras pegadas sobre un archivo oscuro.

## Música

### Pista incluida

El recurso empaquetado `assets/jobsmenu/sounds/musica/defecto.ogg`, registrado como `musica.tema`, se identifica en el catálogo 0.17 como **Absurdism**.

### Segunda pista solicitada

Referencia indicada por el proyecto:

`https://www.youtube.com/watch?v=t9KaSaGEwvI`

No se extrae audio desde YouTube ni se inventan metadatos. La entrada queda pendiente hasta que exista en el proyecto un OGG cuya redistribución esté autorizada.

### Reproductor

`GestorMusica` pasa de una única instancia monolítica a un catálogo con pista principal y pista entrante:

- fade-in desde cero;
- fade-out de retirada;
- ganancia por pista;
- crossfade preparado cuando el catálogo tenga 2+ recursos;
- continuidad durante una visita completa;
- ducking en transición de Nivel;
- ducking en La Suspensión;
- ducking por presencia;
- watchdog de OpenAL/SoundEngine;
- reconstrucción después de recarga;
- hard stop de seguridad al entrar a gameplay.

El hard stop al entrar a un mundo es intencional: hacer fade-out allí volvería a introducir el bug de escuchar música del menú durante los primeros segundos jugables.

## Contratos automáticos

Se añade `tools/verificar_ui_musica.py`.

Falla si:

- desaparecen las constantes de UI neutra;
- componentes compartidos vuelven a usar `PARED`, `PARED_ALTA` o `FLUOR` como UI;
- `PielVanillaJobs` pierde su variante oscura;
- el catálogo deja de identificar Absurdism;
- desaparecen fade/crossfade/hard-stop del gestor;
- `musica.tema` deja de apuntar al OGG empaquetado;
- falta el OGG;
- la documentación pierde la segunda referencia solicitada.

El workflow ejecuta este verificador antes del build Forge.

## Aceptación manual requerida

CI puede certificar estructura y compilación, no percepción final. Dentro de Minecraft verificar:

1. Mundos/Multiplayer/Mods/Recursos sin halo amarillo en botones o foco.
2. Idioma con buscador centrado y selección gris, no amarilla.
3. Focus ring visible con teclado y sin blanco puro.
4. Absurdism entrando gradualmente tras abrir Jobs.
5. No reinicio al navegar por Options/Mods/Recursos.
6. F3+T sin duplicados.
7. Entrada a mundo/servidor sin cola musical.
8. Retorno a Jobs con una visita musical nueva.
9. Con segunda pista autorizada futura: crossfade continuo y sin pico de volumen.
