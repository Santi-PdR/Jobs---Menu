# Auditoría 0.39.0 — Créditos musicales y resource reload

## Objetivo

0.39.0 corrige una deuda de lifecycle/audio que había quedado escondida por la evolución del catálogo musical. Las tres pistas actuales están empaquetadas y documentadas, pero `GestorMusica.creditoAlfa()` seguía dependiendo del recurso `assets/jobsmenu/musica_creditada.txt`. Ese marcador había sido eliminado en una etapa anterior, cuando el catálogo todavía no tenía el estado actual. El resultado era una contradicción: el código conocía título/autor de las pistas y el checklist esperaba créditos, pero la compuerta podía mantenerlos siempre a alfa 0.

## Cambios

### Catálogo acreditado

Se restaura `assets/jobsmenu/musica_creditada.txt` como marcador explícito de que el catálogo empaquetado actual está autorizado para mostrar créditos. El archivo identifica:

- `absurdism`;
- `requiem`;
- `upon_the_hill_v2`.

No añade audio ni descarga material externo. Sólo vuelve coherente la compuerta que ya usa `GestorMusica`.

### Resource reload con generación

`RecargaRecursosCliente` deja de modelar una recarga únicamente con un booleano. Cada callback incrementa una generación atómica. Una ráfaga de reloads comparte tarea cuando es posible, pero si otra generación aparece mientras se cierran instancias, se agenda una segunda pasada en el hilo cliente. Esto protege secuencias como idioma -> F3+T -> resource pack.

La clase sigue sin tocar `SoundInstance` desde el executor de recursos.

### Sesión

`SesionMenu.abrir()` devuelve inmediatamente si la visita ya estaba activa. Cambiar de una pantalla Jobs a otra deja de volver a invocar el arranque del ambiente; el mantenimiento normal continúa por tick y el reload sigue pudiendo reconstruir camas.

### Diagnóstico oculto

El volcado interno añade:

- id de la pista musical dominante;
- generación actual de resource reload.

No se crea ninguna opción ni atajo visible nuevo.

## CI

Se añade `tools/verificar_reload_creditos.py`, que fija como contrato:

- presencia del marcador de catálogo acreditado;
- las tres ids de pista dentro del marcador;
- uso de la compuerta `marcadorHorneado()` en `GestorMusica`;
- generación atómica y reprogramación de reload;
- guard de reapertura de `SesionMenu`;
- campos nuevos del diagnóstico oculto.

## Contratos preservados

0.39.0 no cambia:

- Video Settings vanilla;
- frontera de gameplay;
- hard-stop de música/ambiente al entrar a mundo o servidor;
- selección fija/Aleatoria y crossfade de las tres pistas;
- sonidos Jobs de click/hover;
- retorno de servidor a Multiplayer Jobs;
- ESC/Cancelar/F5 de Multiplayer;
- servidor oficial;
- fondos 10–17 estáticos y 18–31 no destructivos.

## Prueba manual recomendada

1. Abrir el main con créditos activados y confirmar que el bloque de crédito aparece durante su ventana temporal.
2. Forzar Absurdism, REQUIEM y Upon the Hill V2 y confirmar título/autor correspondientes.
3. Cambiar idioma, pulsar F3+T y aplicar un resource pack en secuencia corta.
4. Confirmar que la música vuelve una sola vez y que no aparecen camas duplicadas.
5. Entrar inmediatamente a gameplay después de un reload y confirmar hard-stop total.
