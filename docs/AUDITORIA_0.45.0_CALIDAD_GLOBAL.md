# Auditoría 0.45.0 — Calidad global

Fecha: 2026-09-06

## Objetivo

0.45.0 realiza una pasada transversal de robustez sobre configuración, navegación, resize y callbacks asíncronos. No cambia el contrato gráfico 0.44 ni añade lógica de gameplay.

## Hallazgos corregidos

### 1. Ajustes Jobs era difícil de explorar

La configuración estaba dividida por categorías pero no existía una forma rápida de localizar una opción. Se añade `PantallaBuscarAjustesJobs`, accesible con `Ctrl+F`, con catálogo explícito de preferencias, búsqueda por nombre/detalle/categoría y navegación al control real.

El buscador conserva filtro, foco y scroll durante `resize()` y no duplica setters ni widgets de configuración.

### 2. Config no conservaba contexto de trabajo

Al reabrir la pantalla se regresaba siempre a Visual. 0.45 conserva la última categoría utilizada durante la sesión del cliente. También corrige el indicador superior para mostrar `CUSTOM` cuando `PerfilesJobs.actual()` no reconoce ningún preset.

### 3. Idioma podía quedar parcialmente aplicado

Antes se modificaban `Options.languageCode` y `LanguageManager` antes del resource reload. Si ese reload fallaba, la pantalla quedaba abierta pero el estado podía haber cambiado parcialmente.

0.45 guarda el idioma anterior y, ante error, restaura ambos estados, muestra feedback negativo y permite reintentar. Además conserva idioma pendiente, filtro, foco y scroll durante resize.

### 4. Mundos y Mods perdían búsqueda en resize

`init()` reconstruía los `EditBox` y borraba el filtro activo. Ahora cada pantalla captura filtro/foco antes de `super.resize()` y los restaura después de la reconstrucción.

### 5. Resource Packs podía ejecutar un retorno tardío

El callback de actualización podía volver a Opciones Jobs aunque el usuario ya hubiera abandonado el selector. Ahora sólo ejecuta el retorno mientras la Screen activa siga siendo `PantallaPaquetesJobs`.

### 6. Cierres directos inconsistentes

Apariencia y Controles todavía ejecutaban `setScreen(anterior)` sin guard. Se añade protección idempotente y comprobación de `minecraft != null`, alineándolos con Opciones, Multiplayer, Mundos y Mods.

### 7. Reflection repetida en Sonido

`PantallaSonidoJobs` recorría `SoundOptionsScreen.getDeclaredFields()` en cada `init()`. El `Field` de `OptionsList` ahora se resuelve una sola vez por JVM y se reutiliza.

## Contratos preservados

- Gráficos sigue fuera de toda tematización Jobs.
- Embeddium se abre mediante `ConfigScreenHandler.ConfigScreenFactory`.
- Sin Embeddium se usa `VideoSettingsScreen` vanilla.
- No existe MODPACK.
- Screens externas y gameplay permanecen fuera de chrome/transiciones/click global Jobs.
- Audio de menú recibe hard-stop al entrar a gameplay.
- Multiplayer conserva selección por IP y scroll en F5/resize.
- PNG 10–17 permanecen estáticos; JPG 18–31 sólo usan respiración sutil/desactivable.

## Verificación automática

`tools/verificar_calidad_045.py` exige:

- existencia y contratos del buscador transversal;
- acceso `Ctrl+F` desde Config Jobs;
- última categoría y `CUSTOM` visible;
- cierres protegidos;
- rollback transaccional de idioma;
- continuidad de filtro en Mundos/Mods;
- caché reflectiva de Sonido;
- callback de Resource Packs acotado a su Screen activa.

El workflow ejecuta este verificador después de los guards 0.41–0.44 y antes del build Forge Java 17.

## Pruebas manuales prioritarias

1. Config Jobs: buscar, navegar por Enter/doble clic, resize y ESC por etapas.
2. Idioma: selección pendiente + resize y, si es posible, provocar un reload fallido para comprobar rollback.
3. Mundos/Mods: escribir filtro, maximizar/restaurar y continuar búsqueda.
4. Resource Packs: salir del selector y comprobar que ningún callback tardío secuestra otra navegación.
5. Apariencia/Controles: abrir/cerrar repetidamente con ESC y botones.
6. Sonido: abrir/cerrar repetidamente y comprobar que todos los controles vanilla siguen presentes.
7. Regresión 0.44: Gráficos Embeddium/vanilla continúa completamente intocable.

## Entrega

Versión objetivo: **0.45.0**  
Artefacto: **`jobsmenu-0.45.0.jar`**  
Rama entregable: **`main`**  
Publicación: sólo después de PR CI verde, merge y CI verde de `main`.
