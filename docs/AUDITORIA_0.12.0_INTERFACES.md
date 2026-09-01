# Auditoría de interfaces — Jobs Menu 0.12.0

Fecha: 2026-08-31

## Objetivo

0.12.0 extiende la identidad de Jobs más allá del título y la pausa. La meta no es aplicar una textura encima de Minecraft, sino mantener continuidad de lugar, lenguaje y navegación mientras se preserva la lógica vanilla que ya resuelve correctamente opciones complejas.

`GripeVerde` se revisó como referencia de arquitectura de interfaces: reutilización de listas vanilla, wrappers especializados, hubs propios y fallbacks defensivos. Su tema visual no forma parte de Jobs.

## Principios usados

1. **Una sola familia visual.** Papel fotocopiado, tinta, archivo administrativo, fluorescente y Nivel vigente.
2. **Lógica real, no imitaciones.** Sliders, listas, idiomas, servidores y opciones siguen escribiendo en los objetos reales de Minecraft.
3. **No romper por decorar.** Una pantalla de otro mod se conserva cuando sustituirla requeriría reflection profunda o copiar una implementación externa.
4. **Red de seguridad.** Reflection limitada sólo para presentación; si falla, el control vanilla sigue funcionando.
5. **Rojo exclusivo de Executors.** Peligro administrativo y acciones terminales se comunican con tinta, separación y peso, no con rojo.
6. **Accesibilidad transversal.** Movimiento reducido simplifica también transiciones UI; alto contraste y texto grande siguen siendo parte del mismo sistema.
7. **Versionado visible.** Los expedientes muestran la versión runtime y el build entregado se llama `jobsmenu-0.12.0.jar`.

## Matriz de pantallas

| Pantalla | Estrategia 0.12.0 | Lógica preservada | Tratamiento Jobs |
|---|---|---|---|
| Título | Propia existente | Flujo de Minecraft | Aviso + Nivel vivo |
| Pausa | Propia existente | Secuencia vanilla de salida/guardado | Estancia en suspenso |
| Opciones | Hub propio | `Options` reales | Expediente CFG-012 |
| Sonido | Subclase vanilla | `SoundOptionsScreen` + `OptionsList` | Expediente AUD-012 |
| Video vanilla | Subclase vanilla | `VideoSettingsScreen` | Expediente IMG-012 |
| Video Embeddium | Externa preservada | Embeddium completo | Banda contextual solamente |
| Controles | Hub propio | `Options` reales | Expediente CTL-012 |
| Mouse | Subclase vanilla | `MouseSettingsScreen` | Expediente MSE-012 |
| Teclas | Subclase vanilla | `KeyBindsScreen` | Expediente KEY-012 |
| Idioma | Pantalla propia | `LanguageManager` + `Options` | Expediente LNG-012 |
| Chat | Subclase vanilla | `ChatOptionsScreen` | Expediente COM-012 |
| Accesibilidad | Subclase vanilla ampliada | Opciones vanilla + cuatro ayudas Jobs | Expediente ACC-012 |
| Online | `SimpleOptionsSubScreen` | opciones online reales | Expediente NET-012 |
| Resource Packs | Subclase vanilla | repositorio/callback vanilla | Archivo de paquetes |
| Piel | Pantalla propia | `PlayerModelPart` + mano principal | Ficha ID-012 |
| Multijugador | Subclase vanilla | lista, ping, MOTD, LAN y botones reales | Registro CREW-012 |
| Ajustes Jobs | Lista de opciones propia | `ConfigTurno` real | Expediente JOBS-012 |

## Componentes compartidos

### `ChromeExpediente`

Responsable del recinto detrás, hoja, bordes, perforaciones, cabeceras, divisores, esquinas, banda contextual y pie con formulario/Nivel/versión. Evita que cada pantalla invente su propia paleta o espaciado.

### `BotonExpediente`

