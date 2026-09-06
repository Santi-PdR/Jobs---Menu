# Auditoría 0.46.0 — lifecycle de Idioma y buscador

Fecha: 2026-09-06

## Objetivo

Cerrar dos rutas que todavía dependían de comportamiento implícito: la aplicación de Force Unicode Font dentro del selector Jobs de idioma y la navegación del buscador transversal hacia una categoría de Config.

## Hallazgo 1 — Force Unicode Font podía quedar persistido sin recarga

En 0.45 el toggle modificaba `Options.forceUnicodeFont()` y llamaba `Options.save()` inmediatamente. `Aplicar y cerrar` sólo ejecutaba `reloadResourcePacks()` cuando cambiaba el idioma. Por lo tanto, cambiar únicamente Unicode podía persistir el valor sin aplicar de inmediato el conjunto de recursos/fuentes correspondiente.

### Corrección

`PantallaIdiomaJobs` separa ahora:

- idioma aplicado / pendiente;
- Unicode aplicado / pendiente.

El toggle sólo cambia `unicodePendiente`. Al pulsar Aplicar:

1. se comparan ambos valores con el estado efectivo;
2. si no cambió nada, se cierra sin reload;
3. si cambió idioma o Unicode, se escriben ambos valores y se guarda una vez;
4. se ejecuta una única `reloadResourcePacks()`;
5. éxito confirma ambos estados;
6. fallo restaura idioma, `LanguageManager` y Unicode, persiste el rollback y deja la pantalla abierta.

## Hallazgo 2 — callback tardío de Idioma

El callback exitoso de 0.45 volvía siempre al padre. Aunque la pantalla bloqueaba input durante la aplicación, una sustitución externa de Screen podía hacer que una finalización tardía alterara una navegación posterior.

### Corrección

El callback sólo ejecuta retorno cuando:

`minecraft.screen == this`

Si otra Screen ya está activa, sólo actualiza el estado interno correspondiente y no navega. El cierre añade además guard `cerrando`.

## Hallazgo 3 — buscador simulaba una tecla del padre

Para abrir una categoría, 0.45 hacía primero `setScreen(anterior)` y después llamaba manualmente a `anterior.keyPressed(1 + categoria, ...)`. Funcionaba, pero acoplaba una acción de navegación a la implementación de shortcuts del padre y generaba dos pasos de transición lógica.

### Corrección

`PantallaAjustesAviso` expone el método package-private:

`abrirCategoriaDesdeBusqueda(int indice)`

Este método valida rango, actualiza la última categoría, guarda cambios pendientes y abre explícitamente la Screen correcta. Si ya era la categoría actual, reutiliza el padre existente. El buscador no sintetiza ninguna tecla.

## Hot path del buscador

Cada `EntradaResultado` conserva al construirse:

- título traducido;
- detalle traducido;
- nombre de categoría.

El contador visible también se formatea al reconstruir el filtro. `render()` deja de ejecutar esas traducciones y formato cada frame.

## Contratos preservados

- Gráficos 0.44 sigue intocable: Embeddium vía factory oficial y fallback vanilla.
- Sin MODPACK.
- Pantallas externas aisladas.
- Gameplay sin transición/audio de menú.
- Audio, hard-stop y catálogo musical sin cambios.
- Multiplayer conserva selección+scroll y servidor oficial.
- Fondos 10–17 estáticos; 18–31 sólo respiración mínima.

## Verificación automática

Se añade `tools/verificar_lifecycle_046.py`, que exige:

- staging de Unicode;
- reload único para idioma/Unicode;
- rollback de ambos valores;
- guard de callback con Screen actual;
- cierre idempotente;
- navegación explícita del buscador;
- ausencia de la vieja llamada sintética a `keyPressed`;
- caché de textos/contador del buscador.

CI ejecuta este verificador después de 0.45 y antes del build Forge Java 17.

## Pruebas manuales prioritarias

1. cambiar sólo Unicode y comprobar recarga/aplicación inmediata;
2. cambiar idioma + Unicode simultáneamente y comprobar una sola recarga;
3. provocar fallo de reload y verificar rollback conjunto + reintento;
4. comprobar que un callback tardío no cambia una Screen distinta;
5. abrir resultados de todas las categorías del buscador con Enter y doble clic;
6. repetir ESC/Volver/resize sin dobles retornos;
7. validar que Gráficos, Multiplayer, audio y gameplay no regresionen.
