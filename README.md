# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1. Jobs reemplaza y tematiza el flujo de menús con una interfaz administrativa/industrial propia, audio de sesión y 32 fondos/niveles, conservando la lógica vanilla/Forge cuando reimplementarla perjudicaría compatibilidad.

| Campo | Valor |
|---|---|
| Versión | **0.41.0** |
| Artefacto | **`jobsmenu-0.41.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Rama entregable | **`main`** |
| Niveles | **32 (0–31)** |

## 0.41.0 · Runtime, audio, Embeddium y continuidad

0.41.0 reduce trabajo repetido, cierra huecos de lifecycle y mejora compatibilidad sin ampliar Jobs hacia gameplay.

- **Gráficos respeta Embeddium:** si `embeddium` está instalado, Jobs obtiene la pantalla gráfica desde el `ConfigScreenFactory` oficial registrado en Forge. No hay reflection ni dependencia directa de clases internas. Si no existe proveedor o falla al construir la pantalla, se usa `VideoSettingsScreen` vanilla como fallback.
- Las GUI de Embeddium/Sodium quedan completamente fuera del chrome, transiciones y sustitución de clicks Jobs. El aislamiento cubre tanto `SodiumOptionsGUI` de Embeddium 1.20.1 como las pantallas nuevas de Embeddium y las pantallas gráficas de Iris/Oculus.
- Los sonidos puntuales Jobs —eventos, apagones y FX de transición— ahora se registran mientras están activos y reciben hard-stop explícito al cerrar la visita/entrar a gameplay. Las referencias terminadas se purgan mediante el estado real del `SoundManager`.
- Esos FX ya no caen en `minecraft:ambient.cave` si falta un registro: sonido Jobs o silencio, igual que la identidad musical 0.40.
- `SesionMenu.cerrar()` es idempotente: después del primer corte no recorre música/camas en cada tick de gameplay, pero vuelve a actuar si detecta audio Jobs residual.
- La música vanilla deja de recibir `stopPlaying()` 20 veces por segundo. Se corta una vez al abrir visita y cualquier nueva instancia `SoundSource.MUSIC` queda bloqueada mientras Jobs posee el menú.
- F5/Actualizar en Multiplayer conserva **servidor seleccionado y posición de scroll**. Ese mismo contexto también se conserva al maximizar, redimensionar la ventana o cambiar la escala GUI. `servers.dat` sólo se guarda si realmente hubo alta, baja, renombre, deduplicación o movimiento del servidor oficial.
- Los setters de Config Jobs ignoran valores idénticos y evitan escrituras TOML innecesarias; los sliders mantienen el guardado diferido existente.
- El hover de controles vanilla preservados usa una caché de botones y deja de recorrer toda la jerarquía de hijos en cada frame.
- El diagnóstico oculto añade cierres efectivos de sesión, FX puntuales, purgas, métricas de config y estado del proveedor gráfico.
- CI añade `tools/verificar_graficos_041.py`, mantiene `tools/verificar_runtime_041.py` y usa `actions/checkout@v7`.

## Audio

Catálogo musical empaquetado:

1. **Absurdism** — `musica.tema`.
2. **REQUIEM** — `musica.requiem` — crédito `Emmy Z - Forsaken OST`.
3. **Upon the Hill V2** — `musica.upon_hill` — crédito `ft. @iCosmicCoffee`.

La música pertenece a `SesionMenu`, no a una Screen. No existe fallback a `minecraft:music.menu`. Entrar a mundo/servidor aplica hard-stop a música, camas y FX puntuales. `M` controla mute Jobs y `N` solicita siguiente pista sólo en Aleatoria.

## Interfaz y gameplay

- **Gráficos usa la interfaz real de Embeddium cuando Embeddium está instalado; vanilla sólo es fallback.** Jobs no tematiza ni recoloca esa GUI externa.
- chat, inventario, contenedores y UI normal de gameplay no reciben piel, banda, transición ni sustitución global de clicks Jobs;
- mientras `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs;
- Pausa/Config Jobs pueden mantener tema y feedback breve sin reactivar música/ambiente;
- el main no muestra la antigua barra visible de atajos;
- ningún control visible debe quedar cubierto por una hitbox invisible vanilla.

## Multiplayer

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

- `Ghoul Outbreak` y duplicados legacy no reaparecen.
- ESC y Cancelar vuelven al padre Jobs con una sola acción.
- Cancelar conexión/error pre-login vuelve a Multiplayer Jobs.
- salir/kick/pérdida de conexión desde servidor remoto vuelve a Multiplayer Jobs; mundo local vuelve al main Jobs.
- F5/Actualizar reconstruye Jobs directamente y conserva selección por IP **más scroll de lista**.
- resize/maximizar/cambio de escala GUI conserva también selección y scroll.
- abrir Multiplayer no reescribe `servers.dat` si el servidor oficial ya está correcto.

## Fondos

- **10–17:** PNG históricos totalmente estáticos.
- **18–31:** JPG directos 1920×1080 con respiración de cámara sólo sutil/no destructiva; Movimiento reducido, Bajo consumo o escena quieta la desactivan.

## Build y entrega

GitHub Actions ejecuta política de versión, fondos, verificador general, UI/música, continuidad Multiplayer/docs, optimización, créditos/reload, identidad musical/hard-stop, runtime 0.41 e integración gráfica Embeddium antes del build Forge real con Java 17. `dev-latest` sólo se publica desde `main` verde.

Instancia de prueba:

`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato maestro vigente.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y límites de CI.
- [`CHANGELOG.md`](CHANGELOG.md): evolución reciente.
- [`docs/README.md`](docs/README.md): índice vigente/histórico.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): aceptación en `test-1`.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): fronteras con vanilla/Forge/mods.
- [`docs/musica.md`](docs/musica.md): catálogo y lifecycle de audio.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): publicación e instalación.
- [`docs/AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md`](docs/AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md): detalle técnico de esta tanda.