Conserva hitbox estándar, foco de teclado y narración, pero reemplaza la piel vanilla. Incluye hover/pulsación, elipsis y feedback auditivo del mod. El tipo `TERMINAL` nunca usa rojo.

### `SliderExpediente`

Control entero reutilizable. El primer uso es FOV. Escribe directamente en `Options.fov()` y limita la repetición del sonido mientras se arrastra.

### `ToggleExpediente`

No guarda estado paralelo: consulta el getter real en cada interacción. Admite texto de valor personalizado; Agacharse/Correr usan Mantener/Alternar y no Sí/No.

### `ListasExpediente`

Busca listas vanilla de forma defensiva, desactiva dirt/bandas y opcionalmente ajusta sólo límites verticales. No modifica el ancho porque varias listas calculan columnas e hitboxes con ese valor.

### `TransicionInterfazJobs`

Une visualmente cambios entre expedientes sin flash blanco. Movimiento reducido utiliza una variante simplificada.

## Compatibilidad

### Sustitución de pantallas

`EscuchaCliente` usa clase exacta para los puntos de entrada vanilla importantes. Esto evita que una subclase creada por otro mod sea sustituida sólo porque hereda de `OptionsScreen` o `JoinMultiplayerScreen`.

### Embeddium

Si las clases de Embeddium están disponibles, se construye su pantalla real. Si no, se usa el wrapper de `VideoSettingsScreen`. No se replica su panel de opciones.

### Listas y widgets inyectados

Las pantallas basadas en listas llaman primero a `super.init()`. De ese modo Minecraft y otros hooks tienen oportunidad de construir su contenido antes del tratamiento visual. Los botones Done que se reemplazan quedan invisibles **y desactivados**.

### Recarga de recursos

Cambiar idioma o packs usa el mecanismo real de recarga. `RecargaRecursosCliente` invalida/reconstruye referencias de audio en el hilo del cliente, evitando operar sobre un SoundEngine reemplazado desde el executor de recarga.

## Layout y escalado

El hub de Opciones tiene un modo compacto para poca altura/ancho lógico. Las listas complejas reservan aproximadamente 50 px arriba y 42 px abajo para que cabecera/pie no cubran controles. La prueba manual debe incluir GUI Scale alto, porque una pantalla físicamente grande puede tener pocos píxeles lógicos.

## Decisiones deliberadas de no hacer

- No se fuerza una skin completa sobre la pantalla interna de Embeddium.
- No se reimplementan ping, LAN, MOTD ni almacenamiento de servidores.
- No se sustituyen indiscriminadamente pantallas de terceros.
- No se añaden flashes, blur pesado, shaders de UI ni postprocesado sólo por ornamentación.
- No se usa rojo para botones destructivos.
- No se duplica el estado de `Options` o `ConfigTurno` dentro de widgets cosméticos.

## Riesgos que sólo Minecraft real puede resolver

- Métricas de fuentes de resource packs extremos.
- Hooks de mods que reconstruyen widgets después de `init()`.
- Embeddium u otros mods con paquetes/clases diferentes a los previstos.
- Foco y narración con mods que inyectan controles adicionales.
- Solapamientos a GUI Scale extremos.
- Sensación real de legibilidad sobre los 18 fondos y durante apagones/Suspensión.

La lista operativa vive en `KNOWN_ISSUES.md` y `docs/checklist-manual.md`.

## Criterio de aceptación

La versión puede fusionarse cuando:

- verificadores estáticos pasan sin fallos;
- Forge compila con Java 17;
- el JAR generado se llama `jobsmenu-0.12.0.jar`;
- la rama no contiene archivos temporales ni artefactos generados;
- la PR queda verde;
- `main` vuelve a compilar después del merge;
- `dev-latest` publica únicamente el asset versionado correspondiente.

La certificación visual final requiere una prueba dentro de `test-1`; un build verde no prueba UX por sí solo.
